<scope_estimation>
Changes must maintain consistent quality from first task to last. This requires understanding quality degradation and splitting aggressively.

<quality_insight>
Claude degrades when it *perceives* context pressure and enters "completion mode."

| Context Usage | Quality | Claude's State |
|---------------|---------|----------------|
| 0-30% | PEAK | Thorough, comprehensive |
| 30-50% | GOOD | Confident, solid work |
| 50-70% | DEGRADING | Efficiency mode begins |
| 70%+ | POOR | Rushed, minimal |

**The 40-50% inflection point:** Claude sees context mounting and thinks "I'd better conserve now." Result: "I'll complete the remaining tasks more concisely" = quality crash.

**The rule:** Stop BEFORE quality degrades, not at context limit.
</quality_insight>

<context_target>
**Changes should complete within ~50% of context usage.**

Why 50% not 80%?
- No context anxiety possible
- Quality maintained start to finish
- Room for unexpected complexity
- If you target 80%, you've already spent 40% in degradation mode
</context_target>

<task_rule>
**Each change: 2-3 tasks maximum. Stay under 50% context.**

| Task Complexity | Tasks/Change | Context/Task | Total |
|-----------------|------------|--------------|-------|
| Simple (CRUD, config) | 3 | ~10-15% | ~30-45% |
| Complex (auth, payments) | 2 | ~20-30% | ~40-50% |
| Very complex (migrations, refactors) | 1-2 | ~30-40% | ~30-50% |

**When in doubt: Default to 2 tasks.** Better to have an extra change than degraded quality.
</task_rule>

<tdd_plans>
**TDD features get their own changes. Target ~40% context.**

TDD requires 2-3 execution cycles (RED → GREEN → REFACTOR), each with file reads, test runs, and potential debugging. This is fundamentally heavier than linear task execution.

| TDD Feature Complexity | Context Usage |
|------------------------|---------------|
| Simple utility function | ~25-30% |
| Business logic with edge cases | ~35-40% |
| Complex algorithm | ~40-50% |

**One feature per TDD change.** If features are trivial enough to batch, they're trivial enough to skip TDD.

**Why TDD changes are separate:**
- TDD consumes 40-50% context for a single feature
- Dedicated changes ensure full quality throughout RED-GREEN-REFACTOR
- Each TDD feature gets fresh context, peak quality

See `~/.claude/cat/references/tdd.md` for TDD change structure.
</tdd_plans>

<split_signals>

<always_split>
- **More than 3 tasks** - Even if tasks seem small
- **Multiple subsystems** - DB + API + UI = separate changes
- **Any task with >5 file modifications** - Split by file groups
- **Checkpoint + implementation work** - Checkpoints in one change, implementation after in separate change
- **Discovery + implementation** - DISCOVERY.md in one change, implementation in another
</always_split>

<consider_splitting>
- Estimated >5 files modified total
- Complex domains (auth, payments, data modeling)
- Any uncertainty about approach
- Natural semantic boundaries (Setup -> Core -> Features)
</consider_splitting>
</split_signals>

<splitting_strategies>
**By subsystem:** Auth → 01: DB models, 02: API routes, 03: Protected routes, 04: UI components

**By dependency:** Payments → 01: Stripe setup, 02: Subscription logic, 03: Frontend integration

**By complexity:** Dashboard → 01: Layout shell, 02: Data fetching, 03: Visualization

**By verification:** Deploy → 01: Vercel setup (checkpoint), 02: Env config (auto), 03: CI/CD (checkpoint)
</splitting_strategies>

<anti_patterns>
**Bad - Comprehensive change:**
```
Change: "Complete Authentication System"
Tasks: 8 (models, migrations, API, JWT, middleware, hashing, login form, register form)
Result: Task 1-3 good, Task 4-5 degrading, Task 6-8 rushed
```

**Good - Atomic changes:**
```
Change 1: "Auth Database Models" (2 tasks)
Change 2: "Auth API Core" (3 tasks)
Change 3: "Auth API Protection" (2 tasks)
Change 4: "Auth UI Components" (2 tasks)
Each: 30-40% context, peak quality, atomic commits (2-3 task commits + 1 metadata commit)
```
</anti_patterns>

<estimating_context>
| Files Modified | Context Impact |
|----------------|----------------|
| 0-3 files | ~10-15% (small) |
| 4-6 files | ~20-30% (medium) |
| 7+ files | ~40%+ (large - split) |

| Complexity | Context/Task |
|------------|--------------|
| Simple CRUD | ~15% |
| Business logic | ~25% |
| Complex algorithms | ~40% |
| Domain modeling | ~35% |

**2 tasks:** Simple ~30%, Medium ~50%, Complex ~80% (split)
**3 tasks:** Simple ~45%, Medium ~75% (risky), Complex 120% (impossible)
</estimating_context>

<depth_calibration>
**Depth controls compression tolerance, not artificial inflation.**

| Depth | Typical Releases | Typical Changes/Release | Tasks/Change |
|-------|----------------|---------------------|------------|
| Quick | 3-5 | 1-3 | 2-3 |
| Standard | 5-8 | 3-5 | 2-3 |
| Comprehensive | 8-12 | 5-10 | 2-3 |

Tasks/change is CONSTANT at 2-3. The 50% context rule applies universally.

**Key principle:** Derive from actual work. Depth determines how aggressively you combine things, not a target to hit.

- Comprehensive auth = 8 changes (because auth genuinely has 8 concerns)
- Comprehensive "add favicon" = 1 change (because that's all it is)

Don't pad small work to hit a number. Don't compress complex work to look efficient.

**Comprehensive depth example:**
Auth system at comprehensive depth = 8 changes (not 3 big ones):
- 01: DB models (2 tasks)
- 02: Password hashing (2 tasks)
- 03: JWT generation (2 tasks)
- 04: JWT validation middleware (2 tasks)
- 05: Login endpoint (2 tasks)
- 06: Register endpoint (2 tasks)
- 07: Protected route patterns (2 tasks)
- 08: Auth UI components (3 tasks)

Each change: fresh context, peak quality. More changes = more thoroughness, same quality per change.
</depth_calibration>

<summary>
**2-3 tasks, 50% context target:**
- All tasks: Peak quality
- Git: Atomic per-task commits (each task = 1 commit, change = 1 metadata commit)
- Autonomous changes: Subagent execution (fresh context)

**The principle:** Aggressive atomicity. More changes, smaller scope, consistent quality.

**The rule:** If in doubt, split. Quality over consolidation. Always.

**Depth rule:** Depth increases change COUNT, never change SIZE.

**Commit rule:** Each change produces 3-4 commits total (2-3 task commits + 1 docs commit). More granular history = better observability for Claude.
</summary>
</scope_estimation>
