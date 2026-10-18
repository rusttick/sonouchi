# sonouchi: UX research

What published research says about interfaces for one person who keeps a large, constantly reframed backlog of research tasks,
mirrors a whiteboard after stand-up, and needs to tell coworkers when particular tasks will be done.
It ends with the design ideas that follow, which are proposals and not yet decided.

Sources are cited by author and year and listed at the end, each with a status word as in `../soonish/doc/design3.md`.

## The workflow being designed for

- One user enters every task and every estimate, and is the only consumer of the result.
- Today, in GitHub Projects:
  - a nested, collapsible, searchable list of issues and sub-issues, with iteration and priority columns, ordered by hand
  - a four-column board for this iteration: sprint backlog, ready, in progress, done
  - the same board for the next iteration
- All three are updated after stand-up from the whiteboard, which covers only this sprint.
- The only link is parent.
- No estimates beyond "can this be done this iteration?"
- About one task in ten survives unchanged from backlog to done.
  New learning replaces a task, usually with several smaller ones in a new framing.
  Breaking hard problems into pieces that fit in a day is the core skill of the job.

So the interface must handle three things that most planning tools handle badly:

1. structure that is constantly replaced
2. several views of one set of tasks
3. a few dates that other people depend on

---

## 1. How people actually manage their own tasks

**Bellotti 2004** (read). Seven busy professionals were followed for a month, and 287 to-dos were tracked week by week.
Earlier, a snapshot of 8 people counted about 74 active to-dos each, spread over about 11 places.

- **People prioritise well.** Only 3% of tasks were dropped without a good reason.
  The authors conclude that the hard part is not choosing what matters but
  `the effort that must go into making sure that the important tasks get done`.
- **What predicts completion.** These were the strongest correlates of completion within a week:
  - high urgency: 93% done, against 44% when urgency was low
  - high importance: 81% against 42%
  - "someone expecting" the task: 77% against 53%
  - tasks involving other people were also done more often
  - having a deadline was not significant
- **Task vistas.** Some people kept one comprehensive list of everything, deliberately on one page.
  One said: "I'll shrink the font and change the margins to keep everything all on one page."
- **Lists are replaced, not updated.** One manager running about 10 projects
  "simply replaced [his list] with a new list with different structure when needed".
  This is the same behaviour as replacing post-its after a reframing.
- **Low-effort entry.** Most to-dos were short cues, not descriptions.
  The authors' requirements include:
  - no formal description, category or decomposition up front
  - any level of abstraction allowed
  - a way to let stale to-dos fade out of view without deleting them
- **Estimates were unreliable.** Recalled durations matched predicted ones in only 25% of cases.
  The authors conclude that `a task tool "should not be relied upon for precise time planning"`.
  That is the problem design 3 sets out to solve with data rather than with better guesses.
- **Their prototype, TaskVista:**
  - dragging a to-do up or down set its importance, and the order was kept
  - a task could become a project, containing other tasks, at any time
  - a pop-out Gantt view was the least liked feature: 3.0 on a 1–5 scale, against 4.67 for drag-and-drop creation
  - users "disliked the idea of explicitly adding a lot of metadata such as participants, planned start and end times, or tasks dependencies"

**Haraty 2016** (read in part). Interviews with 19 people and a survey of 178 sorted people into three types:

- *adopters*, who use a dedicated task tool
- *do-it-yourselfers*, who build their own system from general tools and personalise it
- *make-doers*, who use general tools without personalising them

Most people showed all three tendencies, in different strengths.
Do-it-yourselfers often split lists into columns by category and put high-priority items at the top.
The most common complaints about dedicated tools were a missing function (16 of 75) and not being able to change the tool (15 of 75).
The paper quotes the folk wisdom that all such tools have high abandonment rates.
Building sonouchi is do-it-yourself at its fullest, so the owner's own habits are the right specification.

