---
name: cat:remove-release
description: Remove a future release from roadmap and renumber subsequent releases
argument-hint: <release-number>
allowed-tools:
  - Read
  - Write
  - Bash
  - Glob
---

<objective>
Remove an unstarted future release from the roadmap and renumber all subsequent releases to maintain a clean, linear sequence.

Purpose: Clean removal of work you've decided not to do, without polluting context with cancelled/deferred markers.
Output: Release deleted, all subsequent releases renumbered, git commit as historical record.
</objective>

<execution_context>
@.planning/ROADMAP.md
@.planning/STATE.md
</execution_context>

<process>

<step name="parse_arguments">
Parse the command arguments:
- Argument is the release number to remove (integer or decimal)
- Example: `/cat:remove-release 17` → release = 17
- Example: `/cat:remove-release 16.1` → release = 16.1

If no argument provided:

```
ERROR: Release number required
Usage: /cat:remove-release <release-number>
Example: /cat:remove-release 17
```

Exit.
</step>

<step name="load_state">
Load project state:

```bash
cat .planning/STATE.md 2>/dev/null
cat .planning/ROADMAP.md 2>/dev/null
```

Parse current release number from STATE.md "Current Position" section.
</step>

<step name="validate_release_exists">
Verify the target release exists in ROADMAP.md:

1. Search for `### Release {target}:` heading
2. If not found:

   ```
   ERROR: Release {target} not found in roadmap
   Available releases: [list release numbers]
   ```

   Exit.
</step>

<step name="validate_future_release">
Verify the release is a future release (not started):

1. Compare target release to current release from STATE.md
2. Target must be > current release number

If target <= current release:

```
ERROR: Cannot remove Release {target}

Only future releases can be removed:
- Current release: {current}
- Release {target} is current or completed

To abandon current work, use /cat:pause-work instead.
```

Exit.

3. Check for SUMMARY.md files in release directory:

```bash
ls .planning/releases/{target}-*/*-SUMMARY.md 2>/dev/null
```

If any SUMMARY.md files exist:

```
ERROR: Release {target} has completed work

Found executed changes:
- {list of SUMMARY.md files}

Cannot remove releases with completed work.
```

Exit.
</step>

<step name="gather_release_info">
Collect information about the release being removed:

1. Extract release name from ROADMAP.md heading: `### Release {target}: {Name}`
2. Find release directory: `.planning/releases/{target}-{slug}/`
3. Find all subsequent releases (integer and decimal) that need renumbering

**Subsequent release detection:**

For integer release removal (e.g., 17):
- Find all releases > 17 (integers: 18, 19, 20...)
- Find all decimal releases >= 17.0 and < 18.0 (17.1, 17.2...) → these become 16.x
- Find all decimal releases for subsequent integers (18.1, 19.1...) → renumber with their parent

For decimal release removal (e.g., 17.1):
- Find all decimal releases > 17.1 and < 18 (17.2, 17.3...) → renumber down
- Integer releases unchanged

List all releases that will be renumbered.
</step>

<step name="confirm_removal">
Present removal summary and confirm:

```
Removing Release {target}: {Name}

This will:
- Delete: .planning/releases/{target}-{slug}/
- Renumber {N} subsequent releases:
  - Release 18 → Release 17
  - Release 18.1 → Release 17.1
  - Release 19 → Release 18
  [etc.]

Proceed? (y/n)
```

Wait for confirmation.
</step>

<step name="delete_release_directory">
Delete the target release directory if it exists:

```bash
if [ -d ".planning/releases/{target}-{slug}" ]; then
  rm -rf ".planning/releases/{target}-{slug}"
  echo "Deleted: .planning/releases/{target}-{slug}/"
fi
```

If directory doesn't exist, note: "No directory to delete (release not yet created)"
</step>

<step name="renumber_directories">
Rename all subsequent release directories:

For each release directory that needs renumbering (in reverse order to avoid conflicts):

```bash
# Example: renaming 18-dashboard to 17-dashboard
mv ".planning/releases/18-dashboard" ".planning/releases/17-dashboard"
```

Process in descending order (20→19, then 19→18, then 18→17) to avoid overwriting.

Also rename decimal release directories:
- `17.1-fix-bug` → `16.1-fix-bug` (if removing integer 17)
- `17.2-hotfix` → `17.1-hotfix` (if removing decimal 17.1)
</step>

<step name="rename_files_in_directories">
Rename change files inside renumbered directories:

For each renumbered directory, rename files that contain the release number:

```bash
# Inside 17-dashboard (was 18-dashboard):
mv "18-01-setup-CHANGE.md" "17-01-setup-CHANGE.md"
mv "18-02-config-CHANGE.md" "17-02-config-CHANGE.md"
mv "18-01-setup-SUMMARY.md" "17-01-setup-SUMMARY.md"  # if exists
# etc.
```

Also handle CONTEXT.md and DISCOVERY.md (these don't have release prefixes, so no rename needed).
</step>

<step name="update_roadmap">
Update ROADMAP.md:

1. **Remove the release section entirely:**
   - Delete from `### Release {target}:` to the next release heading (or section end)

2. **Remove from release list:**
   - Delete line `- [ ] **Release {target}: {Name}**` or similar

3. **Remove from Progress table:**
   - Delete the row for Release {target}

4. **Renumber all subsequent releases:**
   - `### Release 18:` → `### Release 17:`
   - `- [ ] **Release 18:` → `- [ ] **Release 17:`
   - Table rows: `| 18. Dashboard |` → `| 17. Dashboard |`
   - Change references: `18-01:` → `17-01:`

5. **Update dependency references:**
   - `**Depends on:** Release 18` → `**Depends on:** Release 17`
   - For the release that depended on the removed release:
     - `**Depends on:** Release 17` (removed) → `**Depends on:** Release 16`

6. **Renumber decimal releases:**
   - `### Release 17.1:` → `### Release 16.1:` (if integer 17 removed)
   - Update all references consistently

Write updated ROADMAP.md.
</step>

<step name="update_state">
Update STATE.md:

1. **Update total release count:**
   - `Release: 16 of 20` → `Release: 16 of 19`

2. **Recalculate progress percentage:**
   - New percentage based on completed changes / new total changes

Do NOT add a "Roadmap Evolution" note - the git commit is the record.

Write updated STATE.md.
</step>

<step name="update_file_contents">
Search for and update release references inside change files:

```bash
# Find files that reference the old release numbers
grep -r "Release 18" .planning/releases/17-*/ 2>/dev/null
grep -r "Release 19" .planning/releases/18-*/ 2>/dev/null
# etc.
```

Update any internal references to reflect new numbering.
</step>

<step name="commit">
Stage and commit the removal:

```bash
git add .planning/
git commit -m "planning: remove release {target} ({original-release-name})"
```

The commit message preserves the historical record of what was removed.
</step>

<step name="completion">
Present completion summary:

```
Release {target} ({original-name}) removed.

Changes:
- Deleted: .planning/releases/{target}-{slug}/
- Renumbered: Releases {first-renumbered}-{last-old} → {first-renumbered-1}-{last-new}
- Updated: ROADMAP.md, STATE.md
- Committed: planning: remove release {target} ({original-name})

Current roadmap: {total-remaining} releases
Current position: Release {current} of {new-total}

---

## What's Next

Would you like to:
- `/cat:progress` — see updated roadmap status
- Continue with current release
- Review roadmap

---
```
</step>

</process>

<anti_patterns>

- Don't remove completed releases (have SUMMARY.md files)
- Don't remove current or past releases
- Don't leave gaps in numbering - always renumber
- Don't add "removed release" notes to STATE.md - git commit is the record
- Don't ask about each decimal release - just renumber them
- Don't modify completed release directories
</anti_patterns>

<edge_cases>

**Removing a decimal release (e.g., 17.1):**
- Only affects other decimals in same series (17.2 → 17.1, 17.3 → 17.2)
- Integer releases unchanged
- Simpler operation

**No subsequent releases to renumber:**
- Removing the last release (e.g., Release 20 when that's the end)
- Just delete and update ROADMAP.md, no renumbering needed

**Release directory doesn't exist:**
- Release may be in ROADMAP.md but directory not created yet
- Skip directory deletion, proceed with ROADMAP.md updates

**Decimal releases under removed integer:**
- Removing Release 17 when 17.1, 17.2 exist
- 17.1 → 16.1, 17.2 → 16.2
- They maintain their position in execution order (after current last integer)

</edge_cases>

<success_criteria>
Release removal is complete when:

- [ ] Target release validated as future/unstarted
- [ ] Release directory deleted (if existed)
- [ ] All subsequent release directories renumbered
- [ ] Files inside directories renamed ({old}-01-CHANGE.md → {new}-01-CHANGE.md)
- [ ] ROADMAP.md updated (section removed, all references renumbered)
- [ ] STATE.md updated (release count, progress percentage)
- [ ] Dependency references updated in subsequent releases
- [ ] Changes committed with descriptive message
- [ ] No gaps in release numbering
- [ ] User informed of changes
</success_criteria>
