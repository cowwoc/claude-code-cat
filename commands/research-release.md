---
name: cat:research-release
description: Research how to implement a release before planning
argument-hint: "[release]"
allowed-tools:
  - Read
  - Bash
  - Glob
  - Grep
  - Write
  - WebFetch
  - WebSearch
  - mcp__context7__*
---

<objective>
Comprehensive research on HOW to implement a release before planning.

This is for niche/complex domains where Claude's training data is sparse or outdated. Research discovers:
- What libraries exist for this problem
- What architecture patterns experts use
- What the standard stack looks like
- What problems people commonly hit
- What NOT to hand-roll (use existing solutions)

Output: RESEARCH.md with ecosystem knowledge that informs quality planning.
</objective>

<execution_context>
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/workflows/research-release.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/templates/research.md
@${CLAUDE_PLUGIN_ROOT}/.claude/cat/references/research-pitfalls.md
</execution_context>

<context>
Release number: $ARGUMENTS (required)

**Load project state:**
@.planning/STATE.md

**Load roadmap:**
@.planning/ROADMAP.md

**Load release context if exists:**
Check for `.planning/releases/XX-name/{release}-CONTEXT.md` - bonus context from discuss-release.
</context>

<process>
1. Validate release number argument (error if missing or invalid)
2. Check if release exists in roadmap - extract release description
3. Check if RESEARCH.md already exists (offer to update or use existing)
4. Load CONTEXT.md if it exists (bonus context for research direction)
5. Follow research-release.md workflow:
   - Analyze release to identify knowledge gaps
   - Determine research domains (architecture, ecosystem, patterns, pitfalls)
   - Execute comprehensive research via Context7, official docs, WebSearch
   - Cross-verify all findings
   - Create RESEARCH.md with actionable ecosystem knowledge
6. Offer next steps (change the release)
</process>

<when_to_use>
**Use research-release for:**
- 3D graphics (Three.js, WebGL, procedural generation)
- Game development (physics, collision, AI, procedural content)
- Audio/music (Web Audio API, DSP, synthesis)
- Shaders (GLSL, Metal, ISF)
- ML/AI integration (model serving, inference, pipelines)
- Real-time systems (WebSockets, WebRTC, sync)
- Specialized frameworks with active ecosystems
- Any domain where "how do experts do this" matters

**Skip research-release for:**
- Standard web dev (auth, CRUD, REST APIs)
- Well-known patterns (forms, validation, testing)
- Simple integrations (Stripe, SendGrid with clear docs)
- Commodity features Claude handles well
</when_to_use>

<success_criteria>
- [ ] Release validated against roadmap
- [ ] Domain/ecosystem identified from release description
- [ ] Comprehensive research executed (Context7 + official docs + WebSearch)
- [ ] All WebSearch findings cross-verified with authoritative sources
- [ ] RESEARCH.md created with ecosystem knowledge
- [ ] Standard stack/libraries identified
- [ ] Architecture patterns documented
- [ ] Common pitfalls catalogued
- [ ] What NOT to hand-roll is clear
- [ ] User knows next steps (change release)
</success_criteria>
