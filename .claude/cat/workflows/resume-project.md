<trigger>
Use this workflow when:
- Starting a new session on an existing project
- User says "continue", "what's next", "where were we", "resume"
- Any planning operation when .planning/ already exists
- User returns after time away from project
</trigger>

<purpose>
Instantly restore full project context and present clear status.
Enables seamless session continuity for fully autonomous workflows.

"Where were we?" should have an immediate, complete answer.
</purpose>

<process>

<step name="detect_existing_project">
Check if this is an existing project:

```bash
ls .planning/STATE.md 2>/dev/null && echo "Project exists"
ls .planning/ROADMAP.md 2>/dev/null && echo "Roadmap exists"
ls .planning/PROJECT.md 2>/dev/null && echo "Project file exists"
```

**If STATE.md exists:** Proceed to load_state
**If only ROADMAP.md/PROJECT.md exist:** Offer to reconstruct STATE.md
**If .planning/ doesn't exist:** This is a new project - route to /cat:new-project
</step>

<step name="load_state">

Read and parse STATE.md, then PROJECT.md:

```bash
cat .planning/STATE.md
cat .planning/PROJECT.md
```

**From STATE.md extract:**

- **Project Reference**: Core value and current focus
- **Current Position**: Release X of Y, Change A of B, Status
- **Progress**: Visual progress bar
- **Recent Decisions**: Key decisions affecting current work
- **Deferred Issues**: Open items awaiting attention
- **Blockers/Concerns**: Issues carried forward
- **Session Continuity**: Where we left off, any resume files

**From PROJECT.md extract:**

- **What This Is**: Current accurate description
- **Requirements**: Validated, Active, Out of Scope
- **Key Decisions**: Full decision log with outcomes
- **Constraints**: Hard limits on implementation

</step>

<step name="check_incomplete_work">
Look for incomplete work that needs attention:

```bash
# Check for continue-here files (mid-change resumption)
ls .planning/releases/*/.continue-here*.md 2>/dev/null

# Check for changes without summaries (incomplete execution)
for change in .planning/releases/*/*-CHANGE.md; do
  summary="${change/CHANGE/SUMMARY}"
  [ ! -f "$summary" ] && echo "Incomplete: $change"
done 2>/dev/null
```

**If .continue-here file exists:**

- This is a mid-change resumption point
- Read the file for specific resumption context
- Flag: "Found mid-change checkpoint"

**If CHANGE without SUMMARY exists:**

- Execution was started but not completed
- Flag: "Found incomplete change execution"
  </step>

<step name="present_status">
Present complete project status to user:

```
╔══════════════════════════════════════════════════════════════╗
║  PROJECT STATUS                                               ║
╠══════════════════════════════════════════════════════════════╣
║  Building: [one-liner from PROJECT.md "What This Is"]         ║
║                                                               ║
║  Release: [X] of [Y] - [Release name]                            ║
║  Change:  [A] of [B] - [Status]                                ║
║  Progress: [██████░░░░] XX%                                  ║
║                                                               ║
║  Last activity: [date] - [what happened]                     ║
╚══════════════════════════════════════════════════════════════╝

[If incomplete work found:]
⚠️  Incomplete work detected:
    - [.continue-here file or incomplete change]

[If deferred issues exist:]
📋 [N] deferred issues awaiting attention

[If blockers exist:]
⚠️  Carried concerns:
    - [blocker 1]
    - [blocker 2]

[If alignment is not ✓:]
⚠️  Brief alignment: [status] - [assessment]
```

</step>

<step name="determine_next_action">
Based on project state, determine the most logical next action:

**If .continue-here file exists:**
→ Primary: Resume from checkpoint
→ Option: Start fresh on current change

**If incomplete change (CHANGE without SUMMARY):**
→ Primary: Complete the incomplete change
→ Option: Abandon and move on

**If release in progress, all changes complete:**
→ Primary: Transition to next release
→ Option: Review completed work

