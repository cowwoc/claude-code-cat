---
name: git-rewrite-history
description: Rewrite git history using git-filter-repo (not filter-branch)
---

# Git Rewrite History Skill

**Purpose**: Safely rewrite git history using `git-filter-repo`, the modern replacement for
`git filter-branch`.

## Why git-filter-repo over git-filter-branch

**NEVER use `git filter-branch`**. Git itself warns against it:

> git-filter-branch has a glut of gotchas generating mangled history rewrites.
> Use git-filter-repo instead.

| Feature | git-filter-repo | git-filter-branch |
|---------|-----------------|-------------------|
| Speed | 10-50x faster | Slow |
| Safety | Handles edge cases | Many gotchas |
| Maintenance | Actively maintained | Deprecated |
| Syntax | Simple, intuitive | Complex, error-prone |

## Installation

```bash
pip install git-filter-repo
# or
pip install --break-system-packages git-filter-repo
```

## Safety Pattern: Backup-Verify-Cleanup

**ALWAYS follow this pattern:**

1. **Work on a fresh clone** (filter-repo requires this by default)
2. Create backup of original remote URL
3. Execute the filter operation
4. **Verify immediately** - check history is correct
5. Force push only after verification

## Common Operations

### Remove a File from All History

```bash
# Fresh clone required
git clone --mirror <url> repo-filter
cd repo-filter

# Remove file
git filter-repo --path secrets.txt --invert-paths

# Verify
git log --all --oneline -- secrets.txt  # Should return nothing

# Push (requires --force)
git push origin --force --all
```

### Remove a Directory from All History

```bash
git filter-repo --path vendor/ --invert-paths
```

### Remove a Submodule from History

```bash
# This removes the gitlink entry for a submodule
git filter-repo --path submodule-name --invert-paths
```

### Rename/Move Files in History

```bash
# Rename a file across all history
git filter-repo --path-rename old-name.txt:new-name.txt

# Move directory
git filter-repo --path-rename old-dir/:new-dir/
```

### Remove Large Files

```bash
# Remove files larger than 10MB
git filter-repo --strip-blobs-bigger-than 10M
```

### Keep Only Specific Paths

```bash
# Keep only src/ directory (remove everything else)
git filter-repo --path src/
```

### Filter by Content (Remove Secrets)

```bash
# Replace text patterns
git filter-repo --replace-text expressions.txt

# Where expressions.txt contains:
# PASSWORD=secret123==>PASSWORD=REDACTED
# regex:api_key=\w+==>api_key=REDACTED
```

## Working on Existing Clone

By default, git-filter-repo requires a fresh clone. To work on an existing repo:

```bash
# CAUTION: Only if you understand the implications
git filter-repo --force --path file-to-remove --invert-paths
```

## After Rewriting History

1. **All collaborators must re-clone** or reset their branches
2. **Force push required**: `git push --force-with-lease origin <branch>`
3. **Update any CI/CD** that caches the old commits
4. **GitHub/GitLab**: May need to run garbage collection on server

## Verification Checklist

- [ ] Target files/paths no longer in history: `git log --all -- <path>`
- [ ] Commit count is expected
- [ ] No unexpected files removed: `git diff --stat <old-commit>..<new-head>`
- [ ] Build still works
- [ ] Tests pass

## Recovery

If something goes wrong:

```bash
# If you have original remote
git fetch origin
git reset --hard origin/<branch>

# If you kept a backup branch
git reset --hard backup-before-filter
```

## When to Use This Skill

- Removing accidentally committed secrets
- Removing large binary files to reduce repo size
- Removing submodules from history
- Restructuring repository paths
- Splitting a repository

## References

- [git-filter-repo documentation](https://github.com/newren/git-filter-repo)
- [GitHub: Removing sensitive data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
