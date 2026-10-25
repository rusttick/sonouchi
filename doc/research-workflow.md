# Research: managing tasks, views and plans

What published research says about how people manage their own tasks,
how they restructure them, how several views of the same tasks work,
and how plans, uncertain dates, effort and order are recorded and
shown. These are findings only. What sonouchi makes of them is in
[ux-design.md](ux-design.md).

Sources are cited by author and year, and listed at the end with a
status word as in `doc/design3.md`.

## 1. How people actually manage their own tasks

**Bellotti 2004** (read). Seven busy professionals were followed for a
month, and 287 to-dos were tracked week by week. Earlier, a snapshot of
8 people counted about 74 active to-dos each, spread over about 11
places.

- **People prioritise well.** Only 3% of tasks were dropped without a
  good reason. The authors conclude that the hard part is not choosing
  what matters but `the effort that must go into making sure that the
  important tasks get done`.
- **What predicts completion.** These were the strongest correlates of
  completion within a week:
  - high urgency: 93% done, against 44% when urgency was low
  - high importance: 81% against 42%
  - "someone expecting" the task: 77% against 53%
  - tasks involving other people were also done more often
  - having a deadline was not significant
- **Task vistas.** Some people kept one comprehensive list of
  everything, deliberately on one page. One said: "I'll shrink the font
  and change the margins to keep everything all on one page."
- **Lists are replaced, not updated.** One manager running about 10
  projects "simply replaced [his list] with a new list with different
  structure when needed".
- **Low-effort entry.** Most to-dos were short cues, not descriptions.
  The authors' requirements include:
  - no formal description, category or decomposition up front
  - any level of abstraction allowed
  - a way to let stale to-dos fade out of view without deleting them
- **Estimates were unreliable.** Recalled durations matched predicted
  ones in only 25% of cases. The authors conclude that `a task tool
  "should not be relied upon for precise time planning"`.
- **Their prototype, TaskVista:**
  - dragging a to-do up or down set its importance, and the order was
    kept
  - a task could become a project, containing other tasks, at any time
  - a pop-out Gantt view was the least liked feature: 3.0 on a 1–5
    scale, against 4.67 for drag-and-drop creation
  - users "disliked the idea of explicitly adding a lot of metadata
    such as participants, planned start and end times, or tasks
    dependencies"

**Haraty 2016** (read in part). Interviews with 19 people and a survey
of 178 sorted people into three types:

- *adopters*, who use a dedicated task tool
- *do-it-yourselfers*, who build their own system from general tools
  and personalise it
- *make-doers*, who use general tools without personalising them

Most people showed all three tendencies, in different strengths.
Do-it-yourselfers often split lists into columns by category and put
high-priority items at the top. The most common complaints about
dedicated tools were a missing function (16 of 75) and not being able
to change the tool (15 of 75). The paper quotes the folk wisdom that
all such tools have high abandonment rates.

**González 2004** (checked). Observation of analysts, developers and
managers found that work is highly fragmented: about three minutes on
an event before switching. People organise work into thematically
connected "working spheres".

## 2. Structure that keeps changing

**Shipman 1999, "Formality considered harmful"** (checked). Many
failures of computer systems come from asking people to make structure,
content or procedure explicit before they are able or willing to. The
remedy is *incremental formalisation*: enter things informally, and add
structure later, when it is useful (Shipman 1994).

**Spatial hypertext** (checked). In VIKI (Marshall 1994), people
arranged items on a surface, and the system parsed the arrangement to
find the structure people had implied, such as lists, stacks and piles.
Structure was allowed to emerge, and to change as the organisation
changed.

**Cognitive dimensions of notations** (Green 1996, checked). This is a
standard vocabulary for judging any notation or interface, meant for
quick analysis by a designer. Two of its dimensions:

- **Viscosity:** the effort needed to make one change.
- **Premature commitment:** having to decide something before the
  information is available.

## 3. Breaking work down

- **Cheng 2015** (checked). The same work done as many microtasks
  rather than one large task took longer overall. But it produced
  higher quality, felt easier, and held up better under interruptions.
- **Kokkalis 2013** (checked). Across four experiments, people
  completed more of their tasks when the tasks came with concrete
  action plans.
- **Masicampo 2011** (checked). An unfinished goal causes intrusive
  thoughts and interferes with unrelated work. Making a specific plan
  for it removed these effects, even before the goal was met. A
  follow-up paper by the same authors (2012) warns that a specific plan
  can also make people closed-minded about better routes.
- **Kruger 2004** (in `doc/design3.md`): listing a task's
  parts makes its estimate longer and more realistic.
- **Forsyth 2008** (checked). In three experiments, the time estimated
  for a task as a whole was lower than the sum of the estimates for
  its parts. The authors call this the *segmentation effect*, and
  showed in the third experiment that rounding does not explain it.
  So the total of the parts' estimates is expected to exceed the
  whole's estimate, whichever is nearer the truth.
