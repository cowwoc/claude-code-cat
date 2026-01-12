# Agent Architecture

## Main Agent

### Responsibilities

| Area | Actions |
|------|---------|
| Orchestration | Coordinate subagent execution |
| Planning | Read code, make decisions, decompose tasks |
| Metadata | Create/update planning documents |
| Git operations | Branch creation, merging |
| Conflict resolution | Resolve merge conflicts |
| Queue processing | Handle subagent returns serially |
| State updates | Update STATE.md after completions |

### Does NOT

- Write production code directly
- Execute implementation tasks
- Work in worktrees except for merging

### Worktree Usage

Main agent uses worktrees ONLY for:
- Merging subagent branches into task branches
- Merging task branches into main

## Subagent

### Responsibilities

| Area | Actions |
|------|---------|
| Execution | Perform coding tasks |
| Isolation | Work in dedicated worktree |
| Token tracking | Monitor context usage |
| Compaction detection | Track summary events |
| Reporting | Return metrics on completion |
| Fail-fast | Return immediately on plan issues |

### Token Tracking

Subagents read session file:
```
/home/node/.config/claude/projects/-workspace/{SESSION_ID}.jsonl
```

Collect:
1. Sum `input_tokens + output_tokens`
2. Count `type: "summary"` entries

### Return Protocol

On completion, subagent returns:
- Success/failure status
- Token usage metrics
- Compaction event count
- Work summary

## Communication Flow

```
Main Agent
    |
    +---> Spawn Subagent 1 (task-a)
    |         |
    +---> Spawn Subagent 2 (task-b)
    |         |
    v         v
    [Wait for completions]
         |
         v
    Process returns serially
         |
         v
    Merge branches
         |
         v
    Update STATE.md
```

## Parallel Execution

- No arbitrary limits on concurrent subagents
- Main agent manages based on available resources
- Independent tasks execute simultaneously
- Dependent tasks wait for prerequisites