**González 2004** (checked). Observation of analysts, developers and managers found that work is highly fragmented:
about three minutes on an event before switching.
People organise work into thematically connected "working spheres". Scattered research tasks fit this.

## 2. Structure that keeps changing

**Shipman 1999, "Formality considered harmful"** (checked). Many
failures of computer systems come from asking people to make structure,
content or procedure explicit before they are able or willing to.
The remedy is *incremental formalisation*: enter things informally,
and add structure later, when it is useful (Shipman 1994).

**Spatial hypertext** (checked). In VIKI (Marshall 1994), people
arranged items on a surface, and the system parsed the arrangement
to find the structure people had implied, such as lists, stacks and
piles. Structure was allowed to emerge, and to change as the
organisation changed. A whiteboard of post-its is a spatial hypertext.

**Cognitive dimensions of notations** (Green 1996, checked). This is a
standard vocabulary for judging any notation or interface. Two of its
dimensions matter most here:

- **Viscosity:** the effort needed to make one change. Replacing one
  task with five must take a few seconds, or it will not be recorded.
- **Premature commitment:** having to decide something before the
  information is available. Asking for an estimate, a parent or a
  dependency before the task is understood is premature commitment.

The framework is meant for quick analysis by the designer, so the owner
can apply it to each version of sonouchi.

## 3. Breaking work down

- **Cheng 2015** (checked). The same work done as many microtasks
  rather than one large task took longer overall. But it produced
  higher quality, felt easier, and held up better under
  interruptions.
- **Kokkalis 2013** (checked). Across four experiments, people
  completed more of their tasks when the tasks came with concrete
  action plans.
- **Masicampo 2011** (checked). An unfinished goal causes intrusive
  thoughts and interferes with unrelated work. Making a specific plan
  for it removed these effects, even before the goal was met. A
  follow-up paper by the same authors (2012) warns that a specific
  plan can also make people closed-minded about better routes.
- **Kruger 2004**, already in design 3: listing a task's parts makes
  its estimate longer and more realistic.

Together these support the owner's day-sized chunks. They also suggest
that the act of splitting a task is itself valuable, and worth making
cheap and recording.

## 4. One record, several views

**Baldonado 2000** (checked) gives eight rules for systems with more
than one view of the same data. The rules for when to use multiple
views:

- **diversity:** use them when the attributes, abstraction levels or
  tasks differ
- **decomposition:** use them to split complex data into manageable
  pieces
- **complementarity:** use them to bring out relations or differences
- **parsimony:** use as few as possible

The rules for how to use them include making the coupling between views
self-evident and keeping state consistent across views. A change in one
view must show in the others at once. Today the owner keeps three
GitHub views in step by hand after stand-up, which breaks this rule.

**Kersten 2006** (checked). Mylyn showed programmers only what was
relevant to the active task. In a field study, 16 industry programmers
using it for their daily work showed a statistically significant gain
in productivity. A view filtered to "what I am doing now" earns its
place.

**Robertson 1998** (checked). In Data Mountain, people placed
documents on a surface by hand, and found them faster than with a
bookmark list. A follow-up found that retrieval was not significantly
slower after several months away. A board's columns and positions
give this kind of spatial memory, and a sorted list does not.

## 5. Boards

**Ahmad 2018** (checked) mapped the research on Kanban in software
engineering. Of 382 studies screened, 23 were primary studies, and
the research is "largely descriptive". A synthesis of 20 studies
(checked, authors not recorded) found the strongest benefits were
work visibility, control of tasks, and flow. No study found measured
comprehension or cognitive load. The evidence for boards is
practitioner experience, not experiment.

## 6. Time views and Gantt charts

The evidence is thin, and it is mostly not in favour of Gantt charts.

- **No controlled comparison found.** In four searches I found no
  experiment comparing Gantt charts with network diagrams for reading
  a project schedule. "Gantt charts need no training, network
  diagrams do" appears widely, but only as an assertion.
