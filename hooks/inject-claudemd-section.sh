#!/bin/bash
set -euo pipefail

# Error handler
trap 'echo "ERROR in inject-claudemd-section.sh at line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Inject MANDATORY MISTAKE HANDLING section into CLAUDE.md
#
# This hook runs on SessionStart and ensures the project's CLAUDE.md
# contains instructions for invoking learn-from-mistakes skill.
#
# TRIGGER: SessionStart
#
# BEHAVIOR:
# - Checks if CLAUDE.md exists in project directory
# - If section marker not found, appends the section
# - Idempotent: safe to run multiple times
#
# UNINSTALL: Run /cat:cleanup or manually remove the section from CLAUDE.md

SECTION_MARKER="## MANDATORY MISTAKE HANDLING"

# Require CLAUDE_PROJECT_DIR
if [[ -z "${CLAUDE_PROJECT_DIR:-}" ]]; then
    echo "ERROR: CLAUDE_PROJECT_DIR not set" >&2
    exit 1
fi

# Find CLAUDE.md
CLAUDE_MD=""
if [[ -f "${CLAUDE_PROJECT_DIR}/CLAUDE.md" ]]; then
    CLAUDE_MD="${CLAUDE_PROJECT_DIR}/CLAUDE.md"
fi

# If no CLAUDE.md found, exit silently (project may not use CLAUDE.md)
if [[ -z "$CLAUDE_MD" ]]; then
    echo '{}'
    exit 0
fi

# Check if section already exists
if grep -q "$SECTION_MARKER" "$CLAUDE_MD" 2>/dev/null; then
    # Section already present, nothing to do
    echo '{}'
    exit 0
fi

# Append the section to CLAUDE.md
cat >> "$CLAUDE_MD" << 'EOF'

## MANDATORY MISTAKE HANDLING

**CRITICAL**: Invoke `learn-from-mistakes` skill for ANY mistake.

**Mistakes include**: Protocol violations, rework, build failures, tool misuse, logical errors

**Invocation patterns** (any of these work):
- `/learn-from-mistakes`
- "Learn from mistakes: <description>"
- "run the learn-from-mistakes skill"

**What the skill does**: Analyzes root cause using 5-Whys, implements prevention at the appropriate
level (code fix > validation > hook > test > documentation), and verifies the fix works.
EOF

# Output success message via additionalContext
cat << 'JSONEOF'
{
  "hookSpecificOutput": {
    "hookEventName": "SessionStart",
    "additionalContext": "✅ Added MANDATORY MISTAKE HANDLING section to CLAUDE.md. This ensures the learn-from-mistakes skill is invoked for any mistakes during this session."
  }
}
JSONEOF

exit 0
