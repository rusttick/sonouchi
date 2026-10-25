# Contributing to sonouchi

These rules are for everyone who writes code here, and above all for
coding agents. They follow Rich Hickey's approach to software: values
instead of objects, pure functions instead of methods, and explicit,
minimal state. Clojure and ClojureScript were designed for this way of
working, so most of the rules describe ordinary, idiomatic code. The
rules exist to keep it that way as the code grows.

sonouchi was two projects, the scheduler (soonish) and the browser
client, and these rules are the two sets of rules made one. The core
is shared by the JVM and the browser, so it follows both.

Each rule ends with how it is checked:

- **guard**: the project's own checker, rules in `guard/rules.edn`
- **kondo**: the clj-kondo linter, settings in `.clj-kondo/config.edn`
- **fmt**: the cljfmt formatter
- **build**: the shadow-cljs builds, where every warning is an error
- **tests**: the test suite, run on the JVM and on Node
- **review**: nobody but a reader. These rules are the ones that slip,
  so look for them first when you review.

## The test

Before adding anything, ask: **what does this entangle?** Prefer more
separate parts to fewer knotted ones. Familiar is not the same as
simple.

## The working loop

-  Write a pure function in a core namespace, as `.cljc`, and a test
   that calls it with plain values.
-  Run `bin/check`. It runs, in order: format, lint, guard, the tests
   on the JVM, both builds, and the tests on Node. It must pass before
   you finish. If you work as a Claude Code agent, the hooks lint each
   file you edit and run `bin/check` when you stop.
-  If formatting fails, run `bin/fmt`.

When a check fails, fix the code. Never loosen the check, suppress the
warning, skip the test or weaken an assertion. If a check seems wrong,
stop and tell the owner why.

## The REPLs

There are two, and agents reach both through clojure-mcp's
`clojure_eval` tool.

**The JVM REPL** runs the pure core and the tests. clojure-mcp starts
it with `clojure -M:nrepl`, and it is the default port. Use it the way
a Clojure programmer would:

- **Check before you write.** Before using a function, evaluate it on
  a small value to see what it really returns. Use `(doc f)` and
  `(source f)` instead of guessing an API.
- **Load your changes.** After editing a file, evaluate
  `(require 'the.namespace :reload)` before calling into it.
- **Reproduce with values.** When something fails, capture the input
  value and call the function with it in the REPL.
- **Run one test namespace** with
  `(clojure.test/run-tests 'sonouchi.core.view-test)`.

**The browser REPL** runs in the page. It exists only while the owner
runs `npx shadow-cljs watch app` with the page open. Agents do not
start it; ask the owner. Find its port (7888) with `list_nrepl_ports`,
evaluate `(shadow.cljs.devtools.api/repl :app)` in it to reach the
browser, and `:cljs/quit` to leave. Use it to look at the page and the
state, not to change them: the state changes only through actions.

For both:

- **Files are the truth.** Anything you define only in a REPL is lost,
  and nobody can review it. Put every definition in a file.
  `bin/check` runs outside the REPLs, and it decides.
- **No effects.** Use the REPL to compute, not to write files or start
  processes. A hook blocks evaluations that look like they change
  guard-rail files.
- **Brackets.** If an edit leaves brackets you cannot balance in one
  or two tries, use the `paren_repair` tool on the file. Do not loop
  on manual fixes.

## Guard-rail files

Only the owner changes these, and agent hooks block edits to them:

- `deps.edn`, `shadow-cljs.edn`, `package.json`, `package-lock.json`:
  the dependencies and builds. Ask before adding any dependency.
  Agents may run `npm ci`, which installs exactly what the lockfile
  says, and nothing that changes it.
- `src/sonouchi/core/attrs.cljc`: the attribute registry, which is
  the data model
- `guard/`, `.clj-kondo/config.edn`, `.cljfmt.edn`: the checks
- `bin/`, `.claude/`, `.mcp.json`: the scripts, hooks and agent
  tools
- `CONTRIBUTING.md` and `CLAUDE.md`: these rules

If your work needs a change to one of them, stop and ask, and say
exactly what you need and why.

## Layout

