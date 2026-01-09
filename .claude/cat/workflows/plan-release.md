<decimal_phase_numbering>
Decimal releases enable urgent work insertion without renumbering:

- Integer releases (1, 2, 3) = planned milestone work
- Decimal releases (2.1, 2.2) = urgent insertions between integers

**Rules:**
- Decimals between consecutive integers (2.1 between 2 and 3)
- Filesystem sorting works automatically (2 < 2.1 < 2.2 < 3)
- Directory format: `02.1-description/`, Change format: `02.1-01-{slug}-CHANGE.md`

**Validation:** Integer X must exist and be complete, X+1 must exist, decimal X.Y must not exist, Y >= 1
</decimal_phase_numbering>

<design_principles>
**Apply these principles throughout planning:**

**Security by Design:**
> Assume hostile input on every boundary. Validate, parameterize, authenticate, fail closed.

Plan tasks to incorporate input validation at interfaces, use parameterized approaches to prevent injection, require authentication checks, and default to rejecting requests rather than permitting unknown states.

**Performance by Design:**
> Assume production load, not demo conditions. Plan for efficient data access, appropriate caching, minimal round trips.

Plan for scale from the start—designing data access patterns for efficiency, implementing caching strategically, and minimizing unnecessary interactions rather than optimizing after deployment.

**Observable by Design:**
> Plan to debug your own work. Include meaningful error messages, appropriate logging, and clear failure states.

Each task should produce work that's traceable through clear error reporting, sufficient logging for diagnosis, and explicit handling of failure modes rather than silent degradation.
</design_principles>

<required_reading>
**Read these files NOW:**

1. ~/.claude/cat/templates/release-prompt.md
2. ~/.claude/cat/references/change-format.md
3. ~/.claude/cat/references/scope-estimation.md
4. ~/.claude/cat/references/checkpoints.md
5. ~/.claude/cat/references/tdd.md
6. .planning/ROADMAP.md
7. .planning/PROJECT.md

**Load domain expertise from ROADMAP:**
- Parse ROADMAP.md's `## Domain Expertise` section for paths
- Read each domain SKILL.md (these serve as indexes)
- Determine release type and load ONLY references relevant to THIS release type from each SKILL.md's `<references_index>`
</required_reading>

<purpose>
Create an executable release prompt (CHANGE.md). CHANGE.md IS the prompt that Claude executes - not a document that gets transformed.
</purpose>

<process>

