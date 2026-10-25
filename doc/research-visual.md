# Research: perception and interaction

What research on human perception, attention, memory and motor control
says about what goes on a screen, where, and how a person acts on it.
These are findings only. The rules sonouchi's displays follow, V1 to
V17, are in [ux-design.md](ux-design.md).

Sources are cited by author and year, and listed at the end with a
status word as in `doc/design3.md`.

# Part 1. Seeing

## 1.1 What is seen at a glance

**Healey 2012** (checked) reviews *preattentive* processing: properties
the visual system picks out in parallel across the whole image, in
200 ms or less, however many items are shown. Examples are colour,
orientation, intensity, size, shape, curvature and line length. These
features support four quick tasks:

- finding the one item that differs
- seeing the boundary between groups
- following a group over time
- estimating how many items share a feature

A target defined by a *combination* of features, such as "red and
square", generally has to be searched for item by item. This is the
classic result of Treisman 1980 (unchecked).

## 1.2 Which encodings are read accurately

**Cleveland 1984** (checked) ranked encodings by how accurately people
judge the ratio of two values:

1. position along a common scale
2. position along non-aligned scales
3. length
4. angle and slope
5. area
6. volume, density and colour saturation
7. colour hue

**Heer 2010** (checked) replicated the ranking with crowdsourced
participants, and added circle areas (as in bubble charts) and
rectangle areas (as in treemaps). The order held.

## 1.3 What is seen as belonging together

The Gestalt grouping principles include proximity, similarity, *common
region* (items inside one boundary group together, Palmer 1992,
checked) and *uniform connectedness* (items joined into one region
group together, Palmer 1994, checked). Han 1999 (checked) found that
targets defined by proximity and by connectedness were found equally
fast with one or two groups. With more groups, connectedness was
faster. Thin lines between rows and colour alone are weaker cues than
space and enclosing regions.

## 1.4 Colour

- **Colour blindness.** About 5% of Japanese men and 0.2% of Japanese
  women have a colour vision deficiency (checked, from secondary
  sources). Studies of Chinese and Japanese men give 4% to 6.5%.
- **How many colours.** ColorBrewer's qualitative palettes (Harrower
  2003, checked) have at most 8 to 12 colours. A third-party guide
  reports that with the colourblind-safe filter on, only one palette of
  4 colours remains.
- **Never colour alone.** WCAG requires that colour is not the only
  means of conveying information (success criterion 1.4.1, unchecked).

## 1.5 Light or dark background

**Piepenbrock 2013** (checked) tested visual acuity and proofreading in
younger and older adults. Dark text on a light background (*positive
polarity*) was better for both age groups, according to a secondary
summary. A 2014 study by the same group found that the advantage grew
as the font got smaller. The proposed mechanism is that a brighter
screen makes the pupil smaller, which sharpens the image on the retina.
Dark backgrounds may help some people with low vision, and in dim
rooms.

## 1.6 Japanese text

- **Hasegawa** (checked) studied Japanese characters on small displays.
  Readability was higher at higher resolution, and higher with Gothic
  (sans-serif) faces than with Mincho (serif) faces.
- **Line length.** Studies on the best line length for Japanese are
  small and specific to their setting:
  - for projected captions, 15 to 20 characters per line was most
    readable, falling to 15 when the characters were twice as large
  - breaking lines at phrase boundaries (bunsetsu) gave faster reading
    than fixed-length lines
- **English.** The usual guideline of 50 to 75 characters per line
  comes from practitioner sources.

## 1.7 Memory and change

- **Working memory.** **Cowan 2001** (checked) puts the capacity of
  working memory at about four chunks, not Miller's seven.
- **Change blindness.** People routinely fail to see large changes in a
  scene when the change happens during a glance away, a blink or a
  redraw. Simons 2005 (checked) reviews the effect. In a 2018
  eye-tracking study (authors not recorded), 75% of participants looked
  at a moving banner on a web page, but only 33% could recall anything
  in it.

## 1.8 Animation