**If release ready to change:**
→ Check if CONTEXT.md exists for this release:

- If CONTEXT.md missing:
  → Primary: Discuss release vision (how user imagines it working)
  → Secondary: Change directly (skip context gathering)
- If CONTEXT.md exists:
  → Primary: Change the release
  → Option: Review roadmap

**If release ready to execute:**
→ Primary: Execute next change
→ Option: Review the change first
</step>

<step name="offer_options">
Present contextual options based on project state:

```
What would you like to do?

[Primary action based on state - e.g.:]
1. Resume from checkpoint (/cat:execute-change .planning/releases/XX-name/.continue-here-02-01.md)
   OR
1. Execute next change (/cat:execute-change .planning/releases/XX-name/02-02-add-session-CHANGE.md)
   OR
1. Discuss Release 3 context (/cat:discuss-release 3) [if CONTEXT.md missing]
   OR
1. Change Release 3 (/cat:change-release 3) [if CONTEXT.md exists or discuss option declined]

[Secondary options:]
2. Review current release status
3. Check deferred issues ([N] open)
4. Review brief alignment
5. Something else
```

**Note:** When offering release planning, check for CONTEXT.md existence first:

```bash
ls .planning/releases/XX-name/CONTEXT.md 2>/dev/null
```

If missing, suggest discuss-release before change. If exists, offer change directly.

Wait for user selection.
</step>

<step name="route_to_workflow">
Based on user selection, route to appropriate workflow:

- **Execute change** → Show command for user to run after clearing:
  ```
  ---

  ## ▶ Next Up

  **{release}-{change}-{slug}: [Change Name]** — [objective from CHANGE.md]

  `/cat:execute-change [path]`

  <sub>`/clear` first → fresh context window</sub>

  ---
  ```
- **Change release** → Show command for user to run after clearing:
  ```
  ---

  ## ▶ Next Up

  **Release [N]: [Name]** — [Goal from ROADMAP.md]

  `/cat:change-release [release-number]`

  <sub>`/clear` first → fresh context window</sub>

  ---

  **Also available:**
  - `/cat:discuss-release [N]` — gather context first
  - `/cat:research-release [N]` — investigate unknowns

  ---
  ```
- **Transition** → ./transition.md
- **Review issues** → Read ISSUES.md, present summary
- **Review alignment** → Read PROJECT.md, compare to current state
- **Something else** → Ask what they need
</step>

<step name="update_session">
Before proceeding to routed workflow, update session continuity:

Update STATE.md:

```markdown
## Session Continuity

Last session: [now]
Stopped at: Session resumed, proceeding to [action]
Resume file: [updated if applicable]
```

This ensures if session ends unexpectedly, next resume knows the state.
</step>

</process>

<reconstruction>
If STATE.md is missing but other artifacts exist:

"STATE.md missing. Reconstructing from artifacts..."

1. Read PROJECT.md → Extract "What This Is" and Core Value
2. Read ROADMAP.md → Determine releases, find current position
3. Scan \*-SUMMARY.md files → Extract decisions, issues, concerns
4. Read ISSUES.md → Count deferred issues
5. Check for .continue-here files → Session continuity

Reconstruct and write STATE.md, then proceed normally.

This handles cases where:

- Project predates STATE.md introduction
- File was accidentally deleted
- Cloning repo without full .planning/ state
  </reconstruction>

<quick_resume>
For users who want minimal friction:

If user says just "continue" or "go":

- Load state silently
- Determine primary action
- Execute immediately without presenting options

"Continuing from [state]... [action]"

This enables fully autonomous "just keep going" workflow.
</quick_resume>

<success_criteria>
Resume is complete when:

- [ ] STATE.md loaded (or reconstructed)
- [ ] Incomplete work detected and flagged
- [ ] Clear status presented to user
- [ ] Contextual next actions offered
- [ ] User knows exactly where project stands
- [ ] Session continuity updated
      </success_criteria>