- **Jørgensen 2004b** (checked). Seven estimation teams each estimated
  one project top-down and another bottom-up. Top-down estimates were
  accurate mainly when the estimators recalled very similar past
  projects. The authors advise bottom-up otherwise.

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
self-evident and keeping state consistent across views: a change in one
view must show in the others at once.

**Kersten 2006** (checked). Mylyn showed programmers only what was
relevant to the active task. In a field study, 16 industry programmers
using it for their daily work showed a statistically significant gain
in productivity.

**Robertson 1998** (checked). In Data Mountain, people placed documents
on a surface by hand, and found them faster than with a bookmark list.
A follow-up found that retrieval was not significantly slower after
several months away.

## 5. Boards

**Ahmad 2018** (checked) mapped the research on Kanban in software
engineering. Of 382 studies screened, 23 were primary studies, and the
research is "largely descriptive". A synthesis of 20 studies (checked,
authors not recorded) found the strongest benefits were work
visibility, control of tasks, and flow. No study found measured
comprehension or cognitive load. The evidence for boards is
practitioner experience, not experiment.

## 6. Time views and Gantt charts

The evidence is thin, and it is mostly not in favour of Gantt charts.

- **No controlled comparison found.** In four searches I found no
  experiment comparing Gantt charts with network diagrams for reading a
  project schedule. "Gantt charts need no training, network diagrams
  do" appears widely, but only as an assertion.
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
- **Luz 2009** (checked). The earlier paper on the same mosaics
  reports that, as a static display, they beat Gantt charts on
  temporal inference tasks. Participant numbers not seen.
- **Bellotti 2004** (read). The Gantt view was the least liked part of
  TaskVista. It "seems somewhat unnatural for those not used to looking
  at Gantt charts".
- **A fifth search, 2026-10-25,** for a controlled comparison of Gantt
  charts, network diagrams and tables for reading one schedule found
  nothing. The nearest, "Visual versus Tabular Scheduling Programs"
  (authors not recorded), measured *making* a schedule, and its
  tabular condition included a Gantt chart, so it does not separate
  the forms.
- **Geraldi 2012** (checked) is a conceptual critique, not an
  experiment. The Gantt chart comes from scientific management and
  handles a project's complicatedness well. But it does not express
  complexity, ambiguity, uncertainty or change, and it is applied
  everywhere with little adaptation.

So a Gantt chart is not proved to be the best display of a schedule,
and it is not proved to be the worst.

## 7. Showing uncertain dates

- **Kay 2016, "When(ish) is my bus?"** (checked). The *quantile
  dotplot* shows a forecast as, for example, 20 dots, each an equally
  likely outcome. People's probability estimates from it varied about
  1.15 times less than from a density plot.
- **Fernandes 2018** (checked). In an incentivised experiment on
  deciding when to leave for a bus, quantile dotplots of 50 outcomes
  gave decisions worth 97% of the optimum. That was 5 percentage points
  better than showing no uncertainty, with more consistent decisions.
  Cumulative distribution plots did almost as well. Text descriptions
  of uncertainty did worse than both.
- **Hullman 2019** (checked). A survey of 90 visualisation authors and
  interviews with 13 found that most value showing uncertainty, yet
  usually leave it out.

## 8. Keeping the record going

- **Li 2010** (checked) describes self-tracking in five stages:
  preparation, collection, integration, reflection and action. Barriers
  at any stage stop the whole process.
- **Epstein 2015** (checked). Surveys of a few hundred trackers and 22
  interviews found that *lapsing* and *resuming* are normal stages of
  tracking, not failures. A tool should make it easy to come back after
  a gap, rather than assume unbroken records.

## 9. Requests, promises and classification

**Requests and promises have been modelled before.** Winograd and
Flores' *conversation for action* (Winograd 1987, checked) treats
coordination as speech acts. One person makes a request. Another
promises, or counter-offers, or declines. Then completion is reported,
and the requestor declares it satisfactory. Their tool, The
Coordinator, made these acts explicit in email. Suchman 1993 (checked)
argued that forcing every message into such categories imposes
discipline and control on the people using it. Winograd's reply
(Winograd 1994, checked) was that explicit categories help where
ambiguity cannot be cleared up face to face.

**One task can serve several goals.** Bellotti 2004 found *value
extension*: people choose tasks that serve several goals at once.

**The interface shapes how people organise.** Bergman 2006 (checked)
studied 20 people. When the interface allowed it, they kept a project's
documents, email and web links together. When the interface separated
them, so did they.

