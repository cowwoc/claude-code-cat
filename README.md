# <img src="docs/cat-logo.svg" height="36" alt="CAT logo"> CAT

A Claude Code plugin for orchestrated multi-agent task execution with comprehensive planning and quality gates.

## Features

### Core Capabilities
- **Multi-agent orchestration**: Main agent coordinates, subagents execute in parallel
- **MAJOR → MINOR → TASK hierarchy**: Structured project planning
- **Token-aware decomposition**: Automatic task splitting to prevent context overflow
- **Comprehensive planning**: PLAN.md documents before execution

### Workflow Enhancements
- **Parallel subagent execution**: Multiple tasks concurrently in isolated worktrees
- **Automatic merge handling**: Main agent resolves conflicts and merges branches
- **Quality gates**: Approval checkpoints in interactive mode
- **TDD integration**: Test-driven development support
- **Learn-from-mistakes**: Analysis with conversation length as potential cause
- **Git safety validation**: Protected branch and operation validation

## Installation

In Claude Code, run:

```bash
# Add the marketplace
/plugin marketplace add cowwoc/cat

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
| `/cat:init` | Initialize CAT structure (new or existing project) |
| `/cat:execute-task` | Execute task (continues incomplete work) |
| `/cat:status` | Show hierarchy status with visual tree |
| `/cat:help` | Show all available commands |

### Planning Commands
| Command | Description |
|---------|-------------|
| `/cat:add-task [major.minor]` | Add task to minor version |
| `/cat:add-minor-version [major]` | Add minor version to major |
| `/cat:add-major-version` | Add new major version |

### Remove Commands
| Command | Description |
|---------|-------------|
| `/cat:remove-task` | Remove a task |
| `/cat:remove-minor-version` | Remove a minor version |
| `/cat:remove-major-version` | Remove a major version |

## Project Structure

After running `/cat:init`:

```
your-project/
└── .claude/cat/
    ├── PROJECT.md              # Project overview and goals
    ├── ROADMAP.md              # Major/minor version summaries
    ├── cat-config.json         # Plugin configuration
    └── v1/
        ├── STATE.md            # Major version state
        ├── PLAN.md             # Major version plan
        ├── CHANGELOG.md        # Major version changelog
        └── v1.0/
            ├── STATE.md        # Minor version state
            ├── PLAN.md         # Minor version plan
            ├── CHANGELOG.md    # Minor version changelog
            └── task/
                └── {task-name}/
                    ├── STATE.md    # Task state
                    ├── PLAN.md     # Task plan
                    └── CHANGELOG.md # Task changelog
```

## Configuration

Edit `.claude/cat/cat-config.json`:

```json
{
  "yoloMode": false,
  "contextLimit": 200000,
  "targetContextUsage": 0.4,
  "autoCleanupWorktrees": true
}
```

### Options

| Option | Default | Description |
|--------|---------|-------------|
| `yoloMode` | `false` | Skip approval gates, auto-proceed |
| `contextLimit` | `200000` | Total context window in tokens |
| `targetContextUsage` | `0.4` | Target max usage (40% = 80K tokens) |
| `autoCleanupWorktrees` | `true` | Clean worktrees after merge |

### Operating Modes

**Interactive Mode (Default)**
- Approval gates required at task level
- Main agent proposes next task when dependencies met
- Merge requires user acceptance

**Yolo Mode**
- Approval gates skipped
- Main agent begins immediately when dependencies met
- Automatic merge on task completion

## Version Tracking

See [VERSION.md](VERSION.md) for version history.

## License

Apache-2.0 License - see [LICENSE](LICENSE)