- **Heer 2007** (checked) found, in two controlled experiments, that
  animated transitions between charts improved people's judgements of
  how values changed. Simple *staged* transitions, which change one
  thing at a time, were preferred. Rescaling an axis during a
  transition made perception worse.
- **Tversky 2002** (checked) reviewed animation for learning.
  Animations are often too fast or too complex to perceive accurately,
  and people tend to remember continuous events as discrete steps
  anyway.
- **Archambault 2011** (checked). For understanding how a graph changes
  over time, *small multiples* (snapshots side by side) were
  significantly faster than animation on all five tasks tested.

# Part 2. Layout and navigation

## 2.1 Overview and detail

**Cockburn 2008** (read: summary section) reviewed four ways to show
detail and context together:

- separate panes for the overview and the detail
- zooming
- *focus+context*, such as fisheye views
- *cues*: highlighting or dimming items

The review's conclusions:

- **None is ideal.** Switching between an overview and a detail window
  "required mental effort and time".
- **Zooming** makes people reorient themselves after each zoom.
- **Fisheye distortion** "largely failed to provide evidence of
  performance enhancements", and can make targets harder to hit.
- **Which is best depends on the task.**

## 2.2 Moving between displays

**Woods 1984** (checked) named *visual momentum*: how easily a person
moves between displays without losing their place. The mental effort
needed to place a new display in context should be small. Woods
compared a poor transition to a bad cut in film editing. Bennett 2012
(checked) reviews the techniques and the evidence:

- landmarks that stay put
- overlap between successive displays
- a visible map of where one is

## 2.3 Stability

- **Robertson 1998** (checked, in
  [research-workflow.md](research-workflow.md)). People find items
  faster when they place them by hand and the positions stay fixed.
- **Scarr 2012** (checked) found that expert users selected commands
  faster from a flat, spatially stable layout (CommandMaps) than from
  menus or Microsoft's Ribbon. Novices were about equally fast with
  each. The abstract notes that `hierarchical organisation is known to
  slow experts down`.
- **Findlater 2004** (checked) compared three kinds of menu, with 27
  people:
  - *static*: fixed
  - *adaptable*: rearranged by the user
  - *adaptive*: rearranged automatically by recent and frequent use

  The static menu was significantly faster than the adaptive one, and
  most people preferred the adaptable one.

## 2.4 Tables, lists and graphs

**Vessey 1991** (checked) proposed *cognitive fit*:

- tables suit *symbolic* tasks, which need exact values
- graphs suit *spatial* tasks, which need patterns and comparisons

When the display matches the task, performance improves. The main
result was for symbolic tasks. The 128 participants preferred tables.

**Zebra striping** (Enders 2008, practitioner, checked) had small or
inconsistent effects on the speed and accuracy of reading tables. It
did not hurt.

**Word-sized graphics,** such as Tufte's *sparklines*, put a small
graphic inside a line of text. Beck 2017 (checked) argues that this
avoids the *split attention* of looking between text and a separate
chart. That paper is a review with examples, not an experiment, and I
found no controlled study of them.

# Part 3. Acting

## 3.1 Pointing, dragging and target size

- **Fitts' law.** The time to point at a target grows with its distance
  and shrinks with its size (Fitts 1954, unchecked).
- **Dragging** is slower and has more errors than pointing (MacKenzie
  1991, in [research-workflow.md](research-workflow.md)).
- **Touch.** Targets on a touchscreen should be at least 9.2 mm for
  single taps and 9.6 mm for taps in sequence (Parhi 2006, checked).

## 3.2 Choosing among options

- **Hick–Hyman law.** Decision time grows with the logarithm of the
  number of equally likely choices (Hick 1952, Hyman 1953, unchecked).
  The law holds only when the person can narrow the options down.
  Scanning an unordered list takes time in proportion to its length.
- **Menu breadth.** Landauer 1985 and Snowberry 1983 (checked from
  secondary sources) found that broad, shallow menus beat deep ones,
  and Snowberry's result held only when the order within each level was
  consistent.

## 3.3 The keyboard

