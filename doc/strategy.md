# Strategy: how sonouchi is designed and built

How the design questions get settled, in what order the work is done, and how the documents are
kept true while it happens. This is the method; the content is in the other documents in `doc/`.

## The one rule

**The research decides.** Every design question is answered from [design3.md](design3.md),
[research-workflow.md](research-workflow.md) and [research-visual.md](research-visual.md), and the
grounds are written next to the decision. The owner's preference, and the agent's, count only where
the research is silent, and then the choice is marked as such so a trial can overturn it. This is
already the stated method ([goals.md](goals.md), "How it is made"); this document makes it a
procedure that can be followed and checked.

## What each document is for

| Document           | Holds                                              | Changes when                                   |
|--------------------|----------------------------------------------------|------------------------------------------------|
| `design3.md`       | the theory: the model, its steps, their data needs | a step's data needs change, or a gap is found  |
| `goals.md`         | what sonouchi is, for whom, what each version does | scope changes, or a trial changes what matters |
| `ux-design.md`     | the rules V1 to V17 and the task principles        | a rule is added, or research overturns one     |
| `ux-journey.md`    | the screens of version 0: acts, keys, predictions  | a screen is sketched or built differently      |
| `architecture*.md` | the parts, the data, the acts, the open questions  | code changes, or a question is settled         |
| `research-*.md`    | findings only, with sources and status words       | new research is read                           |
| `todo.md`          | the next tasks, in order, no descriptions          | a task is finished, added or reordered         |
| `strategy.md`      | this method                                        | the method changes                             |

Rules for keeping them true:

- A finding goes in a research document. A decision goes in the design document that owns it, citing
  the finding. A research document never records a decision, and a decision never carries a finding
  that is recorded nowhere else.
- Every decision says what it rests on, in one of three forms: a citation, "the research is silent;
  chosen because …", or "the trial showed …".
- When a decision is changed, the old one is not quietly rewritten. The new text says what changed
  and why, until the owner has reviewed it; after review the history lives in the commits.
- `todo.md` keeps its own rules: no subsections, no descriptions, no numbering, delete when done.
- Lines are at most 120 characters wide, tables included. The existing documents wrap at about 72
  and are left so unless a passage is edited anyway.

## How a design question is settled

1. **State the question** in one sentence, and which document owns the answer.
2. **Read design3 first.** If design3 has a rule for it, it wins. design3 is the theory the app
   exists to test, so the interface serves its data needs, not the other way round.
3. **Then the two research documents.** A finding there is cited by section, as the rules V1 to V17
   already do.
4. **If both are silent, search.** Published studies first, from venues the research documents
   already cite: CHI, UIST, TVCG, JSS, Psychological Bulletin. Practitioner sources only when no
   study exists, and marked as such. Each source gets a status word, `read`, `checked` or
   `unchecked`, as the research documents define them, and the date of the search. The finding is
   written into the right research document before the decision is written anywhere.
5. **If the search finds nothing,** say so in "Where the research is thin", and choose the option
   that is cheapest to reverse and records what design3 needs either way. Mark it "undecided by
   research", and name the trial measurement that would decide it.
6. **If the answer contradicts an existing decision,** the research wins. The change is listed under
   "Changed decisions" below until the owner has reviewed it.
7. **Write the grounds** next to the decision, and move anything now settled out of the open
   questions.

What counts as evidence, in order: a controlled study on the task at hand; a controlled study on a
near task; a field study; a review; a practitioner report; nothing. A design choice may not rest on
a weaker rung when a stronger one exists.

## The order of work

The order follows design3's rule that each version is usable alone and records what the next needs,
and [ux-design.md](ux-design.md)'s nine steps for each part: question, facts, display, acts, frame,
prediction, sketch, build, trial. Design before code. A sketch is worth making only when it is much
cheaper than the change in the app; a text sketch on the running example usually is, an image file
usually is not. When the two cost about the same, the change is made in the app and judged there.

**Phase 1. Make the documents coherent.** Resolve every contradiction listed below, in the
documents, with grounds. Move settled questions out of "Open questions about the model". Rewrite
`todo.md` to match. No code changes in this phase.

**Phase 2. Settle the three design questions that block version 0:** time in the data, which decides
the attributes; the schedule display, by a text sketch on the running example; and corrections,
which decides whether version 0 needs more than undo. All three were settled on 2026-10-25, in
`architecture.md` and `ux-journey.md`. Then the owner registers the attributes, which only the owner
can do.

