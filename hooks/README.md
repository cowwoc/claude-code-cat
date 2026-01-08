# Plugin Hooks

These hooks are **automatically installed** when the CAT plugin is enabled. They provide quality gates
and mistake detection without manual configuration.

## Available Hooks

### SessionStart Hooks

#### echo-session-id.sh
**Purpose**: Inject session ID into Claude's context (not visible to user)

Features:
- Extracts session_id from Claude Code stdin JSON
- Outputs via `hookSpecificOutput` (silent to user, visible to Claude)
- Used by get-session-id skill and session coordination

#### restore-todowrite.sh
**Purpose**: Restore TodoWrite state from backup on session start

Features:
- Reads from `.claude/backups/todowrite/todowrite_session_*.json`
- Only restores if backup is less than 24 hours old
- Displays recovered pending tasks

#### check-retrospective-due.sh
**Purpose**: Check if retrospective analysis is due

#### inject-claudemd-section.sh
**Purpose**: Adds MANDATORY MISTAKE HANDLING section to project's CLAUDE.md

Features:
- Automatically injects learn-from-mistakes skill instructions
- Idempotent: safe to run multiple times

### UserPromptSubmit Hooks

#### critical-thinking.sh
**Purpose**: Injects evidence-based critical thinking requirements

Enforces:
- Gather evidence before making claims
- Identify flaws and edge cases based on evidence

#### detect-giving-up.sh
**Purpose**: Detects when Claude is abandoning complex problems

Detects:
- Phrases like "let's try a simpler approach"
- "this is too complex" patterns
- Injects persistence reminder

**Dependencies**: Requires `lib/json-parser.sh`

#### verify-destructive-operations.sh
**Purpose**: Warn about destructive operations (squash, consolidate, etc.)

#### detect-user-reported-issue.sh
**Purpose**: Detect when user reports issues and prompt for TodoWrite tracking

### PreCompact Hooks

#### save-todowrite.sh
**Purpose**: Save TodoWrite state before context compaction

Features:
- Saves to `.claude/backups/todowrite/todowrite_session_${SESSION_ID}.json`
- Extracts todos from compact context if available
- Keeps only last 5 session backups

### PreToolUse Hooks (Bash)

#### validate-git-operations.sh
**Purpose**: Warn or block dangerous git commands

Intercepts:
- `git filter-branch --all`
- `git push --force`
- `git rebase --all`
- Operations on version branches

#### validate-git-filter-branch.sh
**Purpose**: Validate git filter-branch operations

#### block-main-rebase.sh
**Purpose**: Block rebase operations on main/master branches

#### remind-git-squash-skill.sh
**Purpose**: Remind to use git-squash/git-rebase skills

Detects:
- `git rebase -i` → reminds about git-squash skill
- `git rebase <branch>` → reminds about git-rebase skill

#### block-reflog-destruction.sh
**Purpose**: Block commands that destroy git reflog history

### PreToolUse Hooks (Read|Glob|Grep)

#### predict-batch-opportunity.sh
**Purpose**: Suggest batch operations when multiple sequential reads detected

### PostToolUse Hooks (All)

#### auto-learn-from-mistakes.sh
**Purpose**: Detects errors and prompts root cause analysis

Detects:
- Build failures, test failures, merge conflicts
- Edit failures, git operation failures, parse errors
- Wrong working directory errors, self-acknowledged mistakes

#### detect-assistant-giving-up.sh
**Purpose**: Detect giving-up patterns in assistant responses

### PostToolUse Hooks (Bash)

#### detect-failures.sh
**Purpose**: Detect build/test failures and suggest learn-from-mistakes skill

Detects:
- Non-zero exit codes
- Common failure patterns (BUILD FAILED, ERROR, etc.)

#### detect-concatenated-commit-message.sh
**Purpose**: Detect poorly formatted commit messages from squash operations

#### validate-rebase-target.sh
**Purpose**: Validate rebase target branches

### PostToolUse Hooks (Write|Edit)

#### remind-restart-after-skill-modification.sh
**Purpose**: Remind to restart session after modifying skill files

### PostToolUse Hooks (Read|Glob|Grep|WebFetch|WebSearch)

#### detect-sequential-tools.sh
**Purpose**: Detect sequential tool calls that could be parallelized

### SessionEnd Hooks

#### session-unlock.sh
**Purpose**: Release execution lock on session end (fallback cleanup)

Features:
- Removes lock file if it exists when session ends
- Fallback cleanup for locks not released on plan completion

## Deprecated Hooks

#### session-lock.sh
Was previously triggered on SessionStart. Lock acquisition now happens in
execute-phase.md workflow when /cat:execute-plan is invoked.

## Customization

Each hook has configuration variables at the top of the file. Edit these to match your project's needs:

- Protected branch patterns
- Failure detection patterns
- Lock file location
