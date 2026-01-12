---
name: compare-docs
description: Compare two documents and highlight differences. Use when reviewing changes, validating updates, or auditing document modifications.
---

# Compare Documents Skill

**Purpose**: Systematically compare two documents to identify additions, removals, and modifications.

## When to Use

- Reviewing document updates
- Validating that changes were applied correctly
- Auditing configuration or specification changes
- Comparing before/after states

## Comparison Process

### 1. Load Both Documents

```bash
# Read original
cat "$ORIGINAL_DOC"

# Read updated
cat "$UPDATED_DOC"
```

### 2. Structural Comparison

**For Markdown documents:**
- Compare section headings (## / ### structure)
- Check for added/removed sections
- Note reordering of sections

**For JSON/YAML:**
- Compare keys at each level
- Check for added/removed fields
- Note value changes

**For code files:**
- Compare function/class signatures
- Check for added/removed methods
- Note logic changes

### 3. Report Format

```markdown
## Document Comparison Report

**Original**: [path]
**Updated**: [path]

### Summary
- Sections added: [N]
- Sections removed: [N]
- Sections modified: [N]

### Added
- [Section/field]: [brief description]

### Removed
- [Section/field]: [brief description]

### Modified
- [Section/field]: [what changed]

### Unchanged
- [List of sections that remained the same]
```

## Diff Commands

```bash
# Line-by-line diff
diff -u "$ORIGINAL" "$UPDATED"

# Side-by-side comparison
diff -y --width=120 "$ORIGINAL" "$UPDATED"

# Just show changed lines
diff "$ORIGINAL" "$UPDATED" | grep -E '^[<>]'
```

## Semantic Comparison (Beyond Text Diff)

For meaningful document comparison:

1. **Purpose changes**: Did the document's purpose change?
2. **Scope changes**: Is more or less covered?
3. **Accuracy changes**: Are facts/data updated?
4. **Completeness**: Are there gaps in either version?

## Quick Reference

| Comparison Type | Command/Approach |
|-----------------|------------------|
| Text diff | `diff -u old new` |
| Word-level | `git diff --word-diff old new` |
| JSON structure | Parse and compare keys |
| Markdown sections | Extract headings and compare |
