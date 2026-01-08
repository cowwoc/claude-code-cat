#!/bin/bash
# Injects session ID at SessionStart (including after compaction)
# Simplified bash port of ensure-session-id.py
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in echo-session-id.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Read stdin
if [ -t 0 ]; then
    exit 0
fi

stdin_content=$(cat)

if [ -z "$stdin_content" ]; then
    exit 0
fi

# Extract session_id from stdin JSON
session_id=$(echo "$stdin_content" | jq -r '.session_id // empty')

if [ -z "$session_id" ] || [ "$session_id" = "null" ]; then
    exit 0
fi

# Output session ID via hookSpecificOutput
# Note: SessionStart fires both at initial start AND after compaction,
# so this automatically re-injects the session ID when needed
jq -n --arg sid "$session_id" '{
    "hookSpecificOutput": {
        "hookEventName": "SessionStart",
        "additionalContext": "Session ID: \($sid)"
    }
}'
