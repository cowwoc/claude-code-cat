# UAT Issues: Release {release}-{change}

**Tested:** {date}
**Feature:** {feature name from SUMMARY.md}
**Results:** {pass_count} passed, {fail_count} failed, {partial_count} partial, {skip_count} skipped

## Summary

| Severity | Count |
|----------|-------|
| Critical | 0 |
| Major | 0 |
| Minor | 0 |
| Cosmetic | 0 |

## Issues Found

<!-- Template for each issue -->
### Issue {n}: {brief description}

- **Feature:** {feature name}
- **Severity:** {critical|major|minor|cosmetic}
- **Test:** {which test failed}
- **Problem:** {description of what went wrong}

**Steps to reproduce:**
1. {step}
2. {step}
3. {step}

**Expected:** {what should happen}

**Actual:** {what actually happened}

**Notes:** {any additional context, error messages, screenshots}

---

## Test Results

| Test | Result | Notes |
|------|--------|-------|
| {test name} | Pass/Fail/Partial/Skip | {brief note} |

## Verdict

{PASS - All tests passed | ISSUES FOUND - See issues above}

{If ISSUES FOUND}
**Recommendation:** Run `/cat:plan-fix {release}-{change}` to create fix change.
