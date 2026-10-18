#!/usr/bin/env bash
# PreToolUse hook for clojure-mcp's clojure_eval: the REPL can write
# files too, so it gets the same speed bump as Bash. Code that names a
# guard-rail file and could write, delete or run a process is blocked.
#
# The owner can allow it for one session:  SONOUCHI_OWNER_EDIT=1 claude
set -euo pipefail
[ "${SONOUCHI_OWNER_EDIT:-}" = 1 ] && exit 0

code=$(jq -r '.tool_input.code // empty')

guarded='(\.claude/|bin/|guard/|\.clj-kondo/config\.edn|\.cljfmt\.edn|deps\.edn|shadow-cljs\.edn|package(-lock)?\.json|\.mcp\.json|CONTRIBUTING\.md|CLAUDE\.md|core/attrs\.cljc)'
writes='(spit|writer|output-stream|delete-file|copy|Files/|java\.nio|shell/sh|ProcessBuilder|Runtime)'

if printf '%s' "$code" | grep -Eq "$guarded" &&
  printf '%s' "$code" | grep -Eq "$writes"; then
  echo "This evaluation looks like it changes a guard-rail file, which only the owner changes. Ask them." >&2
  exit 2
fi
