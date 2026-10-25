# UX design

How sonouchi's interface works in general, and why. The research
behind each rule is in [research-workflow.md](research-workflow.md)
and [research-visual.md](research-visual.md), cited by section. The
screens built from these rules are in [ux-journey.md](ux-journey.md).

## The concept

- **The working set comes from a query.** There are no fixed tabs.
  Queries are typed, built from facets, or saved and named.
- **An action is chosen for the set.** Each action has one question to
  answer, its own display and its own keys. Examples: the stand-up,
  replacing tasks, prioritising, estimating, answering a request,
  reviewing stale tasks.
- **Every view shows the same record.** A change made in one view shows
  in every other at once, because they are all computed from the same
  facts (Baldonado 2000, workflow 4). A board is the tasks grouped by
  state; moving a card is recording the change of state.
- **Familiar displays first.** A less familiar display is used only
  when there is evidence for it for that action.

No study has tested interfaces built this way against fixed views
(visual, "Where the research is thin"), so the owner's use is the
evidence.

## The display rules

Every display follows these rules. Each names the research behind it.

**V1. One question per action.** Each action states the question it
answers, and the display is chosen to fit it: a list or table for exact
values, a graph for patterns. There is one main display per action.
Why: cognitive fit (Vessey 1991, visual 2.4), Cockburn 2008 (visual
2.1).

**V2. The quantity goes on a common axis.** The value an action is
about goes on position along a shared scale, such as rank, date or
count. Area and angle are for when nothing better fits, and colour hue
carries categories, not quantities. Why: Cleveland 1984, Heer 2010
(visual 1.2).

**V3. One thing pops out.** Each display uses a single preattentive
feature, such as intensity, for the items that need attention now.
Why: Healey 2012, Treisman 1980 (visual 1.1).

**V4. Few colours, never alone.** At most about four colour
categories, colourblind-safe, each repeated by text, shape or position.
Why: visual 1.4.

**V5. Familiar forms first.** List, table, columns, a linear timeline,
and a small node-link chain. Groups are shown with space and enclosing
regions, as a board's columns do, rather than with thin lines or colour
alone. Anything else needs evidence for the specific action. Why:
Brehmer 2017 (visual 5.3), Bellotti 2004 on the Gantt view (workflow
6), common region (Palmer 1992, visual 1.3).

**V6. Stable, deterministic layouts.** Positions change only through
the owner's acts. The system never reorders things on its own. A sort
by a computed value, such as a forecast date, is asked for explicitly,
because rows would jump whenever the value changed. The same query and
action always give the same layout. A query hides what does not match,
and keeps the order of what is left. Why: Robertson 1998 (workflow 4),
Scarr 2012 and Findlater 2004 (visual 2.3), dynamic queries (Ahlberg
1992, visual 4.2).

**V7. Show what changed.** After every act, mark what changed, because
a change elsewhere on the screen often goes unseen. A short staged
transition may show where something moved. Change over a longer time,
such as a forecast that moved, is shown as snapshots side by side, not
animation. Why: change blindness (Simons 2005, visual 1.7), Heer 2007
and Archambault 2011 (visual 1.8).

**V8. The screen holds the state.** Whatever the owner would otherwise
have to remember, such as what has been recorded this morning, is
shown. Why: Cowan 2001 (visual 1.7).

**V9. One frame for every action.** The query, the action's name and
the size of the working set are always in the same places. Changing
action keeps the working set and the selected task in view. A detail
panel, when one is needed, has a fixed place next to the selected item.
No fisheye distortion, and no zooming. Why: visual momentum (Woods
1984, Bennett 2012, visual 2.2), Cockburn 2008 (visual 2.1).

**V10. Actions are visible modes.** The current action is shown large
and in a fixed place. Escape always leaves it. Short-lived acts, such
as previewing a task, use a key that is held and ends on release. The
list of actions is flat and always in the same order. Why: Sellen 1992
(visual 3.4), Hick–Hyman and menu breadth (visual 3.2).

**V11. Keys shown on screen.** Every act has a key, shown next to what
it acts on, so the slow way and the fast way are the same way and using
the screen teaches the keys. The mouse works too, but nothing needs a
drag. Why: the keystroke-level model (Card 1980, visual 3.3), Lane
2005, Cockburn 2014, MacKenzie 1991 (workflow 11).