**People say they like tags more than they use them.** Bergman 2013
(checked) gave 75 people Gmail labels that could be used as folders or
as tags, and gave 23 people tags in Windows 7. Both studies found a
strong preference for folders, in storing and in finding. When tags
were used, it was usually one tag per item, so in effect as a folder.
Finding things with tags was slower and less successful. In a separate
questionnaire, 168 people said they liked tags and multiple
classification. Behaviour and stated attitude disagreed.

**Hierarchy is how people decompose problems.** Jones 2005 (checked)
found that project folders often mirrored a "divide and conquer"
decomposition of the problem into subprojects. This, and the folder
preference in Bergman 2013, is about storing and finding documents.

## 10. Units, effort and elapsed time

**The unit changes the estimate.** Jørgensen 2015 (read, conference
draft) gave 22 experienced project managers the same project, at
random, to estimate in work-hours or in workdays:

- those estimating in work-hours gave a median of 77 hours
- those estimating in workdays gave a median of 115 hours (17.5 days)
- p = 0.06, so this is a small sample and a suggestive result

The author argues that coarser units give higher estimates. He also
suggests, without testing it, that the most accurate unit is the one
most natural for the size of the task: hours for small tasks, weeks or
months for large ones. The journal version is Jørgensen 2016 (checked).

**Precision says something.** Jørgensen also found that readers take a
very precise estimate, such as 975 hours rather than 1000, as a claim
of certainty. Recorded estimates bunch on round numbers, which is
called *heaping*.

**Effort and elapsed time are different quantities.** Someone who works
on many tasks at once may spend three hours of effort on a task over
four working days. "How much work is it?" asks about effort, and people
answer it naturally. "How long until it is done?" asks about elapsed
time; it is the question Buehler 1994 studied. Measuring effort
directly needs time tracking. I found no study that compares the two
questions for interleaved work.

**Recording costs decide whether records exist.** Jørgensen 2004 (in
`doc/design3.md`) found outcome records missing in 97 of 100
projects, where recording cost more effort than it saved. Recalled
durations are unreliable (Roy 2005, in `doc/design3.md`, and Bellotti
2004 above).

**One's own earlier estimate is an anchor.** Jørgensen 2001 (checked)
found, in two experiments, that an early estimate made with little
information strongly biased the re-estimate, although the estimators
were told not to use it. Jørgensen's 2004 review (checked) concludes
that the first estimate and the re-estimate should be made
independently. Løhre 2016 (checked) found that telling people to
forget an anchor did not reduce its effect, and if anything increased
it. So hiding a number at the moment of estimating does not undo
having seen it earlier.

**Which activities were done yesterday can be recalled; how long each
took cannot.** Time-use research uses diaries of the day, filled in
during or just after it. Gershuny 2017 (read in part) compared 131
such diaries with a wearable camera's record of the same day: the
diaries gave accurate, unbiased totals over the sample, with random
error per person, and short activities were reported worst. A 2022
study of online time-use diaries (checked, authors not recorded)
found that data quality fell significantly once the recall period
went beyond 24 hours. I found no study of recall at the level of
"which tasks did I touch", only of activities and durations.

## 11. Manual ordering and dragging

**People do order their tasks by hand, and do it well.**

- Bellotti 2004 found prioritisation competent, with only 3% of tasks
  dropped without reason.
- TaskVista set importance by dragging a to-do up or down the list.
- Haraty 2016's do-it-yourselfers put high-priority items at the top.

Rogers 2003 calls the fit of a new practice with existing ones
*compatibility*, and lists it among the attributes that predict
adoption.

**Ranking and rating are not the same measurement.** Alwin 1985
(checked) compared rankings and ratings of the same values in a large
survey. The two gave a similar order on average, but measured
different things about each person.

**Dragging is the slow, error-prone way to move.** MacKenzie 1991
(checked) found that, with a mouse, trackball or stylus, dragging was
slower and had more errors than pointing. By Fitts' law, a longer move
costs more time.

**Only the top needs an exact order.** Completion in Bellotti 2004
followed urgency, importance and who was waiting, not position deep in
a list.

## Where the research is thin

- No study in this document compares ways of choosing, day by day,
  among competing commitments to other people. Noted 2026-10-25, not
  yet searched; the nearest literatures are implementation intentions
  (Gollwitzer, which Masicampo 2011 builds on) and interruption and
  working spheres (González 2004, section 1).
- No controlled study found of Gantt charts against network diagrams
  or tables for schedule comprehension, in five searches, the last on
  2026-10-25.
- No study found of recalling, a day later, which of one's tasks were
  worked on, as against how long activities took. Searched 2026-10-25.
- No study found of how the total duration of a task's replacements
  compares with the estimate of the task they replaced. The
  segmentation effect (Forsyth 2008) is about estimates, not
  outcomes. Searched 2026-10-25.
- No experimental evidence found on Kanban boards' effect on
  understanding or memory.
