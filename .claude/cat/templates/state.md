# State Template

Template for `.planning/STATE.md` — the project's living memory.

---

## File Template

```markdown
# Project State

## Project Reference

See: .planning/PROJECT.md (updated [date])

**Core value:** [One-liner from PROJECT.md Core Value section]
**Current focus:** [Current release name]

## Current Position

Release: [X] of [Y] ([Release name])
Change: [A] of [B] in current release
Status: [Ready to change / Planning / Ready to execute / In progress / Release complete]
Last activity: [YYYY-MM-DD] — [What happened]

Progress: [░░░░░░░░░░] 0%

## Performance Metrics

**Velocity:**
- Total changes completed: [N]
- Average duration: [X] min
- Total execution time: [X.X] hours

**By Release:**

| Release | Changes | Total | Avg/Change |
|-------|-------|-------|----------|
| - | - | - | - |

**Recent Trend:**
- Last 5 changes: [durations]
- Trend: [Improving / Stable / Degrading]

*Updated after each change completion*

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- [Release X]: [Decision summary]
- [Release Y]: [Decision summary]

### Deferred Issues

[From ISSUES.md — list open items with release of origin]

None yet.

### Blockers/Concerns

[Issues that affect future work]

None yet.

## Session Continuity

Last session: [YYYY-MM-DD HH:MM]
Stopped at: [Description of last completed action]
Resume file: [Path to .continue-here*.md if exists, otherwise "None"]
```

<purpose>

STATE.md is the project's short-term memory spanning all releases and sessions.

**Problem it solves:** Information is captured in summaries, issues, and decisions but not systematically consumed. Sessions start without context.

**Solution:** A single, small file that's:
- Read first in every workflow
- Updated after every significant action
- Contains digest of accumulated context
- Enables instant session restoration

</purpose>

<lifecycle>

**Creation:** After ROADMAP.md is created (during init)
- Reference PROJECT.md (read it for current context)
- Initialize empty accumulated context sections
- Set position to "Release 1 ready to change"

**Reading:** First step of every workflow
- progress: Present status to user
- change: Inform planning decisions
- execute: Know current position
- transition: Know what's complete

**Writing:** After every significant action
- execute: After SUMMARY.md created
  - Update position (release, change, status)
  - Note new decisions (detail in PROJECT.md)
  - Update deferred issues list
  - Add blockers/concerns
- transition: After release marked complete
  - Update progress bar
  - Clear resolved blockers
  - Refresh Project Reference date

</lifecycle>

<sections>

### Project Reference
Points to PROJECT.md for full context. Includes:
- Core value (the ONE thing that matters)
- Current focus (which release)
- Last update date (triggers re-read if stale)

Claude reads PROJECT.md directly for requirements, constraints, and decisions.

### Current Position
Where we are right now:
- Release X of Y — which release
- Change A of B — which change within release
- Status — current state
- Last activity — what happened most recently
- Progress bar — visual indicator of overall completion

Progress calculation: (completed changes) / (total changes across all releases) × 100%

### Performance Metrics
Track velocity to understand execution patterns:
- Total changes completed
- Average duration per change
- Per-release breakdown
- Recent trend (improving/stable/degrading)

Updated after each change completion.

### Accumulated Context

**Decisions:** Reference to PROJECT.md Key Decisions table, plus recent decisions summary for quick access. Full decision log lives in PROJECT.md.

**Deferred Issues:** Open items from ISSUES.md
- Brief description with ISS-XXX number
- Release where discovered
- Effort estimate if known
- Helps release planning identify what to address

**Blockers/Concerns:** From "Next Release Readiness" sections
- Issues that affect future work
- Prefix with originating release
- Cleared when addressed

### Session Continuity
Enables instant resumption:
- When was last session
- What was last completed
- Is there a .continue-here file to resume from

</sections>

<size_constraint>

Keep STATE.md under 100 lines.

It's a DIGEST, not an archive. If accumulated context grows too large:
- Keep only 3-5 recent decisions in summary (full log in PROJECT.md)
- Reference ISSUES.md instead of listing all: "12 open issues — see ISSUES.md"
- Keep only active blockers, remove resolved ones

The goal is "read once, know where we are" — if it's too long, that fails.

</size_constraint>

<guidelines>

**When created:**
- During project initialization (after ROADMAP.md)
- Reference PROJECT.md (extract core value and current focus)
- Initialize empty sections

**When read:**
- Every workflow starts by reading STATE.md
- Then read PROJECT.md for full context
- Provides instant context restoration

**When updated:**
- After each change execution (update position, note decisions, update issues/blockers)
- After release transitions (update progress bar, clear resolved blockers, refresh project reference)

**Size management:**
- Keep under 100 lines total
- Recent decisions only in STATE.md (full log in PROJECT.md)
- Reference ISSUES.md instead of listing all issues
- Keep only active blockers

**Sections:**
- Project Reference: Pointer to PROJECT.md with core value
- Current Position: Where we are now (release, change, status)
- Performance Metrics: Velocity tracking
- Accumulated Context: Recent decisions, deferred issues, blockers
- Session Continuity: Resume information

</guidelines>
