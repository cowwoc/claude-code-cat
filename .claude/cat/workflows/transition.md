<required_reading>

**Read these files NOW:**

1. `.planning/STATE.md`
2. `.planning/PROJECT.md`
3. `.planning/ROADMAP.md`
4. Current release's change files (`*-CHANGE.md`)
5. Current release's summary files (`*-SUMMARY.md`)

</required_reading>

<purpose>

Mark current release complete and advance to next. This is the natural point where progress tracking and PROJECT.md evolution happen.

"Planning next release" = "current release is done"

</purpose>

<process>

<step name="load_project_state" priority="first">

Before transition, read project state:

```bash
cat .planning/STATE.md 2>/dev/null
cat .planning/PROJECT.md 2>/dev/null
```

Parse current position to verify we're transitioning the right release.
Note accumulated context that may need updating after transition.

</step>

<step name="verify_completion">

Check current release has all change summaries:

```bash
ls .planning/releases/XX-current/*-CHANGE.md 2>/dev/null | sort
ls .planning/releases/XX-current/*-SUMMARY.md 2>/dev/null | sort
```

**Verification logic:**

- Count CHANGE files
- Count SUMMARY files
- If counts match: all changes complete
- If counts don't match: incomplete

<config-check>

```bash
cat .planning/config.json 2>/dev/null
```

</config-check>

**If all changes complete:**

<if mode="yolo">

```
⚡ Auto-approved: Transition Release [X] → Release [X+1]
Release [X] complete — all [Y] changes finished.

Proceeding to mark done and advance...
```

Proceed directly to cleanup_handoff step.

</if>

<if mode="interactive" OR="custom with gates.confirm_transition true">

Ask: "Release [X] complete — all [Y] changes finished. Ready to mark done and move to Release [X+1]?"

Wait for confirmation before proceeding.

</if>

**If changes incomplete:**

**SAFETY RAIL: always_confirm_destructive applies here.**
Skipping incomplete changes is destructive — ALWAYS prompt regardless of mode.

Present:

```
Release [X] has incomplete changes:
- {release}-01-SUMMARY.md ✓ Complete
- {release}-02-SUMMARY.md ✗ Missing
- {release}-03-SUMMARY.md ✗ Missing

⚠️ Safety rail: Skipping changes requires confirmation (destructive action)

Options:
1. Continue current release (execute remaining changes)
2. Mark complete anyway (skip remaining changes)
3. Review what's left
```

Wait for user decision.

</step>

<step name="cleanup_handoff">

Check for lingering handoffs:

```bash
ls .planning/releases/XX-current/.continue-here*.md 2>/dev/null
```

If found, delete them — release is complete, handoffs are stale.

</step>

<step name="update_roadmap">

Update the roadmap file:

```bash
ROADMAP_FILE=".planning/ROADMAP.md"
```

Update the file:

- Mark current release: `[x] Complete`
- Add completion date
- Update change count to final (e.g., "3/3 changes complete")
- Update Progress table
- Keep next release as `[ ] Not started`

**Example:**

```markdown
## Releases

- [x] Release 1: Foundation (completed 2025-01-15)
- [ ] Release 2: Authentication ← Next
- [ ] Release 3: Core Features

## Progress

| Release             | Changes Complete | Status      | Completed  |
| ----------------- | -------------- | ----------- | ---------- |
| 1. Foundation     | 3/3            | Complete    | 2025-01-15 |
| 2. Authentication | 0/2            | Not started | -          |
| 3. Core Features  | 0/1            | Not started | -          |
```

</step>

<step name="archive_prompts">

If prompts were generated for the release, they stay in place.
The `completed/` subfolder pattern from create-meta-prompts handles archival.

</step>

<step name="evolve_project">

Evolve PROJECT.md to reflect learnings from completed release.

**Read release summaries:**

```bash
cat .planning/releases/XX-current/*-SUMMARY.md
```

**Assess requirement changes:**

1. **Requirements validated?**
   - Any Active requirements shipped in this release?
   - Move to Validated with release reference: `- ✓ [Requirement] — Release X`

2. **Requirements invalidated?**
   - Any Active requirements discovered to be unnecessary or wrong?
   - Move to Out of Scope with reason: `- [Requirement] — [why invalidated]`

3. **Requirements emerged?**
   - Any new requirements discovered during building?
   - Add to Active: `- [ ] [New requirement]`

