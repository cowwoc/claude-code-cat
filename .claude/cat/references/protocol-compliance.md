# Protocol Compliance Audits

State machine tracking with audit trail.

## State Machine

```
PLAN_CREATED → PLAN_APPROVED → EXECUTING → EXECUTED →
BUILD_VERIFIED → REVIEWED → APPROVAL_PENDING → MERGED → COMPLETE
```

### State Definitions

| State | Description | Required Artifact |
|-------|-------------|-------------------|
| PLAN_CREATED | Plan file exists | PLAN.md |
| PLAN_APPROVED | User approved execution | Approval in audit trail |
| EXECUTING | Tasks being executed | - |
| EXECUTED | All tasks complete | Task commits |
| BUILD_VERIFIED | Build/test passed | Verification report |
| REVIEWED | Agent reviews complete | Review report |
| APPROVAL_PENDING | Awaiting final approval | - |
| MERGED | Changes merged to main | Merge commit |
| COMPLETE | Summary created, archived | SUMMARY.md |

### Valid Transitions

```
PLAN_CREATED → PLAN_APPROVED (user approval)
PLAN_APPROVED → EXECUTING (automatic)
EXECUTING → EXECUTED (all tasks done)
EXECUTED → BUILD_VERIFIED (verification pass)
BUILD_VERIFIED → REVIEWED (all agents pass)
REVIEWED → APPROVAL_PENDING (automatic)
APPROVAL_PENDING → MERGED (user approval)
MERGED → COMPLETE (summary created)

# Failure transitions
EXECUTING → PLAN_APPROVED (task failure, retry)
BUILD_VERIFIED → EXECUTING (test failure, fix needed)
REVIEWED → EXECUTING (review failure, fix needed)
```

## Compliance Checks

### Required Sequence
```yaml
sequence_rules:
  - PLAN_CREATED must precede EXECUTING
  - PLAN_APPROVED must precede EXECUTING (for HIGH/MEDIUM risk)
  - BUILD_VERIFIED must precede REVIEWED
  - REVIEWED must precede MERGED (for HIGH/MEDIUM risk)
  - APPROVAL_PENDING must precede MERGED
```

### Required Artifacts
```yaml
artifact_rules:
  PLAN_CREATED:
    - PLAN.md exists in phase directory
  EXECUTED:
    - All tasks have associated commits
    - No uncommitted changes
  BUILD_VERIFIED:
    - Verification report in STATE.md or separate file
  REVIEWED:
    - Review report exists
    - All required agents have reported
  COMPLETE:
    - SUMMARY.md exists
    - STATE.md updated with completion
```

### Violation Types
```yaml
violations:
  SKIPPED_STATE:
    severity: ERROR
    description: Required state was bypassed
    example: PLAN_CREATED → EXECUTING (skipped approval)

  MISSING_ARTIFACT:
    severity: ERROR
    description: Required artifact not found
    example: COMPLETE without SUMMARY.md

  WRONG_SEQUENCE:
    severity: ERROR
    description: States occurred out of order
    example: REVIEWED before BUILD_VERIFIED

  INCOMPLETE_REVIEW:
    severity: WARNING
    description: Not all required agents reported
    example: HIGH risk without security review
```

## Audit Trail

Track all state transitions in STATE.md:

```markdown
## Protocol Audit Trail

| Timestamp | State | Actor | Details |
|-----------|-------|-------|---------|
| 2025-01-15T10:00:00Z | PLAN_CREATED | agent | 02-01-setup-auth-PLAN.md created |
| 2025-01-15T10:05:00Z | PLAN_APPROVED | user | Approved via gate |
| 2025-01-15T10:06:00Z | EXECUTING | agent | Started task execution |
| 2025-01-15T10:45:00Z | EXECUTED | agent | 3/3 tasks complete |
| 2025-01-15T10:46:00Z | BUILD_VERIFIED | agent | Build/test passed |
| 2025-01-15T10:50:00Z | REVIEWED | agent | All agents PASS |
| 2025-01-15T10:51:00Z | APPROVAL_PENDING | agent | Awaiting user |
| 2025-01-15T11:00:00Z | MERGED | user | Approved merge |
| 2025-01-15T11:01:00Z | COMPLETE | agent | SUMMARY.md created |
```

## Compliance Report

Generated at plan completion:

```markdown
## Protocol Compliance Report

**Plan:** 02-01-setup-auth-PLAN.md
**Risk Level:** HIGH
**Compliance Status:** COMPLIANT

### State Sequence
✓ PLAN_CREATED (10:00:00)
✓ PLAN_APPROVED (10:05:00) - Required for HIGH risk
✓ EXECUTING (10:06:00)
✓ EXECUTED (10:45:00)
✓ BUILD_VERIFIED (10:46:00)
✓ REVIEWED (10:50:00) - All 5 agents
✓ APPROVAL_PENDING (10:51:00)
✓ MERGED (11:00:00)
✓ COMPLETE (11:01:00)

### Artifact Verification
✓ PLAN.md exists
✓ 3 task commits found
✓ Verification report present
✓ Review report present (5/5 agents)
✓ SUMMARY.md created

### Violations
None

### Duration
Total: 61 minutes
Execution: 39 minutes
Review: 4 minutes
Approval wait: 9 minutes
```

## Configuration

In `.planning/config.json`:

```json
{
  "compliance": {
    "enabled": true,
    "strict_mode": true,
    "audit_trail": true,
    "generate_report": true,
    "fail_on_violation": true
  }
}
```

### Strict Mode
When enabled:
- All state transitions validated
- Missing artifacts block progression
- Violations logged and reported
- Cannot bypass without explicit override

### Non-Strict Mode
When disabled:
- Transitions tracked but not enforced
- Warnings instead of errors
- Useful for rapid prototyping
