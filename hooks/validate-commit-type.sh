#!/bin/bash
set -euo pipefail
trap 'echo "ERROR in validate-commit-type.sh line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Validate commit message types against allowed list
# Triggered by: PreToolUse hook on Bash tool with git commit

# Read JSON input
INPUT=$(cat)

# Check if this is a git commit command
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

if [[ -z "$COMMAND" ]] || ! echo "$COMMAND" | grep -qE 'git\s+commit'; then
    exit 0
fi

# Extract commit message from -m flag or HEREDOC
# Pattern 1: git commit -m "message"
# Pattern 2: git commit -m 'message'
# Pattern 3: git commit -m "$(cat <<'EOF' ... EOF)"

COMMIT_MSG=""

# Try to extract message from -m flag (simple form)
if echo "$COMMAND" | grep -qE -- '-m\s+["\x27]'; then
    # Simple -m "message" or -m 'message'
    COMMIT_MSG=$(echo "$COMMAND" | sed -nE 's/.*-m\s+["\x27]([^"\x27]+).*/\1/p')
fi

# Try HEREDOC pattern
if [[ -z "$COMMIT_MSG" ]] && echo "$COMMAND" | grep -qE "<<'?EOF"; then
    # Extract first line after EOF marker
    COMMIT_MSG=$(echo "$COMMAND" | sed -n '/<<.*EOF/,/EOF/p' | sed '1d;$d' | head -1)
fi

# If no message found, allow (might be interactive or amend)
if [[ -z "$COMMIT_MSG" ]]; then
    exit 0
fi

# Valid commit types from git-integration.md
VALID_TYPES="feature|bugfix|test|refactor|performance|config|docs|wip|planning"

# Extract type from message (first word before colon)
COMMIT_TYPE=$(echo "$COMMIT_MSG" | sed -nE 's/^([a-z]+):.*/\1/p')

if [[ -z "$COMMIT_TYPE" ]]; then
    # No type prefix found - allow (might be merge commit or special format)
    exit 0
fi

# Validate type
if ! echo "$COMMIT_TYPE" | grep -qE "^($VALID_TYPES)$"; then
    cat <<EOF
{
  "decision": "block",
  "reason": "Invalid commit type '$COMMIT_TYPE'. Valid types: feature, bugfix, test, refactor, performance, config, docs, wip, planning"
}
EOF
    exit 0
fi

# Type is valid
exit 0
