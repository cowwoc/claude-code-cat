<purpose>
Define the releases of implementation. Each release is a coherent chunk of work
that delivers value. The roadmap provides structure, not detailed tasks.
</purpose>

<required_reading>
**Read these files NOW:**

1. ~/.claude/cat/templates/roadmap.md
2. ~/.claude/cat/templates/state.md
3. Read `.planning/PROJECT.md` if it exists
   </required_reading>

<process>

<step name="check_brief">
```bash
cat .planning/PROJECT.md 2>/dev/null || echo "No brief found"
```

**If no brief exists:**
Ask: "No brief found. Want to create one first, or proceed with roadmap?"

If proceeding without brief, gather quick context:

- What are we building?
- What's the rough scope?
</step>


<step name="detect_domain">
Scan for available domain expertise:

```bash
ls ~/.claude/skills/expertise/ 2>/dev/null
```

**Inference:** Based on the brief/user request, infer applicable domains:

| Keywords                                 | Domain                   |
| ---------------------------------------- | ------------------------ |
| "macOS", "Mac app", "menu bar", "AppKit" | expertise/macos-apps     |
| "iPhone", "iOS", "iPad", "mobile app"    | expertise/iphone-apps    |
| "Unity", "game", "C#", "3D game"         | expertise/unity-games    |
| "MIDI", "sequencer", "music app"         | expertise/midi           |
| "ISF", "shader", "GLSL", "visual effect" | expertise/isf-shaders    |
| "UI", "design", "frontend", "Tailwind"   | expertise/ui-design      |
| "Agent SDK", "Claude SDK", "agentic"     | expertise/with-agent-sdk |

**If domain inferred:**

```
Detected: [domain] project → expertise/[name]
Include this domain expertise? (Y / see options / none)
```

**If multiple domains apply** (e.g., ISF shaders for a macOS app):

```
Detected multiple domains:
- expertise/isf-shaders (shader development)
- expertise/macos-apps (native app)

Include both? (Y / select one / none)
```

**If no domain obvious:**

```
Available domain expertise:
1. macos-apps
2. iphone-apps
[... others found ...]

N. None - proceed without domain expertise

Select (comma-separate for multiple):
```

**Store selected paths** for inclusion in ROADMAP.md.
</step>

<step name="identify_phases">
Derive releases from the actual work needed.

**Check depth setting:**
```bash
cat .planning/config.json 2>/dev/null | grep depth
```

<depth_guidance>
**Depth controls compression tolerance, not artificial inflation.**

| Depth | Typical Releases | Typical Changes/Release | Tasks/Change |
|-------|----------------|---------------------|------------|
| Quick | 3-5 | 1-3 | 2-3 |
| Standard | 5-8 | 3-5 | 2-3 |
| Comprehensive | 8-12 | 5-10 | 2-3 |

**Key principle:** Derive releases from actual work. Depth determines how aggressively you combine things, not a target to hit.

