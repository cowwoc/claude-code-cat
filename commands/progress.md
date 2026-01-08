---
name: cat:progress
description: Check project progress, show context, and route to next action (execute or change)
allowed-tools:
  - Read
  - Bash
  - Grep
  - Glob
  - SlashCommand
---

<objective>
Check project progress, summarize recent work and what's ahead, then intelligently route to the next action - either executing an existing change or creating the next one.

Provides situational awareness before continuing work.
</objective>


<process>

<step name="verify">
**Verify planning structure exists:**

If no `.planning/` directory:

```
No planning structure found.

Run /cat:new-project to start a new project.
```

Exit.

If missing STATE.md or ROADMAP.md: inform what's missing, suggest running `/cat:new-project`.
</step>

<step name="load">
**Load full project context:**

- Read `.planning/STATE.md` for living memory (position, decisions, issues)
- Read `.planning/ROADMAP.md` for release structure and objectives
- Read `.planning/PROJECT.md` for current state (What This Is, Core Value, Requirements)
  </step>

<step name="recent">
**Gather recent work context:**

- Find the 2-3 most recent SUMMARY.md files
- Extract from each: what was accomplished, key decisions, any issues logged
- This shows "what we've been working on"
  </step>

<step name="position">
**Parse current position:**

- From STATE.md: current release, change number, status
- Calculate: total changes, completed changes, remaining changes
- Note any blockers, concerns, or deferred issues
- Check for CONTEXT.md: For releases without CHANGE.md files, check if `{release}-CONTEXT.md` exists in release directory
  </step>

<step name="report">
**Present rich status report:**

```
# [Project Name]

**Progress:** [████████░░] 8/10 changes complete

## Recent Work
- [Release X, Change Y]: [what was accomplished - 1 line]
- [Release X, Change Z]: [what was accomplished - 1 line]

## Current Position
Release [N] of [total]: [release-name]
Change [M] of [release-total]: [status]
CONTEXT: [✓ if CONTEXT.md exists | - if not]

## Key Decisions Made
- [decision 1 from STATE.md]
- [decision 2]

## Open Issues
- [any deferred issues or blockers]

## What's Next
[Next release/change objective from ROADMAP]
```

</step>

<step name="route">
**Determine next action based on verified counts.**

**Step 1: Count changes and summaries in current release**

List files in the current release directory:

```bash
ls -1 .planning/releases/[current-release-dir]/*-CHANGE.md 2>/dev/null | wc -l
ls -1 .planning/releases/[current-release-dir]/*-SUMMARY.md 2>/dev/null | wc -l
```

State: "This release has {X} changes and {Y} summaries."

**Step 2: Route based on counts**

| Condition | Meaning | Action |
|-----------|---------|--------|
| summaries < changes | Unexecuted changes exist | Go to **Route A** |
| summaries = changes AND changes > 0 | Release complete | Go to Step 3 |
| changes = 0 | Release not yet planned | Go to **Route B** |

---

**Route A: Unexecuted change exists**

Find the first CHANGE.md without matching SUMMARY.md.
Read its `<objective>` section.

```
---

## ▶ Next Up

**{release}-{change}-{slug}: [Change Name]** — [objective summary from CHANGE.md]

`/cat:execute-change [full-path-to-CHANGE.md]`

<sub>`/clear` first → fresh context window</sub>

---
```

---

**Route B: Release needs planning**

Check if `{release}-CONTEXT.md` exists in release directory.

**If CONTEXT.md exists:**

```
---

## ▶ Next Up

**Release {N}: {Name}** — {Goal from ROADMAP.md}
<sub>✓ Context gathered, ready to change</sub>

`/cat:change-release {release-number}`

<sub>`/clear` first → fresh context window</sub>

---
```

**If CONTEXT.md does NOT exist:**

```
---

## ▶ Next Up

**Release {N}: {Name}** — {Goal from ROADMAP.md}

`/cat:change-release {release}`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- `/cat:discuss-release {release}` — gather context first
- `/cat:research-release {release}` — investigate unknowns
- `/cat:list-release-assumptions {release}` — see Claude's assumptions

---
```

---

**Step 3: Check milestone status (only when release complete)**

Read ROADMAP.md and identify:
1. Current release number
2. All release numbers in the current milestone section

Count total releases and identify the highest release number.

State: "Current release is {X}. Milestone has {N} releases (highest: {Y})."

**Route based on milestone status:**

| Condition | Meaning | Action |
|-----------|---------|--------|
| current release < highest release | More releases remain | Go to **Route C** |
| current release = highest release | Milestone complete | Go to **Route D** |

---

**Route C: Release complete, more releases remain**

Read ROADMAP.md to get the next release's name and goal.

```
---

## ✓ Release {Z} Complete

## ▶ Next Up

**Release {Z+1}: {Name}** — {Goal from ROADMAP.md}

`/cat:change-release {Z+1}`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- `/cat:discuss-release {Z+1}` — gather context first
- `/cat:research-release {Z+1}` — investigate unknowns

---
```

---

**Route D: Milestone complete**

```
---

## 🎉 Milestone Complete

All {N} releases finished!

## ▶ Next Up

**Complete Milestone** — archive and prepare for next

`/cat:complete-milestone`

<sub>`/clear` first → fresh context window</sub>

---
```

</step>

<step name="edge_cases">
**Handle edge cases:**

- Release complete but next release not planned → offer `/cat:change-release [next]`
- All work complete → offer milestone completion
- Blockers present → highlight before offering to continue
- Handoff file exists → mention it, offer `/cat:resume-work`
  </step>

</process>

<success_criteria>

- [ ] Rich context provided (recent work, decisions, issues)
- [ ] Current position clear with visual progress
- [ ] What's next clearly explained
- [ ] Smart routing: /cat:execute-change if change exists, /cat:change-release if not
- [ ] User confirms before any action
- [ ] Seamless handoff to appropriate cat command
      </success_criteria>
