#!/bin/bash
# CAT Find Next Plan Script
# Finds the next executable plan based on dependency tracking
#
# Usage: scripts/find-next-plan.sh
# Output: Path to next available plan, or error message
#
# Exit codes:
#   0 - Found a plan to execute (path printed to stdout)
#   1 - All plans complete
#   2 - Plans exist but all are blocked on dependencies

set -euo pipefail

PLANNING_DIR=".planning"

if [[ ! -d "$PLANNING_DIR" ]]; then
  echo "ERROR: No .planning directory found. Run /cat:new-project first." >&2
  exit 1
fi

# Find all PLAN.md files
PLAN_FILES=$(find "$PLANNING_DIR/phases" -name "*-PLAN.md" 2>/dev/null | sort)

if [[ -z "$PLAN_FILES" ]]; then
  echo "ERROR: No plans found. Run /cat:plan-phase first." >&2
  exit 1
fi

# Track blocked plans for reporting
declare -a BLOCKED_PLANS
declare -a BLOCKED_REASONS

# Check each plan
for PLAN_PATH in $PLAN_FILES; do
  PLAN_DIR=$(dirname "$PLAN_PATH")
  PLAN_FILE=$(basename "$PLAN_PATH")

  # Extract plan ID (e.g., 01-01 from 01-01-PLAN.md)
  PLAN_ID=$(echo "$PLAN_FILE" | sed 's/-PLAN\.md$//')

  # Check if already completed (SUMMARY exists)
  SUMMARY_PATH="${PLAN_DIR}/${PLAN_ID}-SUMMARY.md"
  if [[ -f "$SUMMARY_PATH" ]]; then
    continue  # Already done, skip
  fi

  # Extract depends from frontmatter
  DEPENDS=$(grep -A 10 '^---$' "$PLAN_PATH" | grep '^depends:' | sed 's/depends:\s*//' | tr -d '[]"' | tr ',' ' ')

  # If no depends field or empty, plan is ready
  if [[ -z "$DEPENDS" ]]; then
    echo "$PLAN_PATH"
    exit 0
  fi

  # Check each dependency
  ALL_DEPS_MET=true
  MISSING_DEPS=""

  for DEP in $DEPENDS; do
    DEP=$(echo "$DEP" | tr -d ' ')  # Trim whitespace
    [[ -z "$DEP" ]] && continue

    # Find the dependency's SUMMARY.md
    DEP_SUMMARY=$(find "$PLANNING_DIR/phases" -name "${DEP}-SUMMARY.md" 2>/dev/null | head -1)

    if [[ -z "$DEP_SUMMARY" || ! -f "$DEP_SUMMARY" ]]; then
      ALL_DEPS_MET=false
      MISSING_DEPS="$MISSING_DEPS $DEP"
    fi
  done

  if [[ "$ALL_DEPS_MET" == "true" ]]; then
    echo "$PLAN_PATH"
    exit 0
  else
    BLOCKED_PLANS+=("$PLAN_ID")
    BLOCKED_REASONS+=("waiting on:$MISSING_DEPS")
  fi
done

# Check if all plans are complete
TOTAL_PLANS=$(echo "$PLAN_FILES" | wc -l | tr -d ' ')
TOTAL_SUMMARIES=$(find "$PLANNING_DIR/phases" -name "*-SUMMARY.md" 2>/dev/null | wc -l | tr -d ' ')

if [[ "$TOTAL_SUMMARIES" -ge "$TOTAL_PLANS" ]]; then
  echo "ALL_COMPLETE: All $TOTAL_PLANS plans have been executed." >&2
  exit 1
fi

# Plans exist but all are blocked
echo "BLOCKED: All remaining plans are blocked on dependencies:" >&2
for i in "${!BLOCKED_PLANS[@]}"; do
  echo "  - ${BLOCKED_PLANS[$i]}: ${BLOCKED_REASONS[$i]}" >&2
done
exit 2
