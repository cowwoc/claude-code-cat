---
name: retrospective
description: Analyze accumulated mistakes, identify recurring patterns, and generate action items for systemic fixes
allowed-tools: Read, Edit, Bash, Grep, Glob
---

# Retrospective Skill

Analyzes accumulated mistakes, identifies recurring patterns, checks action item effectiveness, and
generates new action items for systemic fixes.

## When This Skill Activates

- Automatically triggered by SessionStart hook (`check-retrospective-due.sh`) when:
  - `trigger_interval_days` (default 14) have passed since last retrospective, OR
  - `mistake_count_threshold` (default 10) mistakes accumulated since last retrospective
- Can be manually invoked anytime

## Data Files

| File | Purpose |
|------|---------|
| `.planning/retrospectives/mistakes.json` | All logged mistakes with pattern linkage |
| `.planning/retrospectives/retrospectives.json` | Retrospective history, patterns, action items |

## Workflow

### Release 1: Data Collection

1. Read all mistakes from `.planning/retrospectives/mistakes.json`
2. Read retrospective history from `.planning/retrospectives/retrospectives.json`
3. Filter to mistakes since last retrospective date
4. Identify mistakes not yet processed (`processed_in_retrospective` is null)

```bash
# Get last retrospective date
LAST_RETRO=$(jq -r '.last_retrospective // "1970-01-01"' .planning/retrospectives/retrospectives.json)

# Count unprocessed mistakes
jq --arg date "$LAST_RETRO" '[.mistakes[] | select(.timestamp > $date)] | length' \
  .planning/retrospectives/mistakes.json
```

### Release 2: Pattern Analysis

For each mistake since last retrospective:

1. **Categorize by type** (from schema):
   - `tdd_violation` - Skipped test releases
   - `detection_gap` - Validation missed issue
   - `bash_error` - Shell command failure
   - `edit_failure` - String not found
   - `architecture_issue` - Design-level problems
   - `protocol_violation` - Skipped required steps
   - `git_operation_failure` - Git command issues
   - `build_failure` - Compilation/style errors
   - `worktree_violation` - Wrong working directory
   - `giving_up` - Abandoned optimal solution
   - `documentation_violation` - Created prohibited docs
   - `logical_error` - Incorrect logic/thresholds
   - `other` - Uncategorized

2. **Extract patterns**:
   - Group by category
   - Identify common keywords in `pattern_keywords`
   - Find same root cause patterns

```bash
# Group mistakes by category
jq '.mistakes | group_by(.category) | map({category: .[0].category, count: length})' \
  .planning/retrospectives/mistakes.json
```

### Release 3: Effectiveness Analysis (CRITICAL)

For each COMPLETED action item, check if the fix was effective:

1. **Query mistakes with matching pattern_id WHERE timestamp > completed_date**

```bash
# Check effectiveness of action item A001
jq --arg pattern "PATTERN-003" --arg completed "2025-11-28" '
  [.mistakes[] | select(.pattern_id == $pattern and .timestamp > $completed)] | length
' .planning/retrospectives/mistakes.json
```

2. **Determine effectiveness status**:

| Post-Fix Mistakes | Status | Action |
|-------------------|--------|--------|
| 0 | `effective` | None - fix is working |
| 1 | `partially_effective` | Monitor closely |
| 2+ | `ineffective` | ESCALATE - needs deeper fix |

3. **Update action item effectiveness** in retrospectives.json

4. **Generate escalation** for ineffective fixes:
   - Create new action item with higher priority
   - Reference the ineffective action item
   - Propose deeper architectural change

### Release 3b: Escalation Protocol (for INEFFECTIVE fixes)

When an action item is marked `ineffective` (2+ post-fix mistakes), escalation is **MANDATORY**.

**Escalation Entry Format**:

```json
{
  "id": "ESCALATE-YYYY-MM-DD-NNN",
  "original_action_id": "A00X",
  "original_fix_description": "What was tried",
  "failure_analysis": {
    "expected_result": "What the fix should have done",
    "actual_result": "What actually happened",
    "root_cause": "Why the fix didn't work",
    "gap_identified": "What the fix missed"
  },
  "proposed_solution": {
    "approach": "defense-in-depth | architectural-change | tool-improvement",
    "description": "Detailed solution proposal",
    "prevention_type": "code_fix | hook | validation (NOT config)",
    "layers": ["Layer 1 description", "Layer 2 description"]
  },
  "priority": "critical",
  "status": "open"
}
```

**Escalation Decision Tree**:

```
Ineffective Fix Detected (2+ post-fix mistakes)
    ↓
1. WHY did the original fix fail?
   - Incomplete detection? → Strengthen detection logic
   - Wrong validation timing? → Add earlier checkpoint
   - Coverage gap? → Expand scope
   - Agent workaround? → Add multi-layer defense
    ↓
2. What PREVENTION TYPE is needed?
   - If original was `config` → MUST escalate to `hook` or `code_fix`
   - If original was `hook` → Consider `code_fix` or multi-layer hooks
   - If original was `code_fix` → Deeper architectural analysis needed
    ↓
3. Propose DEFENSE-IN-DEPTH (if single fix keeps failing):
   - Layer 1: PreToolUse validation (block before execution)
   - Layer 2: PostToolUse detection (catch after execution)
   - Layer 3: SessionStart reminder (proactive guidance)
   - Layer 4: Build/test integration (final safety net)
```

**Key Principle**:

> "Prevention requires active enforcement, not passive reporting."

When fixes prove ineffective, the solution is NEVER "add more documentation". The solution is:
1. Analyze why detection/prevention failed
2. Implement stronger enforcement mechanism
3. Add multiple layers of defense

### Release 4: Cross-Retrospective Analysis

Compare current patterns with ALL previous retrospectives:

1. Load `recurring_patterns` from retrospectives.json
2. For each current pattern:
   - Check if it matches any previous pattern (by category + keywords)
   - If match: increment `occurrences_total`
   - If occurrences >= `recurrence_threshold` (default 2): mark as RECURRING

3. For RECURRING patterns:
   - Check `occurrences_after_fix` (mistakes after last action date)
   - If `occurrences_after_fix` >= `recurrence_after_fix_threshold`: escalate

### Release 4.7: Action Item Coverage Validation (MANDATORY)

**⚠️ CRITICAL: Every recurring pattern with new occurrences MUST have an action item.**

Before generating the report, validate action item coverage:

1. **Identify patterns with new occurrences in this period**:
   ```
   patternsWithNewOccurrences = currentMistakes
     .filter(m => m.pattern_id != null)
     .groupBy(m => m.pattern_id)
     .map(group => { pattern_id, count: group.length })
   ```

2. **Check each pattern has an action item**:
   ```
   for each pattern in patternsWithNewOccurrences:
     if pattern.count >= 1:
       // Pattern recurred - MUST have action item
       existingAction = actionItems.find(a =>
         a.pattern_id == pattern.pattern_id AND
         a.status == "pending"
       )
       if NOT existingAction:
         // ⚠️ VIOLATION: Missing action item for recurring pattern
         createActionItem(pattern)
   ```

3. **Mandatory action item triggers**:

   | Condition | Action Required |
   |-----------|-----------------|
   | Pattern has 3+ new occurrences | MUST create action item |
   | Pattern is most frequent in period | MUST create action item |
   | Previous action item marked INEFFECTIVE | MUST create escalation |
   | Pattern has escalated status | MUST create action item |

4. **Self-check before Release 5**:
   ```
   □ Every pattern with 3+ new occurrences has a pending action item?
   □ The most frequent pattern has an action item?
   □ Every INEFFECTIVE action item has an escalation?
   □ Every ESCALATED pattern with new mistakes has an action item?
   ```

5. **If validation fails**:
   - DO NOT proceed to Release 5
   - Generate missing action items
   - Re-validate
   - Only proceed when all patterns are covered

**Background (Session 2c58ba44):**
RETRO-2025-12-01-001 missed creating action items for PATTERN-002 (Detection Gaps) despite:
- 6 new occurrences (55% of all mistakes)
- Previous escalation (ESCALATE-2025-11-29-001) marked INEFFECTIVE
- Being the most frequent pattern

This validation release prevents that oversight.

### Release 5: Action Item Generation

For each RECURRING pattern (occurs 2+ times):

1. **Analyze root cause type**:
   - Knowledge gap? → Add to CLAUDE.md
   - Missing check? → Create new hook
   - Design flaw? → Propose architecture change
   - Tooling issue? → Propose tool improvement

2. **Generate action item**:

```json
{
  "id": "A00X",
  "priority": "high|medium|low",
  "description": "What needs to be done",
  "category": "mistake category",
  "pattern_id": "PATTERN-XXX",
  "status": "open",
  "created_date": "ISO timestamp",
  "completed_date": null,
  "related_mistakes": ["M001", "M002"],
  "effectiveness_check": {
    "mistakes_before": N,
    "mistakes_after": null,
    "post_fix_mistakes": [],
    "verdict": "pending"
  }
}
```