- `src/sonouchi/core/`: the pure core, in `.cljc`, so that it runs on
  the JVM and in the browser alike. Views, actions and queries. Values
  in, values out. No state, no I/O, no clock, no randomness, no Java,
  no JavaScript, no DOM. **guard**
- everything else in `src/`: the shells. Keep them thin.
  - `sonouchi.app`, in `.cljs`: the browser. It holds the one atom,
    renders with Replicant, dispatches actions, and carries out
    effects such as storage.
  - `sonouchi.main`, in `.clj`: the command line on the JVM. It reads
    tasks from an EDN file and prints their schedule, as in
    `clojure -M -m sonouchi.main examples/running-example.edn`.
- `examples/`: EDN inputs for `sonouchi.main`, such as design3's
  running example.
- `src/sonouchi/core/attrs.cljc`: every attribute, defined once.
- `test/`: tests, mirroring `src/`, in `.cljc`.
- `public/index.html`: the page. The builds write `public/js/`.

The core may require only other core namespaces and `clojure.math`,
`clojure.set`, `clojure.string` and `clojure.walk`, which exist on both
platforms. **guard**

## 1. Data

- Represent information as plain maps, sets, vectors and scalars.
  Never define a record or a type per entity: no `defrecord`,
  `deftype` or `definterface`. **guard**
- Keys are qualified keywords with a meaning of their own, such as
  `:state/facts`. The meaning holds in any map that holds the key.
  **review**
- Every qualified keyword must be in the attribute registry,
  `sonouchi.core.attrs/registry`, with a `:doc` and a `:schema`. A
  misspelt key returns `nil` silently. The registry makes it a failure
  instead. Action names are qualified keywords too, so every action is
  listed there with what it does. Replicant's own keys, such as
  `:replicant/key`, are exempt. **guard**
- Write keywords in full. Never use auto-resolved keywords (`::k`).
  They tie the key's name to a namespace's name, and they change if the
  namespace is renamed. **guard**
- Unqualified keys are only for maps that never leave one namespace,
  and for hiccup's own tags and attributes, such as `:div` and
  `:class`. **review**
- Leave unknown information out. Never store `nil` as a value, and
  never add a key with a placeholder. A missing key means "not known".
  **review**
- Read only the keys you need, and pass the rest through untouched.
  Use `assoc` and `update` on the map you were given. Do not rebuild
  it with `select-keys` or a new literal. **review**
- Name the keys you need where you need them: destructure with
  `{:keys [...]}` or `{:state/keys [...]}` in the function that uses
  them. Do not destructure one structure in many places. **review**
- Use named keys, not positions. If a vector or an argument list
  passes about four items, use a map. An action vector names its
  action first, and passes a map when it carries more than one or two
  values. **review**

## 2. Functions

- Core functions are pure: the same arguments always give the same
  result, and nothing outside is read or changed. **guard**, for the
  forms it can see; **review** for the rest
- Pass in everything a function depends on, including the time, random
  numbers, ids and configuration. The core never calls `rand`,
  `random-uuid`, `js/Date` or the clock. **guard**
- Return new values. Never change an argument. Never use JavaScript
  objects or arrays in the core. **guard**
- Keep functions small, and their inputs generic. Prefer many small
  functions over a few data shapes. Small forms also keep brackets easy
  to balance. **review**
- Give every public function a docstring that says what it returns and
  what it requires of its arguments. **kondo**
- Delete dead code. A public var in `src` that nothing in `src` or
  `test` uses fails the check. Entry points that are used from outside,
  such as those shadow-cljs calls, are listed in `guard/rules.edn`.
  Make a helper private with `defn-` when it is not part of what the
  namespace offers; clj-kondo then reports it when it falls out of use.
  **guard**, **kondo**
- Describe problems as data. Return a map that names the problem
  instead of throwing, when the caller can do something about it.
  Throw `ex-info` only for programming errors. **review**

## 3. State

- Assume you do not need state. Most programs need very little.
  **review**
- The app holds its state in one `atom` in the shell, containing an
  immutable value. The core never touches atoms, refs, agents,
  volatiles or dynamic bindings. **guard**
- Change state only with `swap!` and a pure function of the current
  value. **review**
- Keep the number of references small, at the edges of the program.
  The ratchet counts them: there is one, the app's state. **guard**
