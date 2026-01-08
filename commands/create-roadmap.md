---
name: cat:create-roadmap
description: Create roadmap with releases for the project
allowed-tools:
  - Read
  - Write
  - Bash
  - AskUserQuestion
  - Glob
---

<objective>
Create project roadmap with release breakdown.

Roadmaps define what work happens in what order. Run after /cat:new-project.
</objective>

<execution_context>
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/workflows/create-roadmap.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/templates/roadmap.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/templates/state.md
</execution_context>

<context>
@.planning/PROJECT.md
@.planning/config.json
</context>

<process>

<step name="validate">
```bash
# Verify project exists
[ -f .planning/PROJECT.md ] || { echo "ERROR: No PROJECT.md found. Run /cat:new-project first."; exit 1; }
```
</step>

<step name="check_existing">
Check if roadmap already exists:

```bash
[ -f .planning/ROADMAP.md ] && echo "ROADMAP_EXISTS" || echo "NO_ROADMAP"
```

**If ROADMAP_EXISTS:**
Use AskUserQuestion:
- header: "Roadmap exists"
- question: "A roadmap already exists. What would you like to do?"
- options:
  - "View existing" - Show current roadmap
  - "Replace" - Create new roadmap (will overwrite)
  - "Cancel" - Keep existing roadmap

If "View existing": `cat .planning/ROADMAP.md` and exit
If "Cancel": Exit
If "Replace": Continue with workflow
</step>

<step name="create_roadmap">
Follow the create-roadmap.md workflow starting from detect_domain step.

The workflow handles:
- Domain expertise detection
- Release identification
- Research flags for each release
- Confirmation gates (respecting config mode)
- ROADMAP.md creation
- STATE.md initialization
- Release directory creation
- Git commit
</step>

<step name="done">
```
Roadmap created:
- Roadmap: .planning/ROADMAP.md
- State: .planning/STATE.md
- [N] releases defined

---

## ▶ Next Up

**Release 1: [Name]** — [Goal from ROADMAP.md]

`/cat:change-release 1`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- `/cat:discuss-release 1` — gather context first
- `/cat:research-release 1` — investigate unknowns
- Review roadmap

---
```
</step>

</process>

<output>
- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `.planning/releases/XX-name/` directories
</output>

<success_criteria>
- [ ] PROJECT.md validated
- [ ] ROADMAP.md created with releases
- [ ] STATE.md initialized
- [ ] Release directories created
- [ ] Changes committed
</success_criteria>
