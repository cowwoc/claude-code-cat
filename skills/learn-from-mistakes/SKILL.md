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

### Step 1: Verify Event Sequence (MANDATORY)

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

### Step 2: Document the Mistake

```markdown
**What happened**: [Describe the error]
**When**: [Date/time or session context]
**Impact**: [What was affected]
**How discovered**: [How was it caught]
```

### Step 3: Ask "Why" 5 Times

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

### Step 4: Identify Prevention

Choose the most effective prevention (prefer earlier options):

| Level | Prevention Type | Example |
|-------|----------------|---------|
| 1 | Make impossible | Type system prevents null |
| 2 | Make automatic | Hook blocks dangerous command |
| 3 | Make obvious | Clear error message |
| 4 | Make documented | Add to README/guide |

## Prevention Hierarchy

#### Check Documentation First (MANDATORY)

**BEFORE choosing prevention type**, ask:

1. **Was relevant documentation read before the violation?**
   - If NO → The problem may be discoverability, not missing docs
   - If YES → Was the documentation clear and unambiguous?

2. **Is documentation itself the root cause?**
   - Misleading wording that suggests wrong approach
   - Missing critical warning or clarification
   - Ambiguous instructions that can be misinterpreted

**If documentation IS the root cause** → Fix the documentation FIRST
- This is often the BEST prevention, not just a warning
- Unclear docs cause recurring mistakes across sessions
- A hook that catches the symptom doesn't fix the cause

### Prevention Types (Best → Worst)

**For documentation-caused mistakes:**
1. **Fix misleading/unclear documentation** - Prevents future misunderstanding
2. **Add missing documentation reference** - Ensures docs are consulted
3. **Hook as backup** - Catches mistakes when docs are ignored

**For non-documentation mistakes:**
1. **Code fix** - Change code so error can't happen
2. **Validation** - Add check that fails fast
3. **Hook** - Automated gate that blocks error
4. **Test** - Catch error before deployment
5. **New documentation warning** - Last resort (relies on human reading)

### Example: Documentation as Root Cause

```markdown
**Mistake**: Used placeholder technique to capture actual test output
**5 Whys**:
1. Why? → Followed "placeholder technique" incorrectly
2. Why? → Documentation said "use placeholder technique"
3. Why? → Summary didn't clarify it's for position VERIFICATION only
4. Why? → Summary was misleading without full context
5. Why? → Root cause: Documentation wording was ambiguous

**Prevention**: Fix documentation (NOT add hook)
- Update summary to clarify: "manually derive structure FIRST"
- Hook would only catch symptom, not prevent misunderstanding
```

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

### Step 5: Implement Prevention

After identifying the prevention approach (Step 4), implement it:

- [ ] Create or modify code/hook/validation/documentation
- [ ] Follow existing patterns in the codebase
- [ ] Document what was changed and why (in commit message or inline comments)

### Step 6: Verify Prevention Works

**MANDATORY** - Do not skip this step:

- [ ] Test that the prevention actually blocks the mistake
- [ ] Verify it doesn't break other workflows
- [ ] If a hook, test that it triggers correctly
- [ ] If code fix, run relevant tests

## Implementation Checklist

**MANDATORY EXECUTION ORDER** (no exceptions):

1. Identify mistake and root cause (Steps 1-3)
2. Identify prevention approach (Step 4)
3. Implement the prevention (Step 5)
4. Verify prevention works (Step 6)
5. Log to mistakes.json (Step 7)
6. **ONLY THEN** return to original task

**DO NOT** offer to continue with original work until prevention is fully implemented and verified.

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

### Step 7: Log to Retrospectives (MANDATORY)

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

- [ ] Event sequence verified via get-history (Step 1)
- [ ] Mistake documented with context (Step 2)
- [ ] Root cause identified via 5-whys (Step 3)
- [ ] Prevention approach chosen (Step 4)
- [ ] Prevention implemented (Step 5)
- [ ] Prevention tested and verified (Step 6)
- [ ] Mistake logged to retrospectives/mistakes.json (Step 7)
- [ ] Same mistake won't happen again
