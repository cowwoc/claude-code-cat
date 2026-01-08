# Multi-Agent Review System

Peer review from specialized perspectives to ensure quality.

## Review Agents

### Architect Agent
**Focus:** Design patterns, SOLID principles, separation of concerns

**Checklist:**
- [ ] Appropriate abstraction levels
- [ ] No tight coupling between modules
- [ ] Consistent architectural patterns
- [ ] Single responsibility per module
- [ ] Dependencies flow in correct direction

**Prompt Template:**
```
Review these changes as a software architect. Focus on:
- Design patterns and architectural consistency
- SOLID principles compliance
- Module boundaries and coupling
- Dependency direction and inversion

Changes to review:
{git_diff}

Provide: PASS or FAIL with specific issues.
```

### Security Agent
**Focus:** Security vulnerabilities, input validation, authentication

**Checklist:**
- [ ] OWASP Top 10 compliance
- [ ] Input sanitization on all user data
- [ ] Proper error handling (no sensitive data leakage)
- [ ] Authentication/authorization on protected routes
- [ ] No hardcoded secrets or credentials

**Prompt Template:**
```
Review these changes as a security engineer. Focus on:
- OWASP Top 10 vulnerabilities
- Input validation and sanitization
- Authentication and authorization
- Error handling and data exposure
- Secrets management

Changes to review:
{git_diff}

Provide: PASS or FAIL with specific vulnerabilities.
```

### Quality Agent
**Focus:** Test coverage, error handling, edge cases

**Checklist:**
- [ ] Adequate test coverage for new code
- [ ] All error paths handled
- [ ] Edge cases considered
- [ ] No obvious bugs or logic errors
- [ ] Proper null/undefined handling

**Prompt Template:**
```
Review these changes as a QA engineer. Focus on:
- Test coverage adequacy
- Error handling completeness
- Edge case coverage
- Logic correctness
- Defensive programming

Changes to review:
{git_diff}

Provide: PASS or FAIL with specific quality issues.
```

### Style Agent
**Focus:** Code style, formatting, conventions

**Checklist:**
- [ ] Project style guide compliance
- [ ] Consistent naming conventions
- [ ] Documentation completeness
- [ ] No commented-out code
- [ ] Appropriate code organization

**Prompt Template:**
```
Review these changes for code style. Focus on:
- Project style guide compliance
- Naming conventions
- Documentation quality
- Code organization
- Formatting consistency

Changes to review:
{git_diff}

Provide: PASS or FAIL with specific style issues.
```

### Performance Agent
**Focus:** Efficiency, scalability, resource usage

**Checklist:**
- [ ] No N+1 queries
- [ ] Appropriate data structures
- [ ] Memory/CPU considerations
- [ ] Caching where appropriate
- [ ] No unnecessary computations in loops

**Prompt Template:**
```
Review these changes for performance. Focus on:
- Query efficiency (N+1 problems)
- Algorithm complexity
- Memory usage patterns
- Caching opportunities
- Resource management

Changes to review:
{git_diff}

Provide: PASS or FAIL with specific performance issues.
```

## Agent Selection by Risk Level

| Risk Level | Agents Used |
|------------|-------------|
| HIGH | architect, security, quality, style, performance |
| MEDIUM | quality, style |
| LOW | style |

## Review Execution Flow

```
1. Execution completes
2. Determine risk level (auto or frontmatter)
3. Select agents based on risk
4. For each agent:
   a. Load agent prompt with focus areas
   b. Provide git diff of changes
   c. Collect findings (PASS/FAIL + issues)
5. Aggregate results:
   - All PASS → Proceed to user approval
   - Any FAIL → Present issues, require fixes
6. Generate review report
```

## Unanimous Approval Requirement

All selected agents must PASS for review to succeed. A single FAIL blocks progression until issues are resolved.

**Rationale:** Each agent represents a critical quality dimension. Allowing any to fail compromises the corresponding quality aspect.
