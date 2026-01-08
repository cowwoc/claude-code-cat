# Roadmap Template

Template for `.planning/ROADMAP.md`.

## Initial Roadmap (v1.0 Greenfield)

```markdown
# Roadmap: [Project Name]

## Overview

[One paragraph describing the journey from start to finish]

## Domain Expertise

[Paths to domain skills that inform planning. These SKILL.md files serve as indexes - during release planning, read them to find relevant references for each release type.]

- ~/.claude/skills/expertise/[domain]/SKILL.md
[Add additional domains if project spans multiple (e.g., ISF shaders + macOS app)]

Or: None

## Releases

**Release Numbering:**
- Integer releases (1, 2, 3): Planned milestone work
- Decimal releases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal releases appear between their surrounding integers in numeric order.

- [ ] **Release 1: [Name]** - [One-line description]
- [ ] **Release 2: [Name]** - [One-line description]
- [ ] **Release 3: [Name]** - [One-line description]
- [ ] **Release 4: [Name]** - [One-line description]

## Release Details

### Release 1: [Name]
**Goal**: [What this release delivers]
**Depends on**: Nothing (first release)
**Research**: Unlikely (established patterns)
**Changes**: [Number of changes, e.g., "3 changes" or "TBD"]

Changes:
- [ ] 01-01: [Brief description of first change]
- [ ] 01-02: [Brief description of second change]
- [ ] 01-03: [Brief description of third change]

### Release 2: [Name]
**Goal**: [What this release delivers]
**Depends on**: Release 1
**Research**: Likely (new integration)
**Research topics**: [What needs investigating]
**Changes**: [Number of changes]

Changes:
- [ ] 02-01: [Brief description]
- [ ] 02-02: [Brief description]

### Release 2.1: Critical Fix (INSERTED)
**Goal**: [Urgent work inserted between releases]
**Depends on**: Release 2
**Changes**: 1 change

Changes:
- [ ] 2.1-01: [Description]

### Release 3: [Name]
**Goal**: [What this release delivers]
**Depends on**: Release 2
**Research**: Likely (external API)
**Research topics**: [What needs investigating]
**Changes**: [Number of changes]

Changes:
- [ ] 03-01: [Brief description]
- [ ] 03-02: [Brief description]

### Release 4: [Name]
**Goal**: [What this release delivers]
**Depends on**: Release 3
**Research**: Unlikely (internal patterns)
**Changes**: [Number of changes]

Changes:
- [ ] 04-01: [Brief description]

## Progress

**Execution Order:**
Releases execute in numeric order: 2 → 2.1 → 2.2 → 3 → 3.1 → 4

| Release | Changes Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. [Name] | 0/3 | Not started | - |
| 2. [Name] | 0/2 | Not started | - |
| 3. [Name] | 0/2 | Not started | - |
| 4. [Name] | 0/1 | Not started | - |
```

<guidelines>
**Initial planning (v1.0):**
- Release count depends on depth setting (quick: 3-5, standard: 5-8, comprehensive: 8-12)
- Each release delivers something coherent
- Releases can have 1+ changes (split if >3 tasks or multiple subsystems)
- Changes use naming: {release}-{change}-{slug}-CHANGE.md (e.g., 01-02-setup-auth-CHANGE.md)
- No time estimates (this isn't enterprise PM)
- Progress table updated by execute workflow
- Change count can be "TBD" initially, refined during planning

**Research flags:**
- `Research: Likely` - External APIs, new libraries, architectural decisions
- `Research: Unlikely` - Internal patterns, CRUD operations, established conventions
- Include `Research topics:` when Likely
- Flags are hints, not mandates - validate at planning time

**After milestones ship:**
- Collapse completed milestones in `<details>` tags
- Add new milestone sections for upcoming work
- Keep continuous release numbering (never restart at 01)
</guidelines>

<status_values>
- `Not started` - Haven't begun
- `In progress` - Currently working
- `Complete` - Done (add completion date)
- `Deferred` - Pushed to later (with reason)
</status_values>

## Milestone-Grouped Roadmap (After v1.0 Ships)

After completing first milestone, reorganize with milestone groupings:

```markdown
# Roadmap: [Project Name]

## Milestones

- ✅ **v1.0 MVP** - Releases 1-4 (shipped YYYY-MM-DD)
- 🚧 **v1.1 [Name]** - Releases 5-6 (in progress)
- 📋 **v2.0 [Name]** - Releases 7-10 (planned)

## Releases

<details>
<summary>✅ v1.0 MVP (Releases 1-4) - SHIPPED YYYY-MM-DD</summary>

### Release 1: [Name]
**Goal**: [What this release delivers]
**Changes**: 3 changes

Changes:
- [x] 01-01: [Brief description]
- [x] 01-02: [Brief description]
- [x] 01-03: [Brief description]

[... remaining v1.0 releases ...]

</details>

### 🚧 v1.1 [Name] (In Progress)

**Milestone Goal:** [What v1.1 delivers]

#### Release 5: [Name]
**Goal**: [What this release delivers]
**Depends on**: Release 4
**Changes**: 2 changes

Changes:
- [ ] 05-01: [Brief description]
- [ ] 05-02: [Brief description]

[... remaining v1.1 releases ...]

### 📋 v2.0 [Name] (Planned)

**Milestone Goal:** [What v2.0 delivers]

[... v2.0 releases ...]

## Progress

| Release | Milestone | Changes Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. Foundation | v1.0 | 3/3 | Complete | YYYY-MM-DD |
| 2. Features | v1.0 | 2/2 | Complete | YYYY-MM-DD |
| 5. Security | v1.1 | 0/2 | Not started | - |
```

**Notes:**
- Milestone emoji: ✅ shipped, 🚧 in progress, 📋 planned
- Completed milestones collapsed in `<details>` for readability
- Current/future milestones expanded
- Continuous release numbering (01-99)
- Progress table includes milestone column