- Dereference once, then pass the value down. A function that reads
  an atom is not pure; take the value as an argument instead.
  **review**
- No component state. Replicant has no components; do not imitate them
  with closures, local atoms or state hidden in the DOM. What the
  screen shows is a function of the one state value. **review**
- To detect change, compare two values. **review**

## 4. Effects

- Structure every entry point as: gather the inputs, compute with pure
  functions, carry out the effects. Keep the middle large and the ends
  thin. `sonouchi.app` shows the shape. **review**, and the guard
  prints the sizes of core and shell
- Let pure code return a description of what should happen, as data,
  and let the shell carry it out. **review**
- Browser APIs are effects: the DOM, storage, timers, the clock,
  `js/console`, `fetch`. They belong in the shell. **guard**
- Never put effects inside a lazy sequence. `(map save! xs)` does
  nothing until something reads it, and then it runs at a surprising
  time. In the shell, use `run!` or `doseq` for effects. In the core,
  `doseq`, `dotimes` and `run!` are banned, because the core has no
  effects. **guard** for core; **review** for the shell
- Do not hide I/O or logging that matters behind a function that looks
  pure. Printing and `tap>` are banned in the core. **guard**

## 5. The interface

- **Views are pure functions** from the state value to hiccup, in the
  core. The shell only renders what they return. **guard**
- **Event handlers are data.** In hiccup, `:on` and Replicant's
  lifecycle hooks hold action vectors, such as
  `[:action/record-worked-on task-id]`, never functions. One dispatch
  function in the shell receives every action, adds what only the
  shell knows (the time, new ids), and calls a pure function in the
  core that returns the new state. **review**
- **Every act is a fact.** An action that changes the record appends
  facts with their time; it never edits one in place. Undo appends a
  retraction. **review**
- **Stable and deterministic.** The same state gives the same screen.
  Nothing on screen moves unless the owner moved it (V6). **tests**,
  where a view can be tested; **review** otherwise
- **Measure release builds.** Judge speed against V12's 0.1 s with
  `npx shadow-cljs release app`. Development builds are much slower.
  **review**

## 6. Composition

- Describe transformations: `map`, `filter`, `reduce`, `into`,
  `group-by`, `for`, and threading with `->` and `->>`. Avoid
  `loop`/`recur` and hand-built accumulators; when an algorithm is
  iterative, a step function with `iterate` usually reads better. The
  ratchet counts `loop`. **guard**
- Write transformations independently of where their data comes from
  and where it goes. **review**
- Do not rely on the order of a map or a set. Sort when order matters,
  so that the same input always gives the same output. This is also
  what keeps layouts stable (V6). **review**
- Take collaborators as arguments; do not construct or look them up
  inside a function. **review**
- Keep transforming, moving, routing and remembering in separate
  parts. **review**
- Put policy and business rules in one place, as data where possible,
  instead of conditionals spread over files. **review**
- For behaviour that varies by kind of data, use a multimethod or a
  protocol, which can be extended without editing existing code. Do
  not repeat `case` or `cond` on the same kind in several places. The
  action dispatch is one such place: one multimethod, or one map from
  action to function. **review**
- Never call the host from the core: no Java, no JavaScript, no
  `js/`, no `#js`. Clojure has a function for nearly everything the
  core needs; for mathematics, use `clojure.math`. **guard**

## 7. Facts, storage and names

- Record facts with their time and author. Append; never update in
  place. A correction is a new fact. **review**, until there is
  storage code to check
- Keep history unless there is a specific reason to discard it.
  **review**
- Treat a read from storage as obtaining a value, and write queries as
  functions of that value. **review**
- Send values across every boundary: storage, files, HTTP to a server.
  EDN, not JSON built by hand. **review**
- Grow by accretion: provide more, require less. Never remove or
  repurpose a public function or an attribute. If a meaning changes,
  add a new name beside the old one. **review**

## 8. Testing and debugging

- Test pure functions with plain, made-up values. If a test needs
  elaborate setup, the code under test has too much state or too many
  hidden dependencies; extract the pure part and test that. **review**
- Test views as data: call the view with a state value, and look for
  what must be in the hiccup. No browser is needed. **review**