**Phase 3. Build version 0,** one component at a time, in dependency order: `days`, `estimates`,
`board`, the rest of `schedule`, then the views and the acts, each with its tests, each through the
check script. The quick-entry page is replaced, not extended, and its dead code deleted. The
keystroke-level predictions in `ux-journey.md` are checked against the built screens.

**Phase 4. Measure and try.** A release build measured against V12 with a few hundred tasks. Then
the owner tries version 0 for a short while and the two of us discuss what happened, to tell apart
what the owner needs to learn from what the app needs to change. What the discussion settles is
written into the design. The real record starts when the owner judges the app useful enough to
replace the whiteboard and the GitHub boards.

**Phase 5. Version 1,** design3's Step 1, once about 50 finished tasks exist (design3, 3.1). Its
display, the quantile dotplot, is already settled by research (workflow 7).

Nothing from "Parked" in `goals.md` is designed or built before Phase 4 is done.

## What the review found

The documents are, on the whole, coherent and well grounded. design3, the research, the rules and
the screens cite each other consistently, and each rule V1 to V17 names its source. The problems are
few, and of three kinds: a decision that drifted from design3, a rule that the screens do not
follow, and a thing the data model records that design3 does not ask for, or the reverse. Each is
listed with the resolution this strategy proposed and what became of it. All of them are now settled
in the documents, and version 0 is built on them.

### Changed decisions

Decisions the research overrides. Reviewed by the owner on 2026-10-25 and applied to the documents
the same day; the searches below sharpened the second one, so the board now shows only whether a
task has an estimate, and the schedule shows the number.

1. **Time in the data: calendar days, not millisecond instants.** `architecture.md` says time is to
   become instants and durations in milliseconds. design3 Step 0 defines the actual duration as
   *working days from the recorded start to the recorded finish, with the unit stored with every
   number*, and adds calendars only at Step 6, *if working time and elapsed time diverge in the
   data*. A stand-up records that a task started "yesterday": a calendar day is the fact, and an
   instant would invent precision that was never observed, which the record then presents as
   certainty (Jørgensen 2015, "precision says something", workflow 10). The time the fact was
   recorded is already `:act/at`, an instant. So: a start, finish, worked-on day or wait is a
   calendar day, as an ISO string; working days and elapsed days are both computed from it, which
   is what Step 6 needs to test its calendar question; and the scheduler's day-number attributes
   become the input shape that `sonouchi.core.board` computes from the facts, not stored facts.
   `sonouchi.core.days` does the calendar arithmetic, as planned.

2. **The estimate on the card.** `ux-design.md` says no other estimate of a task is shown *before or
   after* a fresh one, yet `ux-journey.md` shows each card's latest estimate under its title at all
   times, and the schedule needs it (V8: the screen holds the state). design3's anchoring rule is
   about a number seen *before entering* an estimate, and its clearest evidence is numbers from
   another source (Tversky 1974, Aranda 2005). The two are reconciled by narrowing the principle to
   what the research supports: the replaced task's estimate is never shown in the replacement
   display, and a task's own estimate is hidden while its field is open. The card shows it
   otherwise. Whether seeing one's own previous estimate biases a re-estimate is a research question
   (below); re-estimates after a start do not count for design3 anyway.

3. **The late-start reasons.** design3 Step 0 records three reasons for a late start: waiting for a
   person, waiting for an outside party, and *not in the plan*. The data model keeps two, as wait
   reasons. Step 6's H6 needs the third, because it separates waits from work that was never in the
   network. It is added as a reason, and a wait with that reason is the owner saying "something else
   came first".

4. **The kind label.** design3 Step 0 records an optional kind label, and Step 2 needs it. The data
   model does not have it. It stays out of version 0's screens, by the principle that a task can be
   just a title, but the attribute is registered now, as an optional patch on a task, so that the
   owner can add it when a trial shows that kinds differ. This is accretion, not scope.

5. **The replacement's start.** `ux-journey.md` says a new task recorded as in progress *started on
   the recording day*. When a task that started a week ago is split, that loses the elapsed time
   design3 measures. The new task's start is the old task's start when exactly one old task was in
   progress, and the recording day otherwise; the display says which. This is the sibling of the
   open question on a replaced task's estimate, and the record keeps what both answers need.

### Rules the screens did not follow

- **Facets and saved queries (V15)** are a rule in `ux-design.md` and a parked item in `goals.md`.
  The rule stays, with a note that version 0 implements typed queries only, so the concept and the
  version agree. Done: V15 says so.
