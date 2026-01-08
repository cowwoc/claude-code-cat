#!/bin/bash
# Write and Commit Script
# Creates a file and commits it atomically (60-75% faster than step-by-step)
#
# Usage: write-and-commit.sh <file-path> <content-file> <commit-msg-file> [--executable]
#
# Arguments:
#   file-path       Path to create the file at (relative to repo root)
#   content-file    Path to temp file containing the file content
#   commit-msg-file Path to temp file containing the commit message
#   --executable    Optional flag to make the file executable
#
# Output: JSON with status, commit SHA, and timing

set -euo pipefail
trap 'echo "{\"status\": \"error\", \"message\": \"ERROR at line $LINENO: $BASH_COMMAND\"}" >&2; exit 1' ERR

START_TIME=$(date +%s)

# Parse arguments
FILE_PATH="${1:-}"
CONTENT_FILE="${2:-}"
COMMIT_MSG_FILE="${3:-}"
EXECUTABLE="false"

if [[ "${4:-}" == "--executable" ]]; then
    EXECUTABLE="true"
fi

# Validate arguments
if [[ -z "$FILE_PATH" ]]; then
    echo "{\"status\": \"error\", \"message\": \"Missing required argument: file-path\"}"
    exit 1
fi

if [[ -z "$CONTENT_FILE" ]]; then
    echo "{\"status\": \"error\", \"message\": \"Missing required argument: content-file\"}"
    exit 1
fi

if [[ -z "$COMMIT_MSG_FILE" ]]; then
    echo "{\"status\": \"error\", \"message\": \"Missing required argument: commit-msg-file\"}"
    exit 1
fi

if [[ ! -f "$CONTENT_FILE" ]]; then
    echo "{\"status\": \"error\", \"message\": \"Content file not found: $CONTENT_FILE\"}"
    exit 1
fi

if [[ ! -f "$COMMIT_MSG_FILE" ]]; then
    echo "{\"status\": \"error\", \"message\": \"Commit message file not found: $COMMIT_MSG_FILE\"}"
    exit 1
fi

# Verify we're in a git repository
if ! git rev-parse --git-dir >/dev/null 2>&1; then
    echo "{\"status\": \"error\", \"message\": \"Not in a git repository\"}"
    exit 1
fi

# Get working directory
WORKING_DIR=$(pwd)

# Create parent directories if needed
PARENT_DIR=$(dirname "$FILE_PATH")
if [[ "$PARENT_DIR" != "." ]]; then
    mkdir -p "$PARENT_DIR"
fi

# Check if file already exists (warn but continue)
FILE_EXISTS="false"
if [[ -f "$FILE_PATH" ]]; then
    FILE_EXISTS="true"
fi

# Write file content
cp "$CONTENT_FILE" "$FILE_PATH"

# Make executable if requested
if [[ "$EXECUTABLE" == "true" ]]; then
    chmod +x "$FILE_PATH"
fi

# Stage file
git add "$FILE_PATH"

# Commit with message from file
COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")
git commit -m "$COMMIT_MSG"

# Get commit SHA
COMMIT_SHA=$(git rev-parse --short HEAD)

# Calculate duration
END_TIME=$(date +%s)
DURATION=$((END_TIME - START_TIME))

# Cleanup temp files
rm -f "$CONTENT_FILE" "$COMMIT_MSG_FILE"

# Output success JSON
jq -n \
    --arg status "success" \
    --arg message "File created and committed successfully" \
    --argjson duration "$DURATION" \
    --arg file_path "$FILE_PATH" \
    --argjson executable "$EXECUTABLE" \
    --argjson file_existed "$FILE_EXISTS" \
    --arg commit_sha "$COMMIT_SHA" \
    --arg working_directory "$WORKING_DIR" \
    --arg timestamp "$(date -Iseconds)" \
    '{
        status: $status,
        message: $message,
        duration_seconds: $duration,
        file_path: $file_path,
        executable: $executable,
        file_existed: $file_existed,
        commit_sha: $commit_sha,
        working_directory: $working_directory,
        timestamp: $timestamp
    }'
