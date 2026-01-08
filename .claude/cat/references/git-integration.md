<overview>
Git integration for CAT framework.
</overview>

<core_principle>

**Commit outcomes, not process.**

The git log should read like a changelog of what shipped, not a diary of planning activity.
</core_principle>

<commit_points>

| Event                   | Commit? | Why                                              |
| ----------------------- | ------- | ------------------------------------------------ |
| BRIEF + ROADMAP created | YES     | Project initialization                           |
| CHANGE.md created         | NO      | Intermediate - commit with first task            |
| RESEARCH.md created     | NO      | Intermediate                                     |
| DISCOVERY.md created    | NO      | Intermediate                                     |
| **Task completed**      | YES     | Implementation + planning metadata (1 per task)  |
| Handoff created         | YES     | WIP state preserved                              |

</commit_points>

<git_check>

```bash
[ -d .git ] && echo "GIT_EXISTS" || echo "NO_GIT"
```

If NO_GIT: Run `git init` silently. CAT projects always get their own repo.
</git_check>

<commit_formats>

<format name="initialization">
## Project Initialization (brief + roadmap together)

```
docs: initialize [project-name] ([N] releases)

[One-liner from PROJECT.md]

Releases:
1. [release-name]: [goal]
2. [release-name]: [goal]
3. [release-name]: [goal]
```

What to commit:

```bash
git add .planning/
git commit
```

</format>

<format name="task-completion">
## Task Completion (During Change Execution)

Each task gets its own commit with implementation + planning metadata.

```
{type}: {task-name}

- [Key change 1]
- [Key change 2]
- [Key change 3]
```

**Commit types:**
- `feature` - New feature/functionality
- `bugfix` - Bug fix
- `test` - Test-only (TDD RED release)
- `refactor` - Code cleanup (TDD REFACTOR release)
- `performance` - Performance improvement
- `config` - Dependencies, config, tooling

**What to commit per task:**

```bash
# Implementation files
git add src/api/auth.ts src/types/user.ts

# Planning metadata (always include STATE.md)
git add .planning/STATE.md

# For final task only: include SUMMARY.md and ROADMAP.md
git add .planning/releases/XX-name/{release}-{change}-SUMMARY.md
git add .planning/ROADMAP.md

git commit -m "feature: create user registration endpoint

- POST /auth/register validates email and password
- Checks for duplicate users
- Returns JWT token on success
"
```

**Examples:**

```bash
# Standard task (not final)
git add src/api/auth.ts src/types/user.ts .planning/STATE.md
git commit -m "feature: create user registration endpoint

- POST /auth/register validates email and password
- Checks for duplicate users
- Returns JWT token on success
"

# TDD task - RED release
git add src/__tests__/jwt.test.ts .planning/STATE.md
git commit -m "test: add failing test for JWT generation

- Tests token contains user ID claim
- Tests token expires in 1 hour
- Tests signature verification
"

# Final task (includes SUMMARY + ROADMAP)
git add src/utils/jwt.ts .planning/STATE.md .planning/releases/07-auth/07-02-SUMMARY.md .planning/ROADMAP.md
git commit -m "feature: implement JWT generation

- Uses jose library for signing
- Includes user ID and expiry claims
- Signs with HS256 algorithm
"
```

</format>


<format name="handoff">
## Handoff (WIP)

```
wip: [release-name] paused at task [X]/[Y]

Current: [task name]
[If blocked:] Blocked: [reason]
```

What to commit:

```bash
git add .planning/
git commit
```

</format>
</commit_formats>

<example_log>

**Old approach (per-change commits):**
```
a7f2d1 feature(checkout): Stripe payments with webhook verification
3e9c4b feature(products): catalog with search, filters, and pagination
8a1b2c feature(auth): JWT with refresh rotation using jose
5c3d7e feature(foundation): Next.js 15 + Prisma + Tailwind scaffold
2f4a8d docs: initialize ecommerce-app (5 releases)
```

**New approach (per-task commits with unified metadata):**
```
# Release 04 - Checkout
4d5e6f feature: add webhook signature verification
7g8h9i feature: implement payment session creation
0j1k2l feature: create checkout page component

# Release 03 - Products
6p7q8r feature: add pagination controls
9s0t1u feature: implement search and filters
2v3w4x feature: create product catalog schema

# Release 02 - Auth
8b9c0d feature: implement refresh token rotation
1e2f3g test: add failing test for token refresh
7k8l9m feature: add JWT generation and validation
0n1o2p config: install jose library

# Release 01 - Foundation
6t7u8v feature: configure Tailwind and globals
9w0x1y feature: set up Prisma with database
2z3a4b feature: create Next.js 15 project

# Initialization
5c6d7e docs: initialize ecommerce-app (5 releases)
```

Each task commit includes implementation + planning metadata. Clear, granular, bisectable.

</example_log>

<anti_patterns>

**Still don't commit (intermediate artifacts):**
- CHANGE.md creation (commit with change completion)
- RESEARCH.md (intermediate)
- DISCOVERY.md (intermediate)
- Minor planning tweaks
- "Fixed typo in roadmap"

**Do commit (outcomes):**
- Each task completion (implementation + planning metadata)
- Project initialization (docs)

**Key principle:** Commit working code and shipped outcomes, not planning process.

</anti_patterns>

<commit_strategy_rationale>

## Why Per-Task Commits?

**Context engineering for AI:**
- Git history becomes primary context source for future Claude sessions
- `git diff <hash>^..<hash>` shows exact changes per task
- SUMMARY.md records commit hashes for each task
- Less reliance on parsing SUMMARY.md = more context for actual work

**Failure recovery:**
- Task 1 committed ✅, Task 2 failed ❌
- Claude in next session: sees task 1 complete, can retry task 2
- Can `git reset --hard` to last successful task

**Debugging:**
- `git bisect` finds exact failing task, not just failing change
- `git blame` traces line to specific task context
- Each commit is independently revertable

**Observability:**
- Solo developer + Claude workflow benefits from granular attribution
- Atomic commits are git best practice
- "Commit noise" irrelevant when consumer is Claude, not humans

</commit_strategy_rationale>
