#!/bin/bash
# CAT Cleanup and Merge Script
# Cleans up locks, merges worktree work, merges completed releases to main
#
# Usage: scripts/cleanup-and-merge.sh <CHANGE_PATH>

set -euo pipefail

CHANGE_PATH="${1:-}"
if [[ -z "$CHANGE_PATH" ]]; then
  echo "Usage: scripts/cleanup-and-merge.sh <CHANGE_PATH>"
  exit 1
fi

LOCK_FILE=".cat-execution.lock"

# Extract change ID for change-specific lock
CHANGE_ID=$(basename "$CHANGE_PATH" | grep -oE '^[0-9]+(\.[0-9]+)?-[0-9]+' || echo "unknown")
CHANGE_LOCK_FILE=".cat-change-${CHANGE_ID}.lock"

# Remove lock files
rm -f "$LOCK_FILE"
rm -f "$CHANGE_LOCK_FILE"

# Check if release is complete
RELEASE_DIR=$(dirname "$CHANGE_PATH")
CHANGE_COUNT=$(ls "$RELEASE_DIR"/*-CHANGE.md 2>/dev/null | wc -l | tr -d ' ')
SUMMARY_COUNT=$(ls "$RELEASE_DIR"/*-SUMMARY.md 2>/dev/null | wc -l | tr -d ' ')

RELEASE_COMPLETE=false
if [[ $SUMMARY_COUNT -eq $CHANGE_COUNT ]]; then
  RELEASE_COMPLETE=true
fi

# Load execution context
if [[ -f .cat-execution-context ]]; then
  source .cat-execution-context
fi

# Handle worktree cleanup if running in parallel instance
if [[ -n "${WORKTREE_PATH:-}" ]]; then
  echo "Merging parallel work from worktree..."

  # Go to main project
  cd "$MAIN_PROJECT"

  # Merge worktree branch into release branch
  git checkout "$RELEASE_BRANCH" 2>/dev/null || git checkout -b "$RELEASE_BRANCH"
  git merge "$WORKTREE_BRANCH" -m "Merge work from $WORKTREE_BRANCH"

  # Remove worktree and branch
  git worktree remove "$WORKTREE_PATH" --force
  git branch -d "$WORKTREE_BRANCH"

  echo "Merged and cleaned up worktree"
fi

# Merge to main if release complete
if [[ "$RELEASE_COMPLETE" == "true" && -n "${RELEASE_BRANCH:-}" ]]; then
  echo "Release complete - merging to main..."

  git checkout main

  # Fast-forward merge (linear history)
  if ! git merge --ff-only "$RELEASE_BRANCH"; then
    # If ff-only fails, rebase and retry
    git checkout "$RELEASE_BRANCH"
    git rebase main
    git checkout main
    git merge --ff-only "$RELEASE_BRANCH"
  fi

  # Delete release branch
  git branch -d "$RELEASE_BRANCH"

  echo "Release merged to main"
fi

# Cleanup context file
rm -f .cat-execution-context

echo "Cleanup complete"
