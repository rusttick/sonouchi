# Architecture

The parts of sonouchi, the data they share, and how an act flows
through them. Components of more than about 50 lines have their own
document, linked below. The coding rules are in
[CONTRIBUTING.md](../CONTRIBUTING.md).

## The shape

```
 browser events ──▶ shell (sonouchi.app, .cljs) ──▶ pure core (sonouchi.core.*, .cljc)
                     │  the one atom: the state            values in, values out
                     │  ids, the clock, the platform       views → hiccup
                     │  storage, files, timing             acts → facts to append
                     ◀──────────── hiccup, acts ◀──────────┘
                     │
                     ▼
                  Replicant renders the hiccup
```

- **The pure core** holds every rule: what an act appends, what the
  record means, what the schedule is, what the screen shows. It has no
  state, no clock, no host calls, and runs on the JVM and in the
  browser alike, so it is tested on both, and the same scheduler
  serves the browser and the command line.
- **The shell** gathers inputs (a key press, the time, a new id), calls
  the core, and carries out effects: appending to the log, saving it,
  rendering. It is kept thin. See
  [architecture-shell.md](architecture-shell.md).
- **The command line,** `sonouchi.main`, is a second shell, on the JVM.
  It reads tasks from an EDN file and prints their schedule, as in
  `clojure -M -m sonouchi.main examples/running-example.edn`.

Event handlers in the hiccup are data: action vectors, which the
shell's one dispatch function receives.

## The state

One immutable map, in one atom in the shell. Every key is a qualified
attribute in the registry, `src/sonouchi/core/attrs.cljc`, with its
meaning and schema.

| Key | What it holds |
|---|---|
| `:state/log` | the fact log: every act, oldest first |
| `:state/problems` | problems to show the owner, as data |
| `:state/platform` | `:mac` or `:other`, for the keyboard conventions |
| `:state/now`, `:state/utc-offset-minutes` | the shell's clock, the only time the core sees |
| `:state/last-act-ms` | how long the last act took to paint |

The screen is a function of this value alone.

## The record: a log of facts

The record is a vector of acts, only ever appended. An act says which
action made it, when, and which entities it changed, as patches: maps
of the attributes it set, and optionally the attributes it removed. An
entity's current map is its patches merged in order. Undo appends an
act whose patches put back the values from before. See
[architecture-log.md](architecture-log.md).

The log is all there is. Tasks, columns, counts, the schedule and the
search index are computed from it. Saved to EDN, it can be merged with
another log, because every id is a random UUID.

## The core's components

Built:

| Namespace | What it does | Document |
|---|---|---|
| `sonouchi.core.attrs` | the attribute registry: every qualified keyword, with its meaning and schema | this page, "The state" |
| `sonouchi.core.log` | entities from the log, the patches that undo an act, checking a log read from a file | [architecture-log.md](architecture-log.md) |
| `sonouchi.core.history` | undo and redo, worked out from the log alone | [architecture-log.md](architecture-log.md) |
| `sonouchi.core.search` | the search index of task titles, as a value | [architecture-search.md](architecture-search.md) |
| `sonouchi.core.keys` | which key does what on each platform, and how keys are shown | [architecture-interface.md](architecture-interface.md) |
| `sonouchi.core.tasks` | adding tasks, and the tasks added today | [architecture-interface.md](architecture-interface.md) |
| `sonouchi.core.view` | the page, as hiccup | [architecture-interface.md](architecture-interface.md) |
| `sonouchi.core.schedule` | design3's Step 0: the forward pass of the critical path method, with actual days replacing computed ones, and cycles and unknown tasks described as data. The backward pass and the critical path are not written yet | this page |

The page built today is a quick-entry field with the tasks added today
under it. The screens in [ux-journey.md](ux-journey.md) replace it.

Planned for version 0:

| Namespace | What it does |
|---|---|
| `sonouchi.core.days` | days as ISO strings: the next and previous day, working days, and counting working days between two days |
| `sonouchi.core.board` | each task's column, the order in a column, and whether a task has a record for the recording day |
| `sonouchi.core.estimates` | each task's latest estimate, and whether design3 counts it |
| `sonouchi.core.schedule`, more | the backward pass, slack and the critical path; and the tasks on the board turned into the scheduler's input |

