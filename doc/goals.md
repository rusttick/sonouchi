# Goals

## What sonouchi is

sonouchi is a project estimation app. [design3.md](design3.md) is the
theory: forecasts of when work will finish, learned from a record of
estimates and outcomes, built up in steps. design3 leaves one thing
out on purpose: display and interaction. sonouchi supplies both, as
well as the model.

So sonouchi is where the owner enters tasks, estimates and
dependencies, records what actually happened, and sees the schedule
and the forecast. The model and the interface are one project: the
scheduler, from what was soonish, is in the same pure core as the
views, and runs in the browser.

## The question it answers

**When will the tasks that I and my coworkers care about be finished,
and how sure is that?**

Everything sonouchi does either answers this question or keeps the
record that answers it. A feature that does neither waits until one
of them needs it.

## Who it is for

One person, the owner, who enters every task and every estimate and is
the only one who reads the results. Coworkers are told dates; they do
not use the tool. Other users come later, if at all.

## The work it serves

The owner does research. The work is scattered across many tasks, and
it keeps changing shape: about one task in ten survives unchanged from
backlog to done. New learning replaces a task, usually with several
smaller ones in a new framing.

The team holds a stand-up each morning at a whiteboard of post-its.
Much of the work happens away from any computer. sonouchi is used in
short sessions, chiefly just after the stand-up, and is not left open
while working.

## What it must do, in order

sonouchi follows design3's development path. As design3 requires,
each version is usable on its own, and records what the next one
needs.

### Version 0: design3 Step 0, with a face

1. **Enter a task with its estimate and what it waits for.** The
   estimate is asked before the task starts, in design3's words: *the
   number of working days you think is equally likely to be too high
   or too low.* It is typed into an empty field, and no other estimate
   is shown first. A task without an estimate may be entered, but it
   cannot be forecast, and design3 cannot learn from it.
2. **See the plan:** what comes after what, each task's dates and the
   critical path, computed by the critical path method from the
   estimates. In design3's running example, A 0–3, B 3–8, C 3–7,
   D 8–10, E 10–13, with A, B, D, E critical. It is shown as a table
   of dates with the critical path marked, and the whole dependency
   graph drawn under it as a node-link diagram, by cognitive fit and
   V17 ([ux-journey.md](ux-journey.md), "The plan"). A Gantt chart
   and PlanningLines (Aigner 2005) wait until a trial asks for them.
3. **Record what happened, one act at a time:** a start, a finish, the
   days worked, and waits with their reason. The stand-up board in
   [ux-journey.md](ux-journey.md) is how this is done. Once a task has
   an actual date, it replaces the computed one, and the schedule is
   computed again.
4. **Replace tasks** when the work changes shape: N old tasks by M new
   ones, in one act. Each new task is asked for a fresh estimate.
5. **Check for duplicates:** a query bar that filters the tasks by the
   words of their titles, so that a task is not entered twice.

### Requests, before version 1

The trial of 2026-10-25 found that the owner could record the work but
not reason about what it was for or what had been promised, so
requests come off the parked list. The design is
[ux-requests.md](ux-requests.md): who asked, in what words, what the
owner promised, what others promised the owner, and which tasks serve
each request, with a screen that says when each request will be done
against what was promised.

### Version 1: design3 Step 1

6. **See the forecast:** the finish of the project, and of any task,
   as a distribution, from the team's history. It is shown as a
   quantile dotplot with the P50 and P80 marked (Kay 2016, Fernandes
   2018), and the probability of finishing by a given date. The owner's
   own estimate is shown beside the forecast, and is never silently
   replaced (design3, Step 1).
7. **Keep every forecast shown,** so that it can be scored against the
   outcome (design3, rule 4).

Later versions follow design3's later steps, when the data justifies
them.

## What it records, and why

design3 learns from estimates made before work started, and from how
long the work then took. So sonouchi's main product is a clean record.
Every act is kept as a fact with its time, never edited in place
(design3, rule 3). Undo appends a fact too.

For each task, the record holds:

- its title, and each correction of the wording
- its estimate, always in working days, with the unit stored with the
  number, and the time it was made. The unit is fixed because the unit
  changes the estimate (Jørgensen 2015), and design3 can compare only
  estimates asked the same way. An estimate counts only if it was made
  before the task started; later ones are kept as revisions
- the tasks it waits for
- when it started and finished, as days
- which days the owner worked on it, recorded at the stand-up. Effort
  is counted in days worked, never in hours
- when it waited, on whom, and why: a person, an outside party, or
  not in the plan (design3's three reasons, used in its Step 6)
- when it was replaced, and by which tasks
- room for a kind label, which design3's Step 2 needs. No screen sets
  it in version 0

There is no seed data; the owner enters every task by hand.

## Open questions for design3

- **A replaced task's estimate.** It never gets an outcome. Is it
  censored, left out, or compared with the total of its replacements?
  The record keeps what all three need, and Step 1 chooses by
  out-of-time score (design3, 3.1), trying "left out" first, since
  estimates of parts are known to add up to more than the estimate of
  the whole (Forsyth 2008, workflow 3).

The rest are in [architecture.md](architecture.md), "Open questions
about the model".

## How it is made

- **The research decides, not the owner's preference.** The owner,
  like most people, does not estimate well or meet deadlines easily,
  and sonouchi is there to help with that. So design questions are
  settled from the research and design3, and the grounds are written
  down. Where the research is silent, sonouchi records what design3
  needs to decide later by out-of-time score.
- **In the browser alone, first.** The record is held in the browser,
  with no server, until the interface shows what a server must
  provide.
- **Tried by the owner on their own work,** as soon as version 0 can
  be used: a session or a few, then a discussion of what went wrong
  and whether the owner or the app has to change. What the trials
  show is written back into the design. The record proper begins when
  the app is useful enough to replace the whiteboard and the GitHub
  boards.
- **One set of rules.** Clojure and ClojureScript, values and pure
  functions, thin shells around one pure core, and facts appended
  rather than overwritten ([CONTRIBUTING.md](../CONTRIBUTING.md)).

## Parked

These wait until version 1 is in use, and then only if the question
above needs them:

- the ranked backlog, and how a manual order is recorded
- retrospective views of how the work evolved through replacements,
  which the owner asked for in the trial of 2026-10-25; the record
  already keeps `:task/replaced-by` for it
- requests' forecasts, which are version 1's; the requests themselves
  are designed in [ux-requests.md](ux-requests.md)
- facets, suggestions and saved queries
- stale tasks fading, and lapses showing
- a time view for the owner's boss
- predictions by the keystroke-level model, and the performance log:
  each act cross-checked with the Event Timing API, and a recorded
  morning replayed on the Pi and an old MacBook
- Japanese text, beyond what works at no cost
- a settings screen, until the first setting is needed
- a drawing library, D3 or Vega-Lite, if the schedule needs more than
  plain SVG from hiccup
- a browser test runner for the shell, if the shell grows beyond what
  the REPL and the trials catch
- whether the GitHub Projects board is still needed, once the session
  after the stand-up works

The research behind them stays in
[research-workflow.md](research-workflow.md) and
[research-visual.md](research-visual.md).

## What it is not

- not a work companion left open on the desktop
- not a timesheet
- not a tree: a task can wait for several tasks, a merge has several
  predecessors, and dependencies form a graph, so there is no nested,
  collapsible outline
- not for several people editing at once: one user, on a laptop
- not responsible for signing in. If it ever needs sign-in, a proxy in
  front handles it
