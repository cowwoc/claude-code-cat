# Coding Conventions Template

Template for `.planning/codebase/CONVENTIONS.md` - captures coding style and patterns.

**Purpose:** Document how code is written in this codebase. Prescriptive guide for Claude to match existing style.

**Load-on-demand pattern:** For complex projects, use index + subdirectory structure. Index stays compact (~100-150 lines), detailed rules in `conventions/` subdirectory.

---

## File Template

```markdown
# Coding Conventions

**Analysis Date:** [YYYY-MM-DD]
**Language:** [Primary language - e.g., Java, TypeScript, Python, Go, Rust]

## Quick Reference

| Category | Key Rules | Details |
|----------|-----------|---------|
| Naming | [e.g., camelCase functions, PascalCase types] | [conventions/naming.md or inline below] |
| Formatting | [e.g., 4-space indent, 120 char lines] | [conventions/style.md or inline below] |
| Documentation | [e.g., JavaDoc/JSDoc for public API] | [conventions/documentation.md or inline below] |
| Error Handling | [e.g., fail-fast, no silent failures] | [conventions/errors.md or inline below] |

## Naming Patterns

**Files:**
- [Pattern: e.g., "PascalCase.java", "kebab-case.ts", "snake_case.py"]
- [Test files: e.g., "*.test.ts alongside source" or "*Test.java in src/test/"]
- [Components: e.g., "PascalCase.tsx for React components"]

**Types (Classes/Interfaces/Records):**
- [Pattern: e.g., "PascalCase for all types"]
- [Interfaces: e.g., "no I prefix" or "IPrefix convention"]
- [Enums: e.g., "PascalCase name, UPPER_CASE values"]

**Functions/Methods:**
- [Pattern: e.g., "camelCase for all functions"]
- [Async: e.g., "no special prefix" or "async prefix"]
- [Handlers: e.g., "handleEventName for event handlers"]

**Variables:**
- [Pattern: e.g., "camelCase for variables"]
- [Constants: e.g., "UPPER_SNAKE_CASE for static final/const"]
- [Private: e.g., "no prefix" or "_prefix for private members"]

## Code Style

**Formatting:**
- [Tool: e.g., "Prettier with .prettierrc" or "Checkstyle" or "black"]
- [Indentation: e.g., "4 spaces" or "2 spaces" or "tabs"]
- [Line length: e.g., "100 characters max" or "120 characters"]
- [Brace style: e.g., "Allman (own line)" or "K&R (same line)"]
- [Quotes: e.g., "single quotes" or "double quotes"]
- [Semicolons: e.g., "required" or "omitted"]

**Linting:**
- [Tool: e.g., "ESLint", "PMD", "ruff", "clippy"]
- [Rules: e.g., "extends recommended, no console in production"]
- [Run: e.g., "npm run lint" or "./mvnw checkstyle:check pmd:check"]

## Import/Dependency Organization

**Order:**
1. [e.g., "Standard library (java.*, stdlib)"]
2. [e.g., "Third-party packages (external dependencies)"]
3. [e.g., "Internal modules (@/lib, project packages)"]
4. [e.g., "Relative imports (., ..)"]
5. [e.g., "Type imports last (import type {})"]

**Grouping:**
- [Blank lines: e.g., "blank line between groups"]
- [Sorting: e.g., "alphabetical within each group"]

**Path Aliases:**
- [Aliases used: e.g., "@/ for src/" or "none"]

## Error Handling

**Strategy:**
- [e.g., "fail-fast with descriptive messages"]
- [e.g., "throw errors, catch at boundaries"]
- [e.g., "validate at public API entry points"]

**Custom Errors:**
- [e.g., "extend Error/Exception class, named *Error/*Exception"]
- [e.g., "include context in error message"]

**Never:**
- [e.g., "swallow exceptions silently"]
- [e.g., "return null/empty on invalid input"]

**Async Error Handling:**
- [e.g., "use try/catch, no .catch() chains"]
- [e.g., "propagate errors to caller"]

## Logging

**Framework:**
- [Tool: e.g., "SLF4J + Logback", "pino", "logging module"]
- [Levels: e.g., "debug, info, warn, error"]

**Patterns:**
- [Format: e.g., "structured logging with context object"]
- [When: e.g., "log state transitions, external calls, errors"]
- [Where: e.g., "log at service boundaries, not in utils"]
- [Avoid: e.g., "no console.log/System.out in production"]

## Comments & Documentation

**When to Comment:**
- [e.g., "explain why, not what"]
- [e.g., "document business logic, algorithms, edge cases"]
- [e.g., "avoid obvious comments"]

**Doc Blocks (JavaDoc/JSDoc/docstrings):**
- [Usage: e.g., "required for public APIs, optional for internal"]
- [Format: e.g., "use @param, @returns/@return, @throws tags"]
- [Style: e.g., "summary line first, then details"]

**TODO Comments:**
- [Pattern: e.g., "// TODO: description" or "# TODO(username): description"]
- [Tracking: e.g., "link to issue number if available"]

## Function/Method Design

**Size:**
- [e.g., "keep under 50 lines, extract helpers"]
- [e.g., "one level of abstraction per function"]

**Parameters:**
- [e.g., "max 3 parameters, use object/record for more"]
- [e.g., "destructure objects in parameter list"]

**Return Values:**
- [e.g., "explicit return statements"]
- [e.g., "return early for guard clauses"]
- [e.g., "use Result/Option types for expected failures"]

## Module/Package Design

**Exports/Visibility:**
- [e.g., "named exports preferred, default exports for components"]
- [e.g., "package-private by default, public for API"]
- [e.g., "export public API from index/barrel files"]

**Organization:**
- [e.g., "one class per file" or "related functions grouped"]
- [e.g., "avoid circular dependencies"]
- [e.g., "internal packages for implementation details"]

## Style Enforcement

**Automated Tools:**
- [Tool 1]: [e.g., "./mvnw checkstyle:check" or "npm run lint"]
- [Tool 2]: [e.g., "./mvnw pmd:check" or "npm run format:check"]

**Manual Rules:**
- See conventions/ subdirectory for rules not covered by tooling (if applicable)

---

*Convention analysis: [date]*
*Update when patterns change*
```

