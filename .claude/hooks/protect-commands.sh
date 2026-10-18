#!/usr/bin/env bash
# PreToolUse hook for Bash: blocks commands that would get around the
# guard-rails. It is a speed bump, not a wall. The real check is the
# owner reviewing guard-rail changes before each commit.
#
# The owner can allow it for one session:  SONOUCHI_OWNER_EDIT=1 claude
set -euo pipefail
[ "${SONOUCHI_OWNER_EDIT:-}" = 1 ] && exit 0

cmd=$(jq -r '.tool_input.command // empty')

block() {
  echo "$1" >&2
  exit 2
}

case "$cmd" in
  *--update-baseline*)
    block "Only the owner raises the guard baseline. Tell them what rose and why." ;;
esac

# npm changes package.json and package-lock.json itself. Only `npm ci`,
# which installs exactly what the lockfile says, is for agents.
npm_changes='(^|[^[:alnum:]_-])(npm|pnpm|yarn) +(install|i|add|uninstall|remove|rm|update|up|upgrade|dedupe|audit +fix|pkg)([[:space:]]|$)'
if printf '%s' "$cmd" | grep -Eq "$npm_changes"; then
  block "This npm command changes the dependencies, which only the owner changes. Use npm ci to install what the lockfile says, or ask the owner."
fi

guarded='(\.claude/|bin/|guard/|\.clj-kondo/config\.edn|\.cljfmt\.edn|deps\.edn|shadow-cljs\.edn|package(-lock)?\.json|\.mcp\.json|CONTRIBUTING\.md|CLAUDE\.md|core/attrs\.cljc)'
writes='(sed +-i|perl +-[a-z]*i|(^|[^[:alnum:]_-])(tee|mv|cp|rm)[[:space:]]|>[^&]|git +(checkout|restore|rm|mv|stash))'

if printf '%s' "$cmd" | grep -Eq "$guarded" &&
  printf '%s' "$cmd" | grep -Eq "$writes"; then
  block "This command looks like it changes a guard-rail file, which only the owner changes. Ask them. (To run bin/check, do not redirect its output.)"
fi
