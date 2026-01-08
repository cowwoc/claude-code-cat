---
name: batch-read
description: Find and read multiple files in one operation (50-70% faster for exploration)
allowed-tools: Bash
---

# Batch Read Skill

**Purpose**: Find files matching a pattern and read them in a single atomic operation, reducing LLM round-trips from 1+N to 2-3.

**Performance**: 50-70% faster for reading 3+ files during codebase exploration

## When to Use This Skill

### ✅ Use batch-read When:

- **Exploring codebase** to understand how a feature works
- **Finding examples** of a pattern across multiple files
- **Gathering context** for a code change
- **Reviewing implementations** of similar functionality
- **Understanding usage** of a particular API or method
- Reading **related configuration files**
- Collecting **test examples** from multiple test files

### ❌ Do NOT Use When:

- Reading **specific known files** (use Read tool directly)
- Need to read **entire large files** (>1000 lines each)
- Files are **unrelated** (no common pattern)
- Need **precise file selection** (specific paths known)
- Reading **binary files** or **generated code**
- Files require **deep analysis** (better to read individually)

## Performance Comparison

### Traditional Workflow (1+N LLM round-trips, 10s + 5s×N)

```
[LLM Round 1] Search for pattern
  → Grep: Find files containing "FormattingRule"
  → Returns: file1.java, file2.java, file3.java

[LLM Round 2] Read first file
  → Read: file1.java

[LLM Round 3] Read second file
  → Read: file2.java

[LLM Round 4] Read third file
  → Read: file3.java

[LLM Round 5] Analyze and report
  → Summarize findings from all files
```

**Total**: 10s + (5s × 3) = 25s, 5 LLM round-trips

### Optimized Workflow (2-3 LLM round-trips, 8-12s)

```
[LLM Round 1] Execute batch-read
  → Bash: batch-read.sh "FormattingRule" --max-files 3
  → [Script finds files + reads all + returns content]

[LLM Round 2] Analyze and report
  → Parse combined output
  → Summarize findings
```

**Total**: 8-12s, 2-3 LLM round-trips

**Savings**: 50-70% faster for N≥3 files

## Usage

### Basic Pattern Search

```bash
# Find and read files containing "FormattingRule"
~/.claude/scripts/batch-read.sh "FormattingRule"
```

### With File Type Filter

```bash
# Only search Java files
~/.claude/scripts/batch-read.sh "FormattingRule" --type java
```

### Limit Results

```bash
# Read at most 3 files
~/.claude/scripts/batch-read.sh "FormattingRule" --max-files 3
```

### Control Output Size

```bash
# Show only first 50 lines of each file
~/.claude/scripts/batch-read.sh "FormattingRule" --context-lines 50
```

### Read Entire Files

```bash
# Read complete files (no truncation)
~/.claude/scripts/batch-read.sh "FormattingRule" --context-lines 0
```

### Combined Options

```bash
# Search Java files, read 5 files, show 100 lines each
~/.claude/scripts/batch-read.sh "FormattingRule" \
  --type java \
  --max-files 5 \
  --context-lines 100
```

## Output Format

Script returns two outputs:

### 1. JSON Metadata

```json
{
  "status": "success",
  "message": "Successfully read 3 file(s) matching pattern",
  "duration_seconds": 2,
  "pattern": "FormattingRule",
  "files_found": 3,
  "files_read": 3,
  "output_file": "/tmp/batch-read-output-12345.txt",
  "working_directory": "/path/to/project",
  "timestamp": "2025-11-08T00:45:00-05:00"
}
```

### 2. Combined File Contents

```
═══════════════════════════════════════════════════════════
FILE: src/main/java/FormattingRule.java
═══════════════════════════════════════════════════════════

     1  package io.github.cowwoc.styler;
     2
     3  public interface FormattingRule {
     4      void apply(StyleContext context);
     5  }

[... file content ...]

───────────────────────────────────────────────────────────

═══════════════════════════════════════════════════════════
FILE: src/test/java/FormattingRuleTest.java
═══════════════════════════════════════════════════════════

     1  package io.github.cowwoc.styler;
     2
     3  import org.testng.annotations.Test;
     4
     5  public class FormattingRuleTest {
     6      @Test
     7      public void testRule() {
     8          // Test implementation
     9      }
    10  }

[... file content ...]

───────────────────────────────────────────────────────────
```

## LLM Integration Pattern

### Example 1: User asks how a feature is implemented

**User**: How is the FormattingRule implemented across the codebase?

**LLM Round 1** (Execute):
```bash
~/.claude/scripts/batch-read.sh "FormattingRule" \
  --type java \
  --max-files 5
```