---

## Subdirectory Structure (For Complex Projects)

For projects with extensive style rules (Java with Checkstyle/PMD, detailed manual rules, tiered violations), use index + subdirectory:

```
.planning/codebase/
├── CONVENTIONS.md          # Compact index (~100-150 lines)
└── conventions/            # Detailed docs (loaded on-demand)
    ├── naming.md           # File, type, function, variable naming
    ├── style.md            # Formatting, braces, whitespace, line breaking
    ├── documentation.md    # Comments, doc blocks, API documentation
    ├── errors.md           # Exception handling, error types, messages
    └── validation.md       # Input validation, preconditions, invariants
```

### When to use subdirectory:
- Multiple enforcement tools (e.g., Checkstyle + PMD, ESLint + Prettier)
- Tiered violations (TIER1/TIER2/TIER3 or error/warning/info levels)
- 20+ distinct style rules observed
- Custom validation patterns (requireThat, zod, pydantic)
- Extensive documentation conventions (JavaDoc requirements, etc.)

### When subdirectory is optional:
- Small projects with simple conventions
- Projects relying entirely on automated formatting
- Greenfield projects without legacy patterns

---

## Subdirectory File Templates

### naming.md
```markdown
# Naming Conventions

**Language:** [Language]

## Files
[Detailed file naming rules with examples]
- Pattern: [e.g., "PascalCase.java matching class name"]
- Test files: [e.g., "*Test.java in src/test/java/"]
- Exceptions: [e.g., "package-info.java for package documentation"]

## Types (Classes/Interfaces/Records)
[Detailed type naming with examples]
- Classes: [e.g., "PascalCase nouns"]
- Interfaces: [e.g., "PascalCase, no I prefix"]
- Enums: [e.g., "PascalCase, values in UPPER_SNAKE_CASE"]

## Functions/Methods
[Detailed function naming with examples]
- General: [e.g., "camelCase verbs"]
- Getters: [e.g., "getPropertyName() or propertyName()"]
- Boolean: [e.g., "isEnabled(), hasPermission()"]

## Variables
[Detailed variable naming with examples]
- Local: [e.g., "camelCase descriptive names"]
- Constants: [e.g., "UPPER_SNAKE_CASE"]
- Avoid: [e.g., "single letters except loop indices"]

## Acronym Handling
[How to handle acronyms - e.g., "HttpClient not HTTPClient"]

---
*Update when patterns change*
```

