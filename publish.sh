#!/bin/bash

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_help() {
    echo "Usage: $0 [OPTIONS] [VERSION]"
    echo ""
    echo "Options:"
    echo "  --local              Publish to local Maven repository only"
    echo "  --version VERSION    Set new version and publish (updates all files)"
    echo "  --help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --local                    # Publish current version locally"
    echo "  $0                            # Publish current version to Maven Central"
    echo "  $0 --version 0.0.7            # Update to version 0.0.7 and publish"
    echo "  $0 0.0.7                      # Same as above (shorthand)"
}

update_version() {
    local new_version=$1
    echo -e "${BLUE}📝 Updating version to ${new_version}...${NC}"
    
    # Update gradle.properties
    if [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        sed -i '' "s/VERSION_NAME=.*/VERSION_NAME=${new_version}/" gradle.properties
        sed -i '' "s/id(\"dev.supersam.compilugin\") version \"[^\"]*\"/id(\"dev.supersam.compilugin\") version \"${new_version}\"/" README.md
    else
        # Linux
        sed -i "s/VERSION_NAME=.*/VERSION_NAME=${new_version}/" gradle.properties
        sed -i "s/id(\"dev.supersam.compilugin\") version \"[^\"]*\"/id(\"dev.supersam.compilugin\") version \"${new_version}\"/" README.md
    fi
    
    echo -e "${GREEN}✅ Version updated to ${new_version}${NC}"
}

commit_and_tag() {
    local version=$1
    echo -e "${BLUE}📦 Committing changes and creating tag...${NC}"
    
    git add -A
    if git diff --staged --quiet; then
        echo -e "${YELLOW}⚠️ No changes to commit${NC}"
    else
        git commit -m "🚀 Release v${version}

- Updated version to ${version}
- Updated documentation
- Prepared for release"
        
        echo -e "${GREEN}✅ Changes committed${NC}"
    fi
    
    # Create and push tag
    git tag "v${version}"
    git push origin main
    git push origin "v${version}"
    
    echo -e "${GREEN}✅ Tag v${version} created and pushed${NC}"
}

run_tests() {
    echo -e "${BLUE}🧪 Running tests and checks...${NC}"
    ./gradlew spotlessCheck test --parallel
    echo -e "${GREEN}✅ All tests passed${NC}"
}

publish_local() {
    echo -e "${BLUE}📦 Publishing to local Maven repository...${NC}"
    cd compilugin-compiler-plugin-gradle || exit
    ./gradlew publishToMavenLocal
    cd ..
    ./gradlew publishToMavenLocal
    echo -e "${GREEN}✅ Published to local Maven repository${NC}"
}

publish_remote() {
    echo -e "${BLUE}🚀 Publishing to Maven Central...${NC}"
    cd compilugin-compiler-plugin-gradle || exit
    ./gradlew publish --no-configuration-cache
    cd ..
    ./gradlew publish --no-configuration-cache
    echo -e "${GREEN}✅ Published to Maven Central${NC}"
}

# Parse arguments
local_only=false
new_version=""
show_help=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --local)
            local_only=true
            shift
            ;;
        --version)
            new_version="$2"
            shift 2
            ;;
        --help)
            show_help=true
            shift
            ;;
        -*)
            echo -e "${RED}❌ Unknown option $1${NC}"
            print_help
            exit 1
            ;;
        *)
            # If it looks like a version number, treat it as such
            if [[ $1 =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
                new_version="$1"
            else
                echo -e "${RED}❌ Unknown argument $1${NC}"
                print_help
                exit 1
            fi
            shift
            ;;
    esac
done

if [[ $show_help == true ]]; then
    print_help
    exit 0
fi

# Main execution
echo -e "${BLUE}🚀 Compilugin Release Script${NC}"
echo -e "${BLUE}=========================${NC}"

# Update version if specified
if [[ -n $new_version ]]; then
    update_version "$new_version"
    run_tests
    if [[ $local_only == false ]]; then
        commit_and_tag "$new_version"
    fi
fi

# Always run tests before publishing
if [[ -z $new_version ]]; then
    run_tests
fi

# Publish
if [[ $local_only == true ]]; then
    publish_local
else
    publish_remote
fi

echo -e "${GREEN}🎉 Done!${NC}"

if [[ -n $new_version && $local_only == false ]]; then
    echo -e "${BLUE}📋 Next steps:${NC}"
    echo -e "   • Check the GitHub Actions workflow for the release"
    echo -e "   • Verify the release appears on GitHub"
    echo -e "   • Check Maven Central for the new version"
fi