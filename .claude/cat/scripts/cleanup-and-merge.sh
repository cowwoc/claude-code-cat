#!/bin/bash
# CAT Cleanup and Merge Script
# Cleans up locks, merges worktree work, merges completed phases to main
#
# Usage: scripts/cleanup-and-merge.sh <PLAN_PATH>

set -euo pipefail

PLAN_PATH="${1:-}"
if [[ -z "$PLAN_PATH" ]]; then
  echo "Usage: scripts/cleanup-and-merge.sh <PLAN_PATH>"
  exit 1
fi

LOCK_FILE=".cat-execution.lock"

# Extract plan ID for plan-specific lock
PLAN_ID=$(basename "$PLAN_PATH" | grep -oE '^[0-9]+(\.[0-9]+)?-[0-9]+' || echo "unknown")
PLAN_LOCK_FILE=".cat-plan-${PLAN_ID}.lock"

# Remove lock files
rm -f "$LOCK_FILE"
rm -f "$PLAN_LOCK_FILE"

# Check if phase is complete
PHASE_DIR=$(dirname "$PLAN_PATH")
PLAN_COUNT=$(ls "$PHASE_DIR"/*-PLAN.md 2>/dev/null | wc -l | tr -d ' ')
SUMMARY_COUNT=$(ls "$PHASE_DIR"/*-SUMMARY.md 2>/dev/null | wc -l | tr -d ' ')

PHASE_COMPLETE=false
if [[ $SUMMARY_COUNT -eq $PLAN_COUNT ]]; then
  PHASE_COMPLETE=true
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

  # Merge worktree branch into phase branch
  git checkout "$PHASE_BRANCH" 2>/dev/null || git checkout -b "$PHASE_BRANCH"
  git merge "$WORKTREE_BRANCH" -m "Merge work from $WORKTREE_BRANCH"

  # Remove worktree and branch
  git worktree remove "$WORKTREE_PATH" --force
  git branch -d "$WORKTREE_BRANCH"

  echo "Merged and cleaned up worktree"
fi

# Merge to main if phase complete
if [[ "$PHASE_COMPLETE" == "true" && -n "${PHASE_BRANCH:-}" ]]; then
  echo "Phase complete - merging to main..."

  git checkout main

  # Fast-forward merge (linear history)
  if ! git merge --ff-only "$PHASE_BRANCH"; then
    # If ff-only fails, rebase and retry
    git checkout "$PHASE_BRANCH"
    git rebase main
    git checkout main
    git merge --ff-only "$PHASE_BRANCH"
  fi

  # Delete phase branch
  git branch -d "$PHASE_BRANCH"

  echo "Phase merged to main"
fi

# Cleanup context file
rm -f .cat-execution-context

echo "Cleanup complete"
