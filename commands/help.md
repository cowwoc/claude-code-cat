---
name: cat:help
description: Show available CAT commands and usage guide
---

<objective>
Display the complete CAT command reference.

Output ONLY the reference content below. Do NOT add:

- Project-specific analysis
- Git status or file context
- Next-step suggestions
- Any commentary beyond the reference
  </objective>

<reference>
# CAT Command Reference

**CAT** creates hierarchical project changes optimized for solo agentic development with Claude Code.

## Quick Start

1. `/cat:new-project` - Initialize project with brief
2. `/cat:create-roadmap` - Create roadmap and releases
3. `/cat:change-release <number>` - Create detailed change for first release
4. `/cat:execute-change <path>` - Execute the change

## Core Workflow

```
Initialization → Planning → Execution → Milestone Completion
```

### Project Initialization

**`/cat:new-project`**
Initialize new project with brief and configuration.

- Creates `.planning/PROJECT.md` (vision and requirements)
- Creates `.planning/config.json` (workflow mode)
- Asks for workflow mode (interactive/yolo) upfront
- Commits initialization files to git

Usage: `/cat:new-project`

**`/cat:create-roadmap`**
Create roadmap and state tracking for initialized project.

- Creates `.planning/ROADMAP.md` (release breakdown)
- Creates `.planning/STATE.md` (project memory)
- Creates `.planning/releases/` directories

Usage: `/cat:create-roadmap`

**`/cat:map-codebase`**
Map an existing codebase for brownfield projects.

- Analyzes codebase with parallel Explore agents
- Creates `.planning/codebase/` with 7 focused documents
- Covers stack, architecture, structure, conventions, testing, integrations, concerns
- Use before `/cat:new-project` on existing codebases

Usage: `/cat:map-codebase`

### Release Planning

**`/cat:discuss-release <number>`**
Help articulate your vision for a release before planning.

- Captures how you imagine this release working
- Creates CONTEXT.md with your vision, essentials, and boundaries
- Use when you have ideas about how something should look/feel

Usage: `/cat:discuss-release 2`

**`/cat:research-release <number>`**
Comprehensive ecosystem research for niche/complex domains.

- Discovers standard stack, architecture patterns, pitfalls
- Creates RESEARCH.md with "how experts build this" knowledge
- Use for 3D, games, audio, shaders, ML, and other specialized domains
- Goes beyond "which library" to ecosystem knowledge

Usage: `/cat:research-release 3`

**`/cat:list-release-assumptions <number>`**
See what Claude is planning to do before it starts.

- Shows Claude's intended approach for a release
- Lets you course-correct if Claude misunderstood your vision
- No files created - conversational output only

Usage: `/cat:list-release-assumptions 3`

**`/cat:change-release <number>`**
Create detailed execution change for a specific release.

- Generates `.planning/releases/XX-release-name/XX-YY-CHANGE.md`
- Breaks release into concrete, actionable tasks
- Includes verification criteria and success measures
- Multiple changes per release supported (XX-01, XX-02, etc.)

Usage: `/cat:change-release 1`
Result: Creates `.planning/releases/01-foundation/01-01-setup-project-CHANGE.md`

### Execution

**`/cat:execute-change <path>`**
Execute a CHANGE.md file directly.

- Runs change tasks sequentially
- Creates SUMMARY.md after completion
- Updates STATE.md with accumulated context
- Fast execution without loading full skill context

Usage: `/cat:execute-change .planning/releases/01-foundation/01-01-setup-project-CHANGE.md`

### Roadmap Management

**`/cat:add-release <description>`**
Add new release to end of current milestone.

- Appends to ROADMAP.md
- Uses next sequential number
- Updates release directory structure

Usage: `/cat:add-release "Add admin dashboard"`

**`/cat:insert-release <after> <description>`**
Insert urgent work as decimal release between existing releases.

- Creates intermediate release (e.g., 7.1 between 7 and 8)
- Useful for discovered work that must happen mid-milestone
- Maintains release ordering

Usage: `/cat:insert-release 7 "Fix critical auth bug"`
Result: Creates Release 7.1

### Milestone Management

**`/cat:discuss-milestone`**
Figure out what you want to build in the next milestone.