- No user study found of outliners (nested, collapsible lists) for
  personal planning, although they are a common tool.
- The KJ method (Kawakita) is the Japanese origin of affinity diagrams,
  and is close to reorganising post-its after a reframing. I found
  descriptions and applications of it, but no evaluation.

## References

Status words: `read` means the cited figures were read in the paper;
`checked` means the bibliographic details and findings were confirmed
from abstracts or publisher pages by web search on 2026-10-18, or on
2026-10-25 where the entry says so;
`unchecked` means cited from prior knowledge.

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
- Buehler 1994. See `doc/design3.md`.
- Cheng 2015. Cheng, J., Teevan, J., Iqbal, S., Bernstein, M. "Break
  it down: a comparison of macro- and microtasks." Proceedings of CHI
  2015, 4061–4064. checked
- Epstein 2015. Epstein, D., Ping, A., Fogarty, J., Munson, S. "A
  lived informatics model of personal informatics." Proceedings of
  UbiComp 2015. doi:10.1145/2750858.2804250. checked
- Fernandes 2018. Fernandes, M., Walls, L., Munson, S., Hullman, J.,
  Kay, M. "Uncertainty displays using quantile dotplots or CDFs
  improve transit decision-making." Proceedings of CHI 2018. checked
- Forsyth 2008. Forsyth, D., Burt, C. "Allocating time to future
  tasks: the effect of task segmentation on planning fallacy bias."
  Memory & Cognition 36(4), 791–798. Findings from the abstract,
  confirmed 2026-10-25. checked
- Gershuny 2017. Gershuny, J., Harms, T., Doherty, A., Thomas, E.,
  Milton, K., Kelly, P., Foster, C. "CAPTURE24: testing self-report
  time-use diaries against objective instruments in real time."
  Centre for Time Use Research working paper 10, University of
  Oxford, October 2017. Abstract and design read; the figure on short
  activities is from a secondary summary. read in part
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
- Jørgensen 2001. Jørgensen, M., Sjøberg, D. "Impact of effort
  estimates on software project work." Information and Software
  Technology 43(15), 939–948. The re-estimation finding is as
  described in Jørgensen's 2004 review of expert estimation, Journal
  of Systems and Software 70, 37–60; the abstract confirms the
  anchoring result. checked
- Jørgensen 2004. See `doc/design3.md`.
- Jørgensen 2004b. Jørgensen, M. "Top-down and bottom-up expert
  estimation of software development effort." Information and
  Software Technology 46(1). Findings from the abstract. checked
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
- Kruger 2004. See `doc/design3.md`.
- Li 2010. Li, I., Dey, A., Forlizzi, J. "A stage-based model of
  personal informatics systems." Proceedings of CHI 2010. The five
  stages were checked from secondary sources
- Løhre 2016. Løhre, E., Jørgensen, M. "Numerical anchors and their
  strong effects on software development effort estimates." Journal
  of Systems and Software 116, 49–56. Findings from the abstract.
  checked
- Luz 2009. Luz, S., Masoodian, M., Rogers, B., Zhang, C. "Chronos: a
  tool for interactive scheduling and visualisation of task
  hierarchies." Proceedings of IV 2009, IEEE. Finding from the
  abstract; the author list beyond the first two is unchecked.
  checked
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
- McConnell 2006. See `doc/design3.md`.
- Robertson 1998. Robertson, G., Czerwinski, M., Larson, K., Robbins,
  D., Thiel, D., van Dantzich, M. "Data Mountain: using spatial memory
  for document management." Proceedings of UIST 1998, 153–162. The
  follow-up is Czerwinski et al., INTERACT 1999. checked
- Rogers 2003. Rogers, E. Diffusion of Innovations, fifth edition.
  Free Press. unchecked
- Roy 2005. See `doc/design3.md`.
- Shipman 1994. Shipman, F., McCall, R. "Supporting knowledge-base
  evolution with incremental formalization." Proceedings of CHI 1994.
  checked
- Shipman 1999. Shipman, F., Marshall, C. "Formality considered
  harmful: experiences, emerging themes, and directions on the use of
  formal representations in interactive systems." Computer Supported
  Cooperative Work 8(4), 333–352. checked
- Suchman 1993. Suchman, L. "Do categories have politics? The
  language/action perspective reconsidered." Proceedings of ECSCW 1993,
  1–14. Her rejoinder is "Speech acts and voices", CSCW 3(1), 85–95.
  checked
- Winograd 1987. Winograd, T. "A language/action perspective on the
  design of cooperative work." Human-Computer Interaction 3(1). The
  Coordinator, and conversation for action. Details unchecked
- Winograd 1994. Winograd, T. "Categories, disciplines, and social
  coordination." Computer Supported Cooperative Work 2(3), 191–197.
  doi:10.1007/BF00749016. checked