- Never use `with-redefs` or mocks. They exist to replace hidden
  dependencies, and the core has none. **guard**
- Add a property test with `test.check` where a rule holds for every
  input. **review**
- The tests run on the JVM and on Node, so a difference between the
  platforms fails the check. **tests**
- When debugging, capture the input values and call the function with
  them at the REPL. If you cannot, note what prevented it and fix
  that. **review**
- Tests and lint are safety nets. They do not replace being able to
  reason about the code. **review**

## 9. Traps for coding agents

- **Brackets.** An edit that loses a `)` breaks the file. Keep forms
  small, and edit whole top-level forms rather than fragments. After
  every edit, the clj-kondo hook reports unbalanced brackets; fix them
  before doing anything else. **kondo**
- **Silent nil.** A missing or misspelt key gives `nil`, and the error
  shows up far away. The registry catches misspelt qualified keys.
  Use the attribute names exactly as the registry has them. **guard**
- **Numbers differ.** On the JVM, `(/ 1 2)` is the ratio `1/2`; in the
  browser it is `0.5`. Integers are exact on the JVM and are doubles in
  the browser. In the core, make the type explicit, for example with
  `quot`, `double` or `clojure.math/round`. **tests**
- **Laziness.** See section 4. Also, do not hold the head of a large
  lazy sequence, and do not depend on when a lazy sequence is
  realised. **review**
- **Invented functions.** Use only functions you can see in
  `clojure.core`, the namespaces listed above, Replicant's documented
  API, or code in this project. clj-kondo reports unknown names and
  wrong arities. Do not guess library APIs; ask the owner instead.
  **kondo**
- **Shadowing.** Do not name locals after `clojure.core` functions,
  such as `name`, `key`, `val`, `count`, `set` or `ns`. **kondo**
- **Build tool.** This project uses shadow-cljs with `deps.edn` for the
  ClojureScript build, and the Clojure CLI for the checks. Never add
  Leiningen files or `project.clj`. **review**
- **Dependencies.** Prefer `clojure.core` and the standard library.
  Ask the owner before adding a library, from Maven or npm. **guard**:
  the dependency files are protected, and npm commands that change
  them are blocked
- **Requires.** Require with `:as` aliases. No `:use` and no
  `:refer :all`. Keep the requires sorted. **kondo**, **fmt**

## Red flags in review

Each line pairs what you see with what to do instead.

- A setter, or a field assigned after creation: return a new value.
- Different results for the same arguments: pass the varying thing in.
- An argument changed by the callee: return the new value.
- A defensive copy: the data is already immutable; remove the copy.
- A record or type whose only job is to hold fields: use a map.
- A long positional argument list or a wide vector: use named keys.
- Every key treated as optional: omit what is unknown, and state the
  requirements per use in the docstring.
- The same structure destructured in many places: read by key where
  needed.
- A function inside hiccup: use an action vector.
- State kept in a closure, a local atom or the DOM: put it in the one
  state value.
- A view that reads the clock, storage or the DOM: pass the value in.
- A loop building a result in an accumulator: use a transformation.
- The same rule's conditionals in several files: one rule table.
- A stored record updated in place: append a new fact.
- A renamed or removed public function or attribute: add the new one
  beside the old one.
- A test with heavy setup: extract the pure core and test that.

## When you must deviate

Sometimes a rule must give way:

- **performance:** measure first, with a release build, then mutate
  locally behind a pure interface, for example with a transient, and
  let only an immutable value escape
- **a JavaScript library that needs mutable objects:** confine it to a
  shell adapter and convert to values at once
- **machine-like work** such as caches and storage handling: isolate
  it in a small, named shell component with a value-based interface

Declare the deviation on the definition, with the reason:

```clojure
(defn ^{:deviation "measured: 40x faster with a transient on 1e6 tasks"}
  index-tasks [tasks]
  (persistent! (reduce (fn [m t] (assoc! m (:task/id t) t))
                       (transient {})
                       tasks)))
```

The guard then accepts the banned names inside that definition, and
counts it. The count may not rise without the owner's agreement, so
explain each new deviation to the owner. Keep each one as small as
possible.

`:task/id` in this example is only an illustration and is not in the
registry.