**The keystroke-level model** (Card 1980, checked) predicts how long an
expert takes for a routine task without errors. Its common operator
times:

- K, a keystroke: about 0.2 s (about 0.28 s for an average typist)
- P, pointing with the mouse: about 1.1 s
- H, moving a hand between keyboard and mouse: about 0.4 s
- M, mental preparation: about 1.35 s

**People don't adopt faster methods on their own.**

- Lane 2005 (checked) surveyed 251 experienced Microsoft Word users.
  Most rarely used keyboard shortcuts, though a second study confirmed
  that shortcuts were the fastest method.
- Cockburn 2014 (checked) reviews the general finding: users
  persistently fail to adopt faster methods, and many expert features
  are seldom used.

## 3.4 Modes

**Sellen 1992** (checked) studied *mode errors*: typing commands while
in insert mode, or text while in command mode.

- Both visual and kinesthetic feedback about the current mode reduced
  errors.
- Kinesthetic feedback did better, both on errors and on mental load.
  Kinesthetic means feeling it, such as holding a foot pedal down.
- Modes that the user holds actively, and that end when released,
  prevented errors better than modes the system keeps until told
  otherwise.

## 3.5 Seeing the result of an act

**Hutchins 1985** (checked) named two gulfs:

- the *gulf of execution*, between what a person intends and the
  actions the system allows
- the *gulf of evaluation*, between the state the system shows and what
  the person expected

Direct manipulation narrows both. It shows the objects being acted on,
and shows the effect of each act at once, in place.

## 3.6 Response time

The usual limits are 0.1 s to feel instantaneous, 1 s to keep the flow
of thought, and 10 s to keep attention. They come from Nielsen 1993
(unchecked), drawing on Miller 1968 (checked), whose figures were based
on experience rather than experiment.

## 3.7 Mistakes: undo, not confirmation

Raskin 2000 (checked, from secondary sources) argues that people learn
to dismiss warning dialogs by habit, so the dialogs stop protecting
them. A universal undo protects them instead. Some practitioners keep
confirmation for severe actions that cannot be reversed.

## 3.8 Entering a number

- **Sliders.** Couper 2006 (checked, from secondary sources) found that
  slider scales gave more missing answers and took longer than radio
  buttons or typed numbers.
- **Starting positions.** Liu 2019 (checked) ran four web experiments,
  with 490 to 3,744 people each. On 0–100 sliders, more people chose
  whatever value the slider started at (25, 50, 75 or 100). On 7- and
  21-point scales the starting value had no consistent effect.
- **Anchoring** in general is in `doc/design3.md` (Tversky
  1974, Aranda 2005).

# Part 4. Finding things

## 4.1 Browsing and searching

- **Bergman 2008** (checked). For personal files, people browsed for
  56–68% of retrievals and searched for 4–15%. Better search engines
  did not change this.
- **Whittaker 2011** (checked). In email, searching found messages as
  reliably as folders, and slightly faster. Scrolling was the most
  common way of refinding.

## 4.2 Facets and dynamic queries

- **Yee 2003** (checked). 32 art history students used a collection of
  35,000 images. 90% preferred browsing by facets with *query
  previews*, which show how many items each choice would leave, over a
  standard search interface.
- **Ahlberg 1992** (checked). 18 students were significantly faster
  with *dynamic queries*, where results update as the controls move,
  than with fill-in forms.

## 4.3 Suggestions while typing

Hearst 2009, chapter 4 (read) reviews the evidence:

- Users clicked dynamic suggestions in 30–37% of search sessions (Anick
  2008).
- In a study of 36 students, suggestions shown as people typed raised
  precision on exploratory tasks. People found them more engaging. The
  1.8 s delay was the main complaint. Suggestions drawn from the wrong
  results can also mislead (White 2007).

## 4.4 Information scent

**Pirolli 1999** (checked) proposed *information foraging*. People
follow the label, link or cue that best suggests it leads to what they
want, which is its *information scent*, and they give up when the
scent fades.

# Part 5. Networks and time

## 5.1 Node-link diagrams or matrices

