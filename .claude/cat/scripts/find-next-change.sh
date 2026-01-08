#!/bin/bash
# CAT Find Next Change Script
# Finds the next executable change based on dependency tracking
#
# Usage: scripts/find-next-change.sh
# Output: Path to next available change, or error message
#
# Exit codes:
#   0 - Found a change to execute (path printed to stdout)
#   1 - All changes complete
#   2 - Changes exist but all are blocked on dependencies

set -euo pipefail

PLANNING_DIR=".planning"

if [[ ! -d "$PLANNING_DIR" ]]; then
  echo "ERROR: No .planning directory found. Run /cat:new-project first." >&2
  exit 1
fi

# Find all CHANGE.md files
CHANGE_FILES=$(find "$PLANNING_DIR/releases" -name "*-CHANGE.md" 2>/dev/null | sort)

if [[ -z "$CHANGE_FILES" ]]; then
  echo "ERROR: No changes found. Run /cat:change-release first." >&2
  exit 1
fi

# Track blocked changes for reporting
declare -a BLOCKED_CHANGES
declare -a BLOCKED_REASONS

# Check each change
for CHANGE_PATH in $CHANGE_FILES; do
  CHANGE_DIR=$(dirname "$CHANGE_PATH")
  CHANGE_FILE=$(basename "$CHANGE_PATH")

  # Extract change ID (e.g., 01-01 from 01-01-setup-auth-CHANGE.md or 01-01-CHANGE.md)
  CHANGE_ID=$(echo "$CHANGE_FILE" | grep -oE '^[0-9]+(\.[0-9]+)?-[0-9]+')

  # Check if already completed (SUMMARY exists with same change ID prefix)
  SUMMARY_PATH=$(ls "${CHANGE_DIR}/${CHANGE_ID}"-*-SUMMARY.md 2>/dev/null | head -1)
  if [[ -z "$SUMMARY_PATH" ]]; then
    # Fallback to old format for backwards compatibility
    SUMMARY_PATH="${CHANGE_DIR}/${CHANGE_ID}-SUMMARY.md"
  fi
  if [[ -f "$SUMMARY_PATH" ]]; then
    continue  # Already done, skip
  fi

  # Extract depends from frontmatter
  DEPENDS=$(grep -A 10 '^---$' "$CHANGE_PATH" | grep '^depends:' | sed 's/depends:\s*//' | tr -d '[]"' | tr ',' ' ')

  # If no depends field or empty, change is ready
  if [[ -z "$DEPENDS" ]]; then
    echo "$CHANGE_PATH"
    exit 0
  fi

  # Check each dependency
  ALL_DEPS_MET=true
  MISSING_DEPS=""

  for DEP in $DEPENDS; do
    DEP=$(echo "$DEP" | tr -d ' ')  # Trim whitespace
    [[ -z "$DEP" ]] && continue

    # Find the dependency's SUMMARY.md (handles both old and new formats)
    DEP_SUMMARY=$(find "$PLANNING_DIR/releases" -name "${DEP}-*-SUMMARY.md" 2>/dev/null | head -1)
    if [[ -z "$DEP_SUMMARY" ]]; then
      # Fallback to old format
      DEP_SUMMARY=$(find "$PLANNING_DIR/releases" -name "${DEP}-SUMMARY.md" 2>/dev/null | head -1)
    fi

    if [[ -z "$DEP_SUMMARY" || ! -f "$DEP_SUMMARY" ]]; then
      ALL_DEPS_MET=false
      MISSING_DEPS="$MISSING_DEPS $DEP"
    fi
  done

  if [[ "$ALL_DEPS_MET" == "true" ]]; then
    echo "$CHANGE_PATH"
    exit 0
  else
    BLOCKED_CHANGES+=("$CHANGE_ID")
    BLOCKED_REASONS+=("waiting on:$MISSING_DEPS")
  fi
done

# Check if all changes are complete
TOTAL_CHANGES=$(echo "$CHANGE_FILES" | wc -l | tr -d ' ')
TOTAL_SUMMARIES=$(find "$PLANNING_DIR/releases" -name "*-SUMMARY.md" 2>/dev/null | wc -l | tr -d ' ')

if [[ "$TOTAL_SUMMARIES" -ge "$TOTAL_CHANGES" ]]; then
  echo "ALL_COMPLETE: All $TOTAL_CHANGES changes have been executed." >&2
  exit 1
fi

# Changes exist but all are blocked
echo "BLOCKED: All remaining changes are blocked on dependencies:" >&2
for i in "${!BLOCKED_CHANGES[@]}"; do
  echo "  - ${BLOCKED_CHANGES[$i]}: ${BLOCKED_REASONS[$i]}" >&2
done
exit 2
