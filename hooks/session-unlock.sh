#!/bin/bash
# Hook: session-unlock.sh
# Trigger: SessionEnd
# Purpose: Remove session lock file on clean exit

set -euo pipefail
trap 'echo "ERROR in session-unlock.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Configuration (must match session-lock.sh)
LOCK_DIR="${LOCK_DIR:-.claude/locks}"
PROJECT_NAME="${PWD##*/}"
LOCK_FILE="$LOCK_DIR/${PROJECT_NAME}.lock"

# Remove lock file if it exists
if [[ -f "$LOCK_FILE" ]]; then
    rm -f "$LOCK_FILE"
    echo "Session lock released: $LOCK_FILE"
fi

exit 0
