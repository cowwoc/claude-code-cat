# CAT

A Claude Code plugin for specification-driven development with quality gates.

Based on [get-shit-done](https://github.com/glittercowboy/get-shit-done) v1.3.31 with additional task protocol enhancements.

## Features

### Core Capabilities
- **Specification-driven development**: PROJECT.md → ROADMAP.md → CHANGE.md → SUMMARY.md
- **Context engineering**: Fresh 200k token contexts for task execution
- **Atomic commits**: One commit per task for git bisect capability

### Task Protocol Enhancements
- **Deviation handling**: Automatic bug fixes, critical additions, architectural decisions
- **TDD support**: RED → GREEN → REFACTOR cycle with dedicated changes
- **Risk classification**: AUTO/HIGH/MEDIUM/LOW based on file patterns and keywords
- **Multi-agent review**: 5 agents (architect, security, quality, style, performance)
- **Approval gates**: Change, review, merge checkpoints with unanimous approval
- **Build verification**: Project-type aware (Maven, Node, Python, Rust, Go)
- **Protocol compliance**: State machine tracking with audit trail
- **Dependency tracking**: Task-level dependencies with READY/BLOCKED status

## Installation

In Claude Code, run:

```bash
# Add the marketplace
/plugin marketplace add cowwoc/claude-code-cat

# Install the plugin
/plugin install cat@cowwoc-claude-code-cat

# Verify installation
/cat:help

# Remove
/plugin uninstall cat
```

## Commands

### Core Workflow
| Command | Description |
|---------|-------------|
| `/cat:new-project` | Initialize project with deep context gathering |
| `/cat:create-roadmap` | Create roadmap with releases |
| `/cat:map-codebase` | Analyze existing codebase for brownfield projects |
| `/cat:change-release [N]` | Create detailed change for release N |
| `/cat:execute-change [path]` | Execute a CHANGE.md file |
| `/cat:verify-work [N]` | Guide manual UAT of recently built features |
| `/cat:progress` | Check progress and route to next action |
| `/cat:cleanup` | Clean up abandoned worktrees and locks |
| `/cat:help` | Show all available commands |

### Planning Commands
| Command | Description |
|---------|-------------|
| `/cat:discuss-release [N]` | Gather context before planning release |
| `/cat:research-release [N]` | Research unknowns before planning |
| `/cat:list-release-assumptions [N]` | Surface assumptions about approach |
| `/cat:plan-fix <N-M>` | Create fix change from UAT issues |

### Roadmap Management
| Command | Description |
|---------|-------------|
| `/cat:add-release` | Append release to end of current milestone |
| `/cat:insert-release [N]` | Insert urgent work as decimal release (e.g., 72.1) |
| `/cat:remove-release <N>` | Remove future release with automatic renumbering |
| `/cat:consider-issues` | Review deferred issues, close resolved, identify urgent |

### Milestone Lifecycle
| Command | Description |
|---------|-------------|
| `/cat:new-milestone [name]` | Create new milestone with releases |
| `/cat:discuss-milestone` | Gather context for upcoming milestone |
| `/cat:complete-milestone` | Archive and prepare for next |

### Session Management
| Command | Description |
|---------|-------------|
| `/cat:pause-work` | Create context handoff when pausing |
| `/cat:resume-work` | Resume with full context restoration |

## Project Structure

After running `/cat:new-project`:

```
your-project/
├── .planning/
│   ├── PROJECT.md        # Project definition and constraints
│   ├── ROADMAP.md        # Releases and milestones
│   ├── STATE.md          # Current position and context
│   ├── ISSUES.md         # Deferred issues
│   ├── config.json       # Workflow configuration
│   └── releases/
│       └── 01-foundation/
│           ├── 01-01-setup-project-CHANGE.md
│           └── 01-01-setup-project-SUMMARY.md
└── .claude/
    └── cat/
        ├── references/   # Reference documentation
        ├── templates/    # File templates
        └── workflows/    # Workflow definitions
```

## Configuration

Edit `.planning/config.json` to customize behavior:

```json
{
  "mode": "interactive",
  "depth": "standard",
  "gates": {
    "confirm_releases": true,
    "confirm_roadmap": true,
    "confirm_transition": true,
    "execute_next_change": true,
    "issues_review": true
  }
}
```

### Options

| Option | Values | Description |
|--------|--------|-------------|
| `mode` | `"interactive"`, `"yolo"` | **interactive**: Confirms each major decision. **yolo**: Auto-approves, only stops for critical checkpoints. |
| `depth` | `"quick"`, `"standard"`, `"comprehensive"` | Controls release count and detail level. quick: 3-5 releases, standard: 5-8, comprehensive: 8-12. |

### Gates

Gates control confirmation prompts. Set to `false` to skip that confirmation.

| Gate | Description |
|------|-------------|
| `confirm_releases` | Confirm release breakdown before creating roadmap |
| `confirm_roadmap` | Confirm full roadmap before saving |
| `confirm_transition` | Confirm before transitioning between releases |
| `execute_next_change` | Confirm before executing each change |
| `issues_review` | Review deferred issues before continuing |

In `yolo` mode, all gates are skipped regardless of their values.

## Version Tracking

See [VERSION.md](VERSION.md) for upstream version tracking and sync history.

## License

MIT License - see [LICENSE](LICENSE)
