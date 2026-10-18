# sonouchi

Instructions for coding agents. The full rules are in CONTRIBUTING.md,
imported below. Follow them exactly.

- The plan is `TODO.md`. Each part of the interface is designed with
  the process in its section 2, and checked against rules V1 to V17
  in `ux-research-visual.md`.
- The model the app serves is `../soonish/doc/design3.md`, Step 0.
  soonish is a separate repository; never edit it from here.
- Pure code goes in `src/sonouchi/core/`, as `.cljc`: views, actions
  and queries. The DOM, storage, the clock and the one atom go only in
  the shell, in `.cljs`, and the shell stays thin.
- Event handlers in hiccup are data (action vectors), never functions.
- Use the JVM REPL (`clojure_eval`) to check what core functions
  return before you rely on them. Every definition still goes in a
  file.
- Run `bin/check` before you finish. It must pass.
- When a check fails, fix the code. Never loosen a check, suppress a
  warning or weaken a test.
- Guard-rail files are blocked for agents. When you need one changed,
  stop and ask the owner, and say what and why.
- Ask before adding a dependency, from Maven or npm, or a new
  attribute.

@CONTRIBUTING.md
