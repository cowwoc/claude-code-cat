# Continuation Format

Standard format for presenting next steps after completing a command or workflow.

## Core Structure

```
---

## ▶ Next Up

**{identifier}: {name}** — {one-line description}

`{command to copy-paste}`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- `{alternative option 1}` — description
- `{alternative option 2}` — description

---
```

## Format Rules

1. **Always show what it is** — name + description, never just a command path
2. **Pull context from source** — ROADMAP.md for releases, CHANGE.md `<objective>` for changes
3. **Command in inline code** — backticks, easy to copy-paste, renders as clickable link
4. **`/clear` explanation** — always include, keeps it concise but explains why
5. **"Also available" not "Other options"** — sounds more app-like
6. **Visual separators** — `---` above and below to make it stand out

## Variants

### Execute Next Change

```
---

## ▶ Next Up

**02-03: Refresh Token Rotation** — Add /api/auth/refresh with sliding expiry

`/cat:execute-change .planning/releases/02-auth/02-03-refresh-token-CHANGE.md`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- Review change before executing
- `/cat:list-release-assumptions 2` — check assumptions

---
```

### Execute Final Change in Release

Add note that this is the last change and what comes after:

```
---

## ▶ Next Up

**02-03: Refresh Token Rotation** — Add /api/auth/refresh with sliding expiry
<sub>Final change in Release 2</sub>

`/cat:execute-change .planning/releases/02-auth/02-03-refresh-token-CHANGE.md`

<sub>`/clear` first → fresh context window</sub>

---

**After this completes:**
- Release 2 → Release 3 transition
- Next: **Release 3: Core Features** — User dashboard and settings

---
```

### Change a Release

```
---

## ▶ Next Up

**Release 2: Authentication** — JWT login flow with refresh tokens

`/cat:change-release 2`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- `/cat:discuss-release 2` — gather context first
- `/cat:research-release 2` — investigate unknowns
- Review roadmap

---
```

### Release Complete, Ready for Next

Show completion status before next action:

```
---

## ✓ Release 2 Complete

3/3 changes executed

## ▶ Next Up

**Release 3: Core Features** — User dashboard, settings, and data export

`/cat:change-release 3`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- `/cat:discuss-release 3` — gather context first
- `/cat:research-release 3` — investigate unknowns
- Review what Release 2 built

---
```

### Multiple Equal Options

When there's no clear primary action:

```
---

## ▶ Next Up

**Release 3: Core Features** — User dashboard, settings, and data export

**To change directly:** `/cat:change-release 3`

**To discuss context first:** `/cat:discuss-release 3`

**To research unknowns:** `/cat:research-release 3`

<sub>`/clear` first → fresh context window</sub>

---
```

### Milestone Complete

```
---

## 🎉 Milestone v1.0 Complete

All 4 releases shipped

## ▶ Next Up

**Change v1.1** — Enhanced features and optimizations

`/cat:discuss-milestone`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- `/cat:new-milestone` — create directly if scope is clear
- Review accomplishments before moving on

---
```

## Pulling Context

### For releases (from ROADMAP.md):

```markdown
### Release 2: Authentication
**Goal**: JWT login flow with refresh tokens
```

Extract: `**Release 2: Authentication** — JWT login flow with refresh tokens`

### For changes (from ROADMAP.md):

```markdown
Changes:
- [ ] 02-03: Add refresh token rotation
```

Or from CHANGE.md `<objective>`:

```xml
<objective>
Add refresh token rotation with sliding expiry window.

Purpose: Extend session lifetime without compromising security.
</objective>
```

Extract: `**02-03: Refresh Token Rotation** — Add /api/auth/refresh with sliding expiry`

## Anti-Patterns

### Don't: Command-only (no context)

```
## To Continue

Run `/clear`, then paste:
/cat:execute-change .planning/releases/02-auth/02-03-refresh-token-CHANGE.md
```

User has no idea what 02-03 is about.

### Don't: Missing /clear explanation

```
`/cat:change-release 3`

Run /clear first.
```

Doesn't explain why. User might skip it.

### Don't: "Other options" language

```
Other options:
- Review roadmap
```

Sounds like an afterthought. Use "Also available:" instead.

### Don't: Fenced code blocks for commands

```
```
/cat:change-release 3
```
```

Fenced blocks inside templates create nesting ambiguity. Use inline backticks instead.
