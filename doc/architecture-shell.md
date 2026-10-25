# Architecture: the shell

`sonouchi.app`, the only `.cljs` namespace. The overview is in
[architecture.md](architecture.md).

The shell does what the core may not: it holds the one atom, makes ids,
reads the clock and the platform, splits text into words, reads the
keyboard, renders, moves the focus, keeps the log in the browser's
storage, and reads and writes files. Each entry point gathers its
inputs, calls the core, and carries out the effects.

## Start

`init`, which `index.html` loads through `shadow-cljs.edn`:

1. puts the clock and the platform in the state
2. loads the saved log
3. makes the state ready with `actions/start`, which builds the search
   index
4. sets Replicant's dispatch function, and listens for key presses
5. watches the atom: on every change it renders, and when the log has
   changed it saves it
6. renders

`reload` renders again after each hot reload in development.

## An act

Every command, from a key or a click, goes through `act!`: it merges
the clock into the state, calls `actions/run` with the command, a lazy
seq of fresh UUIDs and the tokenizer, then moves the focus as the
command implies, and times the act from the event to the next painted
frame for the footer (V12).

A key press goes to `on-key!`. A key pressed during Japanese input
conversion is ignored. Otherwise the press is described as a map, the
focused element is described as `keys/command` wants it, from its id
and `data-` attributes, and the command the core returns is run.

Clicks and input events go to `dispatch!` with their action vector.
The keyword `:field/text` in the vector is replaced by the input's
value. Export, import and paste are carried out here, because they
read files and the clipboard; everything else is a command for the
core. Pasting several lines into a title line hands the whole text to
the core, which splits it into lines.

## Focus

Which input has the focus is not in the state, so the shell works it
out after each command from the command and the new state: `/` focuses
the query bar; opening a field or choosing a wait's reason focuses the
field; opening the replacement display or starting a new line focuses
that line's title; and clearing or recording blurs, unless a field or
the replacement display is still open.

## The tokenizer

`tokenize` splits text into words with `Intl.Segmenter` by Unicode
word boundaries (UAX #29), which handles Japanese; where the browser
lacks it, runs of letters and digits are the words. The core receives
it as an argument and never calls the host.

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
