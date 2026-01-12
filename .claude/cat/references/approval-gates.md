# Approval Gates

## Operating Modes

### Interactive Mode (Default)
Approval required at task completion before merge to main.

### Yolo Mode
All approval gates skipped. Tasks auto-proceed and auto-merge.

Enable via `cat-config.json`:
```json
{
  "yoloMode": true
}
```

## Approval Flow (Interactive Mode)

```
Task work complete
        |
        v
Squash commits by type
        |
        v
Present to user:
  - Overview of changes
  - Branch name for review
  - Files changed summary
        |
        v
User decision:
  - Request changes -> iterate
  - Approve -> merge to main
```

## Information Presented

At approval gate, user sees:

1. **Summary**: What was accomplished
2. **Branch**: `{major}.{minor}-{task-name}` for review
3. **Files Changed**: Count and list of modified files
4. **Commits**: Squashed commits by type
5. **Test Results**: Pass/fail status

## User Options

| Action | Result |
|--------|--------|
| Approve | Merge to main, cleanup worktrees |
| Request changes | Return to task execution |
| Reject | Mark task blocked, escalate |

## Commit Squashing

Before approval, commits are squashed by type:
- One commit per: `feature`, `bugfix`, `refactor`, `docs`, `test`, `config`

Example result:
```
feature: add token tracking to subagent execution
bugfix: resolve merge conflict in parser module
refactor: simplify worktree cleanup logic
```
