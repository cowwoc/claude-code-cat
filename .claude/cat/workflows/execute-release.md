<purpose>
Execute a release prompt (CHANGE.md) and create the outcome summary (SUMMARY.md).
</purpose>

<required_reading>
Read STATE.md before any operation to load project context.
</required_reading>

<process>

<step name="load_project_state" priority="first">
Before any operation, read project state:

```bash
cat .planning/STATE.md 2>/dev/null
```

**If file exists:** Parse and internalize:

- Current position (release, change, status)
- Accumulated decisions (constraints on this execution)
- Deferred issues (context for deviations)
- Blockers/concerns (things to watch for)
- Brief alignment status

**If file missing but .planning/ exists:**

```
STATE.md missing but planning artifacts exist.
Options:
1. Reconstruct from existing artifacts
2. Continue without project state (may lose accumulated context)
```

**If .planning/ doesn't exist:** Error - project not initialized.

This ensures every execution has full project context.
</step>

<step name="acquire_execution_lock">
Acquire execution lock to prevent concurrent change execution:

```bash
LOCK_DIR="${LOCK_DIR:-.claude/locks}"
PROJECT_NAME="${PWD##*/}"
LOCK_FILE="$LOCK_DIR/${PROJECT_NAME}.lock"
TIMESTAMP=$(date +%s)

mkdir -p "$LOCK_DIR"

# Check for existing lock
if [[ -f "$LOCK_FILE" ]]; then
    source "$LOCK_FILE"
    AGE_MINUTES=$(( (TIMESTAMP - LOCK_TIMESTAMP) / 60 ))

    if [[ "$AGE_MINUTES" -lt 30 ]]; then
        echo "⚠️  Another execution may be in progress"
        echo "Lock file: $LOCK_FILE"
        echo "Age: ${AGE_MINUTES} minutes"
        echo ""
        echo "If no other session is active, delete the lock: rm $LOCK_FILE"
        # Advisory only - proceed anyway
    fi
fi

# Create/update lock file
cat > "$LOCK_FILE" << EOF
LOCK_SESSION_ID="execution-$(date +%s)"
LOCK_TIMESTAMP=$TIMESTAMP
LOCK_PWD="$PWD"
EOF

echo "Execution lock acquired: $LOCK_FILE"
```

**Note:** Lock is released on change completion or SessionEnd (fallback cleanup).
</step>

<step name="identify_plan">
Find the next change to execute:
- Check roadmap for "In progress" release
- Find changes in that release directory
- Identify first change without corresponding SUMMARY

```bash
cat .planning/ROADMAP.md
# Look for release with "In progress" status
# Then find changes in that release
ls .planning/releases/XX-name/*-CHANGE.md 2>/dev/null | sort
ls .planning/releases/XX-name/*-SUMMARY.md 2>/dev/null | sort
```

**Logic:**

- If `01-01-setup-auth-CHANGE.md` exists but `01-01-setup-auth-SUMMARY.md` doesn't → execute 01-01
- If `01-01-setup-auth-SUMMARY.md` exists but `01-02-*-SUMMARY.md` doesn't → execute 01-02
- Pattern: Find first CHANGE file without matching SUMMARY file (match by change ID prefix)

**Decimal release handling:**

Release directories can be integer or decimal format:

- Integer: `.planning/releases/01-foundation/01-01-setup-auth-CHANGE.md`
- Decimal: `.planning/releases/01.1-hotfix/01.1-01-fix-bug-CHANGE.md`

Parse release number from path (handles both formats):

```bash
# Extract release number (handles XX or XX.Y format)
RELEASE=$(echo "$CHANGE_PATH" | grep -oE '[0-9]+(\.[0-9]+)?-[0-9]+')
```

SUMMARY naming follows same pattern (uses same slug as CHANGE):

- Integer: `01-01-setup-auth-SUMMARY.md`
- Decimal: `01.1-01-fix-bug-SUMMARY.md`

Confirm with user if ambiguous.

<config-check>
```bash
cat .planning/config.json 2>/dev/null
```
</config-check>

<if mode="yolo">
```
⚡ Auto-approved: Execute {release}-{change}-{slug}-CHANGE.md
[Change X of Y for Release Z]

Starting execution...
```

Proceed directly to parse_segments step.
</if>

<if mode="interactive" OR="custom with gates.execute_next_change true">
Present:

```
Found change to execute: {release}-{change}-{slug}-CHANGE.md
[Change X of Y for Release Z]

Proceed with execution?
```

Wait for confirmation before proceeding.
</if>
</step>

<step name="record_start_time">
Record execution start time for performance tracking:

```bash
CHANGE_START_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
CHANGE_START_EPOCH=$(date +%s)
```

Store in shell variables for duration calculation at completion.
</step>

<step name="parse_segments">
**Intelligent segmentation: Parse change into execution segments.**

Changes are divided into segments by checkpoints. Each segment is routed to optimal execution context (subagent or main).

**1. Check for checkpoints:**

```bash
# Find all checkpoints and their types
grep -n "type=\"checkpoint" .planning/releases/XX-name/{release}-{change}-{slug}-CHANGE.md
```

**2. Analyze execution strategy:**

**If NO checkpoints found:**

- **Fully autonomous change** - spawn single subagent for entire change
- Subagent gets fresh 200k context, executes all tasks, creates SUMMARY, commits
- Main context: Just orchestration (~5% usage)

**If checkpoints found, parse into segments:**

Segment = tasks between checkpoints (or start→first checkpoint, or last checkpoint→end)

**For each segment, determine routing:**

```
Segment routing rules:

IF segment has no prior checkpoint:
  → SUBAGENT (first segment, nothing to depend on)

IF segment follows checkpoint:human-verify:
  → SUBAGENT (verification is just confirmation, doesn't affect next work)

IF segment follows checkpoint:decision OR checkpoint:human-action:
  → MAIN CONTEXT (next tasks need the decision/result)
```

**3. Execution pattern:**

**Pattern A: Fully autonomous (no checkpoints)**

```
Spawn subagent → execute all tasks → SUMMARY → commit → report back
```

**Pattern B: Segmented with verify-only checkpoints**

```
Segment 1 (tasks 1-3): Spawn subagent → execute → report back
Checkpoint 4 (human-verify): Main context → you verify → continue
Segment 2 (tasks 5-6): Spawn NEW subagent → execute → report back
Checkpoint 7 (human-verify): Main context → you verify → continue
Aggregate results → SUMMARY → commit
```

**Pattern C: Decision-dependent (must stay in main)**

```
Checkpoint 1 (decision): Main context → you decide → continue in main
Tasks 2-5: Main context (need decision from checkpoint 1)
No segmentation benefit - execute entirely in main
```

