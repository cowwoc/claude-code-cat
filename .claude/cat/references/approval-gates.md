# Mandatory Approval Gates

User approval checkpoints at critical transitions.

## Gate Types

### Change Approval Gate
**When:** After change creation, before execution
**Required for:** HIGH (mandatory), MEDIUM (mandatory), LOW (optional based on config)

**Prompt:**
```
════════════════════════════════════════
APPROVAL REQUIRED: Change Execution
════════════════════════════════════════

Change: {release}-{change}-CHANGE.md
Risk Level: {risk_level}
Reason: {risk_reason}

Tasks: {task_count}
Files affected: {file_list}
Estimated scope: {scope_estimate}

Review the change and approve to proceed.

Options:
- approve: Execute the change as written
- adjust: Request modifications before execution
- cancel: Abort this change

════════════════════════════════════════
```

### Review Approval Gate
**When:** After agent reviews complete, before marking as done
**Required for:** HIGH (mandatory), MEDIUM (mandatory), LOW (skip in yolo mode)

**Prompt:**
```
════════════════════════════════════════
APPROVAL REQUIRED: Review Complete
════════════════════════════════════════

Change: {release}-{change}
Review Status: {all_pass ? "ALL PASS" : "ISSUES FOUND"}

Agent Results:
{agent_results_summary}

{if issues}
Issues to address:
{issue_list}
{endif}

Options:
- approve: Accept changes as reviewed
- fix: Address issues before approval
- cancel: Reject changes

════════════════════════════════════════
```

### Merge Approval Gate
**When:** Before merging completed work to main branch
**Required for:** ALL risk levels (can auto-approve LOW in yolo mode)

**Prompt:**
```
════════════════════════════════════════
APPROVAL REQUIRED: Merge to Main
════════════════════════════════════════

Change: {release}-{change}
Branch: {feature_branch} → main

Changes:
- {commit_count} commits
- {files_changed} files changed
- {insertions}+ / {deletions}-

Verification: {verification_status}
Review: {review_status}

Options:
- merge: Proceed with merge
- cancel: Keep changes on branch

════════════════════════════════════════
```

## Gate Behavior by Risk Level

| Gate | HIGH | MEDIUM | LOW (interactive) | LOW (yolo) |
|------|------|--------|-------------------|------------|
| Change Approval | Required | Required | Required | Auto-approve |
| Review Approval | Required | Required | Required | Skip |
| Merge Approval | Required | Required | Required | Auto-approve |

## Configuration

In `.planning/config.json`:

```json
{
  "gates": {
    "change_approval": true,
    "review_approval": true,
    "merge_approval": true
  },
  "auto_approve": {
    "low_risk_changes": false,
    "low_risk_reviews": false,
    "low_risk_merges": false
  }
}
```

## Gate Timeout Behavior

If user doesn't respond within session:
1. Gate remains pending
2. State saved to STATE.md
3. Next session resumes at gate
4. No automatic progression without approval

## Bypassing Gates (Emergency Only)

Gates can be bypassed with explicit user command:
```
/cat:force-approve {gate_type} --reason "{justification}"
```

Bypass is logged in audit trail with timestamp and reason.
