---
name: cat:monitor-subagents
description: Check status of running subagents including token usage and context limits
---

# Monitor Subagents

## Purpose

Track the status of active subagents, monitor their token consumption, detect context limit
approaches, and identify compaction events. Enables the parent agent to make informed decisions
about intervention or result collection.

## When to Use

- After spawning one or more subagents
- Periodically during long-running orchestration
- When deciding whether to spawn additional subagents
- Before collecting results to verify completion
- When suspecting a subagent has stalled or hit limits

## Workflow

### 1. List Active Worktrees

```bash
# Get all subagent worktrees
git worktree list --porcelain | grep -A2 "worktree.*-sub-"
```

Output interpretation:
```
worktree /workspace/.worktrees/1.2-parser-sub-a1b2c3d4
HEAD abc123def456
branch refs/heads/1.2-parser-sub-a1b2c3d4
```

### 2. Check Token Usage Per Subagent

Read session file for each subagent:

```bash
SESSION_FILE="/home/node/.config/claude/projects/-workspace/${SESSION_ID}.jsonl"

# Calculate total tokens used
jq -s '[.[] | select(.type == "assistant") | .message.usage |
  (.input_tokens + .output_tokens)] | add' "${SESSION_FILE}"
```

### 3. Detect Context Limit Approach

**Critical Threshold: 40% = 80,000 tokens** (of 200K context)

```bash
# Check if approaching limit
TOKENS_USED=$(calculate_tokens "${SESSION_ID}")
THRESHOLD=80000

if [ "${TOKENS_USED}" -ge "${THRESHOLD}" ]; then
  echo "WARNING: Subagent ${SESSION_ID} at ${TOKENS_USED} tokens (40%+ of context)"
fi
```

### 4. Detect Compaction Events

Compaction (context window resets) indicate the subagent hit context limits:

```bash
# Count compaction events
jq -s '[.[] | select(.type == "summary")] | length' "${SESSION_FILE}"
```

**If compaction count > 0**: Subagent has experienced context pressure. Consider:
- Collecting partial results immediately
- Decomposing remaining work
- Spawning fresh subagent for continuation

### 5. Check Subagent Activity

```bash
# Check last activity timestamp
jq -s 'last | .timestamp' "${SESSION_FILE}"

# Compare to current time for staleness detection
```

### 6. Generate Status Report

```yaml
subagent_status:
  - id: a1b2c3d4
    task: 1.2-implement-parser
    status: running
    tokens_used: 45000
    tokens_percent: 22.5%
    compaction_events: 0
    last_activity: 2026-01-10T14:45:00Z
    health: healthy

  - id: b2c3d4e5
    task: 1.3-implement-formatter
    status: running
    tokens_used: 85000
    tokens_percent: 42.5%
    compaction_events: 1
    last_activity: 2026-01-10T14:40:00Z
    health: WARNING - approaching context limit
```

## Examples

### Quick Status Check

```bash
# List all subagent worktrees with status
for worktree in $(git worktree list --porcelain | grep "worktree.*-sub-" | cut -d' ' -f2); do
  echo "Worktree: ${worktree}"
  # Check if session file exists and get metrics
done
```

### Token Usage Summary

```bash
# Aggregate token usage across all subagents
TOTAL=0
for session in $(get_subagent_sessions); do
  TOKENS=$(calculate_tokens "${session}")
  TOTAL=$((TOTAL + TOKENS))
  echo "${session}: ${TOKENS} tokens"
done
echo "Total: ${TOTAL} tokens"
```

### Health Check with Alerts

```yaml
# Output format for parent agent decision-making
health_check:
  timestamp: 2026-01-10T14:50:00Z
  active_subagents: 3
  alerts:
    - subagent: b2c3d4e5
      alert: CONTEXT_LIMIT_WARNING
      tokens_used: 85000
      recommendation: "Consider collecting results and spawning fresh subagent"
    - subagent: c3d4e5f6
      alert: STALLED
      last_activity: 2026-01-10T13:00:00Z
      recommendation: "Check for errors or blocked state"
```

## Anti-Patterns

### Do NOT poll too frequently

```bash
# ❌ Checking every second
while true; do
  monitor-subagents
  sleep 1
done

# ✅ Reasonable polling interval (30-60 seconds)
while true; do
  monitor-subagents
  sleep 60
done
```

### Do NOT ignore compaction events

```bash
# ❌ Ignoring compaction
if [ "${COMPACTIONS}" -gt 0 ]; then
  echo "Compaction detected, continuing anyway..."
fi

# ✅ Treat compaction as intervention signal
if [ "${COMPACTIONS}" -gt 0 ]; then
  echo "Compaction detected - initiating result collection"
  # Trigger collect-results
fi
```

### Do NOT assume worktree presence means running

```bash
# ❌ Assuming worktree = active subagent
ACTIVE_COUNT=$(git worktree list | grep -c "-sub-")

# ✅ Verify session activity
for worktree in $(get_subagent_worktrees); do
  if is_session_active "${worktree}"; then
    ACTIVE_COUNT=$((ACTIVE_COUNT + 1))
  fi
done
```

## Related Skills

- `cat:spawn-subagent` - Launch new subagents
- `cat:collect-results` - Gather completed subagent work
- `cat:token-report` - Detailed token analysis
- `cat:parallel-execute` - Orchestrate multiple subagents
