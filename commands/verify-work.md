---
name: cat:verify-work
description: Guide manual user acceptance testing of recently built features
argument-hint: "[optional: release or change number, e.g., '4' or '04-02']"
allowed-tools:
  - Read
  - Bash
  - Glob
  - Grep
  - Edit
  - Write
  - AskUserQuestion
---

<objective>
Guide the user through manual acceptance testing of recently built features.

Purpose: Validate that what Claude thinks was built actually works from the user's perspective. The USER performs all testing — Claude generates the test checklist, guides the process, and captures issues.

Output: Validation of features, any issues logged to release-scoped ISSUES.md
</objective>

<execution_context>
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/workflows/verify-work.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/templates/uat-issues.md
</execution_context>

<context>
Scope: $ARGUMENTS (optional)
- If provided: Test specific release or change (e.g., "4" or "04-02")
- If not provided: Test most recently completed change

**Load project state:**
@.planning/STATE.md

**Load roadmap:**
@.planning/ROADMAP.md
</context>

<process>
1. Validate arguments (if provided, parse as release or change number)
2. Find relevant SUMMARY.md (specified or most recent)
3. Follow verify-work.md workflow:
   - Extract testable deliverables
   - Generate test checklist
   - Guide through each test via AskUserQuestion
   - Collect and categorize issues
   - Log issues to `.planning/releases/XX-name/{release}-{change}-ISSUES.md`
   - Present summary with verdict
4. Offer next steps based on results:
   - If all passed: Continue to next release
   - If issues found: `/cat:plan-fix {release} {change}` to create fix change
</process>

<anti_patterns>
- Don't run automated tests (that's for CI/test suites)
- Don't make assumptions about test results — USER reports outcomes
- Don't skip the guidance — walk through each test
- Don't dismiss minor issues — log everything user reports
- Don't fix issues during testing — capture for later
</anti_patterns>

<success_criteria>
- [ ] Test scope identified from SUMMARY.md
- [ ] Checklist generated based on deliverables
- [ ] User guided through each test
- [ ] All test results captured (pass/fail/partial/skip)
- [ ] Any issues logged to release-scoped ISSUES.md (not global)
- [ ] Summary presented with verdict
- [ ] User knows next steps based on results
</success_criteria>
