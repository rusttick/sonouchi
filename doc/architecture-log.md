# Architecture: the log, and undo

`sonouchi.core.log` and `sonouchi.core.history`. The overview is in
[architecture.md](architecture.md).

## The log

A vector of acts, oldest first, only ever appended. The order of the
vector is the order of the acts.

```clojure
{:act/id      #uuid "…"          ; made by the shell
 :act/at      #inst "…"          ; when it was recorded, by the shell's clock
 :act/action  :action/add-task   ; which action made it
 :act/patches [{:patch/entity #uuid "…"
                :task/title   "measure spindle runout"}]}
```

- **A patch** is the attributes of one entity that the act changed.
  `:patch/entity` is the entity's id. Every other key is the entity's
  new value for that attribute, and replaces the old one, sets
  included. `:patch/retract`, a set of attribute names, removes
  attributes after the values are applied.
- **`:act/by`** is left out while there is one user: an act without it
  was made by the owner.
- **Ids** are random UUIDs made by the shell, so two exported logs can
  be merged without clashes.

## Entities

`log/entities` merges every patch in order, and returns a map from
entity id to its current map. An entity whose attributes have all been
removed is left out. Every query about the work starts from this map or
from the log itself.

## Undo

`log/undo-patches` gives the patches that undo one act: each attribute
the act changed goes back to its value just before the act, or is
removed if it had none. An attribute that a later act changed again is
left as it is, so undoing an old act never overwrites newer work.

`sonouchi.core.history` decides which act to undo, from the log alone:

- undo appends an `:action/undo` act that undoes the latest act not yet
  undone, and names it in `:act/undoes`
- redo appends an `:action/redo` act that undoes the latest undo
- any other new act ends the chance to redo, as in any editor

It replays the log into two stacks, done and undone, and reads the top
of each. `history/undo-act` and `history/redo-act` return the act to
append, without its id and time, or nil when there is nothing to do.

## Reading a log from a file

`log/problems` says why a value is not a log, as a vector of problems,
each with `:problem/path` and `:problem/message`. It checks that the
log is a vector of maps, that each act has a UUID id, a time, an action
and a vector of patches, that each patch has a UUID entity and a
well-formed retract set, and that no act id appears twice. The shell
loads only a log with no problems.
