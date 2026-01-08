---
name: cat:plan-fix
description: Create fix change from UAT issues
argument-hint: <release-change> (e.g., "04-02")
allowed-tools:
  - Read
  - Write
  - Bash
  - Glob
  - Grep
  - AskUserQuestion
---

<objective>
Create a fix change from issues discovered during verification testing.

Purpose: Structure fixes for UAT issues into an executable CHANGE.md
Output: {release}-{change}-FIX-CHANGE.md file ready for execution
</objective>

<execution_context>
@.planning/STATE.md
@.planning/ROADMAP.md
</execution_context>

<process>

<step name="parse_arguments">
Parse the change reference:
- Format: `{release}-{change}` (e.g., "04-02")
- Example: `/cat:plan-fix 04-02` → release = 04, change = 02

If no argument provided:

```
ERROR: Change reference required
Usage: /cat:plan-fix <release-change>
Example: /cat:plan-fix 04-02
```

Exit.
</step>

<step name="find_issues">
Locate the ISSUES.md file:

```bash
# Find issues file for this change
ls .planning/releases/${RELEASE}-*/${RELEASE}-${CHANGE}-ISSUES.md 2>/dev/null
```

If not found:

```
ERROR: No issues file found for change ${RELEASE}-${CHANGE}

Run /cat:verify-work ${RELEASE}-${CHANGE} first to identify issues.
```

Exit.
</step>

<step name="read_issues">
Parse issues from ISSUES.md:

Extract for each issue:
- ID (Issue 1, Issue 2, etc.)
- Feature affected
- Severity (critical/major/minor/cosmetic)
- Problem description
- Steps to reproduce
- Expected vs actual behavior
- Acceptance criteria for fix
</step>

<step name="plan_fixes">
Generate fix tasks grouped by severity:

**Critical issues first:**
- Must be fixed before proceeding
- Create dedicated task for each

**Major issues second:**
- Should be fixed but not blocking
- May combine related issues

**Minor/cosmetic last:**
- Fix if time permits
- Can be deferred

For each task:
- Identify affected files from original SUMMARY.md
- Define verification steps
- Link back to issue ID
</step>

<step name="write_fix_change">
Create FIX-CHANGE.md:

```markdown
---
release: {release}
change: {change}-fix
type: fix
domain: {from original change}
---

<objective>
Fix issues discovered during UAT of change {release}-{change}.

**Issues addressed:**
- [Critical: {n}] {list}
- [Major: {n}] {list}
- [Minor: {n}] {list}
</objective>

<execution_context>
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/workflows/execute-release.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/templates/summary.md
</execution_context>

<context>
@.planning/releases/XX-name/{release}-{change}-SUMMARY.md
@.planning/releases/XX-name/{release}-{change}-ISSUES.md
</context>

<tasks>
{generated fix tasks with verification}
</tasks>

<verification>
- [ ] All critical issues resolved
- [ ] All major issues resolved
- [ ] Re-run failed UAT tests
</verification>

<success_criteria>
- All addressed issues verified fixed
- No regressions in original functionality
- User acceptance confirmed
</success_criteria>
```

Write to `.planning/releases/XX-name/{release}-{change}-FIX-CHANGE.md`
</step>

<step name="offer_next">
Present options:

```
Fix change created: .planning/releases/XX-name/{release}-{change}-FIX-CHANGE.md

Issues to fix:
- Critical: {n}
- Major: {n}
- Minor: {n}

---

## Next Up

`/cat:execute-change .planning/releases/XX-name/{release}-{change}-FIX-CHANGE.md`

<sub>`/clear` first - fresh context window</sub>

---

**Also available:**
- Review/adjust fix tasks before executing
- `/cat:verify-work {release}-{change}` — re-run UAT after fixes
```
</step>

</process>

<success_criteria>
- [ ] Issues file located and parsed
- [ ] Issues grouped by severity
- [ ] Fix tasks created with verification steps
- [ ] FIX-CHANGE.md written
- [ ] User knows next steps
</success_criteria>
