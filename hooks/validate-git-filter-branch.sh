#!/bin/bash
set -euo pipefail

# Error handler - output helpful message to stderr on failure
trap 'echo "ERROR in validate-git-filter-branch.sh at line $LINENO: Command failed: $BASH_COMMAND" >&2; exit 1' ERR

# Source helper for proper hook blocking
source /workspace/.claude/scripts/json-output.sh

# Validates git filter-branch and other history-rewriting commands
# Prevents use of --all or --branches flags that would rewrite protected version branches
#
# TRIGGER: PreToolUse on Bash commands (via .claude/settings.json)
# CHECKS: Blocks git filter-branch/rebase with --all/--branches
# ACTION: Blocks command and displays safety instructions
#
# Related: CLAUDE.md § Git History Rewriting Safety

# Parse hook input
INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
COMMAND=$(echo "$INPUT" | jq -r '.tool_input.command // empty')

# Only check Bash tool commands
if [[ "$TOOL_NAME" != "Bash" ]]; then
  echo "{}"
  exit 0
fi

# Check for dangerous git history-rewriting patterns
# Only check actual filter-branch/rebase commands, not git commit messages
if echo "$COMMAND" | grep -qE '(^|;|&&|\|)\s*git\s+(filter-branch|rebase)\s+.*\s+--(all|branches)(\s|$)'; then
  cat >&2 <<EOF
🚨 CRITICAL: DANGEROUS GIT HISTORY REWRITING DETECTED

**Blocked command**: git filter-branch/rebase with --all or --branches

**Problem**: This would rewrite ALL branches, including protected version branches (v1, v13, v21, etc.)

**Version branches are permanent project markers and must NEVER be rewritten**

**Solution**: Target SPECIFIC branch instead:

  ✅ CORRECT:
  git filter-branch ... main
  git rebase -i HEAD~10

  ❌ WRONG:
  git filter-branch ... --all
  git rebase --all

**See**: CLAUDE.md § Git History Rewriting Safety
EOF
  # Use proper permission system: reason to Claude, exit 0 for JSON processing
  # Note: detailed message already sent to stderr via heredoc above
  output_hook_block "Blocked: git filter-branch/rebase with --all/--branches would rewrite protected version branches. Target specific branch instead." ""
  exit 0
fi

# Warn about git filter-branch usage (deprecated, use git-filter-repo instead)
if echo "$COMMAND" | grep -qE '(^|;|&&|\|)\s*git\s+filter-branch'; then
  cat >&2 <<EOF
⚠️  WARNING: git filter-branch is DEPRECATED

**Recommended**: Use git-filter-repo instead (faster, safer, maintained)

  ❌ DEPRECATED:
  git filter-branch --index-filter 'git rm --cached file' HEAD

  ✅ MODERN:
  git filter-repo --path file --invert-paths

**Install**: pip install git-filter-repo
**Skill**: /cat:git-rewrite-history

Proceeding with filter-branch (not blocked, but not recommended)...
EOF
fi

# Allow command
echo "{}"
exit 0