- **Luz 2011** (checked). On a static building-project schedule, users
  were as fast and accurate with a *temporal mosaic* as with a Gantt
  chart. This was the setting in which Gantt charts are usually
  strongest. Earlier work by the same group found mosaics better for
  some temporal reasoning tasks, and noted that Gantt charts use space
  poorly when many tasks run in parallel.
- **Aigner 2005, PlanningLines** (checked). A glyph that shows the
  earliest and latest possible start and finish of each task. In a
  controlled experiment, users made fewer errors and worked faster on
  questions about temporal uncertainty than with a PERT chart. The PERT
  chart was still better for reading a single attribute.
- **Bellotti 2004** (read). The Gantt view was the least liked part of
  TaskVista. It "seems somewhat unnatural for those not used to
  looking at Gantt charts".
- **Geraldi 2012** (checked) is a conceptual critique, not an
  experiment. The Gantt chart comes from scientific management and
  handles a project's complicatedness well. But it does not express
  complexity, ambiguity, uncertainty or change, and it is applied
  everywhere with little adaptation.

So a Gantt chart for the boss is not proved to be the best display, and
it is not proved to be the worst. A small comparison on the boss's own
questions would be new evidence, not a repeat of known work.

## 7. Showing uncertain dates

This matters from design 3's step 1 onward, but Step 0's screens should
not make it harder to add later.

- **Kay 2016, "When(ish) is my bus?"** (checked). The *quantile
  dotplot* shows a forecast as, for example, 20 dots, each an equally
  likely outcome. People's probability estimates from it varied about
  1.15 times less than from a density plot.
- **Fernandes 2018** (checked). In an incentivised experiment on
  deciding when to leave for a bus, quantile dotplots of 50 outcomes
  gave decisions worth 97% of the optimum. That was 5 percentage
  points better than showing no uncertainty, with more consistent
  decisions. Cumulative distribution plots did almost as well. Text
  descriptions of uncertainty did worse than both.
- **Hullman 2019** (checked). A survey of 90 visualisation authors and
  interviews with 13 found that most value showing uncertainty, yet
  usually leave it out. This is the habit the boss's single-date
  Gantt chart reflects.

The question people ask about a bus is the one coworkers ask the owner:
"when will it be here?"

## 8. Keeping the record going

- **Li 2010** (checked) describes self-tracking in five stages:
  preparation, collection, integration, reflection and action.
  Barriers at any stage stop the whole process.
- **Epstein 2015** (checked). Surveys of a few hundred trackers and 22
  interviews found that *lapsing* and *resuming* are normal stages of
  tracking, not failures. A tool should make it easy to come back
  after a gap, rather than assume unbroken records.

For design 3, a lapse is a censored or missing record. sonouchi should
make a lapse visible. One way is to list tasks in progress with no
recorded act for a while, which the owner can then confirm or correct
from memory.

## 9. Requests, work, and the parent link

The owner's parent link does four jobs at once:

1. **The request.** A vague ask from someone outside, kept so that
   progress can be reported back to them.
2. **The work.** The children are the actual work, which keeps
   changing as it is done.
3. **The reframing.** When the work turns out to be different from
   what was asked, because the implementor learned more than the
   requestor knew, the parent is renamed, split or merged, and the
   requestor is told.
4. **Dividing time between requestors.** The owner thinks this is not
   needed: there is no mandate for how their time is split, and the
   priority of deliverables matters more.

The owner's hypothesis is that parent was used because it was the only
link offered. With other relations, attributes and tags, they expect
to organise more effectively. The research bears on both halves.

**Requests and promises have been modelled before.** Winograd and
Flores' *conversation for action* (Winograd 1987, checked) treats
coordination as speech acts. One person makes a request. Another
promises, or counter-offers, or declines. Then completion is reported,
and the requestor declares it satisfactory. Their tool, The
Coordinator, made these acts explicit in email. Suchman 1993
(checked) argued that forcing every message into such categories
imposes discipline and control on the people using it. Winograd's
reply (Winograd 1994, checked) was that explicit categories help
where ambiguity cannot be cleared up face to face. For sonouchi, this
suggests:

- record requests and promises as facts
- don't force every exchange through a protocol

Design 3 already separates estimate, target and commitment (McConnell
2006). A promise to a requestor is a commitment, and a task's estimate
is an estimate.

**One task can serve several requests.** Bellotti 2004 found *value
extension*: people choose tasks that serve several goals at once. A
parent link allows only one. A separate "serves" relation, from a task
to the requests it helps answer, allows many.

**The interface shapes how people organise.** Bergman 2006 (checked)
studied 20 people. When the interface allowed it, they kept a
project's documents, email and web links together. When the interface
separated them, so did they. This supports the owner's hypothesis that
parent was used because it was there.

**But people say they like tags more than they use them.** Bergman
2013 (checked) gave 75 people Gmail labels that could be used as
folders or as tags, and gave 23 people tags in Windows 7. Both studies
found a strong preference for folders, in storing and in finding. When
tags were used, it was usually one tag per item, so in effect as a
folder. Finding things with tags was slower and less successful. In a
separate questionnaire, 168 people said they liked tags and multiple
classification. Behaviour and stated attitude disagreed.

**Hierarchy is how people decompose problems.** Jones 2005 (checked)
found that project folders often mirrored a "divide and conquer"
decomposition of the problem into subprojects.

So the research supports splitting the parent link into separate
relations. It warns that a general tagging scheme may go unused,
whatever the user expects. The owner can test this on themselves:
every relation and tag is a recorded fact, so how often each one is
used can be counted.

**Decided 2026-10-18: no tree-shaped views.**
The preference for folders in Bergman 2013 and Jones 2005 is about storing and finding documents.
It is not evidence that task data is tree-shaped, and the owner's data is not:
- one task can serve several requests (value extension, Bellotti 2004)
- merges give a replacement several predecessors
- finish-to-start links already form a graph, not a tree

A tree view would either drop links or show one task in several places.
So decomposition, if it is recorded at all, is a relation that allows several parents, and is never drawn as a tree.
Scarr 2012 also found that hierarchies slow expert users (`ux-research-visual.md`, section 2.3).

## 10. The estimate unit

**Any unit can be stored.** A duration is a number with a unit, and
can be converted to any other unit for calculation. Design 3 already
says that the unit is stored with every number. The open questions
are about the person, not the storage.

**The unit changes the estimate.** Jørgensen 2015 (read, conference
draft) gave 22 experienced project managers the same project, at
random, to estimate in work-hours or in workdays:

- those estimating in work-hours gave a median of 77 hours
- those estimating in workdays gave a median of 115 hours
  (17.5 days)
- p = 0.06, so this is a small sample and a suggestive result

The author argues that coarser units give higher estimates. He also
suggests, without testing it, that the most accurate unit is the one
most natural for the size of the task: hours for small tasks, weeks or
months for large ones. The journal version is Jørgensen 2016
(checked).

**Precision says something.** Jørgensen also found that readers take a
very precise estimate, such as 975 hours rather than 1000, as a claim
of certainty. Recorded estimates bunch on round numbers, which is
called *heaping*.

**What this means for design 3.** Step 1 learns the bias, μ, from the
data, so a consistent bias does no harm. A unit that changes from task
to task, unrecorded, adds noise and so raises σ. If the unit is
recorded with each estimate, the unit can be tested as a factor, the
way step 2 tests the kind of task. So the person can be allowed to
choose a natural unit, provided the choice is recorded.

**Effort and elapsed time are different quantities.** This matters
more than the unit. Design 3 defines the actual duration as working
days from recorded start to recorded finish, which is elapsed time.
Someone who works on many tasks at once, as the owner does, may spend
three hours of effort on a task over four working days. The two
choices:

