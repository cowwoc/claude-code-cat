---
name: git-squash
description: Safely squash multiple commits into one with automatic backup and verification
---

# Git Squash Skill

**Purpose**: Safely squash multiple commits into one with automatic backup, verification, and cleanup.

## Safety Pattern: Backup-Verify-Cleanup

**ALWAYS follow this pattern:**
1. Create timestamped backup branch
2. Execute the squash
3. **Verify immediately** - no changes lost or added
4. Cleanup backup only after verification passes

## Quick Workflow

```bash
# 1. Position HEAD at last commit to squash
git checkout <last-commit-to-squash>

# 2. Create backup
BACKUP="backup-before-squash-$(date +%Y%m%d-%H%M%S)"
git branch "$BACKUP"

# 3. Verify clean working directory
git status --porcelain  # Must be empty

# 4. Soft reset to base (parent of first commit to squash)
git reset --soft <base-commit>

# 5. Verify no changes lost
git diff --stat "$BACKUP"  # Must be empty

# 6. Create squashed commit (see git-commit skill for message guidance)
git commit -m "Unified message describing what code does"

# 7. Verify result
git diff "$BACKUP"  # Must be empty
git rev-list --count <base-commit>..HEAD  # Must be 1

# 8. Update branch and cleanup
git branch -f main HEAD
git checkout main
git branch -D "$BACKUP"
```

## Critical Rules

### Position HEAD First
```bash
# ❌ WRONG - HEAD beyond squash range
git reset --soft <base>  # Squashes ALL commits to current HEAD!

# ✅ CORRECT - Checkout last commit first
git checkout <last-commit>
git reset --soft <base>  # Squashes only intended range
```

### Write Meaningful Commit Messages

```bash
# ❌ WRONG - Concatenated messages
feat(auth): add login
feat(auth): add validation
fix(auth): fix typo

# ✅ CORRECT - Unified message
feat(auth): add login form with validation

- Email/password form with client-side validation
- Server-side validation with descriptive messages
```

See `git-commit` skill for detailed message guidance.

### Verify Immediately After Commit

```bash
# Check no changes lost
git diff "$BACKUP"  # Empty = success

# Check commit count
git rev-list --count <base>..HEAD  # Should be 1
```

## Squash vs Fixup

| Command | Message Behavior | When to Use |
|---------|-----------------|-------------|
| `squash` | Combines all messages | Different features being combined |
| `fixup` | Discards secondary messages | Trivial fixes (typos, forgotten files) |

**When in doubt, use squash** - you can edit the combined message.

## Non-Adjacent Commits

For commits separated by others, use interactive rebase:

```bash
git rebase -i <base-commit>

# In editor: Move commits to be adjacent, mark with 'squash'
# Example:
#   pick abc123 Target commit
#   squash def456 Commit to combine (MOVED here)
#   pick ghi789 Other commit (unchanged)
```

## Error Recovery

```bash
# If anything goes wrong:
git reset --hard $BACKUP

# Or check reflog:
git reflog
git reset --hard HEAD@{N}
```

## Success Criteria

- [ ] Backup created before squash
- [ ] HEAD positioned at correct last commit
- [ ] No changes lost (diff with backup is empty)
- [ ] Single commit created with all changes
- [ ] Meaningful commit message (not "squashed commits")
- [ ] Backup removed after verification
