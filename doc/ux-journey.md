# UX journey: the session after the stand-up

What the owner does each morning, what sonouchi shows at each step,
and the keys for each act. This is version 0 in
[goals.md](goals.md): design3's Step 0, with a face. The rules cited
(V1 to V17) and the principles behind them are in
[ux-design.md](ux-design.md).

## Before sonouchi

The owner has kept the same tasks in three places by hand: the
whiteboard, and two views in GitHub Projects.

- **The whiteboard** shows the current sprint in columns, one post-it
  per task, from the sprint's backlog through in progress to done,
  with a column for blocked tasks: those waiting on something outside
  the owner's control.
- **A nested, searchable list of issues and sub-issues,** ordered by
  hand, for the whole backlog. Its one link, parent, did four jobs at
  once: it held a request from someone outside, it held the work
  answering it, it was renamed, split or merged when the work turned
  out different from the request, and it grouped work by requestor.
- **A board for each sprint,** with large cards, so only about eight
  are visible at once.

All three were updated after the stand-up. The problems this left:

- forgetting whether a task already exists, and adding it again, which
  makes the board longer and the next check harder
- keeping three views in step by hand (Baldonado 2000)
- no estimates beyond "can this be done this sprint?", and so no
  answer to "when will it be done?"

## The journey

| Stage | Where | The owner | sonouchi |
|---|---|---|---|
| 1. Stand-up | at the whiteboard, no computer | hears what moved, moves post-its, writes new ones | — |
| 2. Open | laptop | opens sonouchi | the board, laid out like the whiteboard |
| 3. Record | the board | for each task: started, finished, worked on, waiting | marks each task as it is recorded, so the ones left stand out |
| 4. Change | the board, then the replacement display | adds, drops, splits, merges, reframes, corrects wording, and estimates each new task | one act per change, and the new cards marked on the board |
| 5. Plan | the board or the schedule | estimates tasks that have none, says which tasks come after which | the schedule: each task's dates, and the critical path |
| 6. Close | — | closes the laptop | the record is saved as each act happens |

Stage 1 is where the owner's memory carries the most: what changed on
the whiteboard has to be carried to the laptop. Stage 2 keeps that
short by showing the same layout as the whiteboard, so nothing has to
be translated (visual momentum, Woods 1984). Stages 3 to 5 are each
one key per act, with the keys on screen (V11).

Stage 5 is also where the owner goes, at any time, when a coworker
asks "when will it be done?".

Later stages, not yet designed: the forecast of version 1, ordering
the backlog, and reviewing stale tasks.

## Words on screen

Two things could both be called "waiting", so the screen keeps them
apart:

- **comes after:** a task that cannot start until other tasks finish.
  This is design3's dependency, and the schedule uses it.
- **waiting:** a task held up by something outside the owner's
  control, such as a person or an outside party. It is a mark on the
  card, not a column (ux-design, "Waiting is not a state"), and
  design3 uses its reasons in Step 6.

Days are written weekday, month, day, as "Fri Oct 23", everywhere a
day is shown: the frame, the waits and the schedule. The research is
silent on the order (visual, "Where the research is thin"); the month
is a name, which practitioner guidance asks for and which makes either
order unambiguous. Chosen because it is the order the owner says at
the stand-up, "Friday October 23rd", so nothing is translated between
the whiteboard and the screen; it is also the order of Japanese dates,
10月23日.

## The frame

The same on every screen (V9):

    🦝   STAND-UP   [ query     12 of 31 tasks ]   recording last working day, Fri Oct 23   [ earlier · ] later

- **The action's name,** large (V10): STAND-UP, REPLACE, SCHEDULE or
  RECORD.
- **The query bar,** top centre. `/` moves to it. Typing filters every
  column at once to the tasks that match every typed word: a word of
  the title, or of whom an open wait is on, that starts with it, in
  any case and any order, allowing one typing mistake per five
  letters, with the first letter right.
  What is left keeps its order (V6). This is the check for duplicates.
  Enter opens the replacement display with no old tasks and the query
  text as the first new title, so a task that was not found takes no
  retyping. Escape clears the query and returns to the board.