<step name="load_project_state" priority="first">
Read `.planning/STATE.md` and parse:
- Current position (which release we're planning)
- Accumulated decisions (constraints on this release)
- Deferred issues (candidates for inclusion)
- Blockers/concerns (things this release may address)
- Brief alignment status

If STATE.md missing but .planning/ exists, offer to reconstruct or continue without.
</step>

<step name="load_codebase_context">
Check for codebase map:

```bash
ls .planning/codebase/*.md 2>/dev/null
```

**If .planning/codebase/ exists:** Load relevant documents based on release type:

| Release Keywords | Load These |
|----------------|------------|
| UI, frontend, components | CONVENTIONS.md, STRUCTURE.md |
| API, backend, endpoints | ARCHITECTURE.md, CONVENTIONS.md |
| database, schema, models | ARCHITECTURE.md, STACK.md |
| testing, tests | TESTING.md, CONVENTIONS.md |
| integration, external API | INTEGRATIONS.md, STACK.md |
| refactor, cleanup | CONCERNS.md, ARCHITECTURE.md |
| setup, config | STACK.md, STRUCTURE.md |
| (default) | STACK.md, ARCHITECTURE.md |

Track extracted constraints for CHANGE.md context section.
</step>

<step name="identify_phase">
Check roadmap and existing releases:

```bash
cat .planning/ROADMAP.md
ls .planning/releases/
```

If multiple releases available, ask which one to change. If obvious (first incomplete release), proceed.

**Release number parsing:** Regex `^(\d+)(?:\.(\d+))?$` - Group 1: integer, Group 2: decimal (optional)

**If decimal release:** Validate integer X exists and is complete, X+1 exists in roadmap, decimal X.Y doesn't exist, Y >= 1.

Read any existing CHANGE.md or DISCOVERY.md in the release directory.
</step>

<step name="mandatory_discovery">
**Discovery is MANDATORY unless you can prove current context exists.**

<discovery_decision>
**Level 0 - Skip** (pure internal work, existing patterns only)
- ALL work follows established codebase patterns (grep confirms)
- No new external dependencies
- Pure internal refactoring or feature extension
- Examples: Add delete button, add field to model, create CRUD endpoint

**Level 1 - Quick Verification** (2-5 min)
- Single known library, confirming syntax/version
- Low-risk decision (easily changed later)
- Action: Context7 resolve-library-id + query-docs, no DISCOVERY.md needed

**Level 2 - Standard Research** (15-30 min)
- Choosing between 2-3 options
- New external integration (API, service)
- Medium-risk decision
- Action: Route to workflows/discovery-release.md depth=standard, produces DISCOVERY.md

**Level 3 - Deep Dive** (1+ hour)
- Architectural decision with long-term impact
- Novel problem without clear patterns
- High-risk, hard to change later
- Action: Route to workflows/discovery-release.md depth=deep, full DISCOVERY.md

**Depth indicators:**
- Level 2+: New library not in package.json, external API, "choose/select/evaluate" in description, roadmap marked Research: Yes
- Level 3: "architecture/design/system", multiple external services, data modeling, auth design, real-time/distributed
</discovery_decision>

If roadmap flagged `Research: Likely`, Level 0 (skip) is not available.

For niche domains (3D, games, audio, shaders, ML), suggest `/cat:research-release` before change-release.
</step>

<step name="read_project_history">
**Intelligent context assembly from frontmatter dependency graph:**

**1. Scan all summary frontmatter (cheap - first ~25 lines):**

```bash
for f in .planning/releases/*/*-SUMMARY.md; do
  # Extract frontmatter only (between first two --- markers)
  sed -n '1,/^---$/p; /^---$/q' "$f" | head -30
done
```

Parse YAML to extract: release, subsystem, requires, provides, affects, tags, key-decisions, key-files

**2. Build dependency graph for current release:**

- **Check affects field:** Which prior releases have current release in their `affects` list? → Direct dependencies
- **Check subsystem:** Which prior releases share same subsystem? → Related work
- **Check requires chains:** If release X requires release Y, and we need X, we also need Y → Transitive dependencies
- **Check roadmap:** Any releases marked as dependencies in ROADMAP.md release description?

**3. Select relevant summaries:**

Auto-select releases that match ANY of:
- Current release name/number appears in prior release's `affects` field
- Same `subsystem` value
- In `requires` chain (transitive closure)
- Explicitly mentioned in STATE.md decisions as affecting current release

Typical selection: 2-4 prior releases (immediately prior + related subsystem work)

**4. Extract context from frontmatter (WITHOUT opening full summaries yet):**

From selected releases' frontmatter, extract:
- **Tech available:** Union of all tech-stack.added lists
- **Patterns established:** Union of all tech-stack.patterns and patterns-established
- **Key files:** Union of all key-files (for @context references)
- **Decisions:** Extract key-decisions from frontmatter

**5. Now read FULL summaries for selected releases:**

Only now open and read complete SUMMARY.md files for the selected relevant releases. Extract:
- Detailed "Accomplishments" section
- "Next Release Readiness" warnings/blockers
- "Issues Encountered" that might affect current release
- "Deviations from Change" for patterns

**From STATE.md:** Decisions → constrain approach. Deferred issues → candidates. Blockers → may need to address.

**From ISSUES.md:**

```bash
cat .planning/ISSUES.md 2>/dev/null
```

Assess each open issue - relevant to this release? Waiting long enough? Natural to address now? Blocking something?

**Answer before proceeding:**
- Q1: What decisions from previous releases constrain this release?
- Q2: Are there deferred issues that should become tasks?
- Q3: Are there concerns from "Next Release Readiness" that apply?
- Q4: Given all context, does the roadmap's description still make sense?

**Track for CHANGE.md context section:**
- Which summaries were selected (for @context references)
- Tech stack available (from frontmatter)
- Established patterns (from frontmatter)
- Key files to reference (from frontmatter)
- Applicable decisions (from frontmatter + full summary)
- Issues being addressed (from ISSUES.md)
- Concerns being verified (from "Next Release Readiness")
</step>

<step name="gather_phase_context">
Understand:
- Release goal (from roadmap)
- What exists already (scan codebase if mid-project)
- Dependencies met (previous releases complete?)
- Any {release}-RESEARCH.md (from /cat:research-release)
- Any DISCOVERY.md (from mandatory discovery)
- Any {release}-CONTEXT.md (from /cat:discuss-release)

```bash
# If mid-project, understand current state
ls -la src/ 2>/dev/null
cat package.json 2>/dev/null | head -20

# Check for ecosystem research (from /cat:research-release)
cat .planning/releases/XX-name/${RELEASE}-RESEARCH.md 2>/dev/null

# Check for release context (from /cat:discuss-release)
cat .planning/releases/XX-name/${RELEASE}-CONTEXT.md 2>/dev/null
```

**If RESEARCH.md exists:** Use standard_stack (these libraries), architecture_patterns (follow in task structure), dont_hand_roll (NEVER custom solutions for listed problems), common_pitfalls (inform verification), code_examples (reference in actions).

**If CONTEXT.md exists:** Honor vision, prioritize essential, respect boundaries, incorporate specifics.

**If neither exist:** Suggest /cat:research-release for niche domains, /cat:discuss-release for simpler domains, or proceed with roadmap only.
</step>

<step name="break_into_tasks">
Decompose release into tasks and identify TDD candidates.

**Standard tasks need:**
- **Type**: auto, checkpoint:human-verify, checkpoint:decision (human-action rarely needed)
- **Task name**: Clear, action-oriented
- **Files**: Which files created/modified (for auto tasks)
- **Action**: Specific implementation (including what to avoid and WHY)
- **Verify**: How to prove it worked
- **Done**: Acceptance criteria

**TDD detection:** For each potential task, evaluate TDD fit:

TDD candidates (create dedicated TDD changes):
- Business logic with defined inputs/outputs
- API endpoints with request/response contracts
- Data transformations, parsing, formatting
- Validation rules and constraints
- Algorithms with testable behavior
- State machines and workflows

Standard tasks (remain in standard changes):
- UI layout, styling, visual components
- Configuration changes
- Glue code connecting existing components
- One-off scripts and migrations
- Simple CRUD with no business logic

**Heuristic:** Can you write `expect(fn(input)).toBe(output)` before writing `fn`?
→ Yes: Create a dedicated TDD change for this feature (one feature per TDD change)
→ No: Standard task in standard change

**Why TDD gets its own change:** TDD requires 2-3 execution cycles (RED → GREEN → REFACTOR), each with file reads, test runs, and potential debugging. Embedded in a multi-task change, TDD work consumes 50-60% of context alone, degrading quality for remaining tasks.

**Test framework:** If project has no test setup and TDD changes are needed, the first TDD change's RED release handles framework setup as part of writing the first test.

See `~/.claude/cat/references/tdd.md` for TDD change structure.

**Checkpoints:** Visual/functional verification → checkpoint:human-verify. Implementation choices → checkpoint:decision. Manual action (email, 2FA) → checkpoint:human-action (rare).

**Critical:** If external resource has CLI/API (Vercel, Stripe, etc.), use type="auto" to automate. Only checkpoint for verification AFTER automation.

See ~/.claude/cat/references/checkpoints.md for checkpoint structure.
</step>

<step name="estimate_scope">
After tasks, assess against quality degradation curve.

**Check depth setting:**
```bash
cat .planning/config.json 2>/dev/null | grep depth
```

<depth_aware_splitting>
**Depth controls compression tolerance, not artificial inflation.**

| Depth | Typical Changes/Release | Tasks/Change |
|-------|---------------------|------------|
| Quick | 1-3 | 2-3 |
| Standard | 3-5 | 2-3 |
| Comprehensive | 5-10 | 2-3 |

**Key principle:** Derive changes from actual work. Depth determines how aggressively you combine things, not a target to hit.

- Comprehensive auth release = 8 changes (because auth genuinely has 8 concerns)
- Comprehensive "add config file" release = 1 change (because that's all it is)

For comprehensive depth:
- Create MORE changes when the work warrants it, not bigger ones
- If a release has 15 tasks, that's 5-8 changes (not 3 changes with 5 tasks each)
- Don't compress to look efficient—thoroughness is the goal
- Let small releases stay small—don't pad to hit a number
- Each change stays focused: 2-3 tasks, single concern

For quick depth:
- Combine aggressively into fewer changes
- 1-3 changes per release is fine
- Focus on critical path
</depth_aware_splitting>

**ALWAYS split if:** >3 tasks, multiple subsystems, >5 files in any task, complex domains (auth, payments).

**If scope appropriate (2-3 tasks, single subsystem, <5 files/task):** Proceed to confirm_breakdown.

**If large (>3 tasks):** Split by subsystem, dependency, complexity, or autonomous vs interactive.

**Each change must be:** 2-3 tasks max, ~50% context target, independently committable.

**Autonomous optimization:** No checkpoints → subagent (fresh context). Has checkpoints → main context. Group autonomous work together.

See ~/.claude/cat/references/scope-estimation.md for complete guidance.
</step>

<step name="confirm_breakdown">
**Generate change slugs from objectives:**

For each change, generate a slug from its objective (what the change accomplishes):
```bash
# Generate slug: lowercase, replace non-alphanumeric with hyphens, collapse, trim, max 30 chars
slug=$(echo "$objective" | tr '[:upper:]' '[:lower:]' | sed 's/[^a-z0-9]/-/g' | sed 's/--*/-/g' | sed 's/^-//;s/-$//' | cut -c1-30)
```

**Validate uniqueness within release:**
```bash
# Check if slug already exists in this release directory
if ls .planning/releases/${RELEASE}-*/*-${slug}-CHANGE.md 2>/dev/null | grep -q .; then
  echo "Slug '${slug}' already exists in this release. Choose a different name."
fi
```

If collision detected: prompt user to provide an alternative slug.

<if mode="yolo">
Auto-generate slugs from change objectives and proceed to write_phase_prompt.
</if>

<if mode="interactive">
Present breakdown inline with generated slugs:

```
Release [X] breakdown:

### Change 01: [objective summary]
Filename: {release}-01-{slug}-CHANGE.md
1. [Task] - [brief] [type]
2. [Task] - [brief] [type]

Autonomous: [yes/no]

Does this look right? (yes / adjust slug / adjust tasks / start over)
```

For multiple changes, show each change with its slug and tasks.

Wait for confirmation. If "adjust slug": prompt for alternative. If "adjust tasks": revise tasks. If "start over": return to gather_phase_context.
</if>
</step>

<step name="write_phase_prompt">
Use template from `~/.claude/cat/templates/release-prompt.md`.

**Single change:** Write to `.planning/releases/XX-name/{release}-01-{slug}-CHANGE.md`

**Multiple changes:** Write separate files (`{release}-01-{slug1}-CHANGE.md`, `{release}-02-{slug2}-CHANGE.md`, etc.)

Each change follows template structure with:
- Frontmatter (release, change, type, domain)
- Objective (change-specific goal, purpose, output)
- Execution context (execute-release.md, summary template, checkpoints.md if needed)
- Context (@references to PROJECT, ROADMAP, STATE, codebase docs, RESEARCH/DISCOVERY/CONTEXT if exist, prior summaries, source files, prior decisions, deferred issues, concerns)
- Tasks (XML format with types)
- Verification, Success criteria, Output specification

**Context section population from frontmatter analysis:**

Inject automatically-assembled context package from read_project_history step:

```markdown
<context>
@.planning/PROJECT.md
@.planning/ROADMAP.md
@.planning/STATE.md

# Auto-selected based on dependency graph (from frontmatter):
@.planning/releases/XX-name/YY-ZZ-SUMMARY.md
@.planning/releases/AA-name/BB-CC-SUMMARY.md

# Key files from frontmatter (relevant to this release):
@path/to/important/file.ts
@path/to/another/file.ts

**Tech stack available:** [extracted from frontmatter tech-stack.added]
**Established patterns:** [extracted from frontmatter patterns-established]
**Constraining decisions:**
- [Release X]: [decision from frontmatter]
- [Release Y]: [decision from frontmatter]

**Issues being addressed:** [If any from ISSUES.md]
</context>
```

This ensures every CHANGE.md gets optimal context automatically assembled via dependency graph, making execution as informed as possible.

For multi-change releases: each change has focused scope, references previous change summaries (via frontmatter selection), last change's success criteria includes "Release X complete".
</step>

<step name="update_markers">
Update STATUS markers to reflect new changes:

**1. Update ROADMAP.md task count:**

Find the release line and update the task count:
```bash
# Count CHANGE files in the release directory
CHANGE_COUNT=$(ls .planning/releases/${RELEASE}-*/*-CHANGE.md 2>/dev/null | wc -l)

# Update ROADMAP.md: "Release X: Name (N tasks)" → "Release X: Name (${CHANGE_COUNT} tasks)"
```

**2. Update STATE.md release status (if needed):**

If the release was marked "Complete" but now has new incomplete changes:
- Change `✅ Complete` → `🔄 In Progress`
- Update task count column

```bash
# Check if release is marked Complete in STATE.md
if grep -q "| ${RELEASE} |.*Complete" .planning/STATE.md; then
    # Re-open: Change Complete → In Progress
    # Update task count
fi
```

**3. Stage marker files:**
```bash
git add .planning/ROADMAP.md .planning/STATE.md
```

This ensures STATUS markers stay synchronized with actual CHANGE files.
</step>

<step name="git_commit">
Commit release change(s):

```bash
# Stage all CHANGE.md files for this release
git add .planning/releases/${RELEASE}-*/${RELEASE}-*-CHANGE.md

# Also stage DISCOVERY.md if it was created during mandatory_discovery
git add .planning/releases/${RELEASE}-*/DISCOVERY.md 2>/dev/null

git commit -m "$(cat <<'EOF'
docs(${RELEASE}): create release change

Release ${RELEASE}: ${RELEASE_NAME}
- [N] change(s) created
- [X] total tasks defined
- Ready for execution
EOF
)"
```

Confirm: "Committed: docs(${RELEASE}): create release change"
</step>

<step name="offer_next">
```
Release change created: .planning/releases/XX-name/{release}-01-{slug}-CHANGE.md
[X] tasks defined.

---

## Next Up

**{release}-01-{slug}: [Change Name]** - [objective summary]

`/cat:execute-change .planning/releases/XX-name/{release}-01-{slug}-CHANGE.md`

<sub>`/clear` first - fresh context window</sub>

---

**Also available:**
- Review/adjust tasks before executing
[If multiple changes: - View all changes: `ls .planning/releases/XX-name/*-CHANGE.md`]

---
```
</step>

</process>

<task_quality>
**Good tasks:** Specific files, actions, verification
- "Add User model to Prisma schema with email, passwordHash, createdAt"
- "Create POST /api/auth/login endpoint with bcrypt validation"

**Bad tasks:** Vague, not actionable
- "Set up authentication" / "Make it secure" / "Handle edge cases"

If you can't specify Files + Action + Verify + Done, the task is too vague.

**TDD candidates get dedicated changes.** If "Create price calculator with discount rules" warrants TDD, create a TDD change for it. See `~/.claude/cat/references/tdd.md` for TDD change structure.
</task_quality>

<anti_patterns>
- No story points or hour estimates
- No team assignments
- No acceptance criteria committees
- No sub-sub-sub tasks
Tasks are instructions for Claude, not Jira tickets.
</anti_patterns>

<success_criteria>
Release planning complete when:
- [ ] STATE.md read, project history absorbed
- [ ] Mandatory discovery completed (Level 0-3)
- [ ] Prior decisions, issues, concerns synthesized
- [ ] CHANGE file(s) exist with XML structure
- [ ] Each change: Objective, context, tasks, verification, success criteria, output
- [ ] @context references included (STATE, RESEARCH/DISCOVERY if exist, relevant summaries)
- [ ] Each change: 2-3 tasks (~50% context)
- [ ] Each task: Type, Files (if auto), Action, Verify, Done
- [ ] Checkpoints properly structured
- [ ] If RESEARCH.md exists: "don't hand-roll" items NOT being custom-built
- [ ] CHANGE file(s) committed to git
- [ ] User knows next steps
</success_criteria>
