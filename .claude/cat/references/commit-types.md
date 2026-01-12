# Commit Types

## Core Principle

**Commit outcomes, not process.**

The git log should read like a changelog of what shipped, not a diary of planning activity.

| Event | Commit? | Why |
|-------|---------|-----|
| PROJECT.md + ROADMAP.md created | YES | Project initialization |
| PLAN.md created | NO | Intermediate - commit with first task |
| RESEARCH.md created | NO | Intermediate artifact |
| **Task completed** | YES | Implementation + planning metadata |
| Handoff created | YES | WIP state preserved |

**Key principle:** Commit working code and shipped outcomes, not planning process.

## Standard Types (MANDATORY)

Use ONLY these types when committing in a CAT-managed project:

| Type | When to Use | Example |
|------|-------------|---------|
| `feature` | New functionality, endpoint, component | `feature: add user registration` |
| `bugfix` | Bug fix, error correction | `bugfix: correct email validation` |
| `test` | Test-only changes | `test: add failing test for hashing` |
| `refactor` | Code cleanup, no behavior change | `refactor: extract validation helper` |
| `performance` | Performance improvement | `performance: add database index` |
| `docs` | User-facing docs (README, API docs) | `docs: add API documentation` |
| `style` | Formatting, linting fixes | `style: format auth module` |
| `config` | Config, tooling, deps, Claude-facing docs | `config: add bcrypt dependency` |
| `planning` | Planning system updates (ROADMAP, STATE) | `planning: add task 5 summary` |

## Format

```
{type}: {description}
```

## Invalid Types

**NOT VALID:** `feat`, `fix`, `chore`, `build`, `ci`, `perf`

These abbreviated forms are NOT in the standard types. Use the full names above.

## Squashing by Type

When squashing commits before merge:
- One commit per type
- Group all `feature` commits into one `feature:` commit
- Group all `bugfix` commits into one `bugfix:` commit
- etc.

## Example Result After Squashing

```
feature: add token tracking to subagent execution
bugfix: resolve merge conflict in parser module
refactor: simplify worktree cleanup logic
test: add unit tests for token tracking
```
