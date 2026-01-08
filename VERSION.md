# Version Tracking

## Upstream Source

**Repository:** [glittercowboy/get-shit-done](https://github.com/glittercowboy/get-shit-done)
**Package:** get-shit-done-cc

## Current Version

| Component | Version | Date |
|-----------|---------|------|
| Upstream (glittercowboy/get-shit-done) | 1.3.27 | 2026-01-07 |
| Task Protocol Enhancements | 1.0.0 | 2026-01-07 |
| Combined Plugin (CAT) | 1.1.1 | 2026-01-08 |

## Sync History

### 2026-01-08: v1.1.1

**Improvements:**
- learn-from-mistakes: Added explicit Step 5 (Implement) and Step 6 (Verify) for clearer workflow
- learn-from-mistakes: Renumbered steps 1-7 sequentially, removed awkward "Step 4a" sub-numbering
- Removed deprecated session-lock.sh hook (lock acquisition moved to execute-release workflow)

### 2026-01-08: v1.1.0

**Breaking Change:** Terminology rename
- "phase" → "release" (grouping of related changes)
- "plan" → "change" (detailed execution document)
- All file references updated (e.g., PLAN.md → CHANGE.md)
- All command names updated (e.g., `/cat:plan-phase` → `/cat:plan-release`)

### 2026-01-08: v1.0.2

**Fix:** Worktrees now created inside project directory
- Changed worktree location from `../${PROJECT}-${ID}` to `.worktrees/${ID}`
- Fixes sandbox permission issues when `CLAUDE_PROJECT_DIR` is workspace root
- Worktree ID now includes full change slug (e.g., `m1-02-01-setup-jwt`)
- Auto-adds `.worktrees/` to `.gitignore` on first worktree creation
- Removed unnecessary `.planning/` copy (already in committed branch)

### 2026-01-08: v1.0.1

**Enhancement:** Descriptive slugs for CHANGE.md filenames
- Changed naming from `{release}-{change}-CHANGE.md` to `{release}-{change}-{slug}-CHANGE.md`
- Slug derived from change objective (max 30 chars)
- Added uniqueness validation per release
- Updated all scripts and workflows for new format
- Backwards compatible with old format

### 2026-01-07: Initial Release (v1.0.0)

**Base:** glittercowboy/get-shit-done v1.3.27

**Additional enhancements:**
- `/cat:cleanup` command for worktree/lock cleanup
- Session hooks (echo-session-id.sh, hooks.json)
- Scripts for parallel execution:
  - cleanup-and-merge.sh
  - find-next-change.sh
  - worktree-setup.sh

**Task Protocol Enhancements:**
- Risk classification system (HIGH/MEDIUM/LOW)
- Multi-agent peer review (architect, security, quality, style, performance)
- Mandatory approval gates (change, review, merge)
- Build verification gates (project-type aware)
- Protocol compliance audits (state machine tracking)
- Enhanced change templates with:
  - Task-level dependencies (depends-on attribute)
  - Task IDs for tracking
  - Effort estimates
  - Purpose/rationale
  - READY/BLOCKED status

**Renamed:**
- `gsd` → `cat` 
- `get-shit-done` → `cat`
- All commands: `/gsd:*` → `/cat:*`

## Checking for Updates

To check if upstream has updates:

```bash
# Check upstream version
curl -s https://raw.githubusercontent.com/glittercowboy/get-shit-done/main/package.json | jq -r '.version'
```

Compare with versions listed above. If different, review changes and determine what to merge.

## Update Process

1. Check upstream version (commands above)
2. If newer, review upstream changes
3. Merge relevant changes to this plugin
4. Rename gsd→cat, get-shit-done→cat
5. Update this VERSION.md with new sync entry
6. Commit with message: `chore: sync with upstream gsd vX.Y.Z`
