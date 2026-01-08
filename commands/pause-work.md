---
name: cat:pause-work
description: Create context handoff when pausing work mid-release
allowed-tools:
  - Read
  - Write
  - Bash
---

<objective>
Create `.continue-here.md` handoff file to preserve complete work state across sessions.

Enables seamless resumption in fresh session with full context restoration.
</objective>

<context>
@.planning/STATE.md
</context>

<process>

<step name="detect">
Find current release directory from most recently modified files.
</step>

<step name="gather">
**Collect complete state for handoff:**

1. **Current position**: Which release, which change, which task
2. **Work completed**: What got done this session
3. **Work remaining**: What's left in current change/release
4. **Decisions made**: Key decisions and rationale
5. **Blockers/issues**: Anything stuck
6. **Mental context**: The approach, next steps, "vibe"
7. **Files modified**: What's changed but not committed

Ask user for clarifications if needed.
</step>

<step name="write">
**Write handoff to `.planning/releases/XX-name/.continue-here.md`:**

```markdown
---
release: XX-name
task: 3
total_tasks: 7
status: in_progress
last_updated: [timestamp]
---

<current_state>
[Where exactly are we? Immediate context]
</current_state>

<completed_work>

- Task 1: [name] - Done
- Task 2: [name] - Done
- Task 3: [name] - In progress, [what's done]
  </completed_work>

<remaining_work>

- Task 3: [what's left]
- Task 4: Not started
- Task 5: Not started
  </remaining_work>

<decisions_made>

- Decided to use [X] because [reason]
- Chose [approach] over [alternative] because [reason]
  </decisions_made>

<blockers>
- [Blocker 1]: [status/workaround]
</blockers>

<context>
[Mental state, what were you thinking, the change]
</context>

<next_action>
Start with: [specific first action when resuming]
</next_action>
```

Be specific enough for a fresh Claude to understand immediately.
</step>

<step name="commit">
```bash
git add .planning/releases/*/.continue-here.md
git commit -m "wip: [release-name] paused at task [X]/[Y]"
```
</step>

<step name="confirm">
```
✓ Handoff created: .planning/releases/[XX-name]/.continue-here.md

Current state:

- Release: [XX-name]
- Task: [X] of [Y]
- Status: [in_progress/blocked]
- Committed as WIP

To resume: /cat:resume-work

```
</step>

</process>

<success_criteria>
- [ ] .continue-here.md created in correct release directory
- [ ] All sections filled with specific content
- [ ] Committed as WIP
- [ ] User knows location and how to resume
</success_criteria>
```
