# UX design: requests and promises

How sonouchi records what other people ask of the owner, what the
owner promises them, and what others promise the owner; and how the
tasks that serve a request are seen together. Built on 2026-10-25;
where the build differs from the text, "As built" says so.
The rules cited (V1 to V17) are in [ux-design.md](ux-design.md); the
screens of version 0 are in [ux-journey.md](ux-journey.md). Each
decision names its grounds, as [strategy.md](strategy.md) requires;
"the research is silent" marks a choice a trial can overturn.

## Why now

The trial of 2026-10-25 closed version 0 with one finding that was
not about any screen: the owner can record the work but cannot reason
about how the tasks relate to what has been promised, and to whom, so
estimating has nothing to add up to. On the whiteboard and in GitHub
this was done by clustering post-its by the conversation they came
from, and by the parent link, which held the request, the work that
answered it, and the grouping by requestor at once (ux-journey,
"Before sonouchi"). `goals.md` parked requests until version 1; the
trial pulls them forward, because they are the thing the record must
hold for the owner's question, *when will the tasks that I and my
coworkers care about be finished*, to have a subject.

## What the research says

- **A request has a known shape** (Winograd 1987, workflow 9): a
  request, then a promise, counter-offer or decline, then a report of
  completion, then the asker's acceptance. Suchman 1993 showed that
  forcing every exchange through these categories imposes discipline
  on the people using the tool; Winograd 1994 replied that the
  categories help where ambiguity cannot be cleared up face to face.
  The owner's team meets daily at a whiteboard, so it can. So the
  request and the promise are recorded as facts when they happen,
  and the conversation is not modelled: no counter-offers, no
  declines, no states.
- **Estimate, target and commitment are three things** (McConnell
  2006, design3 1.8), and tools spoil estimates by making one number
  serve as all three. So a promise is a fact of its own, with its own
  day, beside the estimate, never derived from it, and never shown
  where an estimate is typed (design3 Step 0, anchoring). design3's
  Gap 7 says which quantile of a forecast to promise once version 1
  has forecasts; until then the owner chooses the day, and the record
  keeps the promise and the outcome so that Gap 7 can be checked.
- **Someone expecting a task gets it done** (Bellotti 2004, workflow
  1): 77% done within the week against 53%, and tasks involving other
  people were done more often. So the card says who is expecting the
  task.