- **The count:** the tasks shown, of all the tasks on the board, as
  "12 of 31 tasks", or "31 tasks" when there is no query. It is inside
  the query bar's border, at its right end, because it is the query's
  result: items inside one boundary are seen as belonging together
  (common region, Palmer 1992, visual 1.3). The trial showed the need:
  standing between the bar and the recording day, with nothing to group
  it with either, the count took a while to make sense of.
- **The recording day:** the day that started, finished, worked on and
  waiting refer to. It is the last working day before today, because
  the stand-up reports yesterday. `[` and `]` move it a day back or
  forward, so that a gap of several days can be caught up (Epstein
  2015). The date itself has the same weight as the screen's name,
  since both say what the keys act on; "recording", the word for how
  far back it is, and the keys are small and quiet around it, always
  alike. A day more than one working day back is flagged by those
  words alone, such as "3 working days ago", because recall falls off
  after a day (Gershuny 2017, workflow 10); the record is still taken.
  The trial showed that making the words full ink then read as the
  area changing with the keys, so they stay quiet. Moving the recording day
  is also how a wrong start, finish or wait is corrected: the same
  key on the right day appends a new fact, and the old one stays in
  the log (design3, rule 3). The keys are shown key first, as every
  other key is: `[` earlier · `]` later, and a click on either does
  what the key does (V11). Later is greyed out when the day is today.
  Hovering over the date says what it is for and how it moves. Before
  the date, as it would be said, words say how far back it is: today,
  yesterday, last working day (Friday, seen on a Monday), or n working
  days ago. With "recording" they take a fixed width, set against the
  date, so that the date and the keys stay in place while the day is
  stepped (V6); the spare width falls before "recording", not inside
  the phrase. It is
  not in a menu of its own, because it is state the owner needs in
  view whenever `s`, `f`, `w` or `b` is pressed (V8).
- **The actions,** in a flat list that is always in the same order,
  each with its key: `1` the stand-up board, `2` the schedule, `3` the
  record, where the log is exported to a file and imported from one.
  Export and import are seldom used, so they are a screen of their own
  and not in the frame; the screen shows how many acts and tasks the
  record holds and the days it spans, so that one file can be told
  from another. Only the screen keys act there. The
  list is a menu from the mascot, top left: the mascot and the
  screen's name are one button, with one hover text that names the
  keys, and a click on either opens it, as a speech bubble from the
  mascot. Each entry shows its key, its name and the question it
  answers. A click on an entry, a second click on the button, a click
  anywhere else, or Escape closes it. The keys work whether
  the menu is open or not. The owner chose the menu over buttons
  always on screen; V11 asks for keys shown on screen, so a trial
  should check that the keys are still found. The replacement display
  is opened from the board, not from this list.
- **The footer** shows how long the last act took, flagged above 0.1 s
  (V12).
- **Keys** are drawn as key caps wherever they are shown, each before
  what it does, as in `s` started. The research is silent on how a key
  is best marked; chosen because the trial showed that `[` and `]`
  beside the recording day read as punctuation, not as keys. The caps
  keep the text colour around them, so that they do not compete with
  what is meant to pop out (V3).

## Stage 3: the stand-up, on the board

**The question.** Which tasks on the whiteboard have no record yet for
the recording day? (V1, V3.)

**The display.** Three columns, side by side, in the whiteboard's
order. Each is computed from the facts, and nothing about columns or
positions is stored (ux-design, "The concept"):

| Column      | A task is here when              |
|-------------|----------------------------------|
| To do       | it has not started               |
| In progress | it has started, and not finished |
| Done        | it has finished                  |

A card shows the task's title, and under it, small, "no estimate"
when it has none. It does not show the estimate itself, because a
task's own earlier estimate anchors a re-estimate (ux-design,
"Estimates are made fresh"); the schedule shows the numbers. A card
with an open wait says, small, on whom it waits and since when, such
as "waiting on Tanaka since Tue Oct 20", or "waiting: not in the
plan"; the task keeps its column (ux-design, "Waiting is not a
state"). Typing the name in the query bar lists the tasks waiting on
that person. Within a column,
the card that arrived last is at the top: the order follows the times
of the owner's acts, so nothing moves unless the owner moved it (V6).
Done keeps every finished task, newest first; the query finds old
ones.

Tasks with no record for the recording day are in full ink. Recorded
ones are lighter, with a ✓ (V3, V8). Recording a start or finish also
counts as working on the task that day.