**4. Why this works:**

**Segmentation benefits:**

- Fresh context for each autonomous segment (0% start every time)
- Main context only for checkpoints (~10-20% total)
- Can handle 10+ task changes if properly segmented
- Quality impossible to degrade in autonomous segments

**When segmentation provides no benefit:**

- Checkpoint is decision/human-action and following tasks depend on outcome
- Better to execute sequentially in main than break flow

**5. Implementation:**

**For fully autonomous changes:**

```
Use Task tool with subagent_type="general-purpose":

Prompt: "Execute change at .planning/releases/{release}-{change}-{slug}-CHANGE.md

This is an autonomous change (no checkpoints). Execute all tasks, create SUMMARY.md in release directory, commit with message following change's commit guidance.

Follow all deviation rules and authentication gate protocols from the change.

When complete, report: change name, tasks completed, SUMMARY path, commit hash."
```

**For segmented changes (has verify-only checkpoints):**

```
Execute segment-by-segment:

For each autonomous segment:
  Spawn subagent with prompt: "Execute tasks [X-Y] from change at .planning/releases/{release}-{change}-{slug}-CHANGE.md. Read the change for full context and deviation rules. Do NOT create SUMMARY or commit - just execute these tasks and report results."

  Wait for subagent completion

For each checkpoint:
  Execute in main context
  Wait for user interaction
  Continue to next segment

After all segments complete:
  Aggregate all results
  Create SUMMARY.md
  Commit with all changes
```

**For decision-dependent changes:**

```
Execute in main context (standard flow below)
No subagent routing
Quality maintained through small scope (2-3 tasks per change)
```

See step name="segment_execution" for detailed segment execution loop.
</step>

<step name="segment_execution">
**Detailed segment execution loop for segmented changes.**

