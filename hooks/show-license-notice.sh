#!/bin/bash
set -euo pipefail

# Show license notice on session start
#
# TRIGGER: SessionStart
#
# BEHAVIOR:
# - Displays a brief license notice to the user
# - Only shows once per session (first hook execution)

trap 'echo "ERROR in show-license-notice.sh at line $LINENO: $BASH_COMMAND" >&2; exit 1' ERR

# Output license notice to user via systemMessage (the documented way to display to users)
cat <<'EOF'
{"systemMessage":"╔══════════════════════════════════════════════════════════════════════════════╗\n║  CAT LICENSE                                                                 ║\n╠══════════════════════════════════════════════════════════════════════════════╣\n║  Free for personal use and small organizations.                              ║\n║  Commercial use requires a license.                                          ║\n║  https://github.com/cowwoc/claude-code-cat/blob/main/LICENSE.md              ║\n╚══════════════════════════════════════════════════════════════════════════════╝"}
EOF
