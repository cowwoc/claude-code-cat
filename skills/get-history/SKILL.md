---
name: get-history
description: Access raw conversation history from Claude Code session storage for analysis
---

# Get History Skill

**Purpose**: Provide direct access to raw conversation history stored by Claude Code for debugging and conversation analysis.

**When to Use**:
- When investigating tool call sequences
- To verify what happened earlier in a session
- When analyzing agent behavior patterns
- To debug unexpected outcomes

## Skill Capabilities

### 1. Access Current Session Conversation

**Location**: `/home/node/.config/projects/-workspace/{session-id}.jsonl`

**Session ID Detection**:

The session ID is automatically provided via the `get-session-id` skill (SessionStart hook).

Look for the system reminder in context:
```
✅ Session ID: 88194cb6-734b-498c-ab5d-ac7c773d8b34
```

Then use it directly:
```bash
SESSION_ID="88194cb6-734b-498c-ab5d-ac7c773d8b34"
cat /home/node/.config/projects/-workspace/${SESSION_ID}.jsonl
```

### 2. Parse Conversation Structure

**Entry Types**:
- `type: "summary"` - High-level conversation summary
- `type: "message"` - User or assistant messages
- `type: "tool_use"` - Tool invocations
- `type: "tool_result"` - Tool outputs

**Extract Messages**:
```bash
# Get all user messages
jq 'select(.type == "message" and .role == "user") | .content' conversation.jsonl

# Get all tool uses with names
jq 'select(.type == "tool_use") | {name: .name, input: .input}' conversation.jsonl
```

### 3. Agent Sidechain Conversations

**Path Pattern**: `/home/node/.config/projects/-workspace/agent-{agent-id}.jsonl`

```bash
# Find all agent sidechain logs
ls -lht ~/.config/projects/-workspace/agent-*.jsonl | head -10
```

## Error Handling

If session ID not in context, report error - do NOT guess.

## Integration

**Complements learn-from-mistakes**:
- get-history: Provides raw conversation data
- learn-from-mistakes: Analyzes mistakes and recommends fixes
