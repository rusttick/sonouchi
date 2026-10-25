# Guard-rail changes for the merge of soonish into sonouchi

soonish's code is now in sonouchi:

| From soonish | Now |
|---|---|
| `src/soonish/core/schedule.clj` | `src/sonouchi/core/schedule.cljc` |
| `src/soonish/main.clj` | `src/sonouchi/main.clj` |
| `test/soonish/core/schedule_test.clj` | `test/sonouchi/core/schedule_test.cljc` |
| `examples/running-example.edn` | `examples/running-example.edn` |
| `doc/design3.md` | already here; the two copies were identical |

Only the namespaces were renamed. The `soonish/` folder is untouched,
for the owner to delete.

`bin/check` now passes format, lint, both builds, and the tests on the
JVM and on Node. The scheduler's tests, property tests included, pass
on both. Only the guard fails, with 36 violations of two kinds, which
the changes below remove:

- 35 uses of soonish's ten attributes, which are not in the registry
- `sonouchi.main/-main`, which nothing in `src` or `test` calls

The ratchet passed: `guard/baseline.edn` needs no change.

Every change below is to a guard-rail file, which only the owner
changes. Make them by hand, or start a session with
`SONOUCHI_OWNER_EDIT=1 claude` and ask the agent to apply this
document. Then delete this document.

No change is needed to `deps.edn`, `shadow-cljs.edn`, `package.json`,
`bin/`, `.clj-kondo/`, `.cljfmt.edn`, `.mcp.json`, or the hooks:
sonouchi's already did everything soonish's did.

## 1. `src/sonouchi/core/attrs.cljc`

Insert soonish's attributes, with their docs unchanged, after the
`:task/title` entry and before `;; Actions`:

```clojure
   ;; Tasks, as the scheduler reads them. Days are working days counted
   ;; from day 0 of the project.

   :task/estimate-days
   {:doc "Working days that the person doing the task thinks are equally
  likely to be too many or too few: a median."
    :schema [:and number? [:> 0]]}

   :task/after
   {:doc "Ids of the tasks that must finish before this one can start."
    :schema [:set any?]}

   :task/actual-start-day
   {:doc "The working day on which the task actually started, counted
  from day 0 of the project."
    :schema [:and number? [:>= 0]]}

   :task/actual-finish-day
   {:doc "The working day on which the task actually finished, counted
  from day 0 of the project."
    :schema [:and number? [:>= 0]]}

   ;; Schedules, as computed from tasks

   :schedule/start-day
   {:doc "The computed working day on which a task starts."
    :schema number?}

   :schedule/finish-day
   {:doc "The computed working day on which a task finishes."
    :schema number?}

   :schedule/times
   {:doc "Task id to that task's :schedule/start-day and
  :schedule/finish-day."
    :schema [:map-of any? map?]}

   :schedule/order
   {:doc "Task ids in an order where every task comes after the tasks it
  waits for."
    :schema [:vector any?]}

   :schedule/cycle
   {:doc "Ids of the tasks on a dependency cycle, or waiting behind one,
  so that no order exists."
    :schema [:set any?]}

   :schedule/unknown-tasks
   {:doc "Ids named in some :task/after that are not tasks."
    :schema [:set any?]}
```

The day attributes are expected to be replaced when time moves to
instants and millisecond durations (`doc/todo.md`). No record has ever
held them, so they can be removed then, although the registry's
docstring says entries are only added. That rule protects stored data,
and there is none.

## 2. `guard/rules.edn`

`sonouchi.main/-main` is called from the command line, so it is an
entry point. Replace the `:entry-points` form and its comment with:

```clojure
 ;; Every public var in src must be used somewhere in src or test, except
 ;; these, which are used from outside: entry points called by
 ;; shadow-cljs and the command line, and data read by tools such as
 ;; this guard.
 :entry-points #{sonouchi.app/init
                 sonouchi.app/reload
                 sonouchi.main/-main
                 sonouchi.core.attrs/registry}
```

## 3. `.claude/settings.json`

Let agents run the scheduler on an example, as soonish allowed. Add one
line to `permissions.allow`, after `"Bash(node target/node-test.js)",`:

```json
      "Bash(clojure -M -m sonouchi.main:*)",
```

## 4. `CLAUDE.md`

The whole new file:

```markdown
# sonouchi

Instructions for coding agents. The full rules are in CONTRIBUTING.md, imported below.

- The design is `doc/design3.md`. Version 0 is its Step 0
  (`doc/goals.md`).
- Run `bin/check` before you finish a code change (not doc changes). It must pass.
- When a check fails, fix the code. Never loosen a check, suppress a warning or weaken a test.
- Guard-rail files are blocked for agents. When you need one changed, stop and ask the owner, and say what and why.
- Ask before adding a dependency, from Maven or npm, or a new attribute.

@CONTRIBUTING.md
```

## 5. `CONTRIBUTING.md`

Three passages.

**Lines 10–11.** Replace

```markdown
They are adapted from `../soonish/CONTRIBUTING.md`, the server's rules,
for the browser. Where the two differ, it is because of the browser.
```

with

```markdown
sonouchi was two projects, the scheduler (soonish) and the browser
client, and these rules are the two sets of rules made one. The core
is shared by the JVM and the browser, so it follows both.
```

**Layout, lines 105–108.** Replace

```markdown
- everything else in `src/`: the shell, in `.cljs`. It holds the one
  atom, renders with Replicant, dispatches actions, and carries out
  effects such as storage. Keep it thin. `sonouchi.app` is the entry
  point.
```

with

```markdown
- everything else in `src/`: the shells. Keep them thin.
  - `sonouchi.app`, in `.cljs`: the browser. It holds the one atom,
    renders with Replicant, dispatches actions, and carries out
    effects such as storage.
  - `sonouchi.main`, in `.clj`: the command line on the JVM. It reads
    tasks from an EDN file and prints their schedule, as in
    `clojure -M -m sonouchi.main examples/running-example.edn`.
- `examples/`: EDN inputs for `sonouchi.main`, such as design3's
  running example.
```

**Section 7, line 269.** Replace

```markdown
- Send values across every boundary: storage, files, HTTP to soonish.
```

with

```markdown
- Send values across every boundary: storage, files, HTTP to a server.
```

## After the changes

Run `bin/check`. It should pass, with no guard violations.
