# Risk Classification System

Automatic risk assessment for changes based on file patterns, keywords, and scope.

## Risk Levels

### HIGH Risk
Requires: Multi-agent review (all 5 agents), mandatory user approval before AND after, full build verification.

**File Patterns:**
- `**/auth/**`, `**/security/**`, `**/payment/**`, `**/crypto/**`
- `**/*credential*`, `**/*password*`, `**/*token*`, `**/*secret*`
- `**/migrations/**`, `**/schema/**`

**Keywords in Action:**
- authentication, authorization, encryption, decryption
- payment, billing, subscription, checkout
- PII, HIPAA, GDPR, compliance
- database migration, schema change

**Scope Thresholds:**
- 10+ files modified
- 3+ new dependencies added

### MEDIUM Risk
Requires: Focused review (quality + style agents), user approval after execution, build verification.

**File Patterns:**
- `**/api/**`, `**/database/**`, `**/config/**`
- `**/middleware/**`, `**/routes/**`

**Scope Thresholds:**
- 5-9 files modified
- 1-2 new dependencies added

### LOW Risk
Requires: Basic review (style only), optional user approval, basic verification.

**Patterns:**
- Everything not matching HIGH or MEDIUM
- Documentation changes
- Test-only changes
- Style/formatting changes

**Scope Thresholds:**
- 1-4 files modified
- 0 new dependencies

## Risk Detection Algorithm

```
1. Check file patterns against HIGH_RISK_PATTERNS
   → Match found? Risk = HIGH

2. Check action keywords against HIGH_RISK_KEYWORDS
   → Match found? Risk = HIGH

3. Count files to be modified
   → 10+? Risk = HIGH
   → 5-9? Risk = max(current, MEDIUM)

4. Check file patterns against MEDIUM_RISK_PATTERNS
   → Match found? Risk = max(current, MEDIUM)

5. Default: Risk = LOW
```

## Manual Override

Changes can override auto-detected risk in frontmatter:

```yaml
---
risk: HIGH  # Manual override - security-sensitive even if not detected
---
```

Use manual override when:
- Logic is security-sensitive but files don't match patterns
- External API integrations with sensitive data
- Changes that could cause data loss
- User explicitly requests elevated review