**Script Output**:
- JSON metadata showing 3 files found
- Combined content from FormattingRule.java, FormattingRuleTest.java, etc.

**LLM Round 2** (Analyze + Report):
```
I found 3 files implementing FormattingRule:

1. **FormattingRule.java** (Interface)
   - Defines the core interface with apply() method
   - Used by all formatting implementations

2. **IndentationRule.java** (Implementation)
   - Implements FormattingRule for indentation
   - Handles tab/space conversion

3. **FormattingRuleTest.java** (Tests)
   - Unit tests for FormattingRule implementations
   - Shows usage examples

The pattern follows a strategy design where each rule
implements the interface and is applied independently.
```

### Example 2: Exploring configuration files

**User**: Show me all the Maven configuration files

**LLM Round 1** (Execute):
```bash
~/.claude/scripts/batch-read.sh "pom.xml" --max-files 10
```

**Script Output**:
- JSON showing 3 pom.xml files found
- Contents of main/pom.xml, formatter/pom.xml, etc.

**LLM Round 2** (Report):
```
Found 3 Maven configuration files:

1. /path/to/project/pom.xml (Parent POM)
   - Defines modules: formatter, ast-core
   - Java version: 21
   - Dependencies: TestNG, AssertJ

2. /path/to/project/formatter/pom.xml
   - Module: styler-formatter
   - Additional dependencies: ...

3. /path/to/project/ast-core/pom.xml
   - Module: styler-ast-core
   - Dependencies: ...
```

## Common Use Cases

### 1. Understanding Feature Implementation

```bash
# Find all files implementing a specific feature
batch-read.sh "ValidationEngine" --type java --max-files 5
```

### 2. Reviewing Test Coverage

```bash
# Find all test files for a component
batch-read.sh "FormatterTest" --type java
```

### 3. Finding Usage Examples

```bash
# See how an API is used across the codebase
batch-read.sh "StyleContext.apply" --type java
```

### 4. Configuration Review

```bash
# Review all configuration files
batch-read.sh "application.properties" --max-files 10
```

### 5. Documentation Gathering

```bash
# Collect all README files
batch-read.sh "README" --type md --max-files 20
```

## Smart Filtering Features

### Automatic Deduplication

Script uses `grep -l` (list files only) to avoid duplicate results from multiple matches in same file.

### Line Number Preservation

When using `--context-lines`, script includes line numbers to help locate code:
```
     42  public void validate() {
     43      // Implementation
     44  }
```

### Truncation Indication

When files are truncated, script shows how much was omitted:
```
[... truncated: showing 100 of 523 lines ...]
```

### Size Warnings

Script warns if output is very large (>100KB):
```
⚠️  Warning: Output is large (125000 bytes)
   Consider using --context-lines to limit output
```

## Performance Characteristics

### Time Savings by File Count

| Files | Traditional | Optimized | Savings |
|-------|-------------|-----------|---------|
| 1 file | 15s | 10s | 33% |
| 2 files | 20s | 10s | 50% |
| 3 files | 25s | 11s | 56% |
| 5 files | 35s | 12s | 66% |
| 10 files | 60s | 15s | 75% |

### Frequency and Impact

**Expected Usage**: 5-10 times per day

**Time Savings per Use**: ~15-30 seconds (average 3-5 files)

**Daily Impact**: 75-300 seconds (1.25-5 minutes)

**Monthly Impact**: 30-150 minutes (0.5-2.5 hours)

## Limitations

### File Size Limits

- Default: 100 lines per file
- Can read entire files with `--context-lines 0`
- Large files (>1000 lines) may produce too much output

### Pattern Matching

- Uses grep regex (not fuzzy matching)
- Case-sensitive by default
- Searches file contents, not file names

### File Type Detection

- `--type` filter uses file extension only
- Example: `--type java` matches `*.java` files
- Does not inspect file contents for type detection

## When NOT to Use

### Known Specific Files

**❌ Wrong**: Use batch-read to find and read one known file
```bash
batch-read.sh "MyClass.java"
```

**✅ Correct**: Use Read tool directly
```bash
Read: /path/to/project/src/main/java/MyClass.java
```

### Unrelated Files

**❌ Wrong**: Read random files that happen to match pattern
```bash
batch-read.sh "test"  # Too generic, matches everything
```

**✅ Correct**: Use specific pattern or file type
```bash
batch-read.sh "ValidationTest" --type java
```

### Deep Analysis Needed

**❌ Wrong**: Read 10 large files for detailed analysis
**✅ Correct**: Read files one-by-one for thorough review

## Related

- **Read tool**: For reading specific known files
- **Grep tool**: For finding files without reading them
- **Glob tool**: For finding files by pattern (name-based)
- **Task tool with Explore agent**: For complex codebase exploration