**This step applies ONLY to segmented changes (Pattern B: has checkpoints, but they're verify-only).**

For Pattern A (fully autonomous) and Pattern C (decision-dependent), skip this step.

**Execution flow:**

````
1. Parse change to identify segments:
   - Read change file
   - Find checkpoint locations: grep -n "type=\"checkpoint" CHANGE.md
   - Identify checkpoint types: grep "type=\"checkpoint" CHANGE.md | grep -o 'checkpoint:[^"]*'
   - Build segment map:
     * Segment 1: Start → first checkpoint (tasks 1-X)
     * Checkpoint 1: Type and location
     * Segment 2: After checkpoint 1 → next checkpoint (tasks X+1 to Y)
     * Checkpoint 2: Type and location
     * ... continue for all segments

2. For each segment in order:

   A. Determine routing (apply rules from parse_segments):
      - No prior checkpoint? → Subagent
      - Prior checkpoint was human-verify? → Subagent
      - Prior checkpoint was decision/human-action? → Main context

   B. If routing = Subagent:
      ```
      Spawn Task tool with subagent_type="general-purpose":

      Prompt: "Execute tasks [task numbers/names] from change at [change path].

      **Context:**
      - Read the full change for objective, context files, and deviation rules
      - You are executing a SEGMENT of this change (not the full change)
      - Other segments will be executed separately

      **Your responsibilities:**
      - Execute only the tasks assigned to you
      - Follow all deviation rules and authentication gate protocols
      - Track deviations for later Summary
      - DO NOT create SUMMARY.md (will be created after all segments complete)
      - DO NOT commit (will be done after all segments complete)

      **Report back:**
      - Tasks completed
      - Files created/modified
      - Deviations encountered
      - Any issues or blockers"

      Wait for subagent to complete
      Capture results (files changed, deviations, etc.)
      ```

   C. If routing = Main context:
      Execute tasks in main using standard execution flow (step name="execute")
      Track results locally

   D. After segment completes (whether subagent or main):
      Continue to next checkpoint/segment

3. After ALL segments complete:

   A. Aggregate results from all segments:
      - Collect files created/modified from all segments
      - Collect deviations from all segments
      - Collect decisions from all checkpoints
      - Merge into complete picture

   B. Create SUMMARY.md:
      - Use aggregated results
      - Document all work from all segments
      - Include deviations from all segments
      - Note which segments were subagented

   C. Commit:
      - Stage all files from all segments
      - Stage SUMMARY.md
      - Commit with message following change guidance
      - Include note about segmented execution if relevant

   D. Report completion

**Example execution trace:**

````

Change: 01-02-add-user-model-CHANGE.md (8 tasks, 2 verify checkpoints)

Parsing segments...

- Segment 1: Tasks 1-3 (autonomous)
- Checkpoint 4: human-verify
- Segment 2: Tasks 5-6 (autonomous)
- Checkpoint 7: human-verify
- Segment 3: Task 8 (autonomous)

Routing analysis:

- Segment 1: No prior checkpoint → SUBAGENT ✓
- Checkpoint 4: Verify only → MAIN (required)
- Segment 2: After verify → SUBAGENT ✓
- Checkpoint 7: Verify only → MAIN (required)
- Segment 3: After verify → SUBAGENT ✓

Execution:
[1] Spawning subagent for tasks 1-3...
→ Subagent completes: 3 files modified, 0 deviations
[2] Executing checkpoint 4 (human-verify)...
════════════════════════════════════════
CHECKPOINT: Verification Required
Task 4 of 8: Verify database schema
I built: User and Session tables with relations
How to verify: Check src/db/schema.ts for correct types
════════════════════════════════════════
User: "approved"
[3] Spawning subagent for tasks 5-6...
→ Subagent completes: 2 files modified, 1 deviation (added error handling)
[4] Executing checkpoint 7 (human-verify)...
User: "approved"
[5] Spawning subagent for task 8...
→ Subagent completes: 1 file modified, 0 deviations

Aggregating results...

- Total files: 6 modified
- Total deviations: 1
- Segmented execution: 3 subagents, 2 checkpoints

Creating SUMMARY.md...
Committing...
✓ Complete

````

**Benefits of this pattern:**
- Main context usage: ~20% (just orchestration + checkpoints)
- Subagent 1: Fresh 0-30% (tasks 1-3)
- Subagent 2: Fresh 0-30% (tasks 5-6)
- Subagent 3: Fresh 0-20% (task 8)
- All autonomous work: Peak quality
- Can handle large changes with many tasks if properly segmented

**When NOT to use segmentation:**
- Change has decision/human-action checkpoints that affect following tasks
- Following tasks depend on checkpoint outcome
- Better to execute in main sequentially in those cases
</step>

<step name="load_prompt">
Read the change prompt:
```bash
cat .planning/releases/XX-name/{release}-{change}-{slug}-CHANGE.md
````

This IS the execution instructions. Follow it exactly.

**If change references CONTEXT.md:**
The CONTEXT.md file provides the user's vision for this release — how they imagine it working, what's essential, and what's out of scope. Honor this context throughout execution.
</step>

<step name="previous_phase_check">
Before executing, check if previous release had issues:

```bash
# Find previous release summary
ls .planning/releases/*/SUMMARY.md 2>/dev/null | sort -r | head -2 | tail -1
```

If previous release SUMMARY.md has "Issues Encountered" != "None" or "Next Release Readiness" mentions blockers:

Use AskUserQuestion:

- header: "Previous Issues"
- question: "Previous release had unresolved items: [summary]. How to proceed?"
- options:
  - "Proceed anyway" - Issues won't block this release
  - "Address first" - Let's resolve before continuing
  - "Review previous" - Show me the full summary
    </step>

<step name="execute">
Execute each task in the prompt. **Deviations are normal** - handle them automatically using embedded rules below.

1. Read the @context files listed in the prompt

2. For each task:

   **If `type="auto"`:**

   **Before executing:** Check if task has `tdd="true"` attribute:
   - If yes: Follow TDD execution flow (see `<tdd_execution>`) - RED → GREEN → REFACTOR cycle with atomic commits per stage
   - If no: Standard implementation

   - Work toward task completion
   - **If CLI/API returns authentication error:** Handle as authentication gate (see below)
   - **When you discover additional work not in change:** Apply deviation rules (see below) automatically
   - Continue implementing, applying rules as needed
   - Run the verification
   - Confirm done criteria met
   - **Commit the task** (see `<task_commit>` below)
   - Track task completion and commit hash for Summary documentation
   - Continue to next task

   **If `type="checkpoint:*"`:**

   - STOP immediately (do not continue to next task)
   - Execute checkpoint_protocol (see below)
   - Wait for user response
   - Verify if possible (check files, env vars, etc.)
   - Only after user confirmation: continue to next task

3. Run overall verification checks from `<verification>` section
4. Confirm all success criteria from `<success_criteria>` section met
5. Document all deviations in Summary (automatic - see deviation_documentation below)
   </step>

<authentication_gates>

## Handling Authentication Errors During Execution

**When you encounter authentication errors during `type="auto"` task execution:**

This is NOT a failure. Authentication gates are expected and normal. Handle them dynamically:

**Authentication error indicators:**

- CLI returns: "Error: Not authenticated", "Not logged in", "Unauthorized", "401", "403"
- API returns: "Authentication required", "Invalid API key", "Missing credentials"
- Command fails with: "Please run {tool} login" or "Set {ENV_VAR} environment variable"

**Authentication gate protocol:**

1. **Recognize it's an auth gate** - Not a bug, just needs credentials
2. **STOP current task execution** - Don't retry repeatedly
3. **Create dynamic checkpoint:human-action** - Present it to user immediately
4. **Provide exact authentication steps** - CLI commands, where to get keys
5. **Wait for user to authenticate** - Let them complete auth flow
6. **Verify authentication works** - Test that credentials are valid
7. **Retry the original task** - Resume automation where you left off
8. **Continue normally** - Don't treat this as an error in Summary

**Example: Vercel deployment hits auth error**

```
Task 3: Deploy to Vercel
Running: vercel --yes

Error: Not authenticated. Please run 'vercel login'

[Create checkpoint dynamically]

════════════════════════════════════════
CHECKPOINT: Authentication Required
════════════════════════════════════════

Task 3 of 8: Authenticate Vercel CLI

I tried to deploy but got authentication error.

What you need to do:
Run: vercel login

This will open your browser - complete the authentication flow.

I'll verify after: vercel whoami returns your account

Type "done" when authenticated
════════════════════════════════════════

[Wait for user response]

[User types "done"]

Verifying authentication...
Running: vercel whoami
✓ Authenticated as: user@example.com

Retrying deployment...
Running: vercel --yes
✓ Deployed to: https://myapp-abc123.vercel.app

Task 3 complete. Continuing to task 4...
```

**In Summary documentation:**

Document authentication gates as normal flow, not deviations:

```markdown
## Authentication Gates

During execution, I encountered authentication requirements:

1. Task 3: Vercel CLI required authentication
   - Paused for `vercel login`
   - Resumed after authentication
   - Deployed successfully

These are normal gates, not errors.
```

**Key principles:**

- Authentication gates are NOT failures or bugs
- They're expected interaction points during first-time setup
- Handle them gracefully and continue automation after unblocked
- Don't mark tasks as "failed" or "incomplete" due to auth gates
- Document them as normal flow, separate from deviations
  </authentication_gates>

<user_initiated_plan_changes>

## Handling User Feedback That Changes the Change

**When user feedback during execution changes the change design:**

User questions like "Why are we doing X?" or "Isn't Y redundant?" often signal change improvements.

**Protocol:**

1. **Acknowledge the feedback**: "You're right, that's a valid point."

2. **Explicitly announce the change change**:
   ```
   Based on your feedback, I'm modifying the change:

   **Before**: [original approach]
   **After**: [new approach]
   **Reason**: [user's insight]
   ```

3. **Update the CHANGE.md file immediately** to reflect the change

4. **Continue execution** with the modified change

**Why this matters:**
- User should always know what's being implemented vs. what was planned
- Change document should be source of truth (not conversation history)
- Prevents confusion about actual deliverables

</user_initiated_plan_changes>

<implementation_discovery_changes>

## Handling Discoveries During Implementation

**When implementation reveals the change was based on incorrect assumptions:**

Before implementing a task, if you discover existing code/APIs that differ from change assumptions:

**Protocol:**

1. **Stop before implementing**

2. **Announce the discovery**:
   ```
   ## Implementation Discovery

   **Change assumed**: [what the change said]
   **Code actually**: [what the existing code does]
   **Impact**: [how this affects the implementation]
   ```

3. **Update the CHANGE.md** to reflect the actual implementation approach

4. **Ask user if unclear**: If the discovery significantly changes scope or approach, confirm before proceeding

**Examples:**
- Change says "default: available processors" but existing API uses memory-based calculation
- Change says "add new method" but similar method already exists
- Change says "modify X" but X was refactored/renamed since planning

**Why this matters:**
- Changes are written before deep codebase investigation
- Implementation often reveals assumptions were wrong
- User should know what's actually being implemented

</implementation_discovery_changes>

<deviation_rules>

## Automatic Deviation Handling

**While executing tasks, you WILL discover work not in the change.** This is normal.

Apply these rules automatically. Track all deviations for Summary documentation.

---

**RULE 1: Auto-fix bugs**

**Trigger:** Code doesn't work as intended (broken behavior, incorrect output, errors)

**Action:** Fix immediately, track for Summary

**Examples:**

- Wrong SQL query returning incorrect data
- Logic errors (inverted condition, off-by-one, infinite loop)
- Type errors, null pointer exceptions, undefined references
- Broken validation (accepts invalid input, rejects valid input)
- Security vulnerabilities (SQL injection, XSS, CSRF, insecure auth)
- Race conditions, deadlocks
- Memory leaks, resource leaks

**Process:**

1. Fix the bug inline
2. Add/update tests to prevent regression
3. Verify fix works
4. Continue task
5. Track in deviations list: `[Rule 1 - Bug] [description]`

**No user permission needed.** Bugs must be fixed for correct operation.

---

**RULE 2: Auto-add missing critical functionality**

**Trigger:** Code is missing essential features for correctness, security, or basic operation

**Action:** Add immediately, track for Summary

**Examples:**

- Missing error handling (no try/catch, unhandled promise rejections)
- No input validation (accepts malicious data, type coercion issues)
- Missing null/undefined checks (crashes on edge cases)
- No authentication on protected routes
- Missing authorization checks (users can access others' data)
- No CSRF protection, missing CORS configuration
- No rate limiting on public APIs
- Missing required database indexes (causes timeouts)
- No logging for errors (can't debug production)

**Process:**

1. Add the missing functionality inline
2. Add tests for the new functionality
3. Verify it works
4. Continue task
5. Track in deviations list: `[Rule 2 - Missing Critical] [description]`

**Critical = required for correct/secure/performant operation**
**No user permission needed.** These are not "features" - they're requirements for basic correctness.

---

**RULE 3: Auto-fix blocking issues**

**Trigger:** Something prevents you from completing current task

**Action:** Fix immediately to unblock, track for Summary

**Examples:**

- Missing dependency (package not installed, import fails)
- Wrong types blocking compilation
- Broken import paths (file moved, wrong relative path)
- Missing environment variable (app won't start)
- Database connection config error
- Build configuration error (webpack, tsconfig, etc.)
- Missing file referenced in code
- Circular dependency blocking module resolution

**Process:**

1. Fix the blocking issue
2. Verify task can now proceed
3. Continue task
4. Track in deviations list: `[Rule 3 - Blocking] [description]`

**No user permission needed.** Can't complete task without fixing blocker.

---

**RULE 4: Ask about architectural changes**

**Trigger:** Fix/addition requires significant structural modification

**Action:** STOP, present to user, wait for decision

**Examples:**

- Adding new database table (not just column)
- Major schema changes (changing primary key, splitting tables)
- Introducing new service layer or architectural pattern
- Switching libraries/frameworks (React → Vue, REST → GraphQL)
- Changing authentication approach (sessions → JWT)
- Adding new infrastructure (message queue, cache layer, CDN)
- Changing API contracts (breaking changes to endpoints)
- Adding new deployment environment

**Process:**

1. STOP current task
2. Present clearly:

```
⚠️ Architectural Decision Needed

Current task: [task name]
Discovery: [what you found that prompted this]
Proposed change: [architectural modification]
Why needed: [rationale]
Impact: [what this affects - APIs, deployment, dependencies, etc.]
Alternatives: [other approaches, or "none apparent"]

Proceed with proposed change? (yes / different approach / defer)
```

3. WAIT for user response
4. If approved: implement, track as `[Rule 4 - Architectural] [description]`
5. If different approach: discuss and implement
6. If deferred: log to ISSUES.md, continue without change

**User decision required.** These changes affect system design.

---

**RULE 5: Log non-critical enhancements**

**Trigger:** Improvement that would enhance code but isn't essential now

**Action:** Add to .planning/ISSUES.md automatically, continue task

**Examples:**

- Performance optimization (works correctly, just slower than ideal)
- Code refactoring (works, but could be cleaner/DRY-er)
- Better naming (works, but variables could be clearer)
- Organizational improvements (works, but file structure could be better)
- Nice-to-have UX improvements (works, but could be smoother)
- Additional test coverage beyond basics (basics exist, could be more thorough)
- Documentation improvements (code works, docs could be better)
- Accessibility enhancements beyond minimum

**Process:**

1. Create .planning/ISSUES.md if doesn't exist (use `~/.claude/cat/templates/issues.md`)
2. Add entry with ISS-XXX number (auto-increment)
3. Brief notification: `📋 Logged enhancement: [brief] (ISS-XXX)`
4. Continue task without implementing

**No user permission needed.** Logging for future consideration.

---

**RULE PRIORITY (when multiple could apply):**

1. **If Rule 4 applies** → STOP and ask (architectural decision)
2. **If Rules 1-3 apply** → Fix automatically, track for Summary
3. **If Rule 5 applies** → Log to ISSUES.md, continue
4. **If genuinely unsure which rule** → Apply Rule 4 (ask user)

**Edge case guidance:**

- "This validation is missing" → Rule 2 (critical for security)
- "This validation could be better" → Rule 5 (enhancement)
- "This crashes on null" → Rule 1 (bug)
- "This could be faster" → Rule 5 (enhancement) UNLESS actually timing out → Rule 2 (critical)
- "Need to add table" → Rule 4 (architectural)
- "Need to add column" → Rule 1 or 2 (depends: fixing bug or adding critical field)

**When in doubt:** Ask yourself "Does this affect correctness, security, or ability to complete task?"

- YES → Rules 1-3 (fix automatically)
- NO → Rule 5 (log it)
- MAYBE → Rule 4 (ask user)

</deviation_rules>

<deviation_documentation>

## Documenting Deviations in Summary

After all tasks complete, Summary MUST include deviations section.

**If no deviations:**

```markdown
## Deviations from Change

None - change executed exactly as written.
```

**If deviations occurred:**

```markdown
## Deviations from Change

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed case-sensitive email uniqueness constraint**

- **Found during:** Task 4 (Follow/unfollow API implementation)
- **Issue:** User.email unique constraint was case-sensitive - Test@example.com and test@example.com were both allowed, causing duplicate accounts
- **Fix:** Changed to `CREATE UNIQUE INDEX users_email_unique ON users (LOWER(email))`
- **Files modified:** src/models/User.ts, migrations/003_fix_email_unique.sql
- **Verification:** Unique constraint test passes - duplicate emails properly rejected
- **Commit:** abc123f

**2. [Rule 2 - Missing Critical] Added JWT expiry validation to auth middleware**

- **Found during:** Task 3 (Protected route implementation)
- **Issue:** Auth middleware wasn't checking token expiry - expired tokens were being accepted
- **Fix:** Added exp claim validation in middleware, reject with 401 if expired
- **Files modified:** src/middleware/auth.ts, src/middleware/auth.test.ts
- **Verification:** Expired token test passes - properly rejects with 401
- **Commit:** def456g

### Deferred Enhancements

Logged to .planning/ISSUES.md for future consideration:

- ISS-001: Refactor UserService into smaller modules (discovered in Task 3)
- ISS-002: Add connection pooling for Redis (discovered in Task 6)

---

**Total deviations:** 4 auto-fixed (1 bug, 1 missing critical, 1 blocking, 1 architectural with approval), 3 deferred
**Impact on change:** All auto-fixes necessary for correctness/security/performance. No scope creep.
```

**This provides complete transparency:**

- Every deviation documented
- Why it was needed
- What rule applied
- What was done
- User can see exactly what happened beyond the change

</deviation_documentation>

<tdd_plan_execution>
## TDD Change Execution

When executing a change with `type: tdd` in frontmatter, follow the RED-GREEN-REFACTOR cycle for the single feature defined in the change.

**1. Check test infrastructure (if first TDD change):**
If no test framework configured:
- Detect project type from package.json/requirements.txt/etc.
- Install minimal test framework (Jest, pytest, Go testing, etc.)
- Create test config file
- Verify: run empty test suite
- This is part of the RED release, not a separate task

**2. RED - Write failing test:**
- Read `<behavior>` element for test specification
- Create test file if doesn't exist (follow project conventions)
- Write test(s) that describe expected behavior
- Run tests - MUST fail (if passes, test is wrong or feature exists)
- Commit: `test: add failing test for [feature]`

**3. GREEN - Implement to pass:**
- Read `<implementation>` element for guidance
- Write minimal code to make test pass
- Run tests - MUST pass
- Commit: `feature: implement [feature]`

**4. REFACTOR (if needed):**
- Clean up code if obvious improvements
- Run tests - MUST still pass
- Commit only if changes made: `refactor: clean up [feature]`

**Commit pattern for TDD changes:**
Each TDD change produces 2-3 atomic commits:
1. `test: add failing test for X`
2. `feature: implement X`
3. `refactor: clean up X` (optional)

**Error handling:**
- If test doesn't fail in RED release: Test is wrong or feature already exists. Investigate before proceeding.
- If test doesn't pass in GREEN release: Debug implementation, keep iterating until green.
- If tests fail in REFACTOR release: Undo refactor, commit was premature.

**Verification:**
After TDD change completion, ensure:
- All tests pass
- Test coverage for the new behavior exists
- No unrelated tests broken

**Why TDD uses dedicated changes:** TDD requires 2-3 execution cycles (RED → GREEN → REFACTOR), each with file reads, test runs, and potential debugging. This consumes 40-50% of context for a single feature. Dedicated changes ensure full quality throughout the cycle.

**Comparison:**
- Standard changes: Multiple tasks, 1 commit per task, 2-4 commits total
- TDD changes: Single feature, 2-3 commits for RED/GREEN/REFACTOR cycle

See `~/.claude/cat/references/tdd.md` for TDD change structure.
</tdd_plan_execution>

<pre_commit_review>

## Pre-Commit Review Checkpoint

**Before committing implementation work, offer user the opportunity to review.**

### Branch Strategy

**MANDATORY:** All change work MUST be on a dedicated branch, NOT main.

**Branch naming:** `change/{release}-{change}-{slug}`
Example: `change/05-02-fix-switch-expression-case-parsing`

**At change start:**
```bash
git checkout -b change/{release}-{change}-{slug}
```

**Before review:** Squash commits by type (see below).

### Commit Squashing by Type

Before presenting review, squash all working commits into logical commits by type:

| Type | Contains | Example Message |
|------|----------|-----------------|
| `feature` | New functionality, implementation | `feature: add array type pattern support` |
| `bugfix` | Bug fixes | `bugfix: correct email validation` |
| `test` | Test additions/modifications | `test: add switch expression array pattern tests` |
| `docs` | User-facing documentation (README, API docs, guides) | `docs: add API endpoint documentation` |
| `config` | Config, tooling, dependencies, Claude-facing docs | `config: update skill with documentation-first prevention` |
| `planning` | Planning files (STATE, SUMMARY, ROADMAP) | `planning: add 05-02 SUMMARY` |

**Squashing process:**
1. Count commits by type in working branch
2. Interactive rebase to squash: `git rebase -i main`
3. Squash related commits, keeping one per type
4. Result: 2-4 clean commits, one per type present

**Commit ordering:** Order commits by dependency - each commit should only depend on previous commits, never on later ones.

Example order:
1. `config:` - Setup, dependencies, Claude-facing docs (no dependencies)
2. `docs:` - User-facing documentation (may depend on config)
3. `feature:` or `bugfix:` - The main deliverable
4. `test:` - Test additions (depends on feature/bugfix being present)

**Planning metadata:**
- **CHANGE.md**: Update as progress is made. Mark tasks complete in the commit that completes them. Check verification boxes in the commit that verifies them.
- **SUMMARY.md**: Update in the final commit of the change (typically the test commit). The SUMMARY documents what was accomplished and serves as the completion record.

### Review Summary Format

Present a detailed multi-line summary with branch and commit info:

```
## Implementation Summary: {change-slug}

**Branch:** `change/{release}-{change}-{slug}`
**Commits for review:** (list in dependency order)
- `abc1234` config: {description}
- `def5678` feature: {description}
- `ghi9012` test: {description}

---

### Code Changes

**path/to/file1.java**
- [Specific change 1]
- [Specific change 2]

**path/to/file2.java**
- [Specific change 1]
- [Specific change 2]

### Documentation Updates (if any)

**path/to/doc.md** (lines X-Y)
- [What was updated and why]

### Test Changes (if any)

**path/to/test.java**
- Added `testMethodName` covering [scenario]
- Deleted duplicate tests (already covered in OtherTest.java)

### Verification

\`\`\`bash
./mvnw test -pl module
Tests run: X, Failures: 0, Errors: 0, Skipped: 0
\`\`\`

---
```

**IMPORTANT:** Present the detailed summary FIRST, then use AskUserQuestion with a simple question.
Do NOT attempt to fit the summary into the AskUserQuestion tool parameters.

Use AskUserQuestion:
- header: "Pre-commit"
- question: "Approve merging branch `change/{slug}` to main?"
- options:
  - "Approve and merge" - Merge to main
  - "Show full diffs" - Display diffs for review
  - "Discuss issues" - Address concerns before merging

**If user selects "Show full diffs":**
- Run `git diff main...HEAD` to show all changes
- Wait for user feedback
- Address any concerns before merging

**If user selects "Approve and merge":**
- Merge to main: `git checkout main && git merge --no-ff change/{slug}`
- Delete branch: `git branch -d change/{slug}`

**In YOLO mode:**
- Skip this checkpoint, merge directly
- Note: `⚡ Auto-merged (yolo mode)`

**Why this matters:**
- User maintains oversight of code entering main
- Clear branch and commit history for review
- Each commit type reviewable independently
- Collaborative workflow, not autonomous

</pre_commit_review>

<task_commit>
## Task Commit Protocol

After each task completes (verification passed, done criteria met), commit immediately:

**1. Identify modified files:**

Track files changed during this specific task (not the entire change):

```bash
git status --short
```

**2. Stage task-related files and planning metadata:**

Stage each file individually (NEVER use `git add .` or `git add -A`):

```bash
# Implementation files modified by this task
git add src/api/auth.ts
git add src/types/user.ts

# Planning metadata (always include STATE.md with task status update)
git add .planning/STATE.md

# For final task only: also include SUMMARY.md and ROADMAP.md
git add .planning/releases/XX-name/{release}-{change}-SUMMARY.md
git add .planning/ROADMAP.md
```

**3. Determine commit type:**

| Type | When to Use | Example |
|------|-------------|---------|
| `feature` | New feature, endpoint, component, functionality | feature: create user registration endpoint |
| `bugfix` | Bug fix, error correction | bugfix: correct email validation regex |
| `test` | Test-only changes (TDD RED release) | test: add failing test for password hashing |
| `refactor` | Code cleanup, no behavior change (TDD REFACTOR release) | refactor: extract validation to helper |
| `performance` | Performance improvement | performance: add database index for user lookups |
| `docs` | User-facing documentation (README, API docs, guides) | docs: add API endpoint documentation |
| `style` | Formatting, linting fixes | style: format auth module |
| `config` | Config, tooling, dependencies, Claude-facing docs | config: add bcrypt dependency |
| `planning` | Planning system updates (ROADMAP, STATE, releases) | planning: add Release 5 action items |
| `retrospective` | Retrospective analysis and action items | retrospective: R002 analysis with 3 new patterns |

**4. Craft commit message:**

Format: `{type}: {task-name-or-description}`

```bash
git commit -m "{type}: {concise task description}

- {key change 1}
- {key change 2}
- {key change 3}
"
```

**Examples:**

```bash
# Standard change task
git commit -m "feature: create user registration endpoint

- POST /auth/register validates email and password
- Checks for duplicate users
- Returns JWT token on success
"

# Another standard task
git commit -m "bugfix: correct email validation regex

- Fixed regex to accept plus-addressing
- Added tests for edge cases
"
```

**Note:** TDD changes have their own commit pattern (test/feature/refactor for RED/GREEN/REFACTOR releases). See `<tdd_plan_execution>` section above.

**5. Record commit hash:**

After committing, capture hash for SUMMARY.md:

```bash
TASK_COMMIT=$(git rev-parse --short HEAD)
echo "Task ${TASK_NUM} committed: ${TASK_COMMIT}"
```

Store in array or list for SUMMARY generation:
```bash
TASK_COMMITS+=("Task ${TASK_NUM}: ${TASK_COMMIT}")
```

**Atomic commit benefits:**
- Each task independently revertable
- Git bisect finds exact failing task
- Git blame traces line to specific task context
- Clear history for Claude in future sessions
- Better observability for AI-automated workflow

</task_commit>

<step name="checkpoint_protocol">
When encountering `type="checkpoint:*"`:

**Critical: Claude automates everything with CLI/API before checkpoints.** Checkpoints are for verification and decisions, not manual work.

**Display checkpoint clearly:**

```
════════════════════════════════════════
CHECKPOINT: [Type]
════════════════════════════════════════

Task [X] of [Y]: [Action/What-Built/Decision]

[Display task-specific content based on type]

[Resume signal instruction]
════════════════════════════════════════
```

**For checkpoint:human-verify (90% of checkpoints):**

```
I automated: [what was automated - deployed, built, configured]

How to verify:
1. [Step 1 - exact command/URL]
2. [Step 2 - what to check]
3. [Step 3 - expected behavior]

[Resume signal - e.g., "Type 'approved' or describe issues"]
```

**For checkpoint:decision (9% of checkpoints):**

```
Decision needed: [decision]

Context: [why this matters]

Options:
1. [option-id]: [name]
   Pros: [pros]
   Cons: [cons]

2. [option-id]: [name]
   Pros: [pros]
   Cons: [cons]

[Resume signal - e.g., "Select: option-id"]
```

**For checkpoint:human-action (1% - rare, only for truly unavoidable manual steps):**

```
I automated: [what Claude already did via CLI/API]

Need your help with: [the ONE thing with no CLI/API - email link, 2FA code]

Instructions:
[Single unavoidable step]

I'll verify after: [verification]

[Resume signal - e.g., "Type 'done' when complete"]
```

**After displaying:** WAIT for user response. Do NOT hallucinate completion. Do NOT continue to next task.

**After user responds:**

- Run verification if specified (file exists, env var set, tests pass, etc.)
- If verification passes or N/A: continue to next task
- If verification fails: inform user, wait for resolution

See ~/.claude/cat/references/checkpoints.md for complete checkpoint guidance.
</step>

<step name="verification_failure_gate">
If any task verification fails:

STOP. Do not continue to next task.

Present inline:
"Verification failed for Task [X]: [task name]

Expected: [verification criteria]
Actual: [what happened]

How to proceed?

1. Retry - Try the task again
2. Skip - Mark as incomplete, continue
3. Stop - Pause execution, investigate"

Wait for user decision.

If user chose "Skip", note it in SUMMARY.md under "Issues Encountered".
</step>

<step name="record_completion_time">
Record execution end time and calculate duration:

```bash
CHANGE_END_TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
CHANGE_END_EPOCH=$(date +%s)

DURATION_SEC=$(( CHANGE_END_EPOCH - CHANGE_START_EPOCH ))
DURATION_MIN=$(( DURATION_SEC / 60 ))

if [[ $DURATION_MIN -ge 60 ]]; then
  HRS=$(( DURATION_MIN / 60 ))
  MIN=$(( DURATION_MIN % 60 ))
  DURATION="${HRS}h ${MIN}m"
else
  DURATION="${DURATION_MIN} min"
fi
```

Pass timing data to SUMMARY.md creation.
</step>

<step name="create_summary">
Create `{release}-{change}-SUMMARY.md` as specified in the prompt's `<output>` section.
Use ~/.claude/cat/templates/summary.md for structure.

**File location:** `.planning/releases/XX-name/{release}-{change}-SUMMARY.md`

**Frontmatter population:**

Before writing summary content, populate frontmatter fields from execution context:

1. **Basic identification:**
   - release: From CHANGE.md frontmatter
   - change: From CHANGE.md frontmatter
   - subsystem: Categorize based on release focus (auth, payments, ui, api, database, infra, testing, etc.)
   - tags: Extract tech keywords (libraries, frameworks, tools used)

2. **Dependency graph:**
   - requires: List prior releases this built upon (check CHANGE.md context section for referenced prior summaries)
   - provides: Extract from accomplishments - what was delivered
   - affects: Infer from release description/goal what future releases might need this

3. **Tech tracking:**
   - tech-stack.added: New libraries from package.json changes or requirements
   - tech-stack.patterns: Architectural patterns established (from decisions/accomplishments)

4. **File tracking:**
   - key-files.created: From "Files Created/Modified" section
   - key-files.modified: From "Files Created/Modified" section

5. **Decisions:**
   - key-decisions: Extract from "Decisions Made" section

6. **Issues:**
   - issues-created: Check if ISSUES.md was updated during execution

7. **Metrics:**
   - duration: From $DURATION variable
   - completed: From $CHANGE_END_TIME (date only, format YYYY-MM-DD)

Note: If subsystem/affects are unclear, use best judgment based on release name and accomplishments. Can be refined later.

**Title format:** `# Release [X] Change [Y]: [Name] Summary`

The one-liner must be SUBSTANTIVE:

- Good: "JWT auth with refresh rotation using jose library"
- Bad: "Authentication implemented"

**Include performance data:**

- Duration: `$DURATION`
- Started: `$CHANGE_START_TIME`
- Completed: `$CHANGE_END_TIME`
- Tasks completed: (count from execution)
- Files modified: (count from execution)

**Next Step section:**

- If more changes exist in this release: "Ready for {release}-{next-change}-{slug}-CHANGE.md"
- If this is the last change: "Release complete, ready for transition"
  </step>

<step name="update_current_position">
Update Current Position section in STATE.md to reflect change completion.

**Format:**

```markdown
Release: [current] of [total] ([release name])
Change: [just completed] of [total in release]
Status: [In progress / Release complete]
Last activity: [today] - Completed {release}-{change}-{slug}-CHANGE.md

Progress: [progress bar]
```

**Calculate progress bar:**

- Count total changes across all releases (from ROADMAP.md or ROADMAP.md)
- Count completed changes (count SUMMARY.md files that exist)
- Progress = (completed / total) × 100%
- Render: ░ for incomplete, █ for complete

**Example - completing 02-01-setup-jwt-CHANGE.md (change 5 of 10 total):**

Before:

```markdown
## Current Position

Release: 2 of 4 (Authentication)
Change: Not started
Status: Ready to execute
Last activity: 2025-01-18 - Release 1 complete

Progress: ██████░░░░ 40%
```

After:

```markdown
## Current Position

Release: 2 of 4 (Authentication)
Change: 1 of 2 in current release
Status: In progress
Last activity: 2025-01-19 - Completed 02-01-setup-jwt-CHANGE.md

Progress: ███████░░░ 50%
```

**Step complete when:**

- [ ] Release number shows current release (X of total)
- [ ] Change number shows changes complete in current release (N of total-in-release)
- [ ] Status reflects current state (In progress / Release complete)
- [ ] Last activity shows today's date and the change just completed
- [ ] Progress bar calculated correctly from total completed changes
      </step>

<step name="extract_decisions_and_issues">
Extract decisions, issues, and concerns from SUMMARY.md into STATE.md accumulated context.

**Decisions Made:**

- Read SUMMARY.md "## Decisions Made" section
- If content exists (not "None"):
  - Add each decision to STATE.md Decisions table
  - Format: `| [release number] | [decision summary] | [rationale] |`

**Deferred Issues:**

- Read SUMMARY.md to check if new issues were logged to ISSUES.md
- If new ISS-XXX entries created:
  - Update STATE.md "Deferred Issues" section

**Blockers/Concerns:**

- Read SUMMARY.md "## Next Release Readiness" section
- If contains blockers or concerns:
  - Add to STATE.md "Blockers/Concerns Carried Forward"
    </step>

<step name="update_session_continuity">
Update Session Continuity section in STATE.md to enable resumption in future sessions.

**Format:**

```markdown
Last session: [current date and time]
Stopped at: Completed {release}-{change}-{slug}-CHANGE.md
Resume file: [path to .continue-here if exists, else "None"]
```

**Size constraint note:** Keep STATE.md under 150 lines total.
</step>

<step name="issues_review_gate">
Before proceeding, check SUMMARY.md content.

If "Issues Encountered" is NOT "None":

<if mode="yolo">
```
⚡ Auto-approved: Issues acknowledgment
⚠️ Note: Issues were encountered during execution:
- [Issue 1]
- [Issue 2]
(Logged - continuing in yolo mode)
```

Continue without waiting.
</if>

<if mode="interactive" OR="custom with gates.issues_review true">
Present issues and wait for acknowledgment before proceeding.
</if>
</step>

<step name="update_roadmap">
Update the roadmap file:

```bash
ROADMAP_FILE=".planning/ROADMAP.md"
```

**If more changes remain in this release:**

- Update change count: "2/3 changes complete"
- Keep release status as "In progress"

**If this was the last change in the release:**

- Mark release complete: status → "Complete"
- Add completion date
  </step>

<step name="verify_plan_completion">
Verify change execution is complete:

**All tasks committed:** Each task should have been committed with its implementation + STATE.md update.

**Final task included:** The final task commit should include SUMMARY.md and ROADMAP.md updates.

**Git log after change execution:**

```
def456g feature: add email confirmation flow
hij789k feature: implement password hashing with bcrypt
lmn012o feature: create user registration endpoint
```

Each task has its own commit containing both implementation and planning metadata updates.

For commit message conventions, see ~/.claude/cat/references/git-integration.md
</step>

<step name="update_codebase_map">
**If .planning/codebase/ exists:**

Check what changed across all task commits in this change:

```bash
# Get first commit hash from SUMMARY.md (recorded per task)
FIRST_TASK=$(grep -m1 "^- \`[a-f0-9]\{7\}\`" .planning/releases/XX-name/{release}-{change}-SUMMARY.md 2>/dev/null | grep -o '\`[a-f0-9]\{7\}\`' | tr -d '\`')

# Get all changes from first task through now
git diff --name-only ${FIRST_TASK}^..HEAD 2>/dev/null
```

**Update only if structural changes occurred:**

| Change Detected | Update Action |
|-----------------|---------------|
| New directory in src/ | STRUCTURE.md: Add to directory layout |
| package.json deps changed | STACK.md: Add/remove from dependencies list |
| New file pattern (e.g., first .test.ts) | CONVENTIONS.md: Note new pattern |
| New external API client | INTEGRATIONS.md: Add service entry with file path |
| Config file added/changed | STACK.md: Update configuration section |
| File renamed/moved | Update paths in relevant docs |

**Skip update if only:**
- Code changes within existing files
- Bug fixes
- Content changes (no structural impact)

**Update format:**
Make single targeted edits - add a bullet point, update a path, or remove a stale entry. Don't rewrite sections.

```bash
git add .planning/codebase/*.md
git commit -m "config: update codebase map"
```

**If .planning/codebase/ doesn't exist:**
Skip this step.
</step>

<step name="check_phase_issues">
**Check if issues were created during this release:**

```bash
# Check if ISSUES.md exists and has issues from current release
if [ -f .planning/ISSUES.md ]; then
  grep -E "Release ${RELEASE}.*Task" .planning/ISSUES.md | grep -v "^#" || echo "NO_ISSUES_THIS_RELEASE"
fi
```

**If issues were created during this release:**

```
📋 Issues logged during this release:
- ISS-XXX: [brief description]
- ISS-YYY: [brief description]

Review these now?
```

Use AskUserQuestion:
- header: "Release Issues"
- question: "[N] issues were logged during this release. Review now?"
- options:
  - "Review issues" - Analyze with /cat:consider-issues
  - "Continue" - Address later, proceed to next work

**If "Review issues" selected:**
- Invoke: `SlashCommand("/cat:consider-issues")`
- After consider-issues completes, return to offer_next

**If "Continue" selected or no issues found:**
- Proceed to offer_next step

**In YOLO mode:**
- Note issues were logged but don't prompt: `📋 [N] issues logged this release (review later with /cat:consider-issues)`
- Continue to offer_next automatically
</step>

<step name="release_execution_lock">
Release execution lock after change completion:

```bash
LOCK_DIR="${LOCK_DIR:-.claude/locks}"
PROJECT_NAME="${PWD##*/}"
LOCK_FILE="$LOCK_DIR/${PROJECT_NAME}.lock"