4. **Decisions to log?**
   - Extract decisions from SUMMARY.md files
   - Add to Key Decisions table with outcome if known

5. **"What This Is" still accurate?**
   - If the product has meaningfully changed, update the description
   - Keep it current and accurate

**Update PROJECT.md:**

Make the edits inline. Update "Last updated" footer:

```markdown
---
*Last updated: [date] after Release [X]*
```

**Example evolution:**

Before:

```markdown
### Active

- [ ] JWT authentication
- [ ] Real-time sync < 500ms
- [ ] Offline mode

### Out of Scope

- OAuth2 — complexity not needed for v1
```

After (Release 2 shipped JWT auth, discovered rate limiting needed):

```markdown
### Validated

- ✓ JWT authentication — Release 2

### Active

- [ ] Real-time sync < 500ms
- [ ] Offline mode
- [ ] Rate limiting on sync endpoint

### Out of Scope

- OAuth2 — complexity not needed for v1
```

**Step complete when:**

- [ ] Release summaries reviewed for learnings
- [ ] Validated requirements moved from Active
- [ ] Invalidated requirements moved to Out of Scope with reason
- [ ] Emerged requirements added to Active
- [ ] New decisions logged with rationale
- [ ] "What This Is" updated if product changed
- [ ] "Last updated" footer reflects this transition

</step>

<step name="update_current_position_after_transition">

Update Current Position section in STATE.md to reflect release completion and transition.

**Format:**

```markdown
Release: [next] of [total] ([Next release name])
Change: Not started
Status: Ready to change
Last activity: [today] — Release [X] complete, transitioned to Release [X+1]

Progress: [updated progress bar]
```

**Instructions:**

- Increment release number to next release
- Reset change to "Not started"
- Set status to "Ready to change"
- Update last activity to describe transition
- Recalculate progress bar based on completed changes

**Example — transitioning from Release 2 to Release 3:**

Before:

```markdown
## Current Position

Release: 2 of 4 (Authentication)
Change: 2 of 2 in current release
Status: Release complete
Last activity: 2025-01-20 — Completed 02-02-add-session-CHANGE.md

Progress: ███████░░░ 60%
```

After:

```markdown
## Current Position

Release: 3 of 4 (Core Features)
Change: Not started
Status: Ready to change
Last activity: 2025-01-20 — Release 2 complete, transitioned to Release 3

Progress: ███████░░░ 60%
```

**Step complete when:**

- [ ] Release number incremented to next release
- [ ] Change status reset to "Not started"
- [ ] Status shows "Ready to change"
- [ ] Last activity describes the transition
- [ ] Progress bar reflects total completed changes

</step>

<step name="update_project_reference">

Update Project Reference section in STATE.md.

```markdown
## Project Reference

See: .planning/PROJECT.md (updated [today])

**Core value:** [Current core value from PROJECT.md]
**Current focus:** [Next release name]
```

Update the date and current focus to reflect the transition.

</step>

<step name="review_accumulated_context">

Review and update Accumulated Context section in STATE.md.

**Decisions:**

- Note recent decisions from this release (3-5 max)
- Full log lives in PROJECT.md Key Decisions table

**Blockers/Concerns:**

- Review blockers from completed release
- If addressed in this release: Remove from list
- If still relevant for future: Keep with "Release X" prefix
- Add any new concerns from completed release's summaries

**Deferred Issues:**

- Count open issues in ISSUES.md
- Update count: "[N] open issues — see ISSUES.md"
- If many accumulated, note: "Consider addressing ISS-XXX, ISS-YYY in next release"

**Example:**

Before:

```markdown
### Blockers/Concerns

- ⚠️ [Release 1] Database schema not indexed for common queries
- ⚠️ [Release 2] WebSocket reconnection behavior on flaky networks unknown

### Deferred Issues

- ISS-001: Rate limiting on sync endpoint (Release 2) — Medium
```

After (if database indexing was addressed in Release 2):

```markdown
### Blockers/Concerns

- ⚠️ [Release 2] WebSocket reconnection behavior on flaky networks unknown

### Deferred Issues

- ISS-001: Rate limiting on sync endpoint (Release 2) — Medium
- ISS-002: Better sync error messages (Release 2) — Quick
```

**Step complete when:**

