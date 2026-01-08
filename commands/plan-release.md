---
name: cat:change-release
description: Create detailed execution change for a release (CHANGE.md)
argument-hint: "[release]"
allowed-tools:
  - Read
  - Bash
  - Write
  - Glob
  - Grep
  - AskUserQuestion
  - WebFetch
  - mcp__context7__*
---

<objective>
Create executable release prompt with discovery, context injection, and task breakdown.

Purpose: Break down roadmap releases into concrete, executable CHANGE.md files that Claude can execute.
Output: One or more CHANGE.md files in the release directory (.planning/releases/XX-name/{release}-{change}-{slug}-CHANGE.md)
</objective>

<execution_context>
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/workflows/change-release.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/templates/release-prompt.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/references/change-format.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/references/scope-estimation.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/references/checkpoints.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/references/tdd.md
</execution_context>

<context>
Release number: $ARGUMENTS (optional - auto-detects next unplanned release if not provided)

**Load project state first:**
@.planning/STATE.md

**Load roadmap:**
@.planning/ROADMAP.md

**Load release context if exists (created by /cat:discuss-release):**
Check for and read `.planning/releases/XX-name/{release}-CONTEXT.md` - contains research findings, clarifications, and decisions from release discussion.

**Load codebase context if exists:**
Check for `.planning/codebase/` and load relevant documents based on release type.
</context>

<process>
1. Check .planning/ directory exists (error if not - user should run /cat:new-project)
2. If release number provided via $ARGUMENTS, validate it exists in roadmap
3. If no release number, detect next unplanned release from roadmap
4. Follow change-release.md workflow:
   - Load project state and accumulated decisions
   - Perform mandatory discovery (Level 0-3 as appropriate)
   - Read project history (prior decisions, issues, concerns)
   - Break release into tasks
   - Estimate scope and split into multiple changes if needed
   - Create CHANGE.md file(s) with executable structure
</process>

<success_criteria>

- One or more CHANGE.md files created in .planning/releases/XX-name/
- Each change has: objective, execution_context, context, tasks, verification, success_criteria, output
- Tasks are specific enough for Claude to execute
- User knows next steps (execute change or review/adjust)
  </success_criteria>