**The acts.** A cursor moves over the cards with the arrow keys, or a
click. The keys are shown beside the cursor (V11):

| Key | Act | The card |
|---|---|---|
| `s` | started on the recording day | moves to In progress |
| `f` | finished on the recording day | moves to Done |
| `w` | worked on, on the recording day | gets its ✓ |
| `b` | waiting begins: a small field on the card asks the reason and on whom, Enter records | gets its wait line |
| `b` on a waiting card | waiting ends | loses its wait line |
| `e` | correct the wording: the title becomes editable in place, Enter records, Escape leaves it as it was | keeps its place |
| `t` | estimate: see "Estimating", below | loses "no estimate" |
| `a` | the task comes after exactly the marked tasks; with none marked, after none. See "Comes after", below | keeps its place |
| `x` | mark, or unmark | shows a mark |
| `r` | replace the marked cards, or the card under the cursor | opens the replacement display |
| ⌘Z, ⇧⌘Z | undo, redo (Ctrl+Z and Ctrl+Y on Windows and Linux) | as before the act |

Every act changes the card in place, marks it (V7, V12), and can be
undone (V13). The reasons for a wait are design 3's three: a person,
an outside party, and not in the plan, meaning something else came
first. The field offers them as three keys, and "on whom" is typed
only for the first two.

**The prediction.** M + K, about 1.55 s a task, so about 16 s for ten
tasks; by mouse it would be M + P + K, about 27 s (visual 3.3).

## Stage 4: replacing N tasks by M

**The question.** What replaces these tasks, and how long will each
take?

**The display.** The old tasks on the left, by title only: no
estimates, before or after (ux-design, "Estimates are made fresh").
On the right, one line per new task, each with its state, its title
and its estimate. When there are no old tasks, this is adding; when
there are no new lines, it is dropping.

    REPLACE                                   Tab estimate · ⌥↑↓ state · ⌘Enter record · Esc cancel
    measure spindle runout     →   In progress   order dial gauge holder       [ 1 ] working days
                                   To do         measure runout at 3 speeds    [ 2 ] working days
                                   To do         compare with the 2024 log     [   ] working days

**The acts.**

- Type a title; Enter starts the next line. Pasting several lines makes
  several new tasks.
- Tab moves from a line's title to its estimate, and back. The
  estimate field starts empty, with "working days" beside it (V14). A
  line may be left without an estimate.
- ⌥↑ and ⌥↓ (Alt elsewhere) change the current line's state: To do,
  In progress, Done. A line starts with the old task's state when one
  task is replaced, and with To do otherwise. Every new task's state is
  shown, and is part of what is recorded. A new task in progress or
  done started on the old task's start day when exactly one old task
  had started, so that the elapsed time design3 measures is not lost
  when a running task is split or reframed; otherwise it started on
  the recording day. A new task done finished on the recording day.
  The line says which start it will get. A wait is set afterwards, on
  the board.
- ⌘Enter records the whole replacement as one act, with each new
  task's estimate, and returns to the board with the new cards marked
  (V7). The old cards leave the board.
- Escape returns to the board without recording anything.
- Undo removes the new tasks and puts back the old ones, in one step.

A task that came after an old task now comes after all of its
replacements, and its card is marked so the owner can prune them with
`a` (V7). The new tasks come after whatever the old tasks came after,
for the same reason. Carrying them all over errs late, the safe direction when
plans err early (Buehler 1994). A task that came after a dropped task
no longer comes after anything for it.

A new task that is recorded as already in progress or done gets its
estimate after it started, so design3 does not count that estimate
(goals, "What it records"). The display says so beside such a line,
and the estimate is still recorded.

**The prediction.** Splitting one task into three: mark, open and
record are a few keystrokes and one mental step, about 2 s. Typing
three titles of 25 characters is about 26 s, and three estimates a few
seconds more. The 2 s is what has to stay small (viscosity, Green
1996); the typing and the estimating cannot be avoided.

## Stage 5: planning

### Estimating

`t` on a card, on the board or the schedule, opens a small field on
it:

    measure runout at 3 speeds
    [    ] working days, as likely more as less        Enter record · Esc cancel

- The field starts empty, with no example number (V14). On the board
  the task's estimate is never shown; on the schedule it is hidden
  while the field is open, though the anchor remains (ux-design,
  "Estimates are made fresh").
