# Architecture: the interface in the core

`sonouchi.core.view`, `sonouchi.core.keys` and `sonouchi.core.tasks`.
The overview is in [architecture.md](architecture.md), and the screens
they will draw are in [ux-journey.md](ux-journey.md).

These namespaces draw the page built today: a quick-entry field, with
the tasks added today under it. The screens of the session after the
stand-up replace it, following the same pattern.

## The pattern

- **A view is a pure function** from the state to hiccup. The shell
  renders whatever it returns with Replicant.
- **Handlers are data.** `:on` in the hiccup holds an action vector,
  such as `[:command/export]`, never a function. The shell's dispatch
  function carries it out.
- **Keys are data.** A keymap maps a key chord to a command, per
  platform. The shell describes each key press as a map, asks the core
  which command it means, and runs it.
- **Collaborators are arguments.** Whatever the core cannot make, such
  as the tokenizer for search, is passed in.

## `sonouchi.core.view`

`view/root` returns the whole page from the state:

- the header, the frame shared by every screen (V9): the app's name,
  the screen's name, the task count, and export and import
- the new-task field, focused when the page opens, with its keys shown
  under it (V11)
- any problems, such as a file that could not be imported
- today's additions, newest first, each with its local time; the
  latest act's tasks are marked ▶ and bold (V7)
- the footer, with how long the last act took, flagged above 0.1 s
  (V12)

## `sonouchi.core.keys`

- `keys/chord` names a key press, such as `"Meta+Shift+Z"`, from the
  map the shell makes of a browser key event.
- `keys/command` returns the command a chord means on a platform, or
  nil to leave the key to the browser. It is given the text in the
  field when the field has the focus, because Enter and Escape act only
  on a field with text, and undo and redo act only when the field is
  empty or unfocused, so that in a field with text they undo typing as
  usual.
- `keys/label` says how a command's key is shown on screen.

Undo and redo follow each platform: ⌘Z and ⇧⌘Z on macOS, Ctrl+Z and
Ctrl+Y (or Ctrl+Shift+Z) elsewhere. The browser cannot read the
system's own settings, so the platform decides.

## `sonouchi.core.tasks`

- `tasks/titles` splits text into titles, one per line, trimmed, blank
  lines left out. Pasting several lines adds several tasks in one act,
  so one undo removes them all.
- `tasks/add-act` makes the act that adds them, from the titles and the
  ids the shell made.
- `tasks/added-today` lists the tasks added on the local day of the
  shell's clock that still have a title, and `tasks/clock` shows a
  time as local "HH:MM". Local time comes from the offset the shell
  supplies.
- `tasks/latest-add` and `tasks/task-count` give the marked act and the
  count.