- ask "how much work is it?", which people answer naturally, and
  compare it with elapsed time; the ratio then mixes estimating error
  with how much other work intervened, and σ will be large
- ask "how long until it is done?", which is the question coworkers
  ask, and which Buehler 1994 studied

Measuring effort directly needs time tracking, which costs more than
one step. I found no study that compares these choices for interleaved
work.

**Decided: days worked, recorded at the morning stand-up.** Each
morning the owner records which tasks they worked on the day before.
Hours are not recorded. This was chosen for three reasons:

- **It can be recorded.** The owner spends less than half the working
  day at a computer, and coworkers spend much less. A timer or an
  hours log would very likely never be used, which is the failure
  Jørgensen 2004 found in 97 of 100 projects.
- **It is easy to remember.** Which tasks were touched yesterday is
  easy to recall. How many hours each took is not (Roy 2005).
- **It meets less resistance** if coworkers ever use it, because it
  is not a timesheet.

Each task then has three measures:

- elapsed working days, from start to finish
- the number of days it was worked on
- the estimate

So the ratio of estimate to days worked and the ratio of estimate to
elapsed days can both be studied. The wording of the estimate question
is still open (TODO, section 1). Each fact records two times: the day
the work was done, and when it was recorded.

## 11. Manual ordering and dragging

**People do order their tasks by hand, and do it well.**

- Bellotti 2004 found prioritisation competent, with only 3% of
  tasks dropped without reason.
- TaskVista set importance by dragging a to-do up or down the list.
- Haraty 2016's do-it-yourselfers put high-priority items at the
  top.

The owner already does this in GitHub Projects. Keeping a habit that
works is what Rogers 2003 calls *compatibility*.

**Ranking and rating are not the same measurement.** Alwin 1985
(checked) compared rankings and ratings of the same values in a large
survey. The two gave a similar order on average, but measured
different things about each person. A priority field (rating) and a
manual order (ranking) are not interchangeable records.

**Dragging is the slow, error-prone way to move.** MacKenzie 1991
(checked) found that, with a mouse, trackball or stylus, dragging was
slower and had more errors than pointing. By Fitts' law, a longer move
costs more time. So drag is a poor way to move a task a long way in a
long list. Keyboard moves are cheaper over a distance: up, down, to
the top, or below a named task.

**Only the top needs an exact order.** Completion in Bellotti 2004
followed urgency, importance and who was waiting, not position deep
in a list. A full ranking of a long backlog is effort spent on tasks
that may be replaced before anyone looks at their position again.

So the evidence supports ordering by hand, mainly from the keyboard,
with dragging as a convenience for short moves. It supports an exact
order near the top only, and a record of each move.

---

## Design ideas that follow

These are proposals for discussion. Each names the research behind it.

**I1. One record, several views.** The ranked list, the boards and any time view are views of the same tasks.
Each board is the list filtered to one iteration and grouped by state.
Moving a card is the same fact as changing the state in the list.
Why: Baldonado's consistency rule, and it removes the manual three-way sync.

**I2. A flat ranked list.** One list of tasks, ordered by hand, with the order kept as a recorded fact.
It is not nested and has no collapse: the data is not tree-shaped (section 9).
It is driven from the keyboard, with search, and drag for short moves.
Why: Bellotti's task vista and drag-to-prioritise, Haraty's top-is-important,
and the owner's habit of ordering by hand in GitHub Projects.

**I3. Reframing is a first-class act.** "Replace this task with these" is one act.
It records that the old task was superseded, when, and by which new tasks.
The old task leaves the active views but stays in the record.
Why: low viscosity (Green 1996), lists replaced rather than updated (Bellotti 2004),
and facts never overwritten (design 3, rule 3).

**I4. Structure is added when it pays.** A task can be just a title.
A parent, an estimate, a dependency or a kind label is added only when it becomes useful.
The one firm rule is design 3's: an estimate counts only if recorded before the task starts.
Why: incremental formalisation (Shipman 1999), no premature commitment, and Bellotti's users' dislike of entering metadata.

