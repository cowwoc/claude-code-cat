---
name: cat:new-milestone
description: Create a new milestone with releases for an existing project
argument-hint: "[milestone name, e.g., 'v2.0 Features']"
---

<objective>
Create a new milestone for an existing project with defined releases.

Purpose: After completing a milestone (or when ready to define next chunk of work), creates the milestone structure in ROADMAP.md with releases, updates STATE.md, and creates release directories.
Output: New milestone in ROADMAP.md, updated STATE.md, release directories created
</objective>

<execution_context>
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/workflows/create-milestone.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/templates/roadmap.md
</execution_context>

<context>
Milestone name: $ARGUMENTS (optional - will prompt if not provided)

**Load project state first:**
@.planning/STATE.md

**Load roadmap:**
@.planning/ROADMAP.md

**Load milestones (if exists):**
@.planning/MILESTONES.md
</context>

<process>
1. Load project context (STATE.md, ROADMAP.md, MILESTONES.md)
2. Calculate next milestone version and starting release number
3. If milestone name provided in arguments, use it; otherwise prompt
4. Gather releases (per depth setting: quick 3-5, standard 5-8, comprehensive 8-12):
   - If called from /cat:discuss-milestone, use provided context
   - Otherwise, prompt for release breakdown
5. Detect research needs for each release
6. Confirm releases (respect config.json gate settings)
7. Follow create-milestone.md workflow:
   - Update ROADMAP.md with new milestone section
   - Create release directories
   - Update STATE.md for new milestone
   - Git commit milestone creation
8. Offer next steps (discuss first release, change first release, review)
</process>

<success_criteria>

- Next release number calculated correctly (continues from previous milestone)
- Releases defined per depth setting (quick: 3-5, standard: 5-8, comprehensive: 8-12)
- Research flags assigned for each release
- ROADMAP.md updated with new milestone section
- Release directories created
- STATE.md reset for new milestone
- Git commit made
- User knows next steps
  </success_criteria>
