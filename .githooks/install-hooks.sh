#!/bin/bash

# Script to install Git hooks for the project

set -e

HOOKS_DIR=".githooks"
GIT_HOOKS_DIR=".git/hooks"

echo "🔧 Installing Git hooks..."

# Create git hooks directory if it doesn't exist
mkdir -p "$GIT_HOOKS_DIR"

# Install pre-commit hook
if [ -f "$HOOKS_DIR/pre-commit" ]; then
    cp "$HOOKS_DIR/pre-commit" "$GIT_HOOKS_DIR/pre-commit"
    chmod +x "$GIT_HOOKS_DIR/pre-commit"
    echo "✅ Pre-commit hook installed"
else
    echo "❌ Pre-commit hook file not found in $HOOKS_DIR"
    exit 1
fi

# Configure git to use the hooks directory (optional for future hooks)
git config core.hooksPath "$HOOKS_DIR"

echo "🎉 Git hooks installation complete!"
echo ""
echo "The pre-commit hook will now:"
echo "  • Run spotless formatting check before each commit"
echo "  • Automatically fix formatting violations"
echo "  • Add fixed files to your commit"
echo ""
echo "To disable temporarily: git commit --no-verify"