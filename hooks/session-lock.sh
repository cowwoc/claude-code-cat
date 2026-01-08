#!/bin/bash
# Hook: session-lock.sh
# Trigger: SessionStart
# Purpose: Multi-instance coordination via lock files

set -euo pipefail
trap 'echo "ERROR in session-lock.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Require CLAUDE_PROJECT_DIR
if [[ -z "${CLAUDE_PROJECT_DIR:-}" ]]; then
    echo "ERROR: CLAUDE_PROJECT_DIR not set" >&2
    exit 1
fi

# Configuration
LOCK_DIR="${CLAUDE_PROJECT_DIR}/.claude/locks"
LOCK_TIMEOUT_MINUTES=30
PROJECT_NAME="${PWD##*/}"

# Create lock directory if needed
mkdir -p "$LOCK_DIR"

LOCK_FILE="$LOCK_DIR/${PROJECT_NAME}.lock"
TIMESTAMP=$(date +%s)

# Extract session ID from stdin JSON (Claude Code provides this)
SESSION_ID=""
if [ ! -t 0 ]; then
    stdin_content=$(cat)
    if [ -n "$stdin_content" ]; then
        SESSION_ID=$(echo "$stdin_content" | jq -r '.session_id // empty' 2>/dev/null || true)
    fi
fi

# Fallback to timestamp if no session ID available
if [ -z "$SESSION_ID" ] || [ "$SESSION_ID" = "null" ]; then
    SESSION_ID="fallback-$(date +%s)"
fi

# Check for existing lock
if [[ -f "$LOCK_FILE" ]]; then
    # Read existing lock
    source "$LOCK_FILE"

    # Calculate age
    AGE_MINUTES=$(( (TIMESTAMP - LOCK_TIMESTAMP) / 60 ))

    if [[ "$AGE_MINUTES" -lt "$LOCK_TIMEOUT_MINUTES" ]]; then
        echo "⚠️  Another Claude session may be active"
        echo ""
        echo "Lock file: $LOCK_FILE"
        echo "Session: $LOCK_SESSION_ID"
        echo "Age: ${AGE_MINUTES} minutes"
        echo ""
        echo "If the other session has ended, delete the lock file:"
        echo "  rm $LOCK_FILE"
        echo ""
        echo "Proceeding anyway (lock is advisory only)"
    fi
fi

# Create/update lock file
cat > "$LOCK_FILE" << EOF
LOCK_SESSION_ID="$SESSION_ID"
LOCK_TIMESTAMP=$TIMESTAMP
LOCK_PWD="$PWD"
EOF

echo "Session lock acquired: $LOCK_FILE"

# Note: Cleanup should happen at session end
# This hook only acquires the lock
exit 0
