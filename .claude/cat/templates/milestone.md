# Milestone Entry Template

Add this entry to `.planning/MILESTONES.md` when completing a milestone:

```markdown
## v[X.Y] [Name] (Shipped: YYYY-MM-DD)

**Delivered:** [One sentence describing what shipped]

**Releases completed:** [X-Y] ([Z] changes total)

**Key accomplishments:**
- [Major achievement 1]
- [Major achievement 2]
- [Major achievement 3]
- [Major achievement 4]

**Stats:**
- [X] files created/modified
- [Y] lines of code (primary language)
- [Z] releases, [N] changes, [M] tasks
- [D] days from start to ship (or milestone to milestone)

**Git range:** `abc1234` → `def5678` (first → last commit hashes from SUMMARY.md files)

**What's next:** [Brief description of next milestone goals, or "Project complete"]

---
```

<structure>
If MILESTONES.md doesn't exist, create it with header:

```markdown
# Project Milestones: [Project Name]

[Entries in reverse chronological order - newest first]
```
</structure>

<guidelines>
**When to create milestones:**
- Initial v1.0 MVP shipped
- Major version releases (v2.0, v3.0)
- Significant feature milestones (v1.1, v1.2)
- Before archiving planning (capture what was shipped)

**Don't create milestones for:**
- Individual release completions (normal workflow)
- Work in progress (wait until shipped)
- Minor bug fixes that don't constitute a release

**Stats to include:**
- Count modified files: `git diff --stat FIRST_HASH..LAST_HASH | tail -1`
- Count LOC: `find . -name "*.swift" -o -name "*.ts" | xargs wc -l` (or relevant extension)
- Release/change/task counts from ROADMAP
- Timeline from first release commit to last release commit

**Git range format:**
- First commit of milestone → last commit of milestone
- Get commit hashes from SUMMARY.md files in each release
- Example: `a1b2c3d` → `e4f5g6h` for releases 1-4
</guidelines>

<example>
```markdown
# Project Milestones: WeatherBar

## v1.1 Security & Polish (Shipped: 2025-12-10)

**Delivered:** Security hardening with Keychain integration and comprehensive error handling

**Releases completed:** 5-6 (3 changes total)

**Key accomplishments:**
- Migrated API key storage from plaintext to macOS Keychain
- Implemented comprehensive error handling for network failures
- Added Sentry crash reporting integration
- Fixed memory leak in auto-refresh timer

**Stats:**
- 23 files modified
- 650 lines of Swift added
- 2 releases, 3 changes, 12 tasks
- 8 days from v1.0 to v1.1

**Git range:** `c4d5e6f` → `g7h8i9j`

**What's next:** v2.0 SwiftUI redesign with widget support

---

## v1.0 MVP (Shipped: 2025-11-25)

**Delivered:** Menu bar weather app with current conditions and 3-day forecast

**Releases completed:** 1-4 (7 changes total)

**Key accomplishments:**
- Menu bar app with popover UI (AppKit)
- OpenWeather API integration with auto-refresh
- Current weather display with conditions icon
- 3-day forecast list with high/low temperatures
- Code signed and notarized for distribution

**Stats:**
- 47 files created
- 2,450 lines of Swift
- 4 releases, 7 changes, 28 tasks
- 12 days from start to ship

**Git range:** `a1b2c3d` → `e4f5g6h`

**What's next:** Security audit and hardening for v1.1
```
</example>
