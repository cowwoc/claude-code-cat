# Plugin Hooks

These hooks are **automatically installed** when the CAT plugin is enabled. They provide quality gates
and mistake detection without manual configuration.

## Available Hooks

### auto-learn-from-mistakes.sh
**Trigger**: PostToolUse (all tools)
**Purpose**: Detects errors and prompts root cause analysis

Detects:
- Build failures, test failures, merge conflicts
- Edit failures, git operation failures, parse errors
- Wrong working directory errors, self-acknowledged mistakes

### critical-thinking.sh
**Trigger**: UserPromptSubmit
**Purpose**: Injects evidence-based critical thinking requirements

Enforces:
- Gather evidence before making claims
- Identify flaws and edge cases based on evidence

### detect-giving-up.sh
**Trigger**: UserPromptSubmit
**Purpose**: Detects when Claude is abandoning complex problems

Detects:
- Phrases like "let's try a simpler approach"
- "this is too complex" patterns
- Injects persistence reminder to continue problem-solving

**Dependencies**: Requires `lib/json-parser.sh`

### validate-git-operations.sh
**Trigger**: PreToolUse for Bash
**Purpose**: Warn or block dangerous git commands before execution

Intercepts:
- `git filter-branch --all`
- `git push --force`
- `git rebase --all`
- Operations on version branches

### detect-failures.sh
**Trigger**: PostToolUse for Bash
**Purpose**: Detect build/test failures and suggest learn-from-mistakes skill

Detects:
- Non-zero exit codes
- Common failure patterns (BUILD FAILED, ERROR, etc.)
- Test failures

### session-unlock.sh
**Trigger**: SessionEnd
**Purpose**: Release execution lock on session end (fallback cleanup)

Features:
- Removes lock file if it exists when session ends
- Fallback cleanup for locks not released on plan completion
- Handles abnormal session termination

**Note**: Lock acquisition now happens in execute-phase.md workflow, not on SessionStart.
The lock is normally released when a plan completes, but this hook provides
fallback cleanup if the session ends before plan completion.

### session-lock.sh (deprecated - no longer used)
Was previously triggered on SessionStart. Lock acquisition now happens in
execute-phase.md workflow when /cat:execute-plan is invoked.

### echo-session-id.sh
**Trigger**: SessionStart
**Purpose**: Inject session ID into Claude's context (not visible to user)

Features:
- Extracts session_id from Claude Code stdin JSON
- Outputs via `hookSpecificOutput` (silent to user, visible to Claude)
- Used by get-session-id skill and session coordination

### inject-claudemd-section.sh
**Trigger**: SessionStart
**Purpose**: Adds MANDATORY MISTAKE HANDLING section to project's CLAUDE.md

Features:
- Automatically injects learn-from-mistakes skill instructions
- Idempotent: safe to run multiple times
- Ensures Claude knows how to invoke the skill when mistakes occur

## Customization

Each hook has configuration variables at the top of the file. Edit these to match your project's needs:

- Protected branch patterns
- Failure detection patterns
- Lock file location
