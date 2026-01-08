# Milestone Context Template

Template for `.planning/MILESTONE-CONTEXT.md` - temporary handoff file from discuss-milestone to create-milestone.

**Purpose:** Persist milestone discussion context so `/clear` can be used between commands. This file is consumed by `/cat:new-milestone` and deleted after the milestone is created.

---

## File Template

```markdown
# Milestone Context

**Generated:** [date]
**Status:** Ready for /cat:new-milestone

<features>
## Features to Build

[Features identified during discussion - the substance of this milestone]

- **[Feature 1]**: [description]
- **[Feature 2]**: [description]
- **[Feature 3]**: [description]

</features>

<scope>
## Scope

**Suggested name:** v[X.Y] [Theme Name]
**Estimated releases:** [N]
**Focus:** [One sentence theme/focus]

</scope>

<phase_mapping>
## Release Mapping

[How features map to releases - rough breakdown]

- Release [N]: [Feature/goal]
- Release [N+1]: [Feature/goal]
- Release [N+2]: [Feature/goal]

</phase_mapping>

<constraints>
## Constraints

[Any constraints or boundaries mentioned during discussion]

- [Constraint 1]
- [Constraint 2]

</constraints>

<notes>
## Additional Context

[Anything else captured during discussion that informs the milestone]

</notes>

---

*This file is temporary. It will be deleted after /cat:new-milestone creates the milestone.*
```

<guidelines>
**This is a handoff artifact, not permanent documentation.**

The file exists only to pass context from `discuss-milestone` to `create-milestone` across a `/clear` boundary.

**Lifecycle:**
1. `/cat:discuss-milestone` creates this file at end of discussion
2. User runs `/clear` (safe now - context is persisted)
3. `/cat:new-milestone` reads this file
4. `/cat:new-milestone` uses context to populate milestone
5. `/cat:new-milestone` deletes this file after successful creation

**Content should include:**
- Features identified (the core of what to build)
- Suggested milestone name/theme
- Rough release mapping
- Any constraints or scope boundaries
- Notes from discussion

**Content should NOT include:**
- Technical analysis (that comes during release research)
- Detailed release specifications (create-milestone handles that)
- Implementation details
</guidelines>