### Release 6: Report & Update

1. **Display summary**:

```
================================================================================
📊 RETROSPECTIVE REPORT: YYYY-MM-DD
================================================================================

Period: [last_retrospective] to [now]
Mistakes analyzed: N
New patterns: N
Recurring patterns: N

## Action Item Effectiveness

| ID | Pattern | Status | Post-Fix Mistakes |
|----|---------|--------|-------------------|
| A001 | build_failure | effective | 0 |
| A002 | detection_gap | partially_effective | 1 |

## Mistake Breakdown by Category

| Category | Count | % |
|----------|-------|---|
| detection_gap | 7 | 33% |
| git_operation_failure | 4 | 19% |
...

## Recurring Patterns (REQUIRES ACTION)

### PATTERN-003: build_failure
- Occurrences: 3
- Last action: A001 (2025-11-28)
- Status: addressed

## New Action Items

- [ ] A00X: [description] (priority: high)

================================================================================
```

2. **Update data files**:

```bash
# Update retrospectives.json
jq '.last_retrospective = "TIMESTAMP" | .mistake_count_since_last = 0' \
  .planning/retrospectives/retrospectives.json > tmp && mv tmp .planning/retrospectives/retrospectives.json

# Mark mistakes as processed
jq '.mistakes |= map(if .processed_in_retrospective == null then .processed_in_retrospective = "R00X" else . end)' \
  .planning/retrospectives/mistakes.json > tmp && mv tmp .planning/retrospectives/mistakes.json
```

## Effectiveness Tracking Algorithm

```
function checkActionItemEffectiveness(actionItem, mistakes):
  if actionItem.status != "implemented":
    return  // Only check completed items

  completedDate = parseDate(actionItem.completed_date)
  patternId = actionItem.pattern_id

  // Find mistakes matching this pattern AFTER fix was implemented
  postFixMistakes = mistakes.filter(m =>
    m.pattern_id == patternId AND
    parseDate(m.timestamp) > completedDate
  )

  // Determine effectiveness
  if postFixMistakes.length == 0:
    verdict = "effective"
  else if postFixMistakes.length == 1:
    verdict = "partially_effective"
  else:
    verdict = "ineffective"
    // Generate escalation action item
    createEscalation(actionItem, postFixMistakes)

  // Update action item
  actionItem.effectiveness_check.post_fix_mistakes = postFixMistakes.map(m => m.id)
  actionItem.effectiveness_check.verdict = verdict
```

## Integration with learn-from-mistakes

When `learn-from-mistakes` logs a new mistake:

1. Check if mistake matches any existing pattern (by category + keywords)
2. If match found, set `pattern_id` on the mistake
3. Increment `mistake_count_since_last` in retrospectives.json
4. If pattern has a completed action item:
   - Add mistake ID to action item's `post_fix_mistakes`
   - If `post_fix_mistakes.length >= 2`, flag for escalation

This creates a feedback loop:
```
Mistake → Pattern Match → Action Item Check → Effectiveness Update → Escalation if needed
```

## Example Invocation

```
User: Run a retrospective

Claude: I'll run a retrospective analysis.

[Release 1: Reading mistake and retrospective files...]
[Release 2: Analyzing 5 new mistakes...]
[Release 3: Checking effectiveness of 4 action items...]
[Release 4: Cross-referencing with 3 known patterns...]
[Release 5: Generating action items for recurring patterns...]

================================================================================
📊 RETROSPECTIVE REPORT: 2025-11-28
================================================================================

Period: 2025-11-14 to 2025-11-28
Mistakes analyzed: 5
Recurring patterns requiring action: 1

## Action Item Effectiveness

| ID | Description | Status |
|----|-------------|--------|
| A001 | Pre-commit checkstyle/PMD | pending_verification (just implemented) |
| A002 | TDD enforcement | effective (0 violations since) |

## New Action Items

None - all patterns have active preventions.

================================================================================

The retrospective is complete. All patterns have active prevention mechanisms.
```

## Related Skills

- **learn-from-mistakes**: Logs individual mistakes with root cause analysis
- **get-history**: Access conversation logs for mistake investigation
- **get-session-id**: Provides session ID for log correlation

## Notes

- **NEVER delete mistakes** - Full history required for trend analysis
- Only reset `mistake_count_since_last` after retrospective (trigger counter)
- Pattern recurrence is checked using timestamps, not counts
- Retrospective reports are NOT saved as files (per CLAUDE.md policy) - data is in JSON