## The data for version 0

The screens in [ux-journey.md](ux-journey.md) need these facts. They
are not yet in the registry.

**A day** is a local calendar date, written as an ISO string such as
`"2026-10-23"`. EDN has no date type, and a string compares and sorts
correctly on both platforms. The core does the calendar arithmetic
itself, Monday to Friday being working days, until design 3 adds
calendars (its step 6).

This is to change. Time in the data is to be instants and durations in
milliseconds, with working days only in what the owner is asked and
shown ("Time in the data", in the open questions below). Until then, the scheduler counts working
days from day 0, as its attributes `:task/estimate-days` and
`:task/actual-start-day` say.

**A task** is an entity with:

| Attribute | Value |
|---|---|
| `:task/title` | the words on the post-it |
| `:task/after` | the set of tasks it comes after: it cannot start until they finish. Already in the scheduler |
| `:task/started` | the day it started |
| `:task/finished` | the day it finished |
| `:task/worked-on` | the set of days it was worked on |
| `:task/replaced-by` | the set of tasks that replaced it. An empty set means it was dropped. A task with this attribute has left the board |

**An estimate** is an entity of its own, so that every estimate of a
task is kept, each with the time it was made:

| Attribute | Value |
|---|---|
| `:estimate/task` | the task estimated |
| `:estimate/amount` | the number the owner typed, positive |
| `:estimate/unit` | always `:working-days`. design3 stores the unit with every number; the unit is fixed because it changes the estimate (Jørgensen 2015) |

When it was made is its act's `:act/at`, and who made it is the act's
`:act/by`, as with every fact.

**A wait** is an entity of its own, so that a task can wait several
times and each wait keeps its own times:

| Attribute | Value |
|---|---|
| `:wait/task` | the task that waits |
| `:wait/reason` | `:person` or `:outside-party`, design 3's categories |
| `:wait/on` | on whom, in the owner's words |
| `:wait/began` | the day it began |
| `:wait/ended` | the day it ended; absent while it lasts |

**The acts** that append them:

| Action | Patches |
|---|---|
| `:action/record-start` | the task's `:task/started`, and the day added to `:task/worked-on` |
| `:action/record-finish` | `:task/finished`, and the day added to `:task/worked-on` |
| `:action/record-worked-on` | the day added to `:task/worked-on` |
| `:action/begin-wait` | a new wait entity |
| `:action/end-wait` | the wait's `:wait/ended` |
| `:action/replace` | each new task, with its title and, by its chosen state, `:task/started` and `:task/finished`; a new estimate entity for each new task that was given one; each old task's `:task/replaced-by`; and the `:task/after` of each task that came after an old task, with the old task swapped for all of its replacements, or removed if it was dropped |
| `:action/correct-title` | the task's new `:task/title` |
| `:action/estimate` | a new estimate entity |
| `:action/set-after` | the task's whole new `:task/after` set, or its retraction when the set is empty. Not recorded if it would make a cycle |

Each act records two times, as design 3 needs: the day in the patch,
when it happened, and `:act/at`, when it was recorded. A value in a
patch replaces the old one, so `:task/worked-on` and `:task/after` are
patched with the whole new set. `:action/add-task` stays readable, so
that logs from quick entry still load; new tasks come from
`:action/replace`.

**Computed, never stored:**

- a task's column: Done if it has `:task/finished`, Waiting if a wait
  for it has no `:wait/ended`, In progress if it has `:task/started`,
  To do otherwise. Tasks with `:task/replaced-by` are not on the board
- the order in a column: by the time of the act that last moved the
  task into it, latest first
- whether a task has a record for the recording day: any act that
  patched it, one of its waits or one of its estimates, with that day
- a task's latest estimate: the estimate entity for it with the latest
  act
- whether design3 counts an estimate: the local day of its `:act/at`
  is on or before the task's `:task/started`, or the task has not
  started