- Reviews what shipped in previous milestone
- Helps you identify features to add, improve, or fix
- Routes to /cat:new-milestone when ready

Usage: `/cat:discuss-milestone`

**`/cat:new-milestone <name>`**
Create a new milestone with releases for an existing project.

- Adds milestone section to ROADMAP.md
- Creates release directories
- Updates STATE.md for new milestone

Usage: `/cat:new-milestone "v2.0 Features"`

**`/cat:complete-milestone <version>`**
Archive completed milestone and prepare for next version.

- Creates MILESTONES.md entry with stats
- Archives full details to milestones/ directory
- Creates git tag for the release
- Prepares workspace for next version

Usage: `/cat:complete-milestone 1.0.0`

### Progress Tracking

**`/cat:progress`**
Check project status and intelligently route to next action.

- Shows visual progress bar and completion percentage
- Summarizes recent work from SUMMARY files
- Displays current position and what's next
- Lists key decisions and open issues
- Offers to execute next change or create it if missing
- Detects 100% milestone completion

Usage: `/cat:progress`

### Session Management

**`/cat:resume-work`**
Resume work from previous session with full context restoration.

- Reads STATE.md for project context
- Shows current position and recent progress
- Offers next actions based on project state

Usage: `/cat:resume-work`

**`/cat:pause-work`**
Create context handoff when pausing work mid-release.

- Creates .continue-here file with current state
- Updates STATE.md session continuity section
- Captures in-progress work context

Usage: `/cat:pause-work`

### Issue Management

**`/cat:consider-issues`**
Review deferred issues with codebase context.

- Analyzes all open issues against current codebase state
- Identifies resolved issues (can close)
- Identifies urgent issues (should address now)
- Identifies natural fits for upcoming releases
- Offers batch actions (close, insert release, note for planning)

Usage: `/cat:consider-issues`

### Utility Commands

**`/cat:help`**
Show this command reference.

## Files & Structure

```
.planning/
├── PROJECT.md            # Project vision
├── ROADMAP.md            # Current release breakdown
├── STATE.md              # Project memory & context
├── ISSUES.md             # Deferred enhancements (created when needed)
├── config.json           # Workflow mode & gates
├── codebase/             # Codebase map (brownfield projects)
│   ├── STACK.md          # Languages, frameworks, dependencies
│   ├── ARCHITECTURE.md   # Patterns, layers, data flow
│   ├── STRUCTURE.md      # Directory layout, key files
│   ├── CONVENTIONS.md    # Coding standards, naming
│   ├── TESTING.md        # Test setup, patterns
│   ├── INTEGRATIONS.md   # External services, APIs
│   └── CONCERNS.md       # Tech debt, known issues
└── releases/
    ├── 01-foundation/
    │   ├── 01-01-setup-project-CHANGE.md
    │   └── 01-01-setup-project-SUMMARY.md
    └── 02-core-features/
        ├── 02-01-add-api-routes-CHANGE.md
        └── 02-01-add-api-routes-SUMMARY.md
```

## Workflow Modes

Set during `/cat:new-project`:

**Interactive Mode**

- Confirms each major decision
- Pauses at checkpoints for approval
- More guidance throughout

**YOLO Mode**

- Auto-approves most decisions
- Executes changes without confirmation
- Only stops for critical checkpoints

Change anytime by editing `.planning/config.json`

## Common Workflows

**Starting a new project:**

```
/cat:new-project
/cat:create-roadmap
/cat:change-release 1
/cat:execute-change .planning/releases/01-foundation/01-01-setup-project-CHANGE.md
```

**Resuming work after a break:**

```
/cat:progress  # See where you left off and continue
```

**Adding urgent mid-milestone work:**

```
/cat:insert-release 5 "Critical security fix"
/cat:change-release 5.1
/cat:execute-change .planning/releases/05.1-critical-security-fix/05.1-01-fix-auth-vuln-CHANGE.md
```

**Completing a milestone:**

```
/cat:complete-milestone 1.0.0
/cat:new-project  # Start next milestone
```

## Getting Help

- Read `.planning/PROJECT.md` for project vision
- Read `.planning/STATE.md` for current context
- Check `.planning/ROADMAP.md` for release status
- Run `/cat:progress` to check where you're up to
  </reference>