**I5. Dependencies only where a promise depends on them.**
Finish-to-start links are worth entering on the chains that lead to a request someone is waiting on, and hardly anywhere else.
Why: dependency entry was disliked (Bellotti 2004), and the dates people ask about are few.

**I6. Requests are separate from work.** A request records:

- who asked, in their own words, and when
- any promise made
- each report back to them

Tasks *serve* requests, and one task can serve several.
A request's forecast is the finish of the last task serving it:
one date in Step 0,
a dotplot from step 1.
A requests view lists the open ones.
Why: section 9, "someone expecting" as a strong completion correlate (Bellotti 2004), the owner's reason for estimating, and the bus question (Kay 2016).
A request's finish is a join, which is where design 3's gap 1 makes single dates optimistic.

**I7. A stand-up pass.** After stand-up, one fast pass through the iteration.
For each task on the whiteboard, one keystroke records any of the following:

- worked on yesterday
- started
- finished
- waiting, with a reason
- replaced

"Worked on yesterday" is the only record of effort (section 10).
Its cost can be measured in keystrokes and seconds.
Why: one-step recording (design 3, Step 0, "Sociology"), and the board tool path's principle 3.

**I8. Stale tasks fade.** Tasks untouched for a long time drop to the bottom, or out of view, without being deleted.
Why: Bellotti's requirement for "stale to-dos of low importance".

**I9. Lapses are shown, not hidden.** Tasks in progress with no recorded act for some days are listed, for confirmation or correction.
Why: Epstein 2015, and design 3 needs complete outcomes.

**I10. The time view for the boss is an experiment.** Later, compare a Gantt chart with one or two alternatives on the boss's real questions:
a milestone list with ranges, PlanningLines-style bars, or dotplots per milestone.
Why: section 6 found no controlled evidence that Gantt charts are best.

## Questions this raises for soonish

These concern the data, not the screens, so they belong to soonish's design. They are listed here because the workflow raises them.

- **Reframing.** Design 3's Step 0 has no facts for renaming, splitting, merging or replacing tasks.
  If about 90% of tasks are replaced, most estimates never get an outcome.
  Is a replaced task's estimate censored, discarded, or compared with the total of its replacements?
- **Relations.** Step 0 has only finish-to-start links. Section 9 suggests three more:
  - *part of*, for decomposition
  - *serves*, from a task to a request
  - *replaced by*, for reframing

  Plus tags or attributes. Which belong in Step 0, and does a task with parts have an estimate of its own?
- **Requests.** Is a request a task, or a different kind of thing? The hickey guide suggests no entity types, only attributes. So perhaps a request is `anything that has an asker`.
- **Days worked.** A new fact: task, the day it was worked on, and when that was recorded (section 10). Design 3's Step 0 has no such fact.
- **The estimate question.** With both elapsed days and days worked recorded, should the question ask "how much work?" or "how long until it is done?"?
  The wording must be settled before the first estimate is recorded.
- **Unit.** Any unit is storable. Record the unit chosen with each estimate, so that its effect can be tested.
- **Order.** Manual order is a fact with a time. How should it be recorded without overwriting? For example, "A was placed after B",
  or the whole top of the list stored at each change.

## Where the research is thin

- No controlled study found of Gantt charts against network diagrams for schedule comprehension.
- No experimental evidence found on Kanban boards' effect on understanding or memory.
- No user study found of outliners (nested, collapsible lists) for personal planning, although they are a common tool.
- The KJ method (Kawakita) is the Japanese origin of affinity diagrams, and is close to reorganising post-its after a reframing.
  I found descriptions and applications of it, but no evaluation.

In these areas the owner's own use of sonouchi is the research. Keep a dated friction log as each version is used.

## References

Status words: `read` means the cited figures were read in the paper;
`checked` means the bibliographic details and findings were confirmed
from abstracts or publisher pages by web search on 2026-10-18.