- **Ghoniem 2004** (checked) tested random graphs of three sizes and
  three densities. Above 20 nodes, adjacency matrices beat node-link
  diagrams on most tasks. Finding a path still favoured node-link
  diagrams.
- **Okoe 2018** (read) tested 557 crowdsourced participants on a real
  network of 258 nodes and 1,090 edges, with interactive displays.
  Node-link diagrams were better for most questions about connectivity:
  - whether two nodes are connected
  - the length of the shortest path
  - neighbours
  - nodes within two steps

  Matrices were better for counting clusters, and other group tasks
  were similar. Node-link diagrams were also remembered better.

## 5.2 Drawing dependency chains

**Purchase 1997** (checked) found that the number of edge crossings had
the largest effect on how well people understood a graph. The number of
bends and the amount of symmetry mattered less. Minimum angles and an
orthogonal grid had no significant effect.

The standard *layered* drawing of a directed acyclic graph (Sugiyama
1981, unchecked) puts each node in a layer, and is designed to reduce
crossings. It is deterministic.

## 5.3 Timelines

**Brehmer 2017** (checked) surveyed 263 timelines and described a
design space of three dimensions:

- representation: linear, radial, grid, spiral and others
- scale: chronological, relative, logarithmic and others
- layout: one timeline, several, or segmented

20 combinations were judged viable. Linear designs dominate, which the
authors attribute partly to their origins in print.

## Where the research is thin

- No study found of interfaces where an explicit action is chosen for a
  query-defined set, or of how such designs compare with fixed views.
- No study found of the best line length, or title length, for mixed
  Japanese and English on a laptop screen.
- No controlled study found of sparklines or other word-sized graphics
  inside lists.
- The mental map evidence for dynamic graphs is mixed (Archambault
  2011, and the 2013 review cited in it).
- **Date formats.** Searched on 2026-10-25: no controlled study found
  of reading speed or errors for day-month against month-day order.
  What exists is practitioner guidance: write the month as a name, not
  a number, because numeric dates such as 12/11/10 are read
  differently by region (Opquast Digital Quality Framework, rule 4,
  unchecked), and use the order the audience is used to. A survey of
  about 980 people found both orders in common use (Syntagm, about
  2009, unchecked). With the month as a name, either order is
  unambiguous.

## References

Status words:

- `read`: the cited figures were read in the paper, or in the part
  named
- `checked`: the bibliographic details and findings were confirmed
  from abstracts or publisher pages by web search on 2026-10-18
- `unchecked`: cited from prior knowledge

Entries:

- Ahlberg 1992. Ahlberg, C., Williamson, C., Shneiderman, B. "Dynamic
  queries for information exploration: an implementation and
  evaluation." Proceedings of CHI 1992, 619–626. checked
- Anick 2008. Anick, P., Kantamneni, R. A log study of Yahoo Search
  Assist, as reported in Hearst 2009. read in Hearst 2009
- Archambault 2011. Archambault, D., Purchase, H., Pinaud, B.
  "Animation, small multiples, and the effect of mental map
  preservation in dynamic graphs." IEEE TVCG 17(4). checked
- Beck 2017. Beck, F., Weiskopf, D. "Word-sized graphics for
  scientific texts." IEEE TVCG 23(6), 1576–1587. checked
- Bennett 2012. Bennett, K., Flach, J. "Visual momentum redux."
  International Journal of Human-Computer Studies 70(6), 399–414.
  checked
- Bergman 2008. Bergman, O., Beyth-Marom, R., Nachmias, R.,
  Gradovitch, N., Whittaker, S. "Improved search engines and
  navigation preference in personal information management." ACM
  TOIS 26(4). doi:10.1145/1402256.1402259. checked
- Brehmer 2017. Brehmer, M., Lee, B., Bach, B., Henry Riche, N.,
  Munzner, T. "Timelines revisited: a design space and considerations
  for expressive storytelling." IEEE TVCG 23(9), 2151–2164. checked