- **One task serves several goals** (Bellotti 2004, "value
  extension"), so a task serves a set of requests, and a request is
  not a parent. This is the no-trees principle (ux-design).
- **Bad news travels late** (the MUM effect, Rosen 1970, design3 1.8),
  so a promise made to the owner is best recorded with its day, and
  the plan shows when that day has passed. For the same reason the
  promise is not treated as a fact about the future: design3 reports
  dates and deadlines as probabilities rather than enforcing them
  (Step 6), and a single date from another person is as optimistic as
  any other single number (Part 2). So a promise is recorded and
  shown, and never enters the scheduler as a constraint.
- **Formality harms when it comes first** (Shipman 1999, workflow 2):
  a task can be entered with no request, and linked later; a request
  can be entered with no tasks and no promise. Bellotti 2004's users
  disliked entering metadata, "participants, planned start and end
  times, or tasks dependencies", which is close to the list here, so
  every field is optional but the asker. This finding is the one the
  design pushes hardest against, on the owner's word that the promises
  must be recorded. The record goes missing when the tool costs more
  than it saves (Jørgensen 2004, design3 Step 0), so the trial watches
  for tasks left unlinked and promises left unrecorded; if that
  happens, the design changes, not the owner.
- **The interface shapes how people organise** (Bergman 2006, workflow
  9): when a tool lets a project's things be kept together, people
  keep them together. The request is where sonouchi lets them.
- **Grouping is shown with enclosing regions, overlaps with tables**
  (V5, V17): a request's tasks are seen together as a list, and
  which tasks serve which requests is a table with a mark at the
  meeting of row and column, not a drawing of overlapping regions.
- **Tags are used as folders** (Bergman 2013, workflow 9), so the
  request is the one grouping, and there are no free tags.

## The facts

Every act appends facts with their time, as in version 0
([architecture-log.md](architecture-log.md)). Three new entities, one
new task attribute.

**A request** is what someone asked of the owner:

| Attribute | Value |
|---|---|
| `:request/asker` | who asked, in the owner's words, such as "Tanaka" |
| `:request/words` | what was asked, in the asker's words, short |
| `:request/asked` | the day it was asked |
| `:request/closed` | the day the owner reported it done, or dropped it; absent while it is open |

**A promise** is an entity of its own, as an estimate is, so that
every promise is kept with the time it was made and the one before it
is never overwritten; a slipped promise is a new promise:

| Attribute | Value |
|---|---|
| `:promise/about` | the request or the wait the promise is about |
| `:promise/day` | the day promised |

A promise about a request is the owner's promise to the asker. A
promise about a wait is the other party's promise to the owner, since
a wait on a person is a request the owner made (ux-design, "Waiting
is not a state"). The latest promise about a thing is the one in
force. Who made it is `:act/by`, as with every fact, and is left out
while there is one user; the direction is given by what the promise
is about.

**A task** gains:

| Attribute | Value |
|---|---|
| `:task/serves` | the set of requests the task serves. Absent when it serves none |

Whether a request is done is computed: it is done when every task
that serves it has finished, and it has no tasks when none serves it.
A request's finish on the plan is the latest finish among its tasks.
Nothing about this is stored.

**The acts** that append them:

| Action | Patches |
|---|---|
| `:action/add-request` | a new request with its asker, words and day |
| `:action/promise` | a new promise about a request or a wait |
| `:action/set-serves` | a task's whole `:task/serves` set, or its retraction, as `:action/set-after` does for `:task/after` |
| `:action/close-request` | the request's `:request/closed` |
| `:action/correct-request` | the request's new words or asker |

Undo appends a retraction, as for every act (V13).

**What this entangles.** A request knows its asker and words; a task
knows which requests it serves; a promise knows what it is about. The
scheduler does not know requests or promises: the plan computes a
request's finish from its tasks' finishes, after the schedule is made,
and a promise is compared with that finish, never fed into it.

## Where it shows

### The board

A card whose task serves an open request says, small, under the
title, who is expecting it and by when: "for Tanaka · by Fri Nov 6",
or "for Tanaka" when nothing is promised, or "for Tanaka, Sato" when
it serves two. The grounds are Bellotti 2004's "someone expecting";
the line is quiet, because the board's one pop-out stays the task
with no record for the day (V3). A card with an open wait that has a
promise says "waiting on Sato since Tue Oct 20, promised Fri Nov 6".

The query bar matches the asker and the words of the requests a task
serves, as it matches whom a wait is on (V15). Typing "Tanaka" lists
the tasks for Tanaka and the tasks waiting on Tanaka. This is how the
post-it cluster is found: by query, not by position, since a task in
two clusters would need two places (no trees). This is undecided by
research, and the other side has evidence: people find things faster
when they placed them by hand and the positions stay fixed (Robertson
1998, workflow 4), and spatial hypertext let structure emerge from
arrangement (Marshall 1994, workflow 2); piles are not trees. The
owner clusters post-its by conversation today. If the trial shows the
query does not replace that, the cheapest change is a board grouped
by request, which the facts here already allow (ux-design, "The
concept": an action is chosen for a query-defined set).

### The requests screen

**The question (V1).** What have I promised, to whom, and when will
each be done?

**The display.** A table of the open requests, one row each, since
the question asks for exact values (Vessey 1991, visual 2.4). The
columns: asker, words, asked, promised, the finish the plan computes
from the request's tasks, how many tasks serve it, and its slack: the
working days its finish can slip before the promise is missed, as
"can slip 2 working days", or past it, "3 working days late". Slack
is the backward pass of design3's Step 0 (design3 1.1), which the
scheduler already computes for tasks, taken from the promised day
instead of the project's finish. It is the request's urgency made a
number, and it is shown on the request, not on its tasks: people who
know a task has slack start it late (Parkinson 1955, Goldratt 1997,
design3 1.5), while a promise with no slack is what the owner has to
weigh. Two findings count against the column, and the trial weighs
them: having a deadline did not predict completion in Bellotti 2004,
where urgency, importance and someone expecting did, so the card's
"for Tanaka" may move the owner more than the number does; and
Goldratt's argument applies to requests too, so the trial watches
whether requests shown with slack are started late. The column rests
on design3's arithmetic and Gap 7, and stays only if the owner is
seen to use it. The rows are in the order the requests were asked, newest
last, and move only when the owner adds or closes one (V6): the table
is a vista the owner scans (Bellotti 2004's "task vistas"), not a
ranking the app made, because people rank well on their own (workflow
11) and the choice of what to do now depends on costs only the owner
knows (design3, Gap 7 and Step 6: the app "does not decide"). A sort
by slack can be asked for. A request whose slack is zero or less is
the one thing that pops out, in full ink with the rest quiet (V3);
the words say so too (V4). A request with no tasks says "no tasks",
in full ink as well, because it is what the owner can fix now, like
a task with no estimate on the plan (V8).

Under the cursor's request, the task on its chain that sets its
finish and could be worked on today is marked in the list below, as
the critical path is marked on the plan: it is the answer to "what
do I do now for this one?", and it is only shown, never ranked
against the other requests' answers.

Under the table, the tasks that serve the request under the cursor,
as a list in the plan's order, each with its state and its finish.
This is the request's cluster of post-its, seen together (Bergman
2006). V17's table of overlaps, tasks against requests, waits for the
trial to ask which tasks serve two requests; until then the list
under the cursor answers "which tasks serve this one?" and the card's
line answers "which requests does this task serve?".

On design3's running example, if Tanaka asked for the integrated
system and Sato for the front end alone, with today Monday 26 October
2026:

    REQUESTS                                     n new · p promise · v serves · c close · e edit
    asker   asked        words                  promised     finishes      tasks
    Tanaka  Mon Oct 19   the integrated system  Fri Nov 6    Wed Nov 11    4   3 working days late
    Sato    Thu Oct 22   the new front end      Fri Nov 6    Tue Nov 3     2   can slip 3 working days

    Tanaka asked for the integrated system
    ▌ In progress  A  design           Wed Oct 28
      To do        B  build back end   Wed Nov 4
      To do        D  integrate        Fri Nov 6
      To do        E  test             Wed Nov 11

Closed requests are not shown; the query finds them, as it finds done
tasks on the board.

**The acts,** on a cursor over the rows, with the keys shown (V11):

| Key | Act |
|---|---|
| `n` | a new request: a small field asks who and what, Enter records it as asked on the recording day |
| `p` | promise: a field on the row asks for the day; Enter records a new promise, Escape leaves the old one in force |
| `v` | the request is served by exactly the marked tasks, and the marks are cleared; with none marked, by none. The cursor can also move into the list under the table, where `v` on a task removes it from the request |
| `c` | close the request on the recording day |
| `e` | correct the words or the asker in place |
| `x` | mark a request, so that `v` on a card can name it (below) |
| ⌘Z | undo |

**As built:** the cursor stays on the rows; it does not move into the
list under the table. A task is taken off a request from the board,
with `v` on its card and the requests it should still serve marked,
or from the row with `v` and the tasks it should still have marked.
The list marks every task that could be worked on today for the
request, which may be more than one when two chains are open. A new
request is typed on one line, "Tanaka: the integrated system"; the
first colon splits the asker from the words.

`v` from the board, on a card, with requests marked: the card's task
serves exactly the marked requests. Marks on tasks and marks on
requests are kept apart, so that `v` on a request reads task marks
and `v` on a card reads request marks. This mirrors `a` and comes
after (ux-journey, "Comes after"): the same gesture for the same kind
of act. A request is also a line in the replacement display: the new
tasks serve what the old tasks served, as they come after what the
old tasks came after, by the same reasoning (ux-journey, Stage 4).

**The promised day** is typed as a day, as "nov 6" or "11-06", and
the field echoes what it understood, "Fri Nov 6", before Enter (V8).
The research is silent on entering a date by keyboard; chosen because
stepping with `[` and `]` from today is too slow for a day weeks away,
and a date picker is pointing (visual 3.1). The day is kept as an ISO
day like every other day.

**The prediction.** Adding a request and linking four marked tasks:
`n`, who and what typed, about 30 characters, Enter, then `x` four
times on the board with cursor moves, `4`, `v`: about 20 s, nearly all
typing. A promise: `p`, six characters, Enter, about 4 s. Checking
the screen is one glance, which is the point: the question is
answered by the table without any act (V8).

### The plan

No promise reaches the scheduler. A promise about a request is a
target, and the plan's finish is the estimate, kept apart (McConnell
2006); the requests table shows both side by side, which is the
comparison that teaches. A promise about a wait is another person's
single-number estimate, and design3 treats such dates as things to
report against, not to enforce (Step 6: deadlines "reported as
probabilities of being met rather than enforced"); fed into the
forward pass it would present Sato's Friday as certain, and Sato will
be late and say so late (MUM effect). So the plan's row for a waiting
task says "waiting on Sato, promised Fri Nov 6", and after that day
"promised Fri Nov 6, still waiting", and the task is scheduled as
every waiting task is now. Version 1 can treat the promise as an
estimate with a ratio of its own, from the record of how the other
party's promises turned out, which is why each one is kept. An
earlier draft of this document made the promise a constraint,
`:task/not-before-day`; it was withdrawn on 2026-10-25 on the grounds
above, before anything was built.

The plan's graph draws no regions for requests. Tasks serving two
requests would need overlapping regions, which V17 sends to a table
instead, and the research is silent on region drawings of a graph
this small. The list under the requests table does the job; if the
trial asks to see a request's tasks in the graph, the cheapest change
is to draw the cursor's request alone as one region.

## What design3 gets

- every promise with the time it was made, beside the estimate of the
  tasks it rests on, so that Step 1 can score promises as it scores
  estimates, and Gap 7's quantile can be chosen on evidence
- the history of slipped promises, both the owner's and other
  parties', as a quantity of its own: how far promised days move, and
  whose
- for each wait, the day the other party promised beside the day the
  wait ended, which is Step 6's H6 with a number: how late outside
  parties are against their own word
- requests as the unit the forecast is asked about in version 1: the
  question "when will Tanaka's request be done, and how sure?" is the
  question in `goals.md`, and the forecast's P50 and P80 are shown on
  the request's row

## What the research says about choosing what to do now

Recorded here because the owner asked, on 2026-10-25, and it bounds
what this screen may do. The owner's day is weighing the promises
made and choosing which to work on. The research says the weighing
is done well without a tool: people prioritise competently, dropping
only 3% of tasks without reason, and what predicts completion is
urgency, importance and someone expecting the task, not a deadline
(Bellotti 2004, workflow 1). Recording the promises is worth more
than ranking them: an unfinished goal intrudes on other work until a
specific plan for it exists (Masicampo 2011, workflow 3), and the
hard part is the effort of making sure the important things get done,
not choosing them (Bellotti 2004). Daily reframing is healthy, since
a fixed plan closes the mind to better routes (Masicampo 2012), and
structure is best added as it becomes useful (Shipman 1999, workflow
2). So the screen computes urgency as slack and shows who is
expecting what, and leaves the choice to the owner, as design3 does
for every decision with a cost only the people involved know (Gap 7,
Step 6). Version 1 replaces slack with the probability of meeting
each promise, which is the same question with honest uncertainty.

No study in the research compares ways of choosing among competing
commitments day by day. Before the screen recommends a task across
requests, the strategy's rule 4 sends this to a search: the
literature on implementation intentions, which Masicampo builds on,
and on interruption and working spheres (González 2004, workflow 1).

## Undecided by research

- **Whether a wait is a request.** The design says they become one
  thing. They are kept as two here because a wait is attached to a
  task and a request to its asker, and merging them is a data change
  that nothing yet asks for. The trial decides: if the owner records a
  wait and a request for the same exchange, they merge.
- **The table of overlaps** (V17): waits for a trial to ask which
  tasks serve two requests.
- **A day typed by keyboard:** the format is chosen, not researched.
- **Clusters by query, not by position:** see "The board"; Robertson
  1998 and Marshall 1994 are the evidence for position.
- **The slack column:** see "The requests screen"; Bellotti 2004's
  deadline finding and Goldratt 1997 are the evidence against.
- **Closing a request** by one act, with no acceptance step: Winograd
  1987 has the asker accept; Suchman 1993 argues against requiring
  it. Chosen for the owner, who hears acceptance at the stand-up.

## What the owner registers

The attributes above, in `src/sonouchi/core/attrs.cljc`: four on
requests, two on promises, `:task/serves` and the five actions. The
scheduler's input does not change. And the screen
`:requests` in `:state/screen`, with `:command/show-requests` on `4`,
and the commands the fields need, on the pattern of the estimate
field.