- The words beside the field are design3's question in short: the
  number is equally likely to be too high or too low. The unit is
  always working days (goals, "What it records").
- Enter records the estimate; Escape leaves the card as it was. A
  number that is not positive is not recorded, and the field says
  why.
- A task can be estimated again. Each estimate is kept, and the
  schedule uses the latest. An estimate made after the task started is
  kept, but design3 does not count it; one made before the start is a
  revision, scored apart from the first.

### Comes after

To say that a task comes after others: mark those others with `x`,
move the cursor to the task, and press `a`. The task then comes after
exactly the marked tasks, and the marks are cleared. `a` with nothing
marked removes all of the task's predecessors. Undo puts back the
previous ones.

If the result would make a cycle, nothing is recorded, and the frame
shows which tasks form the cycle (Kahn 1962 detects it).

### The schedule

**The question.** When will each task finish, and which tasks set the
date of the last one?

**What it shows.** Every task on the board that is not done is
scheduled by the critical path method (design3, Step 0):

- a task starts today, or on the next working day if today is not
  one, or when the last task it comes after finishes, whichever is
  later
- it takes its latest estimate, in working days
- a task that has started or finished uses its actual dates instead,
  and the tasks after it move with them. A task that is not finished
  cannot finish before today, so a task that has run past its estimate
  finishes today at the earliest
- the critical path is the chain of tasks that sets the last finish

A task with no estimate has no dates, and neither has any task that
comes after it. These tasks are listed apart, in full ink, as "not
estimated", because they are what the owner can fix now (V3, V8).

**The display** is a table of dates with the critical path marked,
and the chain behind the task under the cursor drawn beside it. The
first question asks for exact values, and cognitive fit sends exact
values to a table (Vessey 1991, visual 2.4). The second is a question
about a path, which V17 sends to a small node-link drawing. A Gantt
chart has no evidence for it and some against (workflow 6), and
PlanningLines (Aigner 2005) show slack, which the owner has not asked
about; both wait for a trial to ask for them. The research is thin
here (workflow, "Where the research is thin"), so this is the choice
the rules give, not a proven one.

On design3's running example, with today Monday 26 October 2026:

    SCHEDULE                              finishes Wed Nov 11 · critical: A B D E
    ▌ A  design            3 wd   Mon Oct 26 – Wed Oct 28
    ▌ B  build back end    5 wd   Thu Oct 29 – Wed Nov  4   after A
      C  build front end   4 wd   Thu Oct 29 – Tue Nov  3   after A
    ▌ D  integrate         2 wd   Thu Nov  5 – Fri Nov  6   after B, C
    ▌ E  test              3 wd   Mon Nov  9 – Wed Nov 11   after D

    D comes after:   A ─▶ B ─▶ D
                     A ─▶ C ─┘

- One row per task, in the order of the dependency graph, with ties
  in the board's order. Rows move only when the owner changes what
  comes after what (V6); a sort by date is not offered in version 0.
- The critical path is marked by one feature, a bar in the margin
  (V3), and named in the header, so that the mark is not the only
  carrier (V4).
- Each row shows the task, its latest estimate in working days, its
  start and finish as weekday and date, and the tasks it comes after.
  A task that has started shows its actual start; one that has
  finished is not on the schedule.
- The chain under the table is the tasks the cursor's task comes
  after, back to the first, as a small layered drawing with as few
  crossings as possible (Sugiyama 1981, visual 5.2). It is the answer
  to "what is this waiting on?".
- Tasks with no dates are listed under the table, in full ink, as
  "not estimated", with the task that stops each.
- The query hides rows that do not match; the schedule is always
  computed from every task, and the critical path is marked on what
  is shown.

**The acts** are the board's, on a cursor over the tasks: `t`, `a`,
`x`, `s`, `f` and undo. The cursor moves down the rows and on into
the tasks not estimated, because those are what the owner can fix now
(V3, V8). A mark shows as it does on a card, and the estimate field
opens under the row, with the row's estimate hidden while it is open.
The board's other keys do nothing here. The schedule changes with each act, in place
(V12), and the tasks whose dates moved are marked (V7). The schedule
is never reordered by its dates unless the owner asks (V6).
