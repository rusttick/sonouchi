# Architecture: the shell

`sonouchi.app`, the only `.cljs` namespace. The overview is in
[architecture.md](architecture.md).

The shell does what the core may not: it holds the one atom, makes ids,
reads the clock and the platform, reads the keyboard, renders, keeps
the log in the browser's storage, and reads and writes files. Each
entry point gathers its inputs, calls the core, and carries out the
effects.

## Start

`init`, which `index.html` loads through `shadow-cljs.edn`:

1. puts the clock and the platform in the state
2. loads the saved log
3. sets Replicant's dispatch function, and listens for key presses
4. watches the atom: on every change it renders, and when the log has
   changed it saves it
5. renders, and focuses the new-task field

`reload` renders again after each hot reload in development.

## An act

A key press goes to `on-key!`. A key pressed during Japanese input
conversion is ignored, so that Enter confirms the conversion rather
than adding a task. Otherwise the press is described as a map, and
`keys/command` says which command it means. `run-command!` carries the
command out:

- adding takes the field's text, makes ids, and records the act from
  `tasks/add-act`
- undo and redo record the act from `history/undo-act` or
  `history/redo-act`

`record!` appends an act to the log with a new id and the time.
`time-act!` measures from the key press to the next painted frame, and
puts the time in the state for the footer.

Clicks and other hiccup events go to `dispatch!`, which receives the
action vector: export, import, and paste. Pasting several lines adds a
task for each, in one act; one line is left to the browser.

## Storage

The log is kept in `localStorage` as EDN:

- `sonouchi/log`: the log, saved on every change
- `sonouchi/log-unreadable`: a saved log that could not be read, kept
  aside so that the next save does not lose it
- `sonouchi/log-before-import`: the log an import replaced

`read-log` reads EDN text and checks it with `log/problems`. Export
writes the log to a dated `.edn` file. Import reads a file, and
replaces the log only if the file holds a log, after keeping the old
one; otherwise it shows the problems.
