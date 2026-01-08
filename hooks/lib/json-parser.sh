#!/bin/bash
# Library for JSON parsing utilities
# Source this file in hooks that need to parse JSON input

# Extract a single JSON value
# Usage: extract_json_value "$json" "key"
# Returns: The value of the key, or empty string if not found
extract_json_value() {
    local json="$1"
    local key="$2"
    # Use grep and sed for basic JSON parsing (allow grep to fail without triggering set -e)
    echo "$json" | grep -o "\"$key\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | sed "s/\"$key\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\"/\1/" || true
}

# Cache for parsed JSON to avoid redundant extractions
declare -gA _JSON_CACHE

# Extract and cache JSON value for multiple accesses
# Usage: extract_json_cached "$json" "key"
# Benefits: Use this when extracting multiple fields from same JSON
extract_json_cached() {
    local json="$1"
    local key="$2"
    local cache_key="${json:0:100}:$key"  # Use first 100 chars as cache key

    if [[ -n "${_JSON_CACHE[$cache_key]}" ]]; then
        echo "${_JSON_CACHE[$cache_key]}"
        return 0
    fi

    local value
    value=$(extract_json_value "$json" "$key")
    _JSON_CACHE[$cache_key]="$value"
    echo "$value"
}

# Parse all common hook JSON fields at once
# Usage: parse_hook_json "$JSON_INPUT"
# Sets global variables: HOOK_EVENT, SESSION_ID, USER_PROMPT, TOOL_NAME, TOOL_INPUT_JSON
parse_hook_json() {
    local json="$1"

    # Extract all common fields in one pass
    HOOK_EVENT=$(extract_json_value "$json" "hook_event_name")
    SESSION_ID=$(extract_json_value "$json" "session_id")

    # Try multiple possible fields for user prompt
    USER_PROMPT=$(extract_json_value "$json" "message")
    [[ -z "$USER_PROMPT" ]] && USER_PROMPT=$(extract_json_value "$json" "user_message")
    [[ -z "$USER_PROMPT" ]] && USER_PROMPT=$(extract_json_value "$json" "prompt")

    # Tool-related fields for PreToolUse/PostToolUse hooks
    TOOL_NAME=$(extract_json_value "$json" "tool_name")
    TOOL_INPUT_JSON=$(echo "$json" | grep -o '"tool_input"[[:space:]]*:[[:space:]]*{[^}]*}' | sed 's/"tool_input"[[:space:]]*:[[:space:]]*//' || true)
}

# Validate and sanitize session ID
# Usage: validate_session_id "$session_id_raw"
# Returns: Sanitized session ID (only alphanumeric, dash, underscore)
validate_session_id() {
    local session_id_raw="$1"

    [[ -z "$session_id_raw" ]] && return 1

    # Sanitize: only allow alphanumeric, dash, underscore
    echo "$session_id_raw" | tr -cd 'a-zA-Z0-9_-'
}
