# Version Tracking

## Upstream Source

**Repository:** [glittercowboy/get-shit-done](https://github.com/glittercowboy/get-shit-done)
**Package:** get-shit-done-cc

## Current Version

| Component | Version | Date |
|-----------|---------|------|
| Upstream (glittercowboy/get-shit-done) | 1.3.27 | 2026-01-07 |
| Task Protocol Enhancements | 1.0.0 | 2026-01-07 |
| Combined Plugin (CAT) | 1.0.1 | 2026-01-08 |

## Sync History

### 2026-01-08: v1.0.1

**Enhancement:** Descriptive slugs for PLAN.md filenames
- Changed naming from `{phase}-{plan}-PLAN.md` to `{phase}-{plan}-{slug}-PLAN.md`
- Slug derived from plan objective (max 30 chars)
- Added uniqueness validation per phase
- Updated all scripts and workflows for new format
- Backwards compatible with old format

### 2026-01-07: Initial Release (v1.0.0)

**Base:** glittercowboy/get-shit-done v1.3.27

**Additional enhancements:**
- `/cat:cleanup` command for worktree/lock cleanup
- Session hooks (echo-session-id.sh, hooks.json)
- Scripts for parallel execution:
  - cleanup-and-merge.sh
  - find-next-plan.sh
  - worktree-setup.sh

**Task Protocol Enhancements:**
- Risk classification system (HIGH/MEDIUM/LOW)
- Multi-agent peer review (architect, security, quality, style, performance)
- Mandatory approval gates (plan, review, merge)
- Build verification gates (project-type aware)
- Protocol compliance audits (state machine tracking)
- Enhanced plan templates with:
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
