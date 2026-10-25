# The next design and coding tasks. When an item is done, delete it.

## Owner: read every design doc

Nothing is built until these are read, in this order:


- `doc/architecture.md`
- `doc/architecture-log.md`
- `doc/architecture-search.md`
- `doc/architecture-interface.md`
- `doc/architecture-shell.md`
- `doc/research-workflow.md`
- `doc/research-visual.md`
- `doc/todo.md`

## Owner: finish the merge

- Apply [merge-guard-rails.md](merge-guard-rails.md), run `bin/check`,
  then delete that document and the `../soonish` folder.

## Next, after the merge

- **Time in the data.** Instants and millisecond durations, in the
  form Clojure uses on both platforms. Working days appear only in
  what the owner is asked and shown. Settle from design3 what a
  duration measures, working time or elapsed time, since a 3-day task
  started on a Friday is 5 days on the clock. Then replace the
  scheduler's day attributes, and the ISO days planned in
  architecture.md, with the new attributes.

## Design

- **Sketch the schedule.** Design3's running example, drawn as a Gantt
  chart with the critical path, as a layered drawing with dates, and
  as PlanningLines, on one static page. Choose by the display rules
  and the research, and write the choice and its grounds into
  [ux-journey.md](ux-journey.md), "The schedule".
- Correcting a start, finish or wait that was recorded wrongly, after
  other acts have followed it, when undo would undo too much.

## Build version 0, in order

- **Days.** `sonouchi.core.days`: the presentation of time in working
  days, as settled by "Time in the data": the day before and after,
  whether a day is a working day, the last working day before a given
  day, and the working days between two days, from `:state/now` and
  `:state/utc-offset-minutes`. Pure arithmetic, tested on both
  platforms, with no host calls.
- **Register the attributes** for version 0 in
  `src/sonouchi/core/attrs.cljc`, from architecture.md. Owner: it is a
  guard-rail file.
- **The board's queries.** `sonouchi.core.board`: each task's column,
  the order within each column, and whether a task has a record for
  the recording day, from the log. Tested with made-up logs.
- **The frame and the board.** The view with the frame and the four
  columns; the query bar, with the text in `:state/query`; the search
  index in `:state/index`, updated with each act; the shell's
  tokenizer, on `Intl.Segmenter`.
- **The stand-up keys.** The cursor, `s`, `f`, `w`, `b`, `e`, the
  recording day with `[` and `]`, and undo and redo.
- **Estimates.** `sonouchi.core.estimates`, `t` and its field, and the
  latest estimate on each card.
- **The replacement display.** Marking with `x`, `r`, the lines with
  their states and estimates, ⌘Enter and Escape; Enter in the query
  bar opens it with no old tasks. Delete the quick-entry code that
  nothing uses any more.
- **Comes after.** `a`, with the check for cycles, and carrying a
  replaced task's successors over to its replacements.
- **The schedule.** In `sonouchi.core.schedule`, the backward pass and
  the critical path, and the board's tasks turned into its input; then
  the display chosen from the sketch, `2` in the frame. The forward
  pass is built and tested on design3's running example.
- **Measure** a release build against V12's 0.1 s, with a few hundred
  tasks, including building the search index when the page opens and
  computing the schedule after each act.
- **Try it on real work,** as in [ux-design.md](ux-design.md), "How
  each part is designed", step 9.

## Version 1

- The forecast: design3's Step 1 in the core, and its display as a
  quantile dotplot with P50 and P80, beside the owner's
  estimate. Every forecast shown is kept.

## Parked until version 1 is in use

See [goals.md](goals.md), "Parked".

- The ranked backlog: one flat list, ordered by key, with an exact
  order near the top, and how the order is recorded as facts.
- Requests: who is waiting on what, and the forecast of each.
- Stale tasks fading, and lapses showing.
- Which tasks serve which requests, as a table.
- Finding the working set: facets with counts, suggestions, saved
  queries.
- The time view for the owner's boss, compared on the boss's real
  questions.
- The timing of every act: cross-check with the Event Timing API, keep
  a performance log beside the fact log, and replay a recorded morning
  on the Pi and an old MacBook.
- Choose a drawing library, if the schedule's sketch needs more than
  plain SVG from hiccup: D3 or Vega-Lite.
- A browser test runner for the shell, if the shell grows beyond what
  the REPL and the trials catch.
- Japanese, English or both, once anyone else uses sonouchi.
- Whether the GitHub Projects board is still needed, once the session
  after the stand-up works.
