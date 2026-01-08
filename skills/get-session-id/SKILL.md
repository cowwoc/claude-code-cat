---
name: get-session-id
description: Session ID is automatically injected at SessionStart via echo-session-id.sh hook
allowed-tools: None (automatic hook)
---

# Get Session ID Skill

**Purpose**: The session ID is automatically available at session start via the `echo-session-id.sh` hook.

**How It Works**:
- The `echo-session-id.sh` hook is registered for `SessionStart` events
- When the session starts, the hook automatically injects the session ID into context
- The session ID appears in system reminders as: `✅ Session ID: {uuid}`
- No manual invocation required

**When to Use This Skill**:
- The session ID is already available - just look for it in the SessionStart system reminders
- Use this skill documentation to understand how the session ID is provided
- Reference when coordinating with hooks that use session IDs

## How Session IDs Work

Claude Code assigns a unique session ID (UUID v4) to each conversation session. This ID is used for:
- Naming conversation history files: `/home/node/.config/projects/-workspace/{session-id}.jsonl`
- Session-specific TODO list tracking
- Hook coordination across tools
- Task ownership in multi-instance scenarios

## Automatic Injection via SessionStart Hook

The `echo-session-id.sh` hook is registered in `.claude/settings.json` for `SessionStart` events.

**How it works**:
1. Claude Code invokes SessionStart hooks when the session starts OR resumes after compaction
2. `echo-session-id.sh` reads stdin JSON from Claude Code
3. Extracts `session_id` field using `jq`
4. Outputs `hookSpecificOutput` with formatted session ID
5. Claude Code injects this into context as a system reminder

**Output injected into context**:
```
✅ Session ID: b6933609-ab67-467e-af26-e48c3c8c129e
```

## Usage Examples

### Example 1: Get Current Session ID

The session ID is automatically available in SessionStart system reminders. Look for:

```
✅ Session ID: b6933609-ab67-467e-af26-e48c3c8c129e
```

No manual execution needed - it's already in your context!

### Example 2: Use Session ID for File Operations

After getting the session ID, you can use it to:

```bash
# Access conversation history
cat /home/node/.config/projects/-workspace/{session-id}.jsonl | jq -r 'select(.type == "message")'

# Find session-specific logs
find /home/node/.config/projects/-workspace/ -name "${SESSION_ID}*"

# Access session conversation
cat /home/node/.config/projects/-workspace/${SESSION_ID}.jsonl
```

## Integration with Hooks

The session ID from this skill matches the session ID that hooks receive via stdin:

```bash
# Hooks can use echo-session-id.sh to get formatted output
SESSION_OUTPUT=$(cat | ~/.claude/hooks/echo-session-id.sh)
echo "$SESSION_OUTPUT" | jq -r '.hookSpecificOutput'

# Or extract directly from stdin JSON
SESSION_ID=$(cat | jq -r '.session_id')
```

Both methods read from the same stdin JSON provided by Claude Code.

## Related

- **echo-session-id.sh**: Script that outputs session ID from Claude Code stdin JSON
- **get-history**: Skill that uses session ID to access conversation
- **hook-logger.sh**: Library that uses session ID for session-specific logging

## Implementation Details

The `echo-session-id.sh` script:
- Reads JSON from stdin (provided by Claude Code)
- Extracts `session_id` field using `jq`
- Outputs JSON with `hookSpecificOutput` field
- Requires `jq` to be installed (standard in Claude Code environment)

**Script Location**: `~/.claude/hooks/echo-session-id.sh`
