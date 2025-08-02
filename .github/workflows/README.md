# GitHub Workflows

This directory contains GitHub Actions workflows for the Compilugin project.

## Workflows

### 1. CI (`ci.yml`)
**Trigger**: Push to `main` branch, Pull Requests to `main`

**Jobs** (run in parallel):
- **core-plugin**: Tests and builds the main Kotlin compiler plugin
- **android-example**: Builds the Android example application  
- **kmp-example**: Builds the Kotlin Multiplatform example
- **integration**: Runs after all other jobs to verify full integration
- **code-quality**: Checks code formatting and runs static analysis

**Features**:
- Parallel execution for faster CI
- Caches Gradle dependencies
- Uploads test results and build artifacts
- Checks code formatting with Spotless

### 2. Prepare and Release (`release-prepare.yml`)
**Trigger**: Manual workflow dispatch from GitHub web interface

**Purpose**: 
- **Fully automated releases** - no manual version management needed!
- Updates version in all files (gradle.properties, README.md, etc.)
- Runs tests and formatting checks
- Commits changes and creates git tag
- Publishes to Maven Central
- Creates GitHub release with auto-generated notes

**How to use**:
1. Go to **Actions** tab in GitHub
2. Select **"Prepare and Release"** workflow
3. Click **"Run workflow"**
4. Enter new version (e.g., `0.0.7`)
5. Optionally add release notes
6. Click **"Run workflow"** button

### 3. Dependency Check (`dependency-check.yml`)
**Trigger**: 
- Weekly schedule (Mondays at 9 AM UTC)
- Manual trigger

**Purpose**:
- Checks for dependency updates
- Generates reports for outdated dependencies

## Setup Requirements

For the release workflow to work, the following secrets need to be configured in GitHub:

- `MAVEN_CENTRAL_USERNAME`: Maven Central username (maps to `mavenCentralUsername`)
- `MAVEN_CENTRAL_PASSWORD`: Maven Central password (maps to `mavenCentralPassword`)  
- `SIGNING_KEY`: Base64-encoded GPG signing key file (will be decoded to `/tmp/secring.gpg`)
- `SIGNING_KEY_ID`: GPG signing key ID (maps to `signing.keyId`)
- `SIGNING_PASSWORD`: GPG signing key password (maps to `signing.password`)

## Setting up GitHub Secrets

To configure the `SIGNING_KEY` secret for releases:

```bash
# On macOS/Linux
base64 -i /Users/sam/secring.gpg | pbcopy

# Or using input redirection
base64 < /Users/sam/secring.gpg | pbcopy
```

Then paste the base64-encoded content into the GitHub secret.

## Release Methods

### Method 1: GitHub Web Interface (Recommended)
**Fully automated - no local setup needed!**

1. Go to **Actions** tab in GitHub
2. Select **"Prepare and Release"** workflow  
3. Click **"Run workflow"**
4. Enter version and release notes
5. Everything else is automated!

### Method 2: Enhanced Local Script
**For power users who prefer command line**

The enhanced `publish.sh` script can also handle version management:

```bash
# Update version and publish automatically
./publish.sh 0.0.7

# Or with explicit flag
./publish.sh --version 0.0.7

# Local testing only
./publish.sh --local
./publish.sh --version 0.0.7 --local

# See all options
./publish.sh --help
```

**Features of enhanced publish.sh**:
- ✅ Auto-updates all version references  
- ✅ Updates README.md documentation
- ✅ Runs tests before publishing
- ✅ Creates git commit and tag
- ✅ Pushes to GitHub (triggers CI)
- ✅ Colorful output with progress indicators

## Local Testing

To test workflows locally, you can run similar commands:

```bash
# Core plugin tests
./gradlew spotlessCheck test build --parallel

# Android example
./gradlew publishToMavenLocal
cd android-example && ./gradlew build test --parallel

# KMP example  
cd compilugin-example && ./gradlew build --parallel

# Test current version locally
./publish.sh --local

# Test specific version locally (no git operations)
./publish.sh --version 0.0.8 --local
```