- Comprehensive auth system = 8 releases (because auth genuinely has 8 concerns)
- Comprehensive "add favicon" = 1 release (because that's all it is)

For comprehensive depth:
- Don't compress multiple features into single releases
- Each major capability gets its own release
- Let small things stay small—don't pad to hit a number
- If you're tempted to combine two things, make them separate releases instead

For quick depth:
- Combine related work aggressively
- Focus on critical path only
- Defer nice-to-haves to future milestones
</depth_guidance>

**Release Numbering System:**

Use integer releases (1, 2, 3) for planned milestone work.

Use decimal releases (2.1, 2.2) for urgent insertions:

- Decimal releases inserted between integers (2.1 between 2 and 3)
- Mark with "(INSERTED)" in release title
- Created when urgent work discovered after planning
- Examples: bugfixes, hotfixes, critical patches

**When to use decimals:**

- Urgent work that can't wait for next milestone
- Critical bugs blocking progress
- Security patches needing immediate attention
- NOT for scope creep or "nice to haves" (those go in ISSUES.md)

**Release execution order:**
Numeric sort: 1 → 1.1 → 1.2 → 2 → 2.1 → 3

**Deriving releases:**

1. List all distinct systems/features/capabilities required
2. Group related work into coherent deliverables
3. Each release should deliver ONE complete, verifiable thing
4. If a release delivers multiple unrelated capabilities: split it
5. If a release can't stand alone as a complete deliverable: merge it
6. Order by dependencies

Good releases are:

- **Coherent**: Each delivers one complete, verifiable capability
- **Sequential**: Later releases build on earlier
- **Independent**: Can be verified and committed on its own

Common release patterns:

- Foundation → Core Feature → Enhancement → Polish
- Setup → MVP → Iteration → Launch
- Infrastructure → Backend → Frontend → Integration
  </step>

<step name="detect_research_needs">
**For each release, determine if research is likely needed.**

Scan the brief and release descriptions for research triggers:

<research_triggers>
**Likely (flag the release):**

| Trigger Pattern                                       | Why Research Needed                     |
| ----------------------------------------------------- | --------------------------------------- |
| "integrate [service]", "connect to [API]"             | External API - need current docs        |
| "authentication", "auth", "login", "JWT"              | Architectural decision + library choice |
| "payment", "billing", "Stripe", "subscription"        | External API + compliance patterns      |
| "email", "SMS", "notifications", "SendGrid", "Twilio" | External service integration            |
| "database", "Postgres", "MongoDB", "Supabase"         | If new to project - setup patterns      |
| "real-time", "websocket", "sync", "live updates"      | Architectural decision                  |
| "deploy", "Vercel", "Railway", "hosting"              | If first deployment - config patterns   |
| "choose between", "select", "evaluate", "which"       | Explicit decision needed                |
| "AI", "OpenAI", "Claude", "LLM", "embeddings"         | Fast-moving APIs - need current docs    |
| Any technology not already in codebase                | New integration                         |
| Explicit questions in brief                           | Unknowns flagged by user                |

**Unlikely (no flag needed):**

| Pattern                                     | Why No Research         |
| ------------------------------------------- | ----------------------- |
| "add button", "create form", "update UI"    | Internal patterns       |
| "CRUD operations", "list/detail views"      | Standard patterns       |
| "refactor", "reorganize", "clean up"        | Internal work           |
| "following existing patterns"               | Conventions established |
| Technology already in package.json/codebase | Patterns exist          |

</research_triggers>

**For each release, assign:**

- `Research: Likely ([reason])` + `Research topics: [what to investigate]`
- `Research: Unlikely ([reason])`

**Important:** These are hints, not mandates. The mandatory_discovery step during release planning will validate.

Present research assessment:

```
Research needs detected:

Release 1: Foundation
  Research: Unlikely (project setup, established patterns)

Release 2: Authentication
  Research: Likely (new system, technology choice)
  Topics: JWT library for [stack], session strategy, auth provider options

Release 3: Stripe Integration
  Research: Likely (external API)
  Topics: Current Stripe API, webhook patterns, checkout flow

Release 4: Dashboard
  Research: Unlikely (internal UI using patterns from earlier releases)

Does this look right? (yes / adjust)
```

</step>

<step name="confirm_releases">
<config-check>
```bash
cat .planning/config.json 2>/dev/null
```
Note: Config may not exist yet (project initialization). If missing, default to interactive mode.
</config-check>

<if mode="yolo">
```
⚡ Auto-approved: Release breakdown ([N] releases)

1. [Release name] - [goal]
2. [Release name] - [goal]
3. [Release name] - [goal]

Proceeding to research detection...
```

Proceed directly to detect_research_needs step.
</if>

<if mode="interactive" OR="missing OR custom with gates.confirm_releases true">
Present the release breakdown inline:

"Here's how I'd break this down:

1. [Release name] - [goal]
2. [Release name] - [goal]
3. [Release name] - [goal]
   ...

Does this feel right? (yes / adjust)"

If "adjust": Ask what to change, revise, present again.
</step>

<step name="decision_gate">
<if mode="yolo">
```
⚡ Auto-approved: Create roadmap with [N] releases

Proceeding to create .planning/ROADMAP.md...
```

Proceed directly to create_structure step.
</if>

<if mode="interactive" OR="missing OR custom with gates.confirm_roadmap true">
Use AskUserQuestion:

- header: "Ready"
- question: "Ready to create the roadmap, or would you like me to ask more questions?"
- options:
  - "Create roadmap" - I have enough context
  - "Ask more questions" - There are details to clarify
  - "Let me add context" - I want to provide more information

Loop until "Create roadmap" selected.
</step>

<step name="create_structure">
```bash
mkdir -p .planning/releases
```
</step>

<step name="write_roadmap">
Use template from `~/.claude/cat/templates/roadmap.md`.

Initial roadmaps use integer releases (1, 2, 3...).
Decimal releases added later via /cat:insert-release command (if it exists).

Write to `.planning/ROADMAP.md` with:

- Domain Expertise section (paths from detect_domain step, or "None" if skipped)
- Release list with names and one-line descriptions
- Dependencies (what must complete before what)
- **Research flags** (from detect_research_needs step):
  - `Research: Likely ([reason])` with `Research topics:` for flagged releases
  - `Research: Unlikely ([reason])` for unflagged releases
- Status tracking (all start as "not started")

Create release directories:

```bash
mkdir -p .planning/releases/01-{release-name}
mkdir -p .planning/releases/02-{release-name}
# etc.
```

</step>

<step name="initialize_project_state">

Create STATE.md — the project's living memory.

Use template from `~/.claude/cat/templates/state.md`.

Write to `.planning/STATE.md`:

```markdown
# Project State

## Project Reference

See: .planning/PROJECT.md (updated [today's date])

**Core value:** [Copy Core Value from PROJECT.md]
**Current focus:** Release 1 — [First release name]

## Current Position

Release: 1 of [N] ([First release name])
Change: Not started
Status: Ready to change
Last activity: [today's date] — Project initialized

Progress: ░░░░░░░░░░ 0%

## Performance Metrics

**Velocity:**
- Total changes completed: 0
- Average duration: —
- Total execution time: 0 hours

**By Release:**

| Release | Changes | Total | Avg/Change |
|-------|-------|-------|----------|
| — | — | — | — |

**Recent Trend:**
- Last 5 changes: —
- Trend: —

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

(None yet)

### Deferred Issues

None yet.

### Blockers/Concerns

None yet.

## Session Continuity

Last session: [today's date and time]
Stopped at: Project initialization complete
Resume file: None
```

**Key points:**

- Project Reference points to PROJECT.md for full context
- Claude reads PROJECT.md directly for requirements, constraints, decisions
- This file will be read first in every future operation
- This file will be updated after every execution

</step>

<step name="git_commit_initialization">
Commit project initialization (brief + roadmap + state together):

```bash
git add .planning/PROJECT.md .planning/ROADMAP.md .planning/STATE.md
git add .planning/releases/
# config.json if exists
git add .planning/config.json 2>/dev/null
git commit -m "$(cat <<'EOF'
docs: initialize [project-name] ([N] releases)

[One-liner from PROJECT.md]

Releases:
1. [release-name]: [goal]
2. [release-name]: [goal]
3. [release-name]: [goal]
EOF
)"
```

Confirm: "Committed: docs: initialize [project] ([N] releases)"
</step>

<step name="offer_next">
```
Project initialized:
- Brief: .planning/PROJECT.md
- Roadmap: .planning/ROADMAP.md
- State: .planning/STATE.md
- Committed as: docs: initialize [project] ([N] releases)

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

<phase_naming>
Use `XX-kebab-case-name` format:
- `01-foundation`
- `02-authentication`
- `03-core-features`
- `04-polish`

Numbers ensure ordering. Names describe content.
</phase_naming>

<anti_patterns>
- Don't add time estimates
- Don't create Gantt charts
- Don't add resource allocation
- Don't include risk matrices
- Don't impose arbitrary release counts (let the work determine the count)

Releases are buckets of work, not project management artifacts.
</anti_patterns>

<success_criteria>
Roadmap is complete when:
- [ ] `.planning/ROADMAP.md` exists
- [ ] `.planning/STATE.md` exists (project memory initialized)
- [ ] Releases defined with clear names (count derived from work, not imposed)
- [ ] **Research flags assigned** (Likely/Unlikely for each release)
- [ ] **Research topics listed** for Likely releases
- [ ] Release directories created
- [ ] Dependencies noted if any
- [ ] Status tracking in place
</success_criteria>
```