- Card 1980. Card, S., Moran, T., Newell, A. "The keystroke-level
  model for user performance time with interactive systems."
  Communications of the ACM 23(7). Operator times from secondary
  sources. checked
- Cleveland 1984. Cleveland, W., McGill, R. "Graphical perception:
  theory, experimentation, and application to the development of
  graphical methods." Journal of the American Statistical Association
  79, 531–554. checked
- Cockburn 2008. Cockburn, A., Karlson, A., Bederson, B. "A review of
  overview+detail, zooming, and focus+context interfaces." ACM
  Computing Surveys 41(1). Summary section read. read
- Cockburn 2014. Cockburn, A., Gutwin, C., Scarr, J., Malacria, S.
  "Supporting novice to expert transitions in user interfaces." ACM
  Computing Surveys. doi:10.1145/2659796. Authors and venue unchecked;
  abstract checked
- Couper 2006. Couper, M., Tourangeau, R., Conrad, F., Singer, E.
  "Evaluating the effectiveness of visual analog scales: a web
  experiment." Social Science Computer Review 24(2), 227–245.
  Findings from a secondary review. checked
- Cowan 2001. Cowan, N. "The magical number 4 in short-term memory: a
  reconsideration of mental storage capacity." Behavioral and Brain
  Sciences 24. checked
- Enders 2008. Enders, J. "Zebra striping: does it really help?" A
  List Apart, with a follow-up. Practitioner experiments. checked
- Findlater 2004. Findlater, L., McGrenere, J. "A comparison of
  static, adaptive, and adaptable menus." Proceedings of CHI 2004.
  checked
- Fitts 1954. Fitts, P. "The information capacity of the human motor
  system in controlling the amplitude of movement." Journal of
  Experimental Psychology 47. unchecked
- Ghoniem 2004. Ghoniem, M., Fekete, J.-D., Castagliola, P. "A
  comparison of the readability of graphs using node-link and
  matrix-based representations." Proceedings of IEEE InfoVis 2004.
  checked
- Han 1999. Han, S., Humphreys, G. On uniform connectedness and
  proximity in perceptual grouping. Title and venue unchecked;
  finding checked
- Harrower 2003. Harrower, M., Brewer, C. "ColorBrewer.org: an online
  tool for selecting colour schemes for maps." The Cartographic
  Journal 40(1), 27–37. checked
- Hasegawa. Hasegawa, S. et al. On the readability of Japanese
  characters on small displays. Year and venue unchecked; findings
  checked
- Healey 2012. Healey, C., Enns, J. "Attention and visual memory in
  visualization and computer graphics." IEEE TVCG 18(7), 1170–1188.
  checked
- Hearst 2009. Hearst, M. Search User Interfaces. Cambridge University
  Press. Chapter 4 read online. read
- Heer 2007. Heer, J., Robertson, G. "Animated transitions in
  statistical data graphics." IEEE TVCG (InfoVis 2007).
  doi:10.1109/TVCG.2007.70539. checked
- Heer 2010. Heer, J., Bostock, M. "Crowdsourcing graphical
  perception: using Mechanical Turk to assess visualization design."
  Proceedings of CHI 2010. checked
- Hick 1952, Hyman 1953. The Hick–Hyman law. unchecked
- Hutchins 1985. Hutchins, E., Hollan, J., Norman, D. "Direct
  manipulation interfaces." Human-Computer Interaction 1, 311–338.
  checked
- Lane 2005. Lane, D., Napier, H., Peres, S., Sándor, A. "Hidden costs
  of graphical user interfaces: failure to make the transition from
  menus and icon toolbars to keyboard shortcuts." International
  Journal of Human-Computer Interaction 18(2), 133–144. checked
- Landauer 1985. Landauer, T., Nachbar, D. "Selection from alphabetic
  and numeric menu trees using a touch screen: breadth, depth, and
  width." Proceedings of CHI 1985. Findings from secondary sources.
  checked
- Liu 2019. Liu, M., Conrad, F. "Where should I start? On default
  values for slider questions in web surveys." Social Science
  Computer Review 37, 248–269. checked