- **Manual order** is a task principle, and the board's order is computed from act times. They do
  not conflict, since only an act moves a card (V6), but the principle should say that a hand-set
  order is for the backlog, which is parked, and the board's order is the whiteboard's. Done: the
  principle says so.
- **"Waiting is not a state"** and the board's Waiting column. The column was computed from wait
  entities, so the record was right, but a card in Waiting hid whether the task had started, which
  design3's elapsed time depends on. This strategy proposed keeping the column as a view of waits,
  because the whiteboard has one (visual momentum, Woods 1984). The column was removed instead: a
  waiting task stays in To do or In progress, and its card says on whom it waits and since when. A
  column of waits answers a different question from the board's (whom to chase, not which tasks have
  no record for the day), and it moved cards out and back (V6); the query bar finds the tasks waiting
  on a person. The grounds are in `ux-design.md`, "Waiting is not a state", and a trial can overturn
  it.

### Open questions the research settled

- **The schedule display.** The research already narrows this further than `goals.md` says. "When
  will each task finish?" asks for exact values, which cognitive fit sends to a table (Vessey 1991,
  visual 2.4). "Which tasks set the last date?" is a path question, which V17 sends to a small
  node-link chain. A Gantt chart has no evidence for it and some against (workflow 6), and
  PlanningLines need slack, which is Step 0's backward pass and worth having anyway. Settled on
  2026-10-25: a dated table with the critical path marked, and the chain behind the selected task
  drawn beside it, sketched in text on the running example in `ux-journey.md`, "The plan". A
  Gantt chart and PlanningLines wait until a trial asks for them, since the research is thin here.
- **Corrections.** Undo appends a retraction, and undoing an old act never overwrites newer work
  (`architecture-log.md`). So correcting one old fact is already one act: a new patch for the task,
  with the right day, recorded with `:act/at` now. design3's rule 3 is satisfied, since both facts
  are kept. Version 0 needs no correction screen beyond `s`, `f` and `b` with the recording day
  moved by `[` and `]`, which `ux-journey.md` already has. The question is closed by pointing this
  out, unless the trial shows otherwise.

### Things researched online

The procedure above sent these to a search on 2026-10-25, because the documents were silent or said
they were thin. The findings are in `research-workflow.md`; this is what each one changed.

| Question | Found | What changed |
|---|---|---|
| Does one's own earlier estimate anchor a re-estimate? | yes: Jørgensen 2001, Løhre 2016 | the board hides it |
| Is recall of yesterday's touched tasks reliable? | for one day, yes: Gershuny 2017 | `w` stays; older days flagged |
| Gantt against table or network, reading one schedule | nothing, fifth search | table and chain, by the rules |
| Do split tasks' totals match the parent's estimate? | parts add to more: Forsyth 2008 | Step 1 tries "left out" first |

A search that finds nothing is recorded too, in "Where the research is thin", with the date.

## How the code is built

The working loop is [CONTRIBUTING.md](../CONTRIBUTING.md)'s, and nothing here changes it:

- a pure function in a core namespace, as `.cljc`, with a test that calls it with plain values
- the REPL to check what a function returns before relying on it, and to reproduce a failure with
  the captured value
- the check script before finishing a code change; when it fails, the code is fixed, never the check
- views tested as data, and property tests where a rule holds for every input, as `search` has
- the keystroke-level prediction for each screen checked against the built keys, so that the
  screen's time is known before the trial measures it
- the sizes the guard prints watched: the core grows, the shell stays thin

The owner's part, which the agent stops and asks for: registering attributes in the attribute
registry, any dependency, any change to a guard-rail file, and every commit. The agent uses no git
command. The owner reads every change before it is committed.

## How a trial decides

A trial is short: the owner uses the app for a session or a few, then we discuss it here. The
discussion asks, for each thing that went wrong, whether the owner has something to learn or the
app has something to change, and the research is consulted the same way as for any other design
question. The footer's act timing and the count of undos in the log are the only measurements, and
they are read, not recorded by hand. The prediction in `ux-journey.md` is the standard: a screen
that feels far slower than its prediction is redesigned, not tuned. One user who is also the
designer is biased, so a trial can show that a design is too slow or asks too much, never that it is
best. The questions marked "undecided by research" are the ones a trial is for. Longer trials come
by themselves, once the app is useful enough that the owner's record is kept in it.

## What is not done

- no git commands, and no commits
- no edits to guard-rail files, and no dependencies, without asking
- nothing from "Parked" before Phase 4
- no feature that neither answers the question in `goals.md` nor keeps the record that answers it
- no decision recorded without its grounds
