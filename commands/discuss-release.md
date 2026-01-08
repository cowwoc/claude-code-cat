---
name: cat:discuss-release
description: Gather release context through adaptive questioning before planning
argument-hint: "[release] (optional - auto-detects latest incomplete)"
---

<objective>
Help the user articulate their vision for a release through collaborative thinking.

Purpose: Understand HOW the user imagines this release working — what it looks like, what's essential, what's out of scope. You're a thinking partner helping them crystallize their vision, not an interviewer gathering technical requirements.

Output: {release}-CONTEXT.md capturing the user's vision for the release
</objective>

<execution_context>
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/workflows/discuss-release.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/templates/context.md
</execution_context>

<context>
Release number: $ARGUMENTS (optional - auto-detects if omitted)

**Load project state first:**
@.planning/STATE.md

**Load roadmap:**
@.planning/ROADMAP.md
</context>

<process>
1. Determine release number:
   - If $ARGUMENTS provided: use that release number
   - If $ARGUMENTS empty: auto-detect the latest incomplete release
     * Scan releases in order (5, 6, 7...)
     * For each release, count CHANGE.md files vs SUMMARY.md files
     * First release where summaries < changes is the target
     * If all releases complete, inform user and exit
2. Validate release exists in roadmap
3. Check if CONTEXT.md already exists (offer to update if yes)
4. Follow discuss-release.md workflow with **ALL questions using AskUserQuestion**:
   - Present release from roadmap
   - Use AskUserQuestion: "How do you imagine this working?" with interpretation options
   - Use AskUserQuestion to follow their thread — probe what excites them
   - Use AskUserQuestion to sharpen the core — what's essential for THIS release
   - Use AskUserQuestion to find boundaries — what's explicitly out of scope
   - Use AskUserQuestion for decision gate (ready / ask more / let me add context)
   - Create CONTEXT.md capturing their vision
5. Offer next steps (research or change the release)

**CRITICAL: ALL questions use AskUserQuestion. Never ask inline text questions.**

User is the visionary, you are the builder:
- Ask about vision, feel, essential outcomes
- DON'T ask about technical risks (you figure those out)
- DON'T ask about codebase patterns (you read the code)
- DON'T ask about success metrics (too corporate)
- DON'T interrogate about constraints they didn't mention
</process>

<success_criteria>

- Release validated against roadmap
- Vision gathered through collaborative thinking (not interrogation)
- CONTEXT.md captures: how it works, what's essential, what's out of scope
- User knows next steps (research or change the release)
</success_criteria>