- MacKenzie 1991. See [research-workflow.md](research-workflow.md).
- Miller 1968. Miller, R. "Response time in man-computer
  conversational transactions." Proceedings of the Fall Joint
  Computer Conference, 267–277. checked
- Nielsen 1993. Nielsen, J. "Response times: the 3 important limits."
  unchecked
- Okoe 2018. Okoe, M., Jianu, R., Kobourov, S. "Node-link or adjacency
  matrices: old question, new insights." IEEE TVCG 25(10),
  2940–2952. Task results read in the arXiv version. read
- Palmer 1992. Palmer, S. "Common region: a new principle of
  perceptual grouping." Cognitive Psychology 24. checked
- Palmer 1994. Palmer, S., Rock, I. "Rethinking perceptual
  organization: the role of uniform connectedness." Psychonomic
  Bulletin & Review 1, 29–55. checked
- Parhi 2006. Parhi, P., Karlson, A., Bederson, B. "Target size study
  for one-handed thumb use on small touchscreen devices." Proceedings
  of MobileHCI 2006. checked
- Piepenbrock 2013. Piepenbrock, C., Mayr, S., Mund, I., Buchner, A.
  "Positive display polarity is advantageous for both younger and
  older adults." Ergonomics. doi:10.1080/00140139.2013.790485.
  Outcome from a secondary summary; the third author is unchecked.
  checked
- Pirolli 1999. Pirolli, P., Card, S. "Information foraging."
  Psychological Review 106, 643–675. checked
- Purchase 1997. Purchase, H. "Which aesthetic has the greatest effect
  on human understanding?" Graph Drawing 1997, LNCS 1353, 248–261.
  checked
- Raskin 2000. Raskin, J. The Humane Interface. Addison-Wesley.
  Argument from secondary sources. checked
- Robertson 1998. See [research-workflow.md](research-workflow.md).
- Scarr 2012. Scarr, J., Cockburn, A., Gutwin, C., Bunt, A. "Improving
  command selection with CommandMaps." Proceedings of CHI 2012,
  257–266. checked
- Sellen 1992. Sellen, A., Kurtenbach, G., Buxton, W. "The prevention
  of mode errors through sensory feedback." Human-Computer
  Interaction 7(2), 141–164. checked
- Simons 2005. Simons, D., Rensink, R. "Change blindness: past,
  present, and future." Trends in Cognitive Sciences 9. checked
- Snowberry 1983. Snowberry, K., Parkinson, S., Sisson, N. Menu
  breadth and depth. Cited in secondary sources. unchecked
- Sugiyama 1981. Sugiyama, K., Tagawa, S., Toda, M. "Methods for
  visual understanding of hierarchical system structures." IEEE
  Transactions on Systems, Man, and Cybernetics 11(2). unchecked
- Treisman 1980. Treisman, A., Gelade, G. "A feature-integration
  theory of attention." Cognitive Psychology 12. unchecked
- Tversky 2002. Tversky, B., Morrison, J. B., Betrancourt, M.
  "Animation: can it facilitate?" International Journal of
  Human-Computer Studies 57, 247–262. checked
- Vessey 1991. Vessey, I., Galletta, D. "Cognitive fit: an empirical
  study of information acquisition." Information Systems Research
  2(1), 63–84. checked
- White 2007. White, R., Marchionini, G. Real-time query expansion,
  as reported in Hearst 2009. read in Hearst 2009
- Whittaker 2011. Whittaker, S., Matthews, T., Cerruti, J., Badenes,
  H., Tang, J. "Am I wasting my time organizing email? A study of
  email refinding." Proceedings of CHI 2011, 3449–3458. checked
- Woods 1984. Woods, D. "Visual momentum: a concept to improve the
  cognitive coupling of person and computer." International Journal
  of Man-Machine Studies 21(3), 229–244. checked
- Yee 2003. Yee, K.-P., Swearingen, K., Li, K., Hearst, M. "Faceted
  metadata for image search and browsing." Proceedings of CHI 2003,
  401–408. checked
