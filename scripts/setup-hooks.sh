#!/bin/sh
# Configures git to use the version-controlled hooks in .githooks/

set -e

echo "Setting up Git hooks..."

# Configure git to use .githooks
git config core.hooksPath .githooks

echo "Git hooks configured to use .githooks/"

