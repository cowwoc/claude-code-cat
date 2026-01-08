---
name: cat:insert-release
description: Insert urgent work as decimal release (e.g., 72.1) between existing releases
argument-hint: <after> <description>
allowed-tools:
  - Read
  - Write
  - Bash
---

<objective>
Insert a decimal release for urgent work discovered mid-milestone that must be completed between existing integer releases.

Uses decimal numbering (72.1, 72.2, etc.) to preserve the logical sequence of planned releases while accommodating urgent insertions.

Purpose: Handle urgent work discovered during execution without renumbering entire roadmap.
</objective>

<execution_context>
@.planning/ROADMAP.md
@.planning/STATE.md
</execution_context>

<process>

<step name="parse_arguments">
Parse the command arguments:
- First argument: integer release number to insert after
- Remaining arguments: release description

Example: `/cat:insert-release 72 Fix critical auth bug`
→ after = 72
→ description = "Fix critical auth bug"

Validation:

```bash
if [ $# -lt 2 ]; then
  echo "ERROR: Both release number and description required"
  echo "Usage: /cat:insert-release <after> <description>"
  echo "Example: /cat:insert-release 72 Fix critical auth bug"
  exit 1
fi
```

Parse first argument as integer:

```bash
after_phase=$1
shift
description="$*"

# Validate after_phase is an integer
if ! [[ "$after_phase" =~ ^[0-9]+$ ]]; then
  echo "ERROR: Release number must be an integer"
  exit 1
fi
```

</step>

<step name="load_roadmap">
Load the roadmap file:

```bash
if [ -f .planning/ROADMAP.md ]; then
  ROADMAP=".planning/ROADMAP.md"
else
  echo "ERROR: No roadmap found (.planning/ROADMAP.md)"
  exit 1
fi
```

Read roadmap content for parsing.
</step>

<step name="verify_target_phase">
Verify that the target release exists in the roadmap:

1. Search for "### Release {after_phase}:" heading
2. If not found:

   ```
   ERROR: Release {after_phase} not found in roadmap
   Available releases: [list release numbers]
   ```

   Exit.

3. Verify release is in current milestone (not completed/archived)
   </step>

<step name="find_existing_decimals">
Find existing decimal releases after the target release:

1. Search for all "### Release {after_phase}.N:" headings
2. Extract decimal suffixes (e.g., for Release 72: find 72.1, 72.2, 72.3)
3. Find the highest decimal suffix
4. Calculate next decimal: max + 1

Examples:

- Release 72 with no decimals → next is 72.1
- Release 72 with 72.1 → next is 72.2
- Release 72 with 72.1, 72.2 → next is 72.3

Store as: `decimal_phase="${after_phase}.${next_decimal}"`
</step>

<step name="generate_slug">
Convert the release description to a kebab-case slug:

```bash
slug=$(echo "$description" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/-/g' | sed 's/--*/-/g' | sed 's/^-//;s/-$//')
```

Release directory name: `{decimal-release}-{slug}`
Example: `72.1-fix-critical-auth-bug`
</step>

<step name="create_phase_directory">
Create the release directory structure:

```bash
phase_dir=".planning/releases/${decimal_phase}-${slug}"
mkdir -p "$phase_dir"
```

Confirm: "Created directory: $phase_dir"
</step>

<step name="update_roadmap">
Insert the new release entry into the roadmap:

1. Find insertion point: immediately after Release {after_phase}'s content (before next release heading or "---")
2. Insert new release heading with (INSERTED) marker:

   ```
   ### Release {decimal_phase}: {Description} (INSERTED)

   **Goal:** [Urgent work - to be planned]
   **Depends on:** Release {after_phase}
   **Changes:** 0 changes

   Changes:
   - [ ] TBD (run /cat:change-release {decimal_phase} to break down)

   **Details:**
   [To be added during planning]
   ```

3. Write updated roadmap back to file

The "(INSERTED)" marker helps identify decimal releases as urgent insertions.

Preserve all other content exactly (formatting, spacing, other releases).
</step>

<step name="update_project_state">
Update STATE.md to reflect the inserted release:

1. Read `.planning/STATE.md`
2. Under "## Accumulated Context" → "### Roadmap Evolution" add entry:
   ```
   - Release {decimal_phase} inserted after Release {after_phase}: {description} (URGENT)
   ```

If "Roadmap Evolution" section doesn't exist, create it.

Add note about insertion reason if appropriate.
</step>

<step name="completion">
Present completion summary:

```
Release {decimal_phase} inserted after Release {after_phase}:
- Description: {description}
- Directory: .planning/releases/{decimal-release}-{slug}/
- Status: Not planned yet
- Marker: (INSERTED) - indicates urgent work

Roadmap updated: {roadmap-path}
Project state updated: .planning/STATE.md

---

## ▶ Next Up

**Release {decimal_phase}: {description}** — urgent insertion

`/cat:change-release {decimal_phase}`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- Review insertion impact: Check if Release {next_integer} dependencies still make sense
- Review roadmap

---
```
</step>

</process>

<anti_patterns>

- Don't use this for planned work at end of milestone (use /cat:add-release)
- Don't insert before Release 1 (decimal 0.1 makes no sense)
- Don't renumber existing releases
- Don't modify the target release content
- Don't create changes yet (that's /cat:change-release)
- Don't commit changes (user decides when to commit)
  </anti_patterns>

<success_criteria>
Release insertion is complete when:

- [ ] Release directory created: `.planning/releases/{N.M}-{slug}/`
- [ ] Roadmap updated with new release entry (includes "(INSERTED)" marker)
- [ ] Release inserted in correct position (after target release, before next integer release)
- [ ] STATE.md updated with roadmap evolution note
- [ ] Decimal number calculated correctly (based on existing decimals)
- [ ] User informed of next steps and dependency implications
      </success_criteria>
