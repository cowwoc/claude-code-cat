#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in validate-execution-lock.sh line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Hook: Validate and acquire execution lock before execute-change
# Trigger: PreToolUse for Skill tool when skill is cat:execute-change
# Purpose: Prevent concurrent change execution by different Claude sessions

# Get hook input from stdin
HOOK_INPUT=""
if [ ! -t 0 ]; then
    HOOK_INPUT=$(cat)
fi

if [[ -z "$HOOK_INPUT" ]]; then
    exit 0
fi

# Only process Skill tool invocations
TOOL_NAME=$(echo "$HOOK_INPUT" | jq -r '.tool_name // empty')
if [[ "$TOOL_NAME" != "Skill" ]]; then
    exit 0
fi

# Check if this is cat:execute-change skill
SKILL_NAME=$(echo "$HOOK_INPUT" | jq -r '.tool_input.skill // empty')
if [[ "$SKILL_NAME" != "cat:execute-change" && "$SKILL_NAME" != "execute-change" ]]; then
    exit 0
fi

# Get session ID from hook input (Claude Code provides this)
CURRENT_SESSION_ID=$(echo "$HOOK_INPUT" | jq -r '.session_id // empty')

if [[ -z "$CURRENT_SESSION_ID" || "$CURRENT_SESSION_ID" == "null" ]]; then
    # Session ID not in hook input - log and allow (don't block legitimate use)
    echo "DEBUG: session_id not found in PreToolUse input. Keys available:" >&2
    echo "$HOOK_INPUT" | jq -r 'keys[]' >&2
    exit 0
fi

# Require CLAUDE_PROJECT_DIR
if [[ -z "${CLAUDE_PROJECT_DIR:-}" ]]; then
    echo "ERROR: CLAUDE_PROJECT_DIR not set" >&2
    exit 1
fi

# Configuration
LOCK_DIR="${CLAUDE_PROJECT_DIR}/.claude/locks"
LOCK_FILE="$LOCK_DIR/execute-change.lock"
STALE_THRESHOLD_MINUTES=30

# Ensure lock directory exists
mkdir -p "$LOCK_DIR"

# Check for existing lock
if [[ -f "$LOCK_FILE" ]]; then
    source "$LOCK_FILE"

    # Check ownership
    if [[ "${LOCK_SESSION_ID:-}" == "$CURRENT_SESSION_ID" ]]; then
        # Same session owns the lock - update timestamp and proceed
        cat > "$LOCK_FILE" << EOF
LOCK_SESSION_ID="$CURRENT_SESSION_ID"
LOCK_TIMESTAMP=$(date +%s)
LOCK_PWD="$PWD"
EOF
        exit 0
    fi

    # Different session - check if stale
    CURRENT_TIMESTAMP=$(date +%s)
    LOCK_AGE_MINUTES=$(( (CURRENT_TIMESTAMP - ${LOCK_TIMESTAMP:-0}) / 60 ))

    if [[ "$LOCK_AGE_MINUTES" -ge "$STALE_THRESHOLD_MINUTES" ]]; then
        cat > "$LOCK_FILE" << EOF
LOCK_SESSION_ID="$CURRENT_SESSION_ID"
LOCK_TIMESTAMP=$(date +%s)
LOCK_PWD="$PWD"
EOF
        echo "⚠️  Stale lock (${LOCK_AGE_MINUTES}min) taken over from session ${LOCK_SESSION_ID:-unknown}" >&2
        exit 0
    fi

    # Lock is active and owned by different session - BLOCK
    cat << EOF
{
  "decision": "block",
  "reason": "EXECUTION LOCK CONFLICT

Another Claude session is executing a change.

Lock owner: ${LOCK_SESSION_ID:-unknown}
Lock age: ${LOCK_AGE_MINUTES} minutes
Your session: $CURRENT_SESSION_ID

Options:
1. Wait for the other session to complete
2. If no other session is active, delete: rm $LOCK_FILE
3. Use /cat:cleanup to remove stale locks

DO NOT proceed - concurrent execution may cause conflicts."
}
EOF
    exit 0
fi

# No existing lock - acquire it
cat > "$LOCK_FILE" << EOF
LOCK_SESSION_ID="$CURRENT_SESSION_ID"
LOCK_TIMESTAMP=$(date +%s)
LOCK_PWD="$PWD"
EOF

exit 0
