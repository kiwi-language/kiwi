#!/bin/bash
set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

RELEASE_TAG="0.0.1-alpha"

# Function to print colored messages
print_step() {
    echo -e "${BLUE}==>${NC} $1"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}!${NC} $1"
}

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    print_error "GitHub CLI (gh) is not installed. Install it with: brew install gh"
    exit 1
fi

# Check if authenticated with gh
if ! gh auth status &> /dev/null; then
    print_error "Not authenticated with GitHub CLI. Run: gh auth login"
    exit 1
fi

# Ensure we're on main branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "main" ]; then
    print_error "You must be on the main branch to deploy"
    echo "Current branch: $CURRENT_BRANCH"
    exit 1
fi

# Ensure working directory is clean
if [ -n "$(git status --porcelain)" ]; then
    print_error "Working directory is not clean. Commit or stash your changes first."
    git status --short
    exit 1
fi

# Pull latest changes
print_step "Pulling latest changes from origin/main..."
git pull origin main
print_success "Up to date with origin/main"

echo ""
print_warning "This will recreate the $RELEASE_TAG release and trigger a new build."
print_warning "This should only be done AFTER code has been merged to main."
echo ""
read -p "Do you want to proceed? (y/N): " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_warning "Deployment cancelled"
    exit 0
fi

# Delete local tag
if git tag -l | grep -q "^$RELEASE_TAG$"; then
    print_step "Deleting local tag $RELEASE_TAG..."
    git tag -d "$RELEASE_TAG"
    print_success "Local tag deleted"
fi

# Delete remote tag
print_step "Deleting remote tag $RELEASE_TAG..."
if git ls-remote --tags origin | grep -q "refs/tags/$RELEASE_TAG"; then
    git push origin ":refs/tags/$RELEASE_TAG"
    print_success "Remote tag deleted"
else
    print_warning "Remote tag $RELEASE_TAG does not exist"
fi

# Wait a bit for GitHub to process
sleep 2

# Create new tag
print_step "Creating new tag $RELEASE_TAG..."
git tag -a "$RELEASE_TAG" -m "Release $RELEASE_TAG - $(date +%Y-%m-%d)"
print_success "Tag created"

# Push new tag
print_step "Pushing tag to origin..."
git push origin "$RELEASE_TAG"
print_success "Tag pushed - Release build triggered!"

echo ""
print_success "================================================"
print_success "Deployment complete!"
print_success "================================================"
echo ""
echo "The release build is now running on GitHub Actions."
echo "Monitor progress at:"
echo "  https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/actions"
echo ""
echo "Once complete, the release will be available at:"
echo "  https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/releases/tag/$RELEASE_TAG"
echo ""
print_success "All done! 🎉"
