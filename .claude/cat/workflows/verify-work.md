<verify_work_workflow>
This workflow guides manual testing of recently built features.

<process>

<step name="identify_scope">
**Locate SUMMARY.md files from specified releases or find most recently modified:**

```bash
# If release/change specified
cat .planning/releases/${RELEASE}-*/${RELEASE}-${CHANGE}-*-SUMMARY.md 2>/dev/null

# Otherwise find most recent
ls -t .planning/releases/*/*-SUMMARY.md 2>/dev/null | head -1
```

Parse the SUMMARY.md to understand what was built.
</step>

<step name="extract_deliverables">
From SUMMARY.md, extract:
- **Accomplishments:** What was actually built
- **Modified files:** What code changed
- **User-observable changes:** What the user should be able to see/do

Focus on observable user outcomes rather than implementation details.
</step>

<step name="generate_test_checklist">
Create structured manual test plans:

```markdown
## Test Checklist for Release {release}-{change}

### Feature: {feature_name}
- [ ] **Test 1:** {observable behavior to verify}
  - Steps: {how to test}
  - Expected: {what should happen}
- [ ] **Test 2:** {observable behavior to verify}
  - Steps: {how to test}
  - Expected: {what should happen}
```

Focus on:
- User-observable outcomes
- Happy path scenarios
- Edge cases mentioned in SUMMARY.md
- Error handling behaviors
</step>

<step name="guide_through_tests">
Use AskUserQuestion to walk through each test item:

```
Testing: {feature_name}

**Test:** {test description}
**Steps:** {how to test}
**Expected:** {expected outcome}

What was the result?
```

Options:
- Pass - Works as expected
- Fail - Does not work
- Partial - Partially works (describe issue)
- Skip - Cannot test right now

For each non-pass, prompt for details:
- What happened instead?
- Steps to reproduce?
- Any error messages?
</step>

<step name="collect_issues">
Capture failure details in structured format:

```yaml
- feature: {feature name}
  test: {test description}
  result: {fail|partial}
  problem: {description of what went wrong}
  severity: {critical|major|minor|cosmetic}
  steps_to_reproduce: |
    1. {step}
    2. {step}
  expected: {what should happen}
  actual: {what actually happened}
```

Severity definitions:
- **Critical:** Feature completely broken, blocks core functionality
- **Major:** Feature works but significant issue affects usability
- **Minor:** Feature works but minor issue or inconvenience
- **Cosmetic:** Visual or polish issue, doesn't affect functionality
</step>

<step name="log_findings">
Record issues to release-scoped ISSUES.md:

```bash
# Create or append to release-scoped issues file
ISSUES_FILE=".planning/releases/${RELEASE}-*/${RELEASE}-${CHANGE}-ISSUES.md"
```

Format:
```markdown
# UAT Issues: Release {release}-{change}

**Tested:** {date}
**Results:** {pass_count} passed, {fail_count} failed, {partial_count} partial, {skip_count} skipped

## Issues Found

### Issue 1: {brief description}
- **Feature:** {feature name}
- **Severity:** {severity}
- **Problem:** {description}
- **Steps to reproduce:**
  1. {step}
  2. {step}
- **Expected:** {expected}
- **Actual:** {actual}
```

Keep UAT results tied to specific work for targeted resolution.
</step>

<step name="summarize_results">
Present test summary:

```
## UAT Results: Release {release}-{change}

| Result | Count |
|--------|-------|
| Pass | {n} |
| Fail | {n} |
| Partial | {n} |
| Skip | {n} |

**Verdict:** {PASS / ISSUES FOUND}

{If issues}
### Issues by Severity
- Critical: {n}
- Major: {n}
- Minor: {n}
- Cosmetic: {n}
```
</step>

<step name="offer_next_steps">
Based on results, present options:

**If all passed:**
```
All tests passed! Ready to proceed.

Options:
- `/cat:progress` — continue to next release
- Review logged test results
```

**If issues found:**
```
Issues found during testing.

Options:
- `/cat:plan-fix {release} {change}` — create fix change for issues
- Continue to next release (defer issues)
- Review logged issues
```

**If critical issues:**
```
CRITICAL issues found - recommend fixing before proceeding.

- {list critical issues}

`/cat:plan-fix {release} {change}` to create fix change
```
</step>

</process>

<anti_patterns>
- Don't run automated tests (that's for CI/test suites)
- Don't make assumptions about test results — USER reports outcomes
- Don't skip the guidance — walk through each test
- Don't dismiss minor issues — log everything user reports
- Don't fix issues during testing — capture for later planning
- Don't mix UAT issues with global ISSUES.md — keep them release-scoped
</anti_patterns>

</verify_work_workflow>