**V12. Immediate, in place.** Each act updates the display at the
place acted on, within 0.1 s. Nothing waits behind "save" or "submit".
Why: direct manipulation (Hutchins 1985, visual 3.5), response time
(visual 3.6).

**V13. Undo, no confirmations.** Every act can be undone, by appending
a fact that puts back what was before. There are no confirmation
dialogs. Why: Raskin 2000 (visual 3.7), and the record of facts.

**V14. Estimates typed into an empty field.** A number and a unit,
with no slider, no default and no example number. Why: Liu 2019 and
Couper 2006 (visual 3.8), and anchoring (design 3, Step 0).

**V15. Finding without recalling.** One query bar takes typed words
and clicked facets alike. Facets cover state, request, tag and age, and
each value shows its count before it is chosen. Suggestions are
instant, from the owner's own words: task words, tags and request
names. Saved queries are named in the owner's words, show their
counts, and are one key away. Why: people browse far more than they
search (Bergman 2008, Whittaker 2011, visual 4.1), facets and dynamic
queries (Yee 2003, Ahlberg 1992, visual 4.2), suggestions (Hearst 2009,
visual 4.3), information scent (Pirolli 1999, visual 4.4). Version 0
has the typed words only; facets, suggestions and saved queries are
parked until the record is large enough to need them (goals,
"Parked").

**V16. Light theme and a Gothic face** by default, and short task
titles. A dark theme can be optional. Why: Piepenbrock 2013 (visual
1.5), Hasegawa (visual 1.6).

**V17. Chains as small node-link drawings, overlaps as tables.** "What
is this waiting on?" is a question about paths, so it is drawn as the
small chain behind one item, in layers with as few crossings as
possible. "Which tasks serve which requests?" is a question about
overlaps, so it is a table with a mark where a row and a column meet.
A task that appears in two groups looks identical in both. Why: Okoe
2018 and Ghoniem 2004 (visual 5.1), Purchase 1997 and Sugiyama 1981
(visual 5.2), similarity (visual 1.3).

## Principles for the tasks themselves

- **A task can be just a title.** Everything else, such as an estimate,
  a dependency or a kind label, is added when it becomes useful.
  Nothing is asked for before it can be known. Why: incremental
  formalisation (Shipman 1999), premature commitment (Green 1996,
  workflow 2), Bellotti 2004's users disliking metadata entry
  (workflow 1).
- **Changing what the tasks are is one act.** Adding, dropping,
  splitting, merging, reframing and renaming are all the same act:
  replacing N tasks by M. Adding replaces none; dropping replaces with
  none. The old tasks leave the active views and stay in the record,
  linked to their replacements. It must take seconds beyond the typing,
  or it will not be recorded. Why: lists are replaced rather than
  updated (Bellotti 2004, workflow 1), viscosity (Green 1996), splitting
  is valuable in itself (Cheng 2015, Kokkalis 2013, Masicampo 2011,
  Kruger 2004, workflow 3).
- **Correcting the wording is not a replacement.** A typo fixed, or the
  same thing said better, keeps the task, its history and its estimate.
  The test is whether the estimate would change: if it would, the task
  is replaced. Both titles stay in the record.
- **Effort is days worked.** At the morning stand-up the owner records
  which tasks they worked on the day before. Hours are not recorded.
  Which tasks were touched yesterday is easy to recall, and how long
  each took is not; a timer or an hours log would very likely go
  unused, which is how outcome records go missing (Jørgensen 2004, Roy
  2005, workflow 10). Diaries of the day before are accurate for what
  was done, and worst for short touches; their quality falls once the
  gap passes a day (Gershuny 2017, workflow 10). So the day is
  recorded the next morning, catching up further back is allowed but
  shown as such, and a touch too short to remember is lost, which is
  accepted. Each task then has elapsed working days from start to
  finish, the number of days it was worked on, and its estimate.
