# sonouchi: TODO

sonouchi is the browser client for soonish, the project estimation server.
It is written in ClojureScript.
This file lists the work in order.
Nothing is decided unless it is under "Decided".

**Paths.** `../soonish/` assumes sonouchi sits next to soonish, as it will once it moves out of the factory repository.

**The research.**
- [ux-research.md](ux-research.md): the workflow, the data it needs, and design ideas I1 to I10.
- [ux-research-visual.md](ux-research-visual.md): perception and interaction, and rules V1 to V17.

## Decided

- **The owner's own tool first.** The owner is the only person who enters tasks and estimates, and the only consumer.
  It serves two needs:
  - keeping scattered research tasks in priority order
  - telling themselves and coworkers, with good estimates, when the tasks they care about will be finished

  Coworkers, the board tool path's other user groups, and field research with them come later, if at all.
- **The UI first, standalone in the browser.** It is designed and iterated on a data set held in the browser, with no server.
  The data structures and rules for soonish are revisited afterwards, from what the UI turned out to need.
- **Designed from published research,** not from guesses.
- **Tested by the owner on their own work.** After each change, the owner tries it against their real tasks, and keeps a dated friction log.
- **Mirrors the whiteboard quickly.** The whiteboard covers this sprint. sonouchi covers the whole backlog and the preparation for the next sprint planning.
- **Kanban is not the only organising view.**
- **No tree-shaped views.** The data is not a tree: a task can serve several requests, merges have several predecessors, and dependencies form a graph.
  No nested, collapsible outline (`ux-research.md`, section 9).
- **Not a work companion.** sonouchi is not left open on the desktop while people work, because much of the work happens with no computer nearby.
  It is used in short sessions, such as the morning stand-up.
- **Effort is recorded as days worked, never hours.** At the morning stand-up the owner records which tasks they worked on the day before.
  There is no timer and no timesheet (`ux-research.md`, section 10).
- **Reordering:** by key, with drag only for short moves, and an exact order only near the top (`ux-research.md`, section 11).
- **Less familiar displays only with a proven reason.** Layouts are stable and deterministic. Modes are visible.
- **ClojureScript**, so that client and server share one language, one style and one set of guard-rails.
- **A client for soonish.** The model it serves is `../soonish/doc/design3.md`, starting with Step 0, including dependency links.
- **A self-contained repository** later, like soonish. Its CONTRIBUTING.md will adapt `../soonish/CONTRIBUTING.md` to the browser.
- **The coding philosophy** is the same: values, pure functions, a thin shell around a pure core, and facts appended rather than overwritten.
- **One user on a laptop.** No simultaneous editing, and no CRDTs.
- **Sign-in later, perhaps:** Google OAuth through a proxy in front, so that the app never handles sign-in itself.
- **The boss's Gantt chart comes after the owner's UI works.**

## 1. A prototype that can be changed quickly

Only what is needed to iterate on the UI.

- [x] **Build tool.** shadow-cljs 3.5.7, set up on 2026-10-18.
      Clojure dependencies are in `deps.edn` (alias `:shadow`), and the npm package is pinned in `package.json` and `package-lock.json`.
      `npx shadow-cljs watch app` serves http://localhost:8080 with hot reload.
- [x] **Rendering library.** Replicant 2026.07.1, set up on 2026-10-18.
      The view is a pure function from data to hiccup, with event handlers as data. No React and no npm packages.
      Chosen over Reagent, re-frame and UIx for its size (about 19 KB gzipped), speed and fit with the coding rules.
- [ ] **Timing every act.** Wrap the one dispatch function to time input delay, update, render and paint, with `performance.mark`.
      Cross-check with the Event Timing API. A development overlay shows the last act against V12's 0.1 s.
      Keep the timings in a performance log, apart from the fact log, exported with it.
      A recorded morning of acts can be replayed on the Pi and an old MacBook as a repeatable load test.
      Begun 2026-10-18: each key act is timed from the key press to the next painted frame, and the footer shows it, flagged above 0.1 s.
      Still to do: the Event Timing cross-check, the performance log, and the replay.
- [ ] **Drawing library,** for the displays that are not lists or tables (V5, V17): D3, Vega-Lite, or plain SVG from hiccup.
  Choose when the first such display is needed, not before.
