#!/bin/bash
# CAT Worktree Setup Script
# Creates worktrees for parallel Claude instances using deterministic naming
#
# Usage: source scripts/worktree-setup.sh <PLAN_PATH> [SESSION_ID]
# Output: Sets EXECUTION_DIR variable for where to run commands
#
# Worktree location: .worktrees/m{milestone}-{plan_id}
# Example: .worktrees/m1-02-01-setup-jwt (milestone 1, plan 02-01-setup-jwt)
#
# Session tracking: Each plan has a lock file tracking which session owns it

set -euo pipefail

PLAN_PATH="${1:-}"
SESSION_ID="${2:-${CLAUDE_SESSION_ID:-$$}}"  # Use session ID if provided, else env var, else PID

if [[ -z "$PLAN_PATH" ]]; then
  echo "Usage: source scripts/worktree-setup.sh <PLAN_PATH> [SESSION_ID]"
  exit 1
fi

LOCK_FILE=".cat-execution.lock"

if [[ -z "${CLAUDE_PROJECT_DIR:-}" ]]; then
  echo "ERROR: CLAUDE_PROJECT_DIR environment variable is not set."
  echo "This script must be run from a Claude Code hook context."
  echo "See: https://code.claude.com/docs/en/hooks#project-specific-hook-scripts"
  exit 1
fi

PROJECT_DIR="$CLAUDE_PROJECT_DIR"
WORKTREES_DIR="${PROJECT_DIR}/.worktrees"

# Extract identifiers from plan path (e.g., .planning/phases/02-auth/02-01-setup-jwt-PLAN.md)
PLAN_ID=$(basename "$PLAN_PATH" | sed 's/-PLAN\.md$//')  # e.g., "02-01-setup-jwt"
PHASE_NUM=$(echo "$PLAN_ID" | grep -oE '^[0-9]+(\.[0-9]+)?' | head -1)  # e.g., "02" or "02.1"
PHASE_NAME=$(basename "$(dirname "$PLAN_PATH")" | sed 's/^[0-9.]*-//')

# Plan-specific lock file
PLAN_LOCK_FILE=".cat-plan-${PLAN_ID}.lock"

# Check if this specific plan is already being worked on
if [[ -f "$PLAN_LOCK_FILE" ]]; then
  EXISTING_SESSION=$(cat "$PLAN_LOCK_FILE")
  echo "ERROR: Plan $PLAN_ID is already being executed by another Claude instance."
  echo ""
  echo "  Existing session: $EXISTING_SESSION"
  echo "  Your session: $SESSION_ID"
  echo ""
  echo "Wait for the other instance to complete, or remove the lock file if the session crashed:"
  echo "  rm $PLAN_LOCK_FILE"
  exit 1
fi

# Create plan-specific lock with session ID
echo "$SESSION_ID" > "$PLAN_LOCK_FILE"

# Get current milestone from STATE.md or default to 1
MILESTONE=$(grep -oE 'Milestone: [0-9]+' "${PROJECT_DIR}/.planning/STATE.md" 2>/dev/null | grep -oE '[0-9]+' || echo "1")

# Deterministic worktree name: m1-02-01-setup-jwt
WORKTREE_ID="m${MILESTONE}-${PLAN_ID}"
WORKTREE_PATH="${WORKTREES_DIR}/${WORKTREE_ID}"
PHASE_BRANCH="phase/${PHASE_NUM}-${PHASE_NAME}"
WORKTREE_BRANCH="${PHASE_BRANCH}/${WORKTREE_ID}"

# Check for active execution in main directory (general lock file)
if [[ -f "$LOCK_FILE" ]]; then
  LOCK_SESSION=$(cat "$LOCK_FILE")
  echo "Another instance active (session: $LOCK_SESSION), creating worktree..."

  # Ensure worktrees directory exists and is gitignored
  mkdir -p "$WORKTREES_DIR"
  if ! grep -qxF '.worktrees/' "${PROJECT_DIR}/.gitignore" 2>/dev/null; then
    echo '.worktrees/' >> "${PROJECT_DIR}/.gitignore"
    echo "Added .worktrees/ to .gitignore"
  fi

  # Try to create worktree with deterministic name
  if ! git worktree add "$WORKTREE_PATH" -b "$WORKTREE_BRANCH" 2>/dev/null; then
    echo "ERROR: Worktree $WORKTREE_PATH already exists or branch $WORKTREE_BRANCH exists"
    echo "Another instance may already be working on this plan."
    # Clean up plan lock since we're failing
    rm -f "$PLAN_LOCK_FILE"
    exit 1
  fi

  # Move plan lock to worktree
  mv "$PLAN_LOCK_FILE" "$WORKTREE_PATH/$PLAN_LOCK_FILE"

  # Record context for cleanup
  cat > "$WORKTREE_PATH/.cat-execution-context" <<EOF
WORKTREE_PATH=$WORKTREE_PATH
WORKTREE_BRANCH=$WORKTREE_BRANCH
MAIN_PROJECT=$PROJECT_DIR
PHASE_BRANCH=$PHASE_BRANCH
PLAN_ID=$PLAN_ID
SESSION_ID=$SESSION_ID
EOF

  # Create general lock in worktree
  echo "$SESSION_ID" > "$WORKTREE_PATH/$LOCK_FILE"

  echo "Created worktree at: $WORKTREE_PATH"
  EXECUTION_DIR="$WORKTREE_PATH"
else
  # First instance - work in main directory, create lock
  echo "$SESSION_ID" > "$LOCK_FILE"

  # Create/switch to phase branch
  CURRENT_BRANCH=$(git branch --show-current)

  if [[ "$CURRENT_BRANCH" != "$PHASE_BRANCH" ]]; then
    if git show-ref --verify --quiet "refs/heads/$PHASE_BRANCH"; then
      git checkout "$PHASE_BRANCH"
    else
      git checkout -b "$PHASE_BRANCH"
    fi
  fi

  cat > .cat-execution-context <<EOF
PHASE_BRANCH=$PHASE_BRANCH
PLAN_ID=$PLAN_ID
SESSION_ID=$SESSION_ID
EOF

  EXECUTION_DIR="$PROJECT_DIR"
fi

echo "EXECUTION_DIR=$EXECUTION_DIR"
echo "Plan $PLAN_ID locked by session: $SESSION_ID"
