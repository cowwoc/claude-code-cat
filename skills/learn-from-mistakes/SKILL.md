---
name: learn-from-mistakes
description: Analyze mistakes, identify root causes, and implement prevention
---

# Learn From Mistakes Skill

**Purpose**: When a mistake occurs, analyze the root cause and implement prevention to avoid recurrence.

## When to Use

- Build/test failures
- Incorrect git operations
- Lost work or data
- Process violations
- Any systematic error (not one-off typos)

## Root Cause Analysis Framework

### Step 0: Verify Event Sequence (MANDATORY)

**BEFORE any analysis**, invoke `get-history` skill to access the raw conversation log.

```bash
# Session ID from context (look for "Session ID: xxx" in system reminders)
SESSION_ID="<session-id>"
cat /home/node/.config/projects/-workspace/${SESSION_ID}.jsonl | jq -r 'select(.type == "message") | .content' | tail -100
```

**Why this is mandatory:**
- Memory and summaries can be inaccurate about cause-and-effect
- The actual sequence of events often differs from recalled sequence
- Root cause analysis based on incorrect sequence leads to wrong prevention

**What to verify:**
- Exact order of events (what triggered what)
- Who initiated changes (user feedback vs. code discovery)
- What was actually said/done vs. what is remembered

### Step 1: Document the Mistake

```markdown
**What happened**: [Describe the error]
**When**: [Date/time or session context]
**Impact**: [What was affected]
**How discovered**: [How was it caught]
```

### Step 2: Ask "Why" 5 Times

```markdown
1. Why did this happen?
   → [First-level cause]

2. Why did that happen?
   → [Second-level cause]

3. Why did that happen?
   → [Third-level cause]

4. Why did that happen?
   → [Fourth-level cause]

5. Why did that happen?
   → [Root cause]
```

### Step 3: Identify Prevention

Choose the most effective prevention (prefer earlier options):

| Level | Prevention Type | Example |
|-------|----------------|---------|
| 1 | Make impossible | Type system prevents null |
| 2 | Make automatic | Hook blocks dangerous command |
| 3 | Make obvious | Clear error message |
| 4 | Make documented | Add to README/guide |

## Prevention Hierarchy

**Best → Worst:**

1. **Code fix** - Change code so error can't happen
2. **Validation** - Add check that fails fast
3. **Hook** - Automated gate that blocks error
4. **Test** - Catch error before deployment
5. **Documentation** - Warn about pitfall
6. **Training** - Rely on human memory (weakest)

## Example Analysis

### Mistake: Squashed wrong commits

```markdown
**What happened**: Squashed 43 commits instead of 2
**Impact**: Had to restore from backup
**How discovered**: Commit count didn't match expected

**5 Whys**:
1. Why? → git reset --soft included too many commits
2. Why? → HEAD was at main, not at last commit to squash
3. Why? → Didn't checkout last commit before squash
4. Why? → Skill didn't have mandatory HEAD positioning step
5. Why? → Original skill assumed HEAD was already positioned

**Root cause**: Missing mandatory step in squash workflow

**Prevention**: Add Step 1 to git-squash skill:
"MANDATORY: Checkout last commit to squash BEFORE creating backup"
```

## Implementation Checklist

After identifying prevention:

- [ ] Implement the fix (code/hook/validation/doc)
- [ ] Test that the prevention works
- [ ] Verify it doesn't break other workflows
- [ ] Document what was changed and why

## Common Patterns

### Pattern: Missing Validation
**Symptom**: Invalid input causes downstream failure
**Fix**: Add `requireThat()` or equivalent at entry point

### Pattern: Assumed State
**Symptom**: Operation fails because precondition not met
**Fix**: Add explicit state verification before operation

### Pattern: Missing Backup
**Symptom**: Destructive operation can't be undone
**Fix**: Add mandatory backup step with timestamped branch

### Pattern: Silent Failure
**Symptom**: Error not caught, causes later confusion
**Fix**: Add verification step with clear error message

## Step 4: Log to Retrospectives (MANDATORY)

**AFTER implementing prevention**, log the mistake to `${CLAUDE_PROJECT_DIR}/.claude/retrospectives/mistakes.json`.

```json
{
  "id": "M0XX",
  "timestamp": "2026-01-08T00:00:00-05:00",
  "category": "<category from list>",
  "pattern_id": null,
  "description": "<brief description>",
  "root_cause": "<root cause from 5-whys>",
  "prevention_type": "<code_fix|hook|validation|config|skill|documentation>",
  "prevention_path": "${CLAUDE_PROJECT_DIR}/<path to fix>",
  "pattern_keywords": ["keyword1", "keyword2"],
  "commit": "<commit hash if applicable>",
  "recurrence_count": 0,
  "processed_in_retrospective": null
}
```

**CRITICAL: Path format**
- All paths MUST use `${CLAUDE_PROJECT_DIR}/` prefix, not relative paths
- Relative paths like `.claude/...` are unreliable because working directory changes
- Example: `${CLAUDE_PROJECT_DIR}/.claude/hooks/my-hook.sh` (correct)
- NOT: `.claude/hooks/my-hook.sh` (incorrect - will break)

**Categories**: tdd_violation, detection_gap, bash_error, edit_failure, architecture_issue,
protocol_violation, worktree_violation, merge_conflict, build_failure, test_failure,
logical_error, git_operation_failure, giving_up, documentation_violation, other

**Why logging is mandatory:**
- Enables pattern detection across sessions
- Allows effectiveness tracking of preventions
- Provides data for retrospective analysis
- Creates audit trail for recurring issues

## Success Criteria

- [ ] Event sequence verified via get-history (Step 0)
- [ ] Mistake documented with context
- [ ] Root cause identified (not just symptom)
- [ ] Prevention implemented at appropriate level
- [ ] Prevention tested and verified
- [ ] Mistake logged to retrospectives/mistakes.json (Step 4)
- [ ] Same mistake won't happen again