### style.md
```markdown
# Code Style Rules

**Language:** [Language]
**Enforcement:** [Tools used]

## Formatting
[Detailed formatting rules]
- Indentation: [specifics]
- Line length: [limit and exceptions]
- Trailing whitespace: [policy]

## Brace Placement
[Brace style with examples - Allman vs K&R vs other]

## Line Breaking
[When/how to break lines]
- Break after: [operators, dots, commas]
- Continuation indent: [spaces]

## Whitespace
[Spacing rules around operators, brackets, etc.]

## Detection Patterns
[Commands to find violations - grep/checkstyle/eslint patterns]

---
*Update when patterns change*
```

### documentation.md
```markdown
# Documentation Conventions

**Language:** [Language]

## Doc Block Format
[JavaDoc/JSDoc/docstring format]
- Summary line: [requirements]
- @param/@returns/@throws: [when required]

## When to Document
[Required vs optional documentation]
- Public API: [e.g., "always required"]
- Internal: [e.g., "when non-obvious"]
- Tests: [e.g., "describe test purpose"]

## Parameter Documentation
[How to document parameters]
- Nullability: [e.g., "(may be null)" markers]
- Constraints: [e.g., "must be positive"]

## Example Documentation
[When/how to include code examples]

---
*Update when patterns change*
```

### errors.md
```markdown
# Error Handling Conventions

**Language:** [Language]

## Exception Types
[When to use each exception type]
- NullPointerException/TypeError: [when]
- IllegalArgumentException/ValueError: [when]
- IllegalStateException: [when]
- Custom exceptions: [when to create]

## Error Messages
[Message format and content]
- Include: [actual values, expected values, context]
- Avoid: [generic messages, stack traces in message]

## Exception Propagation
[When to catch vs propagate]
- Catch at: [boundaries, recovery points]
- Propagate: [when caller should handle]
- Wrap: [checked → unchecked patterns]

## Logging Errors
[Error logging patterns]
- Log level: [when error vs warn]
- Context: [what to include]

---
*Update when patterns change*
```

### validation.md
```markdown
# Validation Conventions

**Language:** [Language]

## Input Validation
[When and how to validate]
- Where: [public API boundaries, external input]
- How: [validation library, manual checks]

## Preconditions
[How to check preconditions]
- Library: [e.g., "requireThat()", "Objects.requireNonNull()", "assert"]
- Pattern: [fail-fast at method entry]

## Invariants
[Maintaining class/method invariants]
- Constructor: [validation pattern]
- Setters: [validation pattern]

## Validation Library
[Preferred validation approach with examples]

## @throws Documentation
[How to document validation exceptions]
- NullPointerException: [when thrown]
- IllegalArgumentException: [when thrown]

---
*Update when patterns change*
```

---

<good_examples>

