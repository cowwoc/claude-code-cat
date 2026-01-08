---
description: Clean up abandoned worktrees, lock files, and orphaned branches
allowed-tools:
  - Bash
  - Read
---

<objective>
Identify and clean up abandoned CAT artifacts: worktrees, lock files, and orphaned branches.

Use this when:
- A previous session crashed or was cancelled
- Lock files are blocking new execution
- Orphaned worktrees are cluttering the filesystem
</objective>

<process>

<step name="survey">
**Survey current state:**

```bash
# List all worktrees
echo "=== Git Worktrees ==="
git worktree list

# List lock files
echo ""
echo "=== Lock Files ==="
ls -la .cat-*.lock 2>/dev/null || echo "No lock files found"

# List execution context files
echo ""
echo "=== Context Files ==="
ls -la .cat-execution-context 2>/dev/null || echo "No context file found"

# List CAT-related branches
echo ""
echo "=== CAT Branches ==="
git branch -a | grep -E "(phase/|worktree)" || echo "No CAT branches found"
```

Present findings to user.
</step>

<step name="identify_abandoned">
**Identify abandoned artifacts:**

A worktree is likely abandoned if:
- Its lock file references a session that's no longer active
- The worktree directory exists but has no recent activity
- The main project has a stale `.cat-plan-*.lock` file

Check each lock file:
```bash
for lock in .cat-plan-*.lock; do
  if [[ -f "$lock" ]]; then
    echo "Lock: $lock"
    echo "  Session: $(cat "$lock")"
    echo "  Age: $(stat -c %y "$lock" 2>/dev/null || stat -f %Sm "$lock")"
  fi
done
```
</step>

<step name="check_uncommitted">
**CRITICAL: Check for uncommitted work before cleanup:**

For each worktree to be removed:
```bash
WORKTREE_PATH="<path-from-git-worktree-list>"

# Check for uncommitted changes
cd "$WORKTREE_PATH"
if [[ -n "$(git status --porcelain)" ]]; then
  echo "WARNING: Uncommitted changes in $WORKTREE_PATH"
  git status --short
  echo ""
  echo "Options:"
  echo "1. Commit the changes first"
  echo "2. Stash the changes: git stash"
  echo "3. Discard changes: git checkout -- . (DESTRUCTIVE)"
  echo "4. Skip this worktree"
  # ASK USER before proceeding
fi
cd -
```

**NEVER remove a worktree with uncommitted changes without explicit user approval.**
</step>

<step name="cleanup">
**Perform cleanup (with user confirmation):**

For each abandoned artifact, confirm before removing:

```bash
# Remove worktree (MUST be done BEFORE deleting its branch)
WORKTREE_PATH="<path>"
git worktree remove "$WORKTREE_PATH" --force

# Remove associated lock file
PLAN_ID="<extracted-from-path>"  # e.g., "02-01" from "myapp-m1-02-01"
rm -f ".cat-plan-${PLAN_ID}.lock"

# Remove orphaned branch (AFTER worktree removal)
BRANCH_NAME="<branch-from-worktree>"
git branch -D "$BRANCH_NAME" 2>/dev/null || true
```

**Order matters:**
1. Remove worktree FIRST (git won't delete a branch checked out in a worktree)
2. Remove lock files
3. Delete orphaned branches
4. Remove context file last
</step>

<step name="verify">
**Verify cleanup complete:**

```bash
echo "=== Verification ==="

# Confirm no orphaned worktrees
echo "Remaining worktrees:"
git worktree list

# Confirm no stale locks
echo ""
echo "Remaining lock files:"
ls -la .cat-*.lock 2>/dev/null || echo "None"

# Confirm branches cleaned
echo ""
echo "Remaining CAT branches:"
git branch -a | grep -E "(phase/|worktree)" || echo "None"

echo ""
echo "Cleanup complete"
```
</step>

</process>

<safety_rules>
- ALWAYS check for uncommitted changes before removing worktrees
- ALWAYS ask user before removing anything with uncommitted work
- ALWAYS remove worktree BEFORE deleting its branch
- NEVER force-delete branches that might have unmerged commits
- List what will be removed and get confirmation before proceeding
</safety_rules>

<common_scenarios>

**Scenario 1: Session crashed mid-execution**
```
Symptoms: Lock file exists, worktree may have partial work
Action: Check worktree for uncommitted changes, offer to commit or discard
```

**Scenario 2: User cancelled and wants fresh start**
```
Symptoms: Multiple stale worktrees and lock files
Action: Survey all, confirm cleanup of each
```

**Scenario 3: Lock file blocking new execution**
```
Symptoms: "Plan already being executed" error but no active session
Action: Remove specific lock file after confirming no active work
```

**Scenario 4: Orphaned branches after worktree removal**
```
Symptoms: Branches exist but no worktrees reference them
Action: List branches, confirm they have no unique commits, delete
```

</common_scenarios>