if [[ -f "$LOCK_FILE" ]]; then
    rm -f "$LOCK_FILE"
    echo "Execution lock released: $LOCK_FILE"
fi
```

**Note:** SessionEnd hook provides fallback cleanup if session ends unexpectedly.
</step>

<step name="offer_next">
**MANDATORY: Verify remaining work before presenting next steps.**

Do NOT skip this verification. Do NOT assume release or milestone completion without checking.

**Step 1: Count changes and summaries in current release**

List files in the release directory:

```bash
ls -1 .planning/releases/[current-release-dir]/*-CHANGE.md 2>/dev/null | wc -l
ls -1 .planning/releases/[current-release-dir]/*-SUMMARY.md 2>/dev/null | wc -l
```

State the counts: "This release has [X] changes and [Y] summaries."

**Step 2: Route based on change completion**

Compare the counts from Step 1:

| Condition | Meaning | Action |
|-----------|---------|--------|
| summaries < changes | More changes remain | Go to **Route A** |
| summaries = changes | Release complete | Go to Step 3 |

---

**Route A: More changes remain in this release**

Identify the next unexecuted change:
- Find the first CHANGE.md file that has no matching SUMMARY.md
- Read its `<objective>` section

<if mode="yolo">
```
Change {release}-{change} complete.
Summary: .planning/releases/{release-dir}/{release}-{change}-SUMMARY.md

{Y} of {X} changes complete for Release {Z}.

⚡ Auto-continuing: Execute next change ({release}-{next-change})
```

Loop back to identify_plan step automatically.
</if>

<if mode="interactive" OR="custom with gates.execute_next_change true">
```
Change {release}-{change} complete.
Summary: .planning/releases/{release-dir}/{release}-{change}-SUMMARY.md

{Y} of {X} changes complete for Release {Z}.

---

## ▶ Next Up

**{release}-{next-change}-{slug}: [Change Name]** — [objective from next CHANGE.md]

`/cat:execute-change .planning/releases/{release-dir}/{release}-{next-change}-{slug}-CHANGE.md`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- Review what was built before continuing

---
```

Wait for user to clear and run next command.
</if>

**STOP here if Route A applies. Do not continue to Step 3.**

---

**Step 3: Check milestone status (only when all changes in release are complete)**

Read ROADMAP.md and extract:
1. Current release number (from the change just completed)
2. All release numbers listed in the current milestone section

To find releases in the current milestone, look for:
- Release headers: lines starting with `### Release` or `#### Release`
- Release list items: lines like `- [ ] **Release X:` or `- [x] **Release X:`

Count total releases in the current milestone and identify the highest release number.

State: "Current release is {X}. Milestone has {N} releases (highest: {Y})."

**Step 4: Route based on milestone status**

| Condition | Meaning | Action |
|-----------|---------|--------|
| current release < highest release | More releases remain | Go to **Route B** |
| current release = highest release | Milestone complete | Go to **Route C** |

---

**Route B: Release complete, more releases remain in milestone**

Read ROADMAP.md to get the next release's name and goal.

```
Change {release}-{change} complete.
Summary: .planning/releases/{release-dir}/{release}-{change}-SUMMARY.md

## ✓ Release {Z}: {Release Name} Complete

All {Y} changes finished.

---

## ▶ Next Up

**Release {Z+1}: {Next Release Name}** — {Goal from ROADMAP.md}

`/cat:change-release {Z+1}`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- `/cat:discuss-release {Z+1}` — gather context first
- `/cat:research-release {Z+1}` — investigate unknowns
- Review release accomplishments before continuing

---
```

---

**Route C: Milestone complete (all releases done)**

```
🎉 MILESTONE COMPLETE!

Change {release}-{change} complete.
Summary: .planning/releases/{release-dir}/{release}-{change}-SUMMARY.md

## ✓ Release {Z}: {Release Name} Complete

All {Y} changes finished.

════════════════════════════════════════
All {N} releases complete!
Milestone is 100% done.
════════════════════════════════════════

---

## ▶ Next Up

**Complete Milestone** — archive and prepare for next

`/cat:complete-milestone`

<sub>`/clear` first → fresh context window</sub>

---

**Also available:**
- `/cat:add-release <description>` — add another release before completing
- Review accomplishments before archiving

---
```

</step>

</process>

<success_criteria>

- All tasks from CHANGE.md completed
- All verifications pass
- SUMMARY.md created with substantive content
- STATE.md updated (position, decisions, issues, session)
- ROADMAP.md updated
- If codebase map exists: map updated with execution changes (or skipped if no significant changes)
  </success_criteria>