- Ahmad 2018. Ahmad, M. O., Dennehy, D., Conboy, K., Oivo, M.
  "Kanban in software engineering: a systematic mapping study."
  Journal of Systems and Software. checked
- Aigner 2005. Aigner, W., Miksch, S., Thurnher, B., Biffl, S.
  "PlanningLines: novel glyphs for representing temporal
  uncertainties and their evaluation." Proceedings of the 9th
  International Conference on Information Visualisation (IV05), IEEE,
  457–463. checked
- Alwin 1985. Alwin, D., Krosnick, J. "The measurement of values in
  surveys: a comparison of ratings and rankings." Public Opinion
  Quarterly 49(4), 535–552. checked
- Baldonado 2000. Wang Baldonado, M. Q., Woodruff, A., Kuchinsky, A.
  "Guidelines for using multiple views in information visualization."
  Proceedings of AVI 2000, 110–119. checked
- Bellotti 2004. Bellotti, V., Dalal, B., Good, N., Flynn, P., Bobrow,
  D., Ducheneaut, N. "What a to-do: studies of task management towards
  the design of a personal task list manager." Proceedings of CHI
  2004, 735–742. doi:10.1145/985692.985785. read
- Bergman 2006. Bergman, O., Beyth-Marom, R., Nachmias, R. "The
  project fragmentation problem in personal information management."
  Proceedings of CHI 2006, 271–274. checked
- Bergman 2013. Bergman, O., Gradovitch, N., Bar-Ilan, J.,
  Beyth-Marom, R. "Folder versus tag preference in personal
  information management." Journal of the American Society for
  Information Science and Technology 64(10), 1995–2012. checked
- Buehler 1994. See `../soonish/doc/design3.md`.
- Cheng 2015. Cheng, J., Teevan, J., Iqbal, S., Bernstein, M. "Break
  it down: a comparison of macro- and microtasks." Proceedings of CHI
  2015, 4061–4064. checked
- Epstein 2015. Epstein, D., Ping, A., Fogarty, J., Munson, S. "A
  lived informatics model of personal informatics." Proceedings of
  UbiComp 2015. doi:10.1145/2750858.2804250. checked
- Fernandes 2018. Fernandes, M., Walls, L., Munson, S., Hullman, J.,
  Kay, M. "Uncertainty displays using quantile dotplots or CDFs
  improve transit decision-making." Proceedings of CHI 2018. checked
- Geraldi 2012. Geraldi, J., Lechter, T. "Gantt charts revisited: a
  critical analysis of its roots and implications to the management
  of projects today." International Journal of Managing Projects in
  Business 5(4), 578–594. checked
- González 2004. González, V., Mark, G. "Constant, constant,
  multi-tasking craziness: managing multiple working spheres."
  Proceedings of CHI 2004, 113–120. doi:10.1145/985692.985707.
  checked
- Green 1996. Green, T. R. G., Petre, M. "Usability analysis of visual
  programming environments: a 'cognitive dimensions' framework."
  Journal of Visual Languages and Computing 7(2), 131–174. The
  dimensions were checked from secondary sources; the title and
  volume are unchecked
- Haraty 2016. Haraty, M., McGrenere, J., Tang, C. "How personal task
  management differs across individuals." International Journal of
  Human-Computer Studies 88, 13–37. doi:10.1016/j.ijhcs.2015.11.006.
  read in part
- Hullman 2019. Hullman, J. "Why authors don't visualize uncertainty."
  IEEE Transactions on Visualization and Computer Graphics 26 (2020),
  InfoVis 2019. checked
- Jones 2005. Jones, W., Phuwanartnurak, A. J., Gill, R., Bruce, H.
  "Don't take my folders away! Organizing personal information to get
  things done." CHI 2005 extended abstracts. The finding was checked;
  the author list is unchecked