### TypeScript Project (Inline Style)
```markdown
# Coding Conventions

**Analysis Date:** 2025-01-20
**Language:** TypeScript 5.3

## Quick Reference

| Category | Key Rules | Details |
|----------|-----------|---------|
| Naming | camelCase functions, PascalCase types | Inline below |
| Formatting | 2-space, 100 chars, Prettier | Inline below |
| Documentation | TSDoc for public exports | Inline below |
| Error Handling | throw at boundaries | Inline below |

## Naming Patterns

**Files:**
- kebab-case for all files (command-handler.ts, user-service.ts)
- *.test.ts alongside source files
- index.ts for barrel exports

**Types:**
- PascalCase for interfaces, no I prefix (User, not IUser)
- PascalCase for type aliases (UserConfig, ResponseData)
- PascalCase for enum names, UPPER_CASE for values (Status.PENDING)

**Functions:**
- camelCase for all functions
- No special prefix for async functions
- handleEventName for event handlers (handleClick, handleSubmit)

**Variables:**
- camelCase for variables
- UPPER_SNAKE_CASE for constants (MAX_RETRIES, API_BASE_URL)
- No underscore prefix (no private marker in TS)

## Code Style

**Formatting:**
- Prettier with .prettierrc
- 100 character line length
- Single quotes for strings
- Semicolons required
- 2 space indentation

**Linting:**
- ESLint with eslint.config.js
- Extends @typescript-eslint/recommended
- No console.log in production code (use logger)
- Run: npm run lint

## Import Organization

**Order:**
1. External packages (react, express, commander)
2. Internal modules (@/lib, @/services)
3. Relative imports (./utils, ../types)
4. Type imports (import type { User })

**Grouping:**
- Blank line between groups
- Alphabetical within each group
- Type imports last within each group

**Path Aliases:**
- @/ maps to src/
- No other aliases defined

## Error Handling

**Strategy:**
- Throw errors, catch at boundaries (route handlers, main functions)
- Extend Error class for custom errors (ValidationError, NotFoundError)
- Async functions use try/catch, no .catch() chains

**Never:**
- Catch and return fallback values
- Swallow errors silently

**Error Messages:**
- Include cause in error message: new Error('Failed to X', { cause: originalError })
- Log error with context before throwing: logger.error({ err, userId }, 'Failed to process')

## Logging

**Framework:**
- pino logger instance exported from lib/logger.ts
- Levels: debug, info, warn, error (no trace)

**Patterns:**
- Structured logging with context: logger.info({ userId, action }, 'User action')
- Log at service boundaries, not in utility functions
- Log state transitions, external API calls, errors
- No console.log in committed code

## Comments & Documentation

**When to Comment:**
- Explain why, not what: // Retry 3 times because API has transient failures
- Document business rules: // Users must verify email within 24 hours
- Explain non-obvious algorithms or workarounds
- Avoid obvious comments: // set count to 0

**TSDoc:**
- Required for public API functions
- Optional for internal functions if signature is self-explanatory
- Use @param, @returns, @throws tags

**TODO Comments:**
- Format: // TODO: description (no username, using git blame)
- Link to issue if exists: // TODO: Fix race condition (issue #123)

## Function Design

**Size:**
- Keep under 50 lines
- Extract helpers for complex logic
- One level of abstraction per function

**Parameters:**
- Max 3 parameters
- Use options object for 4+ parameters: function create(options: CreateOptions)
- Destructure in parameter list: function process({ id, name }: ProcessParams)

**Return Values:**
- Explicit return statements
- Return early for guard clauses
- Use Result<T, E> type for expected failures

## Module Design

**Exports:**
- Named exports preferred
- Default exports only for React components
- Export public API from index.ts barrel files

**Organization:**
- index.ts re-exports public API
- Keep internal helpers private (don't export from index)
- Avoid circular dependencies (import from specific files if needed)

## Style Enforcement

**Automated Tools:**
- Lint: `npm run lint`
- Format: `npm run format:check`

---

*Convention analysis: 2025-01-20*
*Update when patterns change*
```

### Java Project (Index + Subdirectory Style)
```markdown
# Coding Conventions

**Analysis Date:** 2025-01-20
**Language:** Java 21

## Quick Reference

| Category | Key Rules | Details |
|----------|-----------|---------|
| Naming | PascalCase types, camelCase methods | conventions/naming.md |
| Formatting | 4-space, 120 chars, Allman braces | conventions/style.md |
| Documentation | JavaDoc for public API | conventions/documentation.md |
| Error Handling | Fail-fast, requireThat() | conventions/errors.md |
| Validation | requireThat() library | conventions/validation.md |

## Naming Patterns

**Files:** PascalCase.java matching class name
**Types:** PascalCase for classes, interfaces, records, enums
**Methods:** camelCase, no abbreviations (getLineNumber not getLineNr)
**Constants:** UPPER_SNAKE_CASE for static final fields

## Code Style

**Formatting:** 4 spaces, 120 chars, Allman braces (opening brace on own line)
**Linting:** Checkstyle + PMD

## Import Organization

**Order:** java.* → javax.* → third-party → internal
**Grouping:** Blank line between groups, static imports last

## Error Handling

**Strategy:** Fail-fast with requireThat() validation at public API entry points
**Never:** Return null/empty on invalid input, swallow exceptions silently

## Logging

**Framework:** SLF4J + Logback
**Pattern:** Structured logging at service boundaries

## Comments & Documentation

**JavaDoc:** Required for public API, @param @return @throws tags
**Style:** Summary first, no blank lines around <p> tags

## Function Design

**Size:** Keep under 50 lines
**Parameters:** Max 3, use record for more

## Module Design

**Visibility:** Package-private by default, internal packages not exported
**Organization:** One class per file

## Style Enforcement

**Automated Tools:**
- Checkstyle: `./mvnw checkstyle:check`
- PMD: `./mvnw pmd:check`

**Manual Rules:** See conventions/style.md for TIER1-3 violations

---

*Index generated: 2025-01-20*
*See conventions/ subdirectory for detailed rules*
```