- [x] **State.** One atom in `sonouchi.app`, holding an immutable value. The ratchet counts it.
- [x] **The in-browser data set is a log of facts.** Each act appends a fact with its time, as soonish will.
  Views are pure functions of the log, so moving to soonish later changes where the log is kept, not the UI.
  **Decided 2026-10-18: maps all the way down.** A fact is a map patch: the attributes of one entity that an act changed.
  An entity's current map is its patches merged in order. Not datoms: they convert mechanically if soonish ever needs them (section 4).
  Built in `sonouchi.core.log`:
  - the log is a vector of acts; an act has `:act/id`, `:act/at`, `:act/action` and `:act/patches`
  - ids are random UUIDs, made by the shell, so exported logs can be merged without clashes
  - `:act/by` is left out while there is one user: an act without it was made by the owner
  - a value in a patch replaces the old one, sets included; `:patch/retract` removes attributes
  - undo appends an act whose patches put back the values from before (`undo-patches`); it has no key or button yet
- [x] **Keeping the data.** The log is saved in the browser's storage on every change, and exported to and imported from an EDN file.
      An import replaces the log, after keeping the old one in the browser's storage. A saved log that cannot be read is kept aside, not overwritten.
- **No seed data** (decided 2026-10-18). The owner enters tasks by hand, which also tests how well adding tasks works.
- [ ] **Estimates made in the prototype** are marked as prototype data.
      They count for design 3 only if the estimate question was already in its final wording (section 4).
- [x] **The pure core in `.cljc`,** with tests on the JVM and on Node, so that it can move to soonish unchanged.
- [x] **clojure-mcp,** with the JVM REPL for the core and shadow-cljs's nREPL (port 7888) for the browser (`CONTRIBUTING.md`, "The REPLs").
- [x] **The guard-rails, ported from soonish** on 2026-10-18: `CLAUDE.md`, `CONTRIBUTING.md`, `bin/check`, the guard, clj-kondo, cljfmt and the Claude Code hooks.
      The guard reads ClojureScript and both sides of `.cljc`, and counts `js/`, `#js` and other JavaScript interop in the core as host calls.

## 2. How each part of the UI is designed

Each part of the UI is designed and tried one at a time, in these steps.
The V numbers are the rules in `ux-research-visual.md`, Part 6, and the I numbers are the ideas in `ux-research.md`.

1. **The question.** Write down the one question the part answers, and what the owner does with the answer (V1).
   Example: "which tasks on the whiteboard have no record yet today?"
2. **The facts.** List the facts it reads, and the facts each act appends.
   These lists are what section 4 uses to revisit soonish's data.
3. **The display.** Choose it by cognitive fit (V1): a list or table for exact values, a graph for patterns.
   - familiar forms first (V5); anything else needs a named source
   - the quantity on a common axis (V2)
   - one feature pops out for what needs attention (V3)
   - at most about four colours, never alone (V4)
   - chains as small node-link drawings, overlaps as tables (V17)
4. **The acts.** List every act, each with its key.
   - keys shown on screen next to what they act on (V11)
   - if it is a mode, it is shown large, and Escape leaves it (V10)
   - every act updates the display in place, within 0.1 s (V12), and marks what changed (V7)
   - every act can be undone; no confirmation dialogs (V13)
   - numbers typed into empty fields (V14)
5. **The frame.** Check that it keeps the shared frame (V9), a stable layout (V6), and holds on screen whatever the owner would otherwise have to remember (V8).
6. **The prediction.** Estimate the time for a typical use with the keystroke-level model (`ux-research-visual.md`, section 3.3).
7. **The sketch.** A text or paper sketch, reviewed against the checklist below before any code.
8. **The build.** Build it in the prototype.
9. **The trial.** Use it on real work for at least a week. Record:
   - the friction log
   - time per use, against the prediction
   - the number of undos, as a count of errors
10. **The verdict.** Keep, change or drop, with the reason, in a dated note in `notes/`.
    A change goes back to step 3. Each trial uses only one version, because practice alone makes any design faster (`ux-research-visual.md`, "How to measure each version on oneself").

**The checklist** for step 7, and again after step 8:

- [ ] V1 one question, stated
- [ ] V2 the quantity on a common axis
- [ ] V3 one thing pops out
- [ ] V4 four colours or fewer, never alone, colourblind-safe
- [ ] V5 a familiar form, or a named source for an unfamiliar one
- [ ] V6 the same input gives the same layout; nothing moves unless the owner moves it
- [ ] V7 changes are marked
- [ ] V8 nothing has to be remembered that could be shown
- [ ] V9 the query, the action and the count are in their usual places
- [ ] V10 the mode is visible, and Escape leaves it
- [ ] V11 every key is shown
- [ ] V12 every act responds in place within 0.1 s
- [ ] V13 every act can be undone, with no confirmations
- [ ] V14 number fields start empty
- [ ] V15 the working set can be reached without typing
- [ ] V16 a light theme and a Gothic face
- [ ] V17 chains drawn as node-link, overlaps as tables

## 3. The parts, in order of use

Ordered by how often the owner needs them, so the most frequent is tried first.
Each goes through section 2.