- the schedule: each task's start and finish day, and the critical
  path, from the tasks' latest estimates, `:task/after`, actual dates
  and today. A task with no estimate has no dates, nor does any task
  after it. A task that is not finished finishes today at the
  earliest, even when it has run past its estimate

**The screen's own state,** in the state map and not in the log,
because it describes the screen and not the work:

| Key | What it holds |
|---|---|
| `:state/screen` | `:stand-up`, `:replace` or `:schedule` |
| `:state/query` | the text in the query bar |
| `:state/recording-day` | the day the stand-up keys record |
| `:state/cursor` | the task under the cursor |
| `:state/marked` | the tasks marked, for replacement or for `a` |
| `:state/draft` | the replacement being typed: the old tasks, and each new line's title, state and estimate text |
| `:state/field` | the one small field open on a card, for a correction, a wait or an estimate, with its text |
| `:state/index` | the search index |

Every typed character goes into the state, not only into the DOM,
because what the screen shows is a function of the state. The search
index is updated with each act, for the tasks the act changed.

## Timing

Each key act is timed in the shell from the key press to the next
painted frame, and the footer shows it, flagged above 0.1 s (V12).
Speed is judged on a release build. The schedule is computed again
after every act, so it is part of what is timed. It is measured with a
few hundred tasks, and includes building the search index when the
page opens.

## Open questions about the model

The interface is designed first, and these questions about the model's
data are answered from what it turns out to need. The answers change
Step 0 in [design3.md](design3.md).

- **Replacement.** Step 0 has no facts for replacing tasks, and about
  nine tasks in ten are replaced. A replaced task's estimate can be
  censored at its replacement, left out, or compared with the total
  of its replacements; the record keeps what all three need, and
  Step 1 chooses by out-of-time score (design3, 3.1). Plain censoring
  assumes that replacement says nothing about how long the task would
  have taken, which is doubtful, because tasks are replaced when they
  turn out different. Tasks that came after a replaced task come
  after all of its replacements (ux-journey, Stage 4).
- **Decided, for the scheduler to follow.** An unfinished task
  finishes today at the earliest, even past its estimate. A task with
  no estimate, and every task after it, has no dates.
- **Days worked,** with the day and when it was recorded.
- **Waits.** Step 0 records a reason only when a start is late. Waits
  in the middle of a task are part of its elapsed duration, and are
  recorded the same way.
- **The estimate question** is design3's: working days, equally likely
  too high or too low, so the estimate is a median of elapsed working
  days. There is no second question for days of work: no study
  compares the two for interleaved work (workflow 10), each question
  asked costs effort, and effort is how outcome records go missing
  (Jørgensen 2004). Days worked are recorded anyway, so design3 can
  study effort later.
- **The unit** the owner is asked in is always working days, stored
  with each estimate. How time is held in the data is a separate
  question, below.
- **Time in the data.** Instants and millisecond durations, in the
  form Clojure uses on both platforms. Working days appear only in
  what the owner is asked and shown. Design3 settles what a duration
  measures, working time or elapsed time, since a 3-day task started
  on a Friday is 5 days on the clock. The answer replaces the
  scheduler's day attributes, and the ISO days of `sonouchi.core.days`
  above, with new attributes; `sonouchi.core.days` then presents time
  in working days, from `:state/now` and `:state/utc-offset-minutes`.
- **Corrections.** How a start, finish or wait that was recorded
  wrongly is corrected after other acts have followed it, when undo
  would undo too much.
- **Order.** How a manual order is recorded as appended facts, with an
  exact order only near the top.
- **Requests.** Who asked, in what words, any promise, each report
  back; which tasks serve which requests; and the requests the owner
  makes, which end waits. Is a request anything with an asker?
- **Relations.** Which were used: comes after, part of, serves,
  replaced by. Does a task with parts have an estimate of its own?
- **Tags and attributes.** How often each is used. Bergman 2013
  predicts less than people expect.
- **A server,** if one is added: the browser sends its facts as EDN or
  Transit, and the log becomes the facts sent. The schedule is
  computed by the same core on both sides.
- **Offline use.** The browser works offline now. Does it need to stay
  so?
