---
name: cat:list-release-assumptions
description: Surface Claude's assumptions about a release approach before planning
argument-hint: "[release]"
allowed-tools:
  - Read
  - Bash
  - Grep
  - Glob
---

<objective>
Analyze a release and present Claude's assumptions about technical approach, implementation order, scope boundaries, risk areas, and dependencies.

Purpose: Help users see what Claude thinks BEFORE planning begins - enabling course correction early when assumptions are wrong.
Output: Conversational output only (no file creation) - ends with "What do you think?" prompt
</objective>

<execution_context>
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/workflows/list-release-assumptions.md
</execution_context>

<context>
Release number: $ARGUMENTS (required)

**Load project state first:**
@.planning/STATE.md

**Load roadmap:**
@.planning/ROADMAP.md
</context>

<process>
1. Validate release number argument (error if missing or invalid)
2. Check if release exists in roadmap
3. Follow list-release-assumptions.md workflow:
   - Analyze roadmap description
   - Surface assumptions about: technical approach, implementation order, scope, risks, dependencies
   - Present assumptions clearly
   - Prompt "What do you think?"
4. Gather feedback and offer next steps
</process>

<success_criteria>

- Release validated against roadmap
- Assumptions surfaced across five areas
- User prompted for feedback
- User knows next steps (discuss context, change release, or correct assumptions)
  </success_criteria>