- **Waiting is not a state.** A task that waits on something outside
  the owner's control has either started or not, and design 3 counts
  elapsed time from the start either way. So a wait is recorded beside
  the state: when it began and ended, the reason, and on whom. The
  reasons are design 3's three: a person, an outside party, and not in
  the plan, which is the owner saying that something else came first.
  Design 3 uses these reasons to measure how much delay came from
  outside the plan (Step 0, "Data recorded"; Step 6's H6). So the
  board has no Waiting column: a waiting task stays in its column, To
  do or In progress, and its card says on whom it waits and since
  when. The board's one question is which tasks have no record for
  the day (V1), and a column of waits answers a different one, whom to
  chase; a column would also hide whether the task has started, and
  would move the card out and back (V6). Whom to chase is answered by
  the query bar, which matches the names in open waits as well as the
  words of titles (V15). No study compares a blocked column with a
  mark in place; the Kanban evidence is descriptive (Ahmad 2018,
  workflow 5), so this is the rules' choice, and a trial can overturn
  it. The trial of 2026-10-25 did not: the owner did not miss the
  column. The whiteboard may keep its blocked column; a post-it there
  maps to a marked card here. A wait on a person is
  usually a request the owner made to them: later, waits and the
  requests made to the owner become one kind of thing, recorded as
  facts without forcing every exchange through a protocol (Winograd
  1987, Suchman 1993, Winograd 1994, workflow 9).
- **Estimates are made fresh, and alone.** An estimate field starts
  empty (V14). The estimate of a task being replaced is never shown in
  the replacement display, before or after: shown after, it invites a
  revision towards it, which is the same anchoring one step later. A
  task's own earlier estimate anchors a re-estimate too, even when
  the estimator is told to ignore it (Jørgensen 2001, Løhre 2016,
  workflow 10), and hiding it at the moment of typing does not undo
  having seen it. So the board does not show the number: a card says
  only whether the task has an estimate, which is what the board
  needs (V3, V8); the trial of 2026-10-25 found the number was not
  missed there. The plan shows the number, because dates are
  what it is for, and a re-estimate made there is anchored; it is
  recorded as a revision, and design 3 scores first estimates and
  revisions apart. The comparison that teaches is design 3's scoring
  of estimates against outcomes. The unit is always working days,
  recorded with each estimate, because the unit changes the estimate
  (Jørgensen 2015, workflow 10).
- **Order is set by hand, mainly by key.** Moves are up, down, to the
  top, or below a named task, with no drag needed. Only the top of a
  list needs an exact order. A manual order is a ranking, not a
  priority rating, and the two are not interchangeable. Why: workflow
  11. This is for the ranked backlog, which is parked (goals,
  "Parked"). The board's order is the whiteboard's: a card's place in
  its column follows the owner's acts, and nothing else moves it
  (V6).
- **No trees.** One task can serve several requests, a merge has
  several predecessors, and dependencies form a graph. A tree view
  would either drop links or show one task in several places, and
  hierarchies slow experts down (Scarr 2012, visual 2.3). The folder
  preference in Bergman 2013 and Jones 2005 is about storing documents,
  not about tasks (workflow 9).
- **Dependencies only where a promise depends on them.** Entering them
  was disliked (Bellotti 2004), and the dates people ask about are few.
- **Stale tasks fade, and lapses show.** Tasks untouched for a long
  time drop out of view without being deleted. Tasks in progress with
  no recorded act for some days are listed, for the owner to confirm or
  correct. Lapsing and resuming are normal (Epstein 2015, workflow 8),
  and design 3 needs complete outcomes.
- **Uncertain dates as quantile dotplots,** once the core forecasts
  distributions (Kay 2016, Fernandes 2018, workflow 7).

## How each part is designed

Each part of the interface goes through these steps before it is built.

1. **The question** it answers, and what the owner does with the answer
   (V1).
2. **The facts** it reads, and the facts each act appends. These lists
   feed [architecture.md](architecture.md), "Open questions about the model".
3. **The display,** chosen by cognitive fit (V1) and checked against V2
   to V5 and V17.
4. **The acts,** each with its key (V10 to V14).
5. **The frame:** the shared frame (V9), a stable layout (V6), and
   nothing left to memory (V8).
6. **The prediction:** the time for a typical use, by the
   keystroke-level model (visual 3.3).
7. **The sketch,** checked against every rule above. A sketch is text
   on the running example, in the design document. When a sketch
   would cost about as much as the change in the app, the change is
   made in the app and judged there instead.
8. **The build.**
9. **The trial:** the owner uses the app for a session or a few, and
   what went wrong is discussed, to tell what the owner has to learn
   from what the app has to change. The footer's act timing and the
   undos in the log are the measurements. What the discussion settles
   is written into the design, which is then edited to say what
   works.

A trial uses one version at a time. One user who is also the designer
is biased towards their own design and gets faster with practice
whatever the design, so a trial can show that a design is too slow or
asks too much, never that it is the best one.
