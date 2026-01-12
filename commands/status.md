---
name: cat:status
description: Show hierarchy status with visual tree
allowed-tools:
  - Read
  - Bash
  - Glob
  - Grep
---

<objective>

Display the complete CAT hierarchy status with a visual tree showing all major versions, minor versions,
and tasks with their current status.

Provides situational awareness for project progress.

</objective>

<process>

<step name="verify">

**Verify planning structure exists:**

```bash
[ ! -d .claude/cat ] && echo "No planning structure found. Run /cat:new-project to start." && exit 1
```

</step>

<step name="load">

**Load project context:**

- Read `.claude/cat/PROJECT.md` for project name and overview
- Read `.claude/cat/ROADMAP.md` for version structure
- Read `.claude/cat/cat-config.json` for configuration

</step>

<step name="scan">

**Scan all STATE.md files:**

```bash
# Find all major versions
ls -1d .claude/cat/v[0-9]* 2>/dev/null | sort -V

# Find all minor versions under each major
find .claude/cat -maxdepth 2 -type d -name "v[0-9]*.[0-9]*" 2>/dev/null | sort -V

# Find all tasks under each minor
find .claude/cat/v*/v*.*/task -maxdepth 1 -type d 2>/dev/null | sort
```

For each STATE.md found, extract:
- Status (pending, in-progress, completed, blocked)
- Progress percentage
- Dependencies

</step>

<step name="calculate">

**Calculate overall progress:**

1. Count total tasks across all major/minor versions
2. Count completed tasks
3. Calculate percentage: (completed / total) * 100
4. Identify current position (first in-progress or pending task)

</step>

<step name="render">

**Render visual tree:**

```
# [Project Name]

**Progress:** [=========>    ] 75% (15/20 tasks)
**Mode:** [Interactive|YOLO]

## MAJOR 1: [Name from ROADMAP] (In Progress)

### MINOR 0: [Description]
- [x] parse-tokens (completed)
- [>] build-ast (in-progress, 60%)
- [ ] validate-ast (pending)

### MINOR 1: [Description]
- [ ] generate-ir (pending)
- [ ] optimize-ir (pending, depends: generate-ir)

## MAJOR 2: [Name from ROADMAP] (Pending)

### MINOR 0: [Description]
- [ ] emit-code (pending)
- [ ] format-output (pending)

---

**Current:** Major 1, Minor 0, Task: build-ast
**Next executable:** validate-ast (after build-ast completes)

---
```

**Status symbols:**
- `[x]` - completed
- `[>]` - in-progress
- `[ ]` - pending
- `[!]` - blocked (dependencies not met)

**Color hints (if terminal supports):**
- Green for completed
- Yellow for in-progress
- Gray for pending
- Red for blocked

</step>

<step name="blockers">

**Identify blockers:**

List any tasks that are blocked:
- Show which dependencies are incomplete
- Calculate when they could become unblocked

```
## Blocked Tasks

- **Major 1.1/optimize-ir** - waiting on: generate-ir
- **Major 2.0/emit-code** - waiting on: Major 1 completion
```

</step>

<step name="next">

**Suggest next action:**

Based on current state, suggest the most appropriate next command:

| State | Suggestion |
|-------|------------|
| Has executable task | `/cat:execute-task` |
| All tasks complete for minor | `/cat:add-task` or `/cat:add-minor-version` |
| All minors complete for major | `/cat:add-major-version` |
| All complete | "Project complete!" |

```
---

## Next Action

`/cat:execute-task 1.0/build-ast`

Or use `/cat:add-task` to add more work.

---
```

</step>

</process>

<output_format>

The status output should be:
1. Compact but informative
2. Easy to scan visually
3. Show progress at all levels
4. Highlight current position
5. Suggest next action

</output_format>

<success_criteria>

- [ ] All major versions displayed
- [ ] All minor versions under each major displayed
- [ ] All tasks with correct status symbols
- [ ] Progress bar accurate
- [ ] Current position identified
- [ ] Next action suggested
- [ ] Blocked tasks explained

</success_criteria>
