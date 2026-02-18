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

# Usage
if [ $# -lt 1 ]; then
    echo "Usage: $0 <branch-name> [--skip-tests] [--skip-pr] [--skip-deploy]"
    echo ""
    echo "Examples:"
    echo "  $0 fix/api-path-generation"
    echo "  $0 feat/add-user-service --skip-tests"
    echo "  $0 refactor/cleanup-code --skip-pr --skip-deploy"
    echo ""
    echo "Options:"
    echo "  --skip-tests   Skip running tests locally"
    echo "  --skip-pr      Skip PR creation and merge (useful if PR already exists)"
    echo "  --skip-deploy  Skip the deployment step (recreating release)"
    exit 1
fi

BRANCH_NAME="$1"
SKIP_TESTS=false
SKIP_PR=false
SKIP_DEPLOY=false

# Parse optional flags
shift
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --skip-pr)
            SKIP_PR=true
            shift
            ;;
        --skip-deploy)
            SKIP_DEPLOY=true
            shift
            ;;
        *)
            print_error "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Ensure we're on main branch
CURRENT_BRANCH=$(git branch --show-current)
if [ "$CURRENT_BRANCH" != "main" ]; then
    print_error "You must be on the main branch to run this script"
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

# Step 1: Run tests locally
if [ "$SKIP_TESTS" = false ]; then
    print_step "Running tests locally..."
    if mvn clean install; then
        print_success "All tests passed!"
    else
        print_error "Tests failed. Fix the issues before proceeding."
        exit 1
    fi
else
    print_warning "Skipping local tests (--skip-tests flag)"
fi

# Step 2: Create and checkout new branch
print_step "Creating branch: $BRANCH_NAME"
git checkout -b "$BRANCH_NAME"
print_success "Created and switched to branch: $BRANCH_NAME"

# Commit changes (if any - they should already be staged/committed by now)
if [ -n "$(git status --porcelain)" ]; then
    print_warning "You have uncommitted changes. Please commit them manually."
    echo "After committing, run: $0 $BRANCH_NAME --skip-tests"
    exit 1
fi

# Check if there are commits to push
COMMITS_AHEAD=$(git rev-list --count main..$BRANCH_NAME)
if [ "$COMMITS_AHEAD" -eq 0 ]; then
    print_error "No commits on this branch. Make your changes and commit them first."
    git checkout main
    git branch -D "$BRANCH_NAME"
    exit 1
fi

# Step 3: Rebase against origin/main and squash into single commit
print_step "Pulling latest changes from origin/main..."
git fetch origin main
print_success "Fetched latest origin/main"

print_step "Rebasing against origin/main..."
if git rebase origin/main; then
    print_success "Rebased successfully"
else
    print_error "Rebase failed. Resolve conflicts and run:"
    echo "  git rebase --continue"
    echo "  git push -u origin $BRANCH_NAME --force-with-lease"
    echo "Then continue with PR creation manually"
    exit 1
fi

# Check if we have multiple commits
COMMITS_AHEAD=$(git rev-list --count origin/main..$BRANCH_NAME)
if [ "$COMMITS_AHEAD" -gt 1 ]; then
    print_step "Squashing $COMMITS_AHEAD commits into a single commit..."

    # Get the commit message from the first commit for the squashed commit
    FIRST_COMMIT_MSG=$(git log --format=%B -n 1 HEAD)

    # Interactive rebase to squash all commits
    if git rebase -i origin/main --autosquash; then
        print_success "Commits squashed into a single commit"
    else
        print_warning "Interactive rebase opened. Please squash commits manually."
        print_warning "After squashing, the script will continue automatically."
        echo ""
        echo "In the editor:"
        echo "  1. Change 'pick' to 'squash' (or 's') for all commits except the first"
        echo "  2. Save and close"
        echo "  3. Edit the commit message if needed"
        echo ""
        read -p "Press Enter when you're done with the rebase..."
    fi
elif [ "$COMMITS_AHEAD" -eq 1 ]; then
    print_success "Branch already has a single commit"
else
    print_error "No commits ahead of origin/main"
    exit 1
fi

# Verify we now have exactly one commit
FINAL_COMMITS=$(git rev-list --count origin/main..$BRANCH_NAME)
if [ "$FINAL_COMMITS" -ne 1 ]; then
    print_warning "Expected 1 commit after squashing, but found $FINAL_COMMITS commits"
    echo "Continuing anyway..."
fi

# Step 4: Push branch
print_step "Pushing branch to origin..."
git push -u origin "$BRANCH_NAME" --force-with-lease
print_success "Branch pushed to origin"

if [ "$SKIP_PR" = false ]; then
    # Step 5: Create PR
    print_step "Creating pull request..."
    PR_URL=$(gh pr create --base main --head "$BRANCH_NAME" --fill)
    print_success "Pull request created: $PR_URL"

    # Step 6: Wait for CI checks
    print_step "Waiting for CI checks to complete..."
    gh pr checks "$BRANCH_NAME" --watch --interval 10

    # Check if all checks passed
    if gh pr checks "$BRANCH_NAME" | grep -q "fail"; then
        print_error "Some checks failed. Fix the issues and re-run."
        exit 1
    fi
    print_success "All CI checks passed!"

    # Step 7: Merge PR
    print_step "Merging pull request..."
    gh pr merge "$BRANCH_NAME" --squash --delete-branch
    print_success "Pull request merged and branch deleted"

    # Switch back to main and pull
    git checkout main
    git pull origin main
    print_success "Switched back to main and pulled latest changes"
else
    print_warning "Skipping PR creation and merge (--skip-pr flag)"
    git checkout main
fi

if [ "$SKIP_DEPLOY" = false ]; then
    # Step 8: Deploy by recreating release
    print_step "Deploying by recreating $RELEASE_TAG release..."

    # Confirm deployment
    echo ""
    print_warning "This will recreate the $RELEASE_TAG release and trigger a new build."
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
else
    print_warning "Skipping deployment (--skip-deploy flag)"
fi

echo ""
print_success "All done! 🎉"