- Jørgensen 2004. See `../soonish/doc/design3.md`.
- Jørgensen 2015. Jørgensen, M. "The effect of the time unit on
  software development effort estimates." SKIMA 2015. Figures read in
  an anonymised conference draft. read
- Jørgensen 2016. Jørgensen, M. "Unit effects in software project
  effort estimation: work-hours gives lower effort estimates than
  workdays." Journal of Systems and Software. checked
- Kay 2016. Kay, M., Kola, T., Hullman, J., Munson, S. "When(ish) is my
  bus? User-centered visualizations of uncertainty in everyday, mobile
  predictive systems." Proceedings of CHI 2016. checked
- Kersten 2006. Kersten, M., Murphy, G. "Using task context to improve
  programmer productivity." Proceedings of ACM SIGSOFT FSE-14.
  checked
- Kokkalis 2013. Kokkalis, N., Köhn, T., Huebner, J., Lee, M.,
  Schulze, F., Klemmer, S. "TaskGenies: automatically providing action
  plans helps people complete tasks." ACM TOCHI 20(5), article 27.
  checked
- Kruger 2004. See `../soonish/doc/design3.md`.
- Li 2010. Li, I., Dey, A., Forlizzi, J. "A stage-based model of
  personal informatics systems." Proceedings of CHI 2010. The five
  stages were checked from secondary sources
- Luz 2011. Luz, S., Masoodian, M. "Comparing static Gantt and mosaic
  charts for visualization of task schedules." 15th International
  Conference on Information Visualisation, IEEE, 182–187.
  doi:10.1109/IV.2011.53. Participant numbers not seen. checked
- MacKenzie 1991. MacKenzie, I. S., Sellen, A., Buxton, W. "A
  comparison of input devices in elemental pointing and dragging
  tasks." Proceedings of CHI 1991, 161–166.
  doi:10.1145/108844.108868. checked
- Marshall 1994. Marshall, C., Shipman, F., Coombs, J. "VIKI: spatial
  hypertext supporting emergent structure." Proceedings of ECHT 1994.
  checked
- Masicampo 2011. Masicampo, E. J., Baumeister, R. "Consider it
  planned: plan making can eliminate the cognitive effects of
  unfulfilled goals." Journal of Personality and Social Psychology
  101(4), 667–683. doi:10.1037/a0024192. The 2012 follow-up is
  "Committed but closed-minded", Social Cognition 30, 37–55. checked
- McConnell 2006. See `../soonish/doc/design3.md`.
- Robertson 1998. Robertson, G., Czerwinski, M., Larson, K., Robbins,
  D., Thiel, D., van Dantzich, M. "Data Mountain: using spatial memory
  for document management." Proceedings of UIST 1998, 153–162. The
  follow-up is Czerwinski et al., INTERACT 1999. checked
- Rogers 2003. Rogers, E. Diffusion of Innovations, fifth edition.
  Free Press. unchecked
- Roy 2005. See `../soonish/doc/design3.md`.
- Shipman 1994. Shipman, F., McCall, R. "Supporting knowledge-base
  evolution with incremental formalization." Proceedings of CHI 1994.
  checked
- Shipman 1999. Shipman, F., Marshall, C. "Formality considered
  harmful: experiences, emerging themes, and directions on the use of
  formal representations in interactive systems." Computer Supported
  Cooperative Work 8(4), 333–352. checked
- Suchman 1993. Suchman, L. "Do categories have politics? The
  language/action perspective reconsidered." Proceedings of ECSCW 1993, 1–14.
  Her rejoinder is "Speech acts and voices", CSCW 3(1), 85–95.
  checked
- Winograd 1987. Winograd, T. "A language/action perspective on the
  design of cooperative work." Human-Computer Interaction 3(1). The
  Coordinator, and conversation for action. Details unchecked
- Winograd 1994. Winograd, T. "Categories, disciplines, and social
  coordination." Computer Supported Cooperative Work 2(3), 191–197.
  doi:10.1007/BF00749016. checked
