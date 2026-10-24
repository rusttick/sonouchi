# Quick entry (I4): design and trial

Designed with the steps in `TODO.md`, section 2, on 2026-10-18.

## 1. The question

What's on the post-it: a few words describing a task. Type it, and let
go of it (Bellotti 2004, Masicampo 2011, Shipman 1999).

## 2. The facts

- Appends one act per Enter or paste: `:action/add-task`, one patch per
  task, each `{:patch/entity <UUID> :task/title "…"}`.
- Undo and redo append `:action/undo` and `:action/redo`.
- Reads today's `:action/add-task` acts for the list.
- New attributes: `:task/title`, the actions, the commands, the key
  press attributes, and the clock and platform the shell supplies.

## 3. The display

A text field, with today's additions under it, newest first, each with
its local time. The latest addition is marked ▶ and bold. A header
holds the frame: the app, the part, the task count, export and import.
The footer shows how long the last act took. Light theme, Gothic face.

## 4. The acts and keys

- The field has the focus when the page opens.
- Enter adds; the field clears and keeps the focus. Enter on an empty
  field does nothing.
- Esc clears the field.
- Undo and redo use the platform's keys: ⌘Z and ⇧⌘Z on macOS, Ctrl+Z
  and Ctrl+Y (or Ctrl+Shift+Z) on Windows and Linux. They act only when
  the field is empty or unfocused; in a field with text they undo
  typing, as usual. Decided by the owner: the browser cannot read the
  system's keys, so the platform decides.
- Pasting several lines adds one task per line, in one act, so one undo
  removes them all. Decided by the owner.
- Enter during Japanese input conversion confirms the conversion and is
  ignored by the app.

## 5. The frame

The header line, shared by later parts (V9).

## 6. The prediction

About 8.6 s per 25-character task by the keystroke-level model, nearly
all of it typing; Japanese conversion is not modelled. The focused
field saves about 1.7 s on the first task.

## 7. The sketch

    sonouchi    Add tasks · 14 tasks              [Export] [Import]
    New task [ measure spindle runout_                            ]
             Enter add · Esc clear · ⌘Z undo, ⇧⌘Z redo (when empty)
    Added today (3)
    ▶ measure spindle runout                                 08:31
      order dial gauge holder                                08:30

## 8. The build

Built on 2026-10-18: `sonouchi.core.tasks`, `sonouchi.core.keys`,
`sonouchi.core.history`, the view and the shell. 16 tests, run on the
JVM and Node.

A scripted run in headless Chrome typed an English and a Japanese
title, pasted two lines, undid, redid and reloaded. Every step showed
what it should, and each act took 4 to 7 ms on the development build.

Not yet tried: real Japanese input conversion (the script inserted the
text directly), and other browsers.

## 9. The trial

Use it for real work from 2026-10-19 for at least a week. Record in the
friction log below, with the date:

- anything slow, surprising or missing
- whether Enter during kanji conversion ever adds a task by mistake
- whether undo ever does something unexpected (each undo is also in the
  log, as a count of errors)
- the times shown in the footer, especially on the Pi or an old MacBook

Known already: after a redo, the restored tasks are not marked ▶.

### Friction log

- 2026-10-18: (start)
- 2026-10-20: trial paused. Too little UI yet to be worth entering real
  tasks. Entering tasks moves to the board (`notes/2026-10-20-board.md`).
  Japanese input is no longer a requirement (`TODO.md`, "Decided").

## 10. The verdict

After the trial: keep, change or drop, with the reason.
