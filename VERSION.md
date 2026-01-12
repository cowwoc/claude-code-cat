# Version Tracking

## Inspiration Sources

**Primary Influences:**
- [glittercowboy/get-shit-done](https://github.com/glittercowboy/get-shit-done) - Original workflow concepts (v1.x)
- Multi-agent orchestration (v2.x)
- Claude prompt engineering best practices

## Current Version

| Component | Version | Date |
|-----------|---------|------|
| Combined Plugin (CAT) | 2.0.0 | 2026-01-12 |

## Version History

### 2026-01-12: v2.0.0

**Major Rewrite:** Multi-agent orchestration system

CAT v2.0 is a complete reimagining of the plugin, loosely based on ideas from get-shit-done and other plugins, introducing:

**New Architecture:**
- **MAJOR → MINOR → TASK** hierarchy (replaces MILESTONE → RELEASE → CHANGE)
- Multi-agent orchestration with parallel subagent execution
- Main agent coordinates, subagents execute in dedicated worktrees
- Token-aware task decomposition to prevent context overflow

**New Commands:**
- `/cat:init` - Initialize CAT structure (new or existing project)
- `/cat:execute-task` - Execute task (continues incomplete work)
- `/cat:status` - Show hierarchy status with visual tree
- `/cat:add-task`, `/cat:add-minor-version`, `/cat:add-major-version`
- `/cat:remove-task`, `/cat:remove-minor-version`, `/cat:remove-major-version`

**New Skills:**
- `spawn-subagent` - Launch subagent with task context in isolated worktree
- `monitor-subagents` - Check status of running subagents including token usage
- `collect-results` - Gather results from completed subagents
- `merge-subagent` - Merge subagent branch into task branch
- `parallel-execute` - Orchestrate multiple independent subagents concurrently
- `decompose-task` - Split oversized tasks based on token analysis
- `token-report` - Generate detailed token usage report

**New Document Types:**
- `STATE.md` - Task metadata and status tracking
- `PLAN.md` - Template-based planning (feature, bugfix, refactor)
- `CHANGELOG.md` - Task completion record with commits
- `ROADMAP.md` - Major/minor version overview
- `PROJECT.md` - Project overview and goals

**Configuration:**
- `cat-config.json` with yoloMode, contextLimit, targetContextUsage settings
- Interactive mode (default) with approval gates
- Yolo mode for automatic progression

**Quality Controls (preserved from v1.x):**
- Comprehensive planning before each task
- TDD integration to prevent regressions
- Learn-from-mistakes with conversation length analysis
- Git safety validation

---

## v1.x History (Archived)

### 2026-01-08: v1.1.1

**Sync with upstream v1.3.31** (from v1.3.27)

**New commands:**
- `/cat:remove-release` - Remove future releases with automatic renumbering
- `/cat:verify-work` - Guide manual UAT of recently built features
- `/cat:plan-fix` - Create fix change from UAT issues

**Workflow improvements:**
- Added design principles to plan-release.md workflow:
  - Security by Design: Assume hostile input, validate, parameterize, authenticate, fail closed
  - Performance by Design: Plan for production load, efficient data access, caching
  - Observable by Design: Meaningful error messages, logging, clear failure states

**Other improvements:**
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

**Workflow Enhancements:**
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