- [ ] **The frame** (open). How the working set and the action are chosen.
      One candidate is a query, then an action, then a display for that action (`ux-research-visual.md`, "The concept being designed").
      Not fixed yet: decide by trying it on the first two or three parts.
- [ ] **Quick entry (I4).** A task can be just a title. Everything else is added later, when it pays.
      First, because with no seed data the log is empty until tasks can be added.
      Designed and built on 2026-10-18; in trial from 2026-10-19 (`notes/2026-10-18-quick-entry.md`).
- [ ] **The stand-up pass (I7).** One key per task: worked on yesterday, started, finished, waiting (with a reason), replaced.
- [ ] **The ranked list (I2).** Flat, ranked and searchable. No nesting.
      Moves are made by key (up, down, to the top, below a named task) and by drag for short moves.
- [ ] **Reframing in one act (I3).** "Replace this with these", plus rename, split and merge.
- [ ] **Boards (I1).** This iteration and the next, grouped by state. They show the same tasks, never a second copy.
- [ ] **Requests (I6).** Who is waiting on what, and the forecast finish of each request.
- [ ] **The estimate prompt.** Design 3's wording every time. No other estimate shown first. Only estimates made before the start count.
- [ ] **Stale tasks fade (I8), and lapses show (I9).**
- [ ] **Overlaps (V17).** Which tasks serve which requests, as a table.
- [ ] **Finding the working set (V15).** Typed words, facets with counts, suggestions from the owner's own words, saved queries.
      Grows with each part, because each part needs a working set.

After all of these, ask whether the GitHub Projects mirror is still needed.

## 4. Revisit the data and rules for soonish

After section 3, using the facts each part actually read and appended (section 2, step 2).
These questions were raised in `ux-research.md`, sections 9 to 11.

- [ ] **Requests and work** (section 9). Is a request anything with an asker? What facts does a request have: asker, words, promise, reports back?
- [ ] **Relations** (section 9). Which were used: finish-to-start, part of, serves, replaced by?
      Does a task with parts have an estimate of its own?
- [ ] **Reframing facts.** Rename, split, merge and replace.
      What happens to the estimate of a task that is replaced before it finishes: censored, discarded, or compared with the total of its replacements?
- [ ] **Tags and attributes** (section 9). How often each was used.
      Bergman 2013 predicts that they will be used less than expected, and the owner predicts the opposite.
- [ ] **Days worked** (section 10). Task, the day it was worked on, and when that was recorded.
- [ ] **The estimate question** (section 10). Does it ask "how much work?" or "how long until it is done?"?
      Both can be compared with the record, one with days worked and the other with elapsed days.
      This must be settled before estimates count as design 3 data.
- [ ] **The unit** (section 10). A natural unit per estimate, recorded with the estimate.
- [ ] **Order** (section 11). How a manual order is recorded as appended facts, given an exact order only near the top.
- [ ] **Write the answers into soonish's design.** They change Step 0 in `../soonish/doc/design3.md`, which soonish owns.
- [ ] **The contract.** The client sends facts and reads back computed schedules, as EDN or Transit.
      The in-browser log becomes the facts sent.
- [ ] **Where the pure core runs.** The Step 0 schedule is a pure function. In `.cljc` it can run in the browser, for V12's 0.1 s.
- [ ] **Where the attribute registry lives.** Today it is `../soonish/src/soonish/core/attrs.clj`.
      Options: a shared library, a git dependency on soonish, or a copy that a check keeps in step.
- [ ] **Offline use.** The prototype already works offline. Does it need to stay that way?

## 5. Project hardening

When the UI has settled.

- [x] clj-kondo, cljfmt and the guard, adapted (section 1).
- [x] Claude Code hooks (section 1).
- [x] Move to its own repository, beside soonish.
- [ ] A browser test runner for the shell, if the shell grows beyond what the REPL and the owner's trials catch.
- [ ] README with development setup, and a Tests section like soonish's.
- [ ] Remove the remaining references to the factory repository: `TODO.md` ("Later", other users) and `ux-research.md` (the board tool path, the hickey guide).

## Later

- [ ] **The time view for the boss (I10).** Compare a Gantt chart with one or two alternatives on the boss's real questions:
      milestone ranges, PlanningLines-style bars, or dotplots per milestone (`ux-research.md`, section 6).
- [ ] **Uncertain dates** from design 3's step 1: quantile dotplots (`ux-research.md`, section 7).
- [ ] **Other users,** when there is something worth showing them.
      `docs/project-management/board-tool-path.md` in the factory repository has the user groups, the ethics rules, and how use and feeling are measured.

## Open questions

- [ ] The frame (section 3).
- [ ] Everything in section 4.
- [ ] Japanese, English, or both, once anyone else uses it.