### Python Project (Inline Style)
```markdown
# Coding Conventions

**Analysis Date:** 2025-01-20
**Language:** Python 3.12

## Quick Reference

| Category | Key Rules | Details |
|----------|-----------|---------|
| Naming | snake_case functions, PascalCase classes | Inline below |
| Formatting | black + ruff, 88 chars | Inline below |
| Documentation | Google-style docstrings | Inline below |
| Error Handling | Custom exceptions, EAFP | Inline below |

## Naming Patterns

**Files:** snake_case.py
**Classes:** PascalCase
**Functions:** snake_case
**Constants:** UPPER_SNAKE_CASE
**Private:** _single_leading_underscore

## Code Style

**Formatting:** black (88 chars), 4-space indent
**Linting:** ruff
**Type Hints:** Required for public API

## Import Organization

**Order:** stdlib → third-party → local
**Grouping:** Blank line between groups
**Tool:** isort

## Error Handling

**Strategy:** EAFP (Easier to Ask Forgiveness than Permission)
**Custom Exceptions:** Inherit from Exception, named *Error
**Never:** Bare except clauses, silently passing exceptions

## Logging

**Framework:** logging module
**Pattern:** logger = logging.getLogger(__name__)

## Comments & Documentation

**Docstrings:** Google-style, required for public functions
**Type Hints:** Preferred over docstring types

## Function Design

**Size:** Keep focused, extract helpers
**Parameters:** Use *args/**kwargs sparingly, prefer explicit params

## Module Design

**Organization:** Related functions grouped, __all__ for public API
**Avoid:** Circular imports, star imports

## Style Enforcement

**Automated Tools:**
- Format: `black .`
- Lint: `ruff check .`
- Type check: `mypy .`

---

*Convention analysis: 2025-01-20*
```

</good_examples>

<guidelines>
**What belongs in CONVENTIONS.md:**
- Naming patterns observed in the codebase
- Formatting rules (tool config, linting rules)
- Import organization patterns
- Error handling strategy
- Logging approach
- Comment conventions
- Function and module design patterns

**What does NOT belong here:**
- Architecture decisions (that's ARCHITECTURE.md)
- Technology choices (that's STACK.md)
- Test patterns (that's TESTING.md)
- File organization (that's STRUCTURE.md)

**When filling this template:**
- Identify primary language first
- Check config files (.prettierrc, .eslintrc, checkstyle.xml, pyproject.toml)
- Examine 5-10 representative source files for patterns
- Look for consistency: if 80%+ follows a pattern, document it
- Be prescriptive: "Use X" not "Sometimes Y is used"
- Note deviations: "Legacy code uses Y, new code should use X"

**Sizing guidelines:**
- Inline style: Target 150-250 lines, max 300
- Index style: Target 80-120 lines, max 150
- Subdirectory files: 100-300 lines each

**Analysis approach:**
1. Identify primary language and version
2. Find style enforcement tools (config files)
3. Scan 5-10 files for patterns
4. Determine complexity (inline vs subdirectory)
5. Fill template with specific findings

**Load-on-demand pattern:**
- For simple projects: all conventions inline
- For complex projects: index always in context, subdirectory read when needed
- Claude reads subdirectory files only when:
  - Working on code in that category
  - User asks about specific conventions
  - Style violations detected
</guidelines>
