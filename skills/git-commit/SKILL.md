---
name: git-commit
description: Guide for writing clear, descriptive commit messages
---

# Git Commit Message Skill

**Purpose**: Provide guidance for writing clear, descriptive commit messages that explain WHAT the code does and WHY.

## Core Principles

### 1. Describe WHAT the Code Does, Not the Process

```
# ❌ WRONG - Describes the process
Squashed commits
Combined multiple commits
Merged feature branch

# ✅ CORRECT - Describes what the code does
Add user authentication with JWT tokens
Fix memory leak in connection pool
Refactor parser to use visitor pattern
```

### 2. Use Imperative Mood (Command Form)

```
# ❌ WRONG
Added authentication
Authentication was added

# ✅ CORRECT
Add user authentication
Fix authentication timeout bug
```

### 3. Subject Line Formula

```
<Verb> <what> [<where/context>]

Examples:
Add   rate limiting      to API endpoints
Fix   memory leak        in connection pool
Refactor  parser         to use visitor pattern
```

**Rules**:
- Max 72 characters (50 ideal)
- Imperative mood (Add, Fix, Update, Remove, Refactor)
- No period at end
- Capitalize first word

## Structure for Complex Changes

```
Subject line: Brief summary (50-72 chars, imperative mood)

Body paragraph: Explain the overall change and why it's needed.

Changes:
- First major change
- Second major change
- Third major change
```

## For Squashed Commits

**Review commits being squashed**:
```bash
git log --oneline base..HEAD
```

**Synthesize into unified message**:

```
# ❌ WRONG - Concatenated messages
feat(auth): add login form
feat(auth): add validation
feat(auth): add error handling
fix(auth): fix typo

# ✅ CORRECT - Unified message
feat(auth): add login form with validation and error handling

- Email/password form with client-side validation
- Server-side validation with descriptive error messages
- Loading states and error display
```

## Good Verbs for Subject Lines

| Verb | Use For |
|------|---------|
| **Add** | New feature, file, function |
| **Fix** | Bug fix or correction |
| **Update** | Modify existing feature (non-breaking) |
| **Remove** | Delete feature, file, or code |
| **Refactor** | Restructure without changing behavior |
| **Improve** | Enhance existing feature |
| **Document** | Documentation only |
| **Test** | Add or update tests |
| **Chore** | Maintenance (deps, build, config) |

## Anti-Patterns to Avoid

```
# ❌ Meaningless
WIP
Fix stuff
Updates
.

# ❌ Overly Generic
Update code
Fix bugs
Refactor

# ❌ Just the Process
Squashed commits
Merged feature branch
Combined work

# ❌ Too Technical
Change variable name from x to userCount
Move function from line 45 to line 67
```

## Checklist Before Committing

- [ ] Subject line is imperative mood ("Add", not "Added")
- [ ] Subject line is specific (not "Update files")
- [ ] Subject line is under 72 characters
- [ ] Body explains WHAT and WHY, not HOW
- [ ] For squashed commits: synthesized meaningful summary
- [ ] Message would make sense in git history 6 months from now

## Quick Test

Ask yourself: "If I read this in git log in 6 months, would I understand what this commit does and why?"

If no, revise the message.