- [ ] Recent decisions noted (full log in PROJECT.md)
- [ ] Resolved blockers removed from list
- [ ] Unresolved blockers kept with release prefix
- [ ] New concerns from completed release added
- [ ] Deferred issues count updated

</step>

<step name="update_session_continuity_after_transition">

Update Session Continuity section in STATE.md to reflect transition completion.

**Format:**

```markdown
Last session: [today]
Stopped at: Release [X] complete, ready to change Release [X+1]
Resume file: None
```

**Step complete when:**

- [ ] Last session timestamp updated to current date and time
- [ ] Stopped at describes release completion and next release
- [ ] Resume file confirmed as None (transitions don't use resume files)

</step>

<step name="offer_next_phase">

**MANDATORY: Verify milestone status before presenting next steps.**

**Step 1: Read ROADMAP.md and identify releases in current milestone**

Read the ROADMAP.md file and extract:
1. Current release number (the release just transitioned from)
2. All release numbers in the current milestone section

To find releases, look for:
- Release headers: lines starting with `### Release` or `#### Release`
- Release list items: lines like `- [ ] **Release X:` or `- [x] **Release X:`

Count total releases and identify the highest release number in the milestone.

State: "Current release is {X}. Milestone has {N} releases (highest: {Y})."

**Step 2: Route based on milestone status**

| Condition | Meaning | Action |
|-----------|---------|--------|
| current release < highest release | More releases remain | Go to **Route A** |
| current release = highest release | Milestone complete | Go to **Route B** |

---

**Route A: More releases remain in milestone**

Read ROADMAP.md to get the next release's name and goal.

**If next release exists:**

<if mode="yolo">

```
Release [X] marked complete.

Next: Release [X+1] — [Name]

⚡ Auto-continuing: Change Release [X+1] in detail
```

Exit skill and invoke SlashCommand("/cat:change-release [X+1]")

</if>

<if mode="interactive" OR="custom with gates.confirm_transition true">

```
## ✓ Release [X] Complete

---

## ▶ Next Up

**Release [X+1]: [Name]** — [Goal from ROADMAP.md]

`/cat:change-release [X+1]`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- `/cat:discuss-release [X+1]` — gather context first
- `/cat:research-release [X+1]` — investigate unknowns
- Review roadmap

---
```

</if>

---

**Route B: Milestone complete (all releases done)**

<if mode="yolo">

```
Release {X} marked complete.

🎉 Milestone {version} is 100% complete — all {N} releases finished!

⚡ Auto-continuing: Complete milestone and archive
```

Exit skill and invoke SlashCommand("/cat:complete-milestone {version}")

</if>

<if mode="interactive" OR="custom with gates.confirm_transition true">

```
## ✓ Release {X}: {Release Name} Complete

🎉 Milestone {version} is 100% complete — all {N} releases finished!

---

## ▶ Next Up

**Complete Milestone {version}** — archive and prepare for next

`/cat:complete-milestone {version}`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- Review accomplishments before archiving

---
```

</if>

</step>

</process>

<implicit_tracking>

Progress tracking is IMPLICIT:

- "Change release 2" → Release 1 must be done (or ask)
- "Change release 3" → Releases 1-2 must be done (or ask)
- Transition workflow makes it explicit in ROADMAP.md

No separate "update progress" step. Forward motion IS progress.

</implicit_tracking>

<partial_completion>

If user wants to move on but release isn't fully complete:

```
Release [X] has incomplete changes:
- {release}-02-{slug}-CHANGE.md (not executed)
- {release}-03-{slug}-CHANGE.md (not executed)

Options:
1. Mark complete anyway (changes weren't needed)
2. Defer work to later release
3. Stay and finish current release
```

Respect user judgment — they know if work matters.

**If marking complete with incomplete changes:**

- Update ROADMAP: "2/3 changes complete" (not "3/3")
- Note in transition message which changes were skipped

</partial_completion>

<success_criteria>

Transition is complete when:

- [ ] Current release change summaries verified (all exist or user chose to skip)
- [ ] Any stale handoffs deleted
- [ ] ROADMAP.md updated with completion status and change count
- [ ] PROJECT.md evolved (requirements, decisions, description if needed)
- [ ] STATE.md updated (position, project reference, context, session)
- [ ] Progress table updated
- [ ] User knows next steps

</success_criteria>
