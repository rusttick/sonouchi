# Architecture: the interface in the core

`sonouchi.core.view`, `sonouchi.core.actions`, `sonouchi.core.screen`,
`sonouchi.core.keys` and `sonouchi.core.chain`. The overview is in
[architecture.md](architecture.md), and the screens they draw are in
[ux-journey.md](ux-journey.md).

## The pattern

- **A view is a pure function** from the state to hiccup. The shell
  renders whatever it returns with Replicant.
- **Handlers are data.** `:on` in the hiccup holds an action vector,
  such as `[:command/mark]` or `[:command/cursor id]`, never a
  function. The keyword `:field/text` in an action vector stands for
  the text of the input the event came from; the shell fills it in
  before dispatching.
- **Commands are one map.** `actions/handlers` maps each command name
  to the function that carries it out, of the state, the command's
  arguments, a seq of fresh ids and the tokenizer. `actions/run` looks
  the command up and returns the new state. Keys that change the record
  map straight to the action names, `:action/record-start` and so on,
  and the same handlers serve clicks and keys alike.
- **Keys are data.** `keys/command` maps a chord to a command, given
  the screen and what has the focus. The shell describes each key press
  as a map, asks the core, and runs the command.
- **Collaborators are arguments.** Whatever the core cannot make, such
  as the tokenizer for search, is passed in.

## `sonouchi.core.screen`

The values both the views and the commands derive from the state:
today and the recording day from the clock; the entities from the
log; which tasks the query leaves visible; the three columns in the
board's order; the cursor, which falls back to the first visible task;
each task's latest estimate; the schedule from today; the rows of the
schedule screen, in the order of the dependency graph with ties in the
board's order; the tasks the last act changed (V7); and the search
index, built or updated from the board's search texts.

## `sonouchi.core.actions`

- **Recording.** Every command that changes the record goes through one
  private function that appends the act with the first fresh id and
  the state's clock, updates the search index for the tasks the act
  touched, and clears the problems shown.
- **The stand-up keys** act on the task under the cursor for the
  recording day: start, finish, worked on. Finishing an unstarted task
  starts it too.
- **Fields.** Opening a field puts `:state/field` in the state;
  `:command/typed` changes its text; `:command/record` records what it
  holds, or writes the reason it cannot into the field as
  `:problem/message`. The wait field takes its reason from a digit
  first, and "not in the plan" records at once.
- **Replacement.** `:command/open-replace` makes the draft from the
  marked tasks or the cursor, or from the query; typing, new lines and
  the states change the draft; `:command/record` turns the draft into
  one `:action/replace` act through `actions/replacement-act`, which is
  public so that it can be tested with plain values. New tasks come
  after what the old tasks came after, and inherit the start of the one
  old task that had started, when exactly one had. Lines without a
  title are skipped; an estimate that does not parse is not recorded,
  and the display says so beside the line.
- **Comes after** checks the proposed graph for a cycle with
  `schedule/topological-order` before recording, and names the cycle in
  `:state/problems` otherwise.
- **The frame.** Escape closes the field, else leaves the replacement
  display, else clears the query. The recording day moves a working day
  at a time, and not past today.
- `actions/start` makes a loaded state ready: it builds the index and
  shows the board.

## `sonouchi.core.keys`

- `keys/chord` names a key press, such as `"Meta+Shift+Z"`.
- `keys/command` returns the command for a chord on a platform, given
  the screen and the focus, or nil to leave the key to the browser. The
  board's keys act only when nothing has the focus. Enter in the query
  bar opens the replacement display; Enter in a field records it; on a
  line of the replacement display Enter starts the next line and
  Cmd+Enter or Ctrl+Enter records the whole. Undo and redo act only when
  no input with text has the focus, so that in one they undo typing.
- `keys/label` says how a command's key is shown on screen, per
  platform where the platforms differ.

## `sonouchi.core.chain`

`chain/layout` lays out the tasks behind one task in layers from left
to right: each task's layer is one more than the deepest task it comes
after, and within a layer tasks are ordered by the mean position of
what they come after, which keeps crossings few (Sugiyama 1981). It
returns `:chain/layers` and `:chain/edges`. `chain/depth` gives every
task's depth, which orders the schedule's rows.

## `sonouchi.core.view`

`view/root` returns the whole page from the state and the tokenizer:

- the frame (V9): the app's name, the screen's name, the query bar, the
  count shown of all, the recording day flagged when more than a
  working day back, the screens with their keys, export and import
- the board: three columns of cards. A card shows its title, "no
  estimate" when it has none, its open wait, the open field when one is
  on it, and beside the cursor the keys (V11). Recorded cards are
  lighter with a ✓, marked cards show ✗, and the cards the last act
  changed are outlined (V7)
- the replacement display: the old tasks, an arrow, and one line per
  new task with its state, title, estimate and a note when the estimate
  does not parse or will not count
- the schedule: the finish and the critical path in words, a table of
  rows with a bar on the critical ones, the chain behind the cursor's
  task as SVG, and the tasks not estimated with what stops each
- any problems, and the footer with the last act's time (V12)
