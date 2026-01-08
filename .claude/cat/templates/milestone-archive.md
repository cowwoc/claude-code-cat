# Milestone Archive Template

This template is used by the complete-milestone workflow to create archive files in `.planning/milestones/`.

---

## File Template

# Milestone v{{VERSION}}: {{MILESTONE_NAME}}

**Status:** ✅ SHIPPED {{DATE}}
**Releases:** {{RELEASE_START}}-{{RELEASE_END}}
**Total Changes:** {{TOTAL_CHANGES}}

## Overview

{{MILESTONE_DESCRIPTION}}

## Releases

{{PHASES_SECTION}}

[For each release in this milestone, include:]

### Release {{RELEASE_NUM}}: {{RELEASE_NAME}}

**Goal**: {{RELEASE_GOAL}}
**Depends on**: {{DEPENDS_ON}}
**Changes**: {{CHANGE_COUNT}} changes

Changes:

- [x] {{RELEASE}}-01: {{CHANGE_DESCRIPTION}}
- [x] {{RELEASE}}-02: {{CHANGE_DESCRIPTION}}
      [... all changes ...]

**Details:**
{{RELEASE_DETAILS_FROM_ROADMAP}}

**For decimal releases, include (INSERTED) marker:**

### Release 2.1: Critical Security Patch (INSERTED)

**Goal**: Fix authentication bypass vulnerability
**Depends on**: Release 2
**Changes**: 1 change

Changes:

- [x] 2.1-01: Patch auth vulnerability

**Details:**
{{RELEASE_DETAILS_FROM_ROADMAP}}

---

## Milestone Summary

**Decimal Releases:**

- Release 2.1: Critical Security Patch (inserted after Release 2 for urgent fix)
- Release 5.1: Performance Hotfix (inserted after Release 5 for production issue)

**Key Decisions:**
{{DECISIONS_FROM_PROJECT_STATE}}
[Example:]

- Decision: Use ROADMAP.md split (Rationale: Constant context cost)
- Decision: Decimal release numbering (Rationale: Clear insertion semantics)

**Issues Resolved:**
{{ISSUES_RESOLVED_DURING_MILESTONE}}
[Example:]

- Fixed context overflow at 100+ releases
- Resolved release insertion confusion

**Issues Deferred:**
{{ISSUES_DEFERRED_TO_LATER}}
[Example:]

- PROJECT-STATE.md tiering (deferred until decisions > 300)

**Technical Debt Incurred:**
{{SHORTCUTS_NEEDING_FUTURE_WORK}}
[Example:]

- Some workflows still have hardcoded paths (fix in Release 5)

---

_For current project status, see .planning/ROADMAP.md_

---

## Usage Guidelines

<guidelines>
**When to create milestone archives:**
- After completing all releases in a milestone (v1.0, v1.1, v2.0, etc.)
- Triggered by complete-milestone workflow
- Before planning next milestone work

**How to fill template:**

- Replace {{PLACEHOLDERS}} with actual values
- Extract release details from ROADMAP.md
- Document decimal releases with (INSERTED) marker
- Include key decisions from PROJECT-STATE.md or SUMMARY files
- List issues resolved vs deferred
- Capture technical debt for future reference

**Archive location:**

- Save to `.planning/milestones/v{VERSION}-{NAME}.md`
- Example: `.planning/milestones/v1.0-mvp.md`

**After archiving:**

- Update ROADMAP.md to collapse completed milestone in `<details>` tag
- Update PROJECT.md to brownfield format with Current State section
- Continue release numbering in next milestone (never restart at 01)
  </guidelines>
