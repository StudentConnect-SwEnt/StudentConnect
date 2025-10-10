# Configures git to use the version-controlled hooks in .githooks/

Write-Output "Setting up Git hooks..."

# Configure git to use .githooks
git config core.hooksPath .githooks

Write-Output "Git hooks configured to use .githooks/"

