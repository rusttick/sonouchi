(ns sonouchi.core.attrs
  "Every attribute this program uses, defined once.

  An attribute is a qualified keyword whose meaning holds in any map it
  appears in. bin/check fails on a qualified keyword that is not listed
  here, so a misspelt key cannot pass silently.

  Entries are only ever added. Never remove one or change its meaning;
  if the meaning changes, add a new name. Only the owner changes this
  file.

  :schema is written in Malli syntax. It is data, and nothing enforces
  it yet.")

(def registry
  "Attribute to {:doc string, :schema Malli form}."
  {;; The state the shell holds

   :state/log
   {:doc "The fact log: every act recorded, oldest first. Acts are only
  ever appended, and the order of the log is the order of the acts."
    :schema [:vector map?]}

   :state/problems
   {:doc "Problems the owner should see, such as why an imported file
  could not be read. Each is a map with :problem/message."
    :schema [:vector map?]}

   :state/platform
   {:doc "Which keyboard conventions apply: :mac, or :other for Windows
  and Linux. Set by the shell."
    :schema [:enum :mac :other]}

   :state/now
   {:doc "The shell's clock when the state last changed. The core reads
  the time only from here."
    :schema inst?}

   :state/utc-offset-minutes
   {:doc "How many minutes local time is ahead of UTC, such as 540 in
  Japan. Set by the shell, so that the core can show local times."
    :schema int?}

   :state/last-act-ms
   {:doc "How long the last act took, from the key press to the next
  painted frame, in milliseconds (V12)."
    :schema number?}

   ;; The screen's own state, in the state map and not in the log

   :state/screen
   {:doc "Which screen is shown: :stand-up, :replace or :schedule."
    :schema [:enum :stand-up :replace :schedule]}

   :state/query
   {:doc "The text in the query bar."
    :schema string?}

   :state/recording-day
   {:doc "The day the stand-up keys record, as an ISO day. The last
  working day before today unless the owner moved it."
    :schema string?}

   :state/cursor
   {:doc "The id of the task under the cursor."
    :schema uuid?}

   :state/marked
   {:doc "The ids of the marked tasks, for replacement or for comes
  after."
    :schema [:set uuid?]}

   :state/draft
   {:doc "The replacement being typed: the old tasks' ids under
  :draft/old, and the new lines under :draft/lines."
    :schema map?}

   :draft/old
   {:doc "The ids of the tasks being replaced. Empty when adding."
    :schema [:set uuid?]}

   :draft/lines
   {:doc "The new tasks being typed, in order, each with :draft/title,
  :draft/state and :draft/estimate-text."
    :schema [:vector map?]}

   :draft/title
   {:doc "A new task's title as typed so far."
    :schema string?}

   :draft/state
   {:doc "A new task's state: :to-do, :in-progress or :done."
    :schema [:enum :to-do :in-progress :done]}

   :draft/estimate-text
   {:doc "A new task's estimate as typed so far. Empty for none."
    :schema string?}

   :state/field
   {:doc "The one small field open on a card: which kind under
  :field/kind, the task under :field/task, and the text under
  :field/text."
    :schema map?}

   :field/kind
   {:doc "What the open field is for: :title, :wait or :estimate."
    :schema [:enum :title :wait :estimate]}

   :field/task
   {:doc "The id of the task the open field is on."
    :schema uuid?}

   :field/text
   {:doc "The text typed into the open field so far."
    :schema string?}

   :state/index
   {:doc "The search index of task titles, as sonouchi.core.search
  builds it."
    :schema map?}

   ;; Tasks

   :task/title
   {:doc "What is on the post-it: a few words describing the task. Never
  blank."
    :schema [:and string? [:fn #(re-find #"\S" %)]]}

   ;; Tasks, as the board records them. Days are ISO strings, such as
   ;; "2026-10-26".

   :task/started
   {:doc "The day the task started, as an ISO day."
    :schema string?}

   :task/finished
   {:doc "The day the task finished, as an ISO day."
    :schema string?}

   :task/worked-on
   {:doc "The days the owner worked on the task, as ISO days. Recording
  a start or a finish on a day also puts the day here."
    :schema [:set string?]}

   :task/replaced-by
   {:doc "The tasks that replaced this one. An empty set means it was
  dropped. A task with this attribute has left the board."
    :schema [:set uuid?]}

   :task/kind
   {:doc "An optional kind label from a small fixed list, as design3's
  Step 0 records and its Step 2 needs. No screen in version 0 sets it."
    :schema keyword?}

   ;; Estimates: an entity per estimate, so that every one is kept

   :estimate/task
   {:doc "The task this estimate is of."
    :schema uuid?}

   :estimate/amount
   {:doc "The number the owner typed: the working days they think are
  equally likely to be too many or too few. Positive."
    :schema [:and number? [:> 0]]}

   :estimate/unit
   {:doc "The unit of :estimate/amount. Always :working-days; stored
  because the unit changes the estimate (Jørgensen 2015)."
    :schema [:enum :working-days]}

   ;; Waits: an entity per wait, so that each keeps its own days

   :wait/task
   {:doc "The task that waits."
    :schema uuid?}

   :wait/reason
   {:doc "Why the task waits, in design3's three categories for a late
  start: :person, :outside-party, or :not-in-plan, meaning something
  else came first."
    :schema [:enum :person :outside-party :not-in-plan]}

   :wait/on
   {:doc "On whom the task waits, in the owner's words. Absent when the
  reason is :not-in-plan."
    :schema string?}

   :wait/began
   {:doc "The day the wait began, as an ISO day."
    :schema string?}

   :wait/ended
   {:doc "The day the wait ended, as an ISO day. Absent while it lasts."
    :schema string?}

   ;; Tasks, as the scheduler reads them. Days are working days counted
   ;; from day 0 of the project.

   :task/estimate-days
   {:doc "Working days that the person doing the task thinks are equally
  likely to be too many or too few: a median."
    :schema [:and number? [:> 0]]}

   :task/after
   {:doc "Ids of the tasks that must finish before this one can start."
    :schema [:set any?]}

   :task/actual-start-day
   {:doc "The working day on which the task actually started, counted
  from day 0 of the project."
    :schema [:and number? [:>= 0]]}

   :task/actual-finish-day
   {:doc "The working day on which the task actually finished, counted
  from day 0 of the project."
    :schema [:and number? [:>= 0]]}

   ;; Schedules, as computed from tasks

   :schedule/start-day
   {:doc "The computed working day on which a task starts."
    :schema number?}

   :schedule/finish-day
   {:doc "The computed working day on which a task finishes."
    :schema number?}

   :schedule/times
   {:doc "Task id to that task's :schedule/start-day and
  :schedule/finish-day."
    :schema [:map-of any? map?]}

   :schedule/order
   {:doc "Task ids in an order where every task comes after the tasks it
  waits for."
    :schema [:vector any?]}

   :schedule/cycle
   {:doc "Ids of the tasks on a dependency cycle, or waiting behind one,
  so that no order exists."
    :schema [:set any?]}

   :schedule/unknown-tasks
   {:doc "Ids named in some :task/after that are not tasks."
    :schema [:set any?]}

   :schedule/critical
   {:doc "The ids of the tasks on the critical path: the chain that sets
  the last finish."
    :schema [:set any?]}

   :schedule/slack-days
   {:doc "How many working days a task can move without moving the last
  finish. 0 on the critical path."
    :schema number?}

   ;; The chain behind a task, as sonouchi.core.chain lays it out

   :chain/layers
   {:doc "The tasks behind one task in layers, left to right: a vector
  of vectors of task ids, each layer in its drawing order. The last
  layer holds the task itself."
    :schema [:vector [:vector any?]]}

   :chain/edges
   {:doc "The comes-after links among the tasks in :chain/layers, as a
  set of [before after] pairs of task ids."
    :schema [:set [:tuple any? any?]]}

   ;; Actions: what made an act (:act/action)

   :action/add-task
   {:doc "Adds tasks, one patch per task, each with a :task/title."
    :schema keyword?}

   :action/undo
   {:doc "Undoes the act named in :act/undoes, the latest one not yet
  undone."
    :schema keyword?}

   :action/redo
   {:doc "Undoes the undo named in :act/undoes, which puts back what it
  removed."
    :schema keyword?}

   ;; Actions of the stand-up board and the replacement display

   :action/record-start
   {:doc "The task started on the recording day: sets :task/started and
  adds the day to :task/worked-on."
    :schema keyword?}

   :action/record-finish
   {:doc "The task finished on the recording day: sets :task/finished
  and adds the day to :task/worked-on."
    :schema keyword?}

   :action/record-worked-on
   {:doc "The owner worked on the task on the recording day: adds the
  day to :task/worked-on."
    :schema keyword?}

   :action/begin-wait
   {:doc "A wait begins: a new wait entity for the task, with its reason,
  on whom, and the recording day as :wait/began."
    :schema keyword?}

   :action/end-wait
   {:doc "A wait ends: sets the wait's :wait/ended to the recording day."
    :schema keyword?}

   :action/replace
   {:doc "Replaces N tasks by M new ones in one act: each new task with
  its title, state and estimate; each old task's :task/replaced-by; and
  the :task/after of every task that came after an old task, with the
  old task swapped for its replacements."
    :schema keyword?}

   :action/correct-title
   {:doc "Corrects a task's wording: sets a new :task/title. The task,
  its history and its estimate are kept."
    :schema keyword?}

   :action/estimate
   {:doc "Estimates a task: a new estimate entity. The estimate counts
  for design3 only if made before the task started."
    :schema keyword?}

   :action/set-after
   {:doc "Sets a task's whole :task/after set, or retracts it when the
  set is empty. Not recorded if it would make a cycle."
    :schema keyword?}

   ;; Acts: one per thing the owner does that changes the record

   :act/id
   {:doc "The act's id, made by the shell."
    :schema uuid?}

   :act/at
   {:doc "When the act was recorded, by the shell's clock."
    :schema inst?}

   :act/by
   {:doc "Who made the act. Left out while sonouchi has one user: an act
  without :act/by was made by the owner."
    :schema string?}

   :act/action
   {:doc "Which action made the act, such as :action/add-task."
    :schema qualified-keyword?}

   :act/patches
   {:doc "What the act changed: one patch per entity it changed."
    :schema [:vector map?]}

   :act/undoes
   {:doc "The :act/id of the act that this act undoes. A redo undoes an
  undo."
    :schema uuid?}

   ;; Patches: the attributes of one entity that an act changed

   :patch/entity
   {:doc "The id of the entity the patch is about, made by the shell.
  Every other attribute in the patch is that entity's new value for it,
  sets included."
    :schema uuid?}

   :patch/retract
   {:doc "Attributes the patch removes from the entity. They are removed
  after the patch's values are applied."
    :schema [:set qualified-keyword?]}

   ;; Problems, described as data

   :problem/path
   {:doc "Where the problem is, as a get-in path into the value checked."
    :schema [:vector any?]}

   :problem/message
   {:doc "What is wrong, in words for the owner."
    :schema string?}

   ;; Commands: things the owner asks for that change no record

   :command/export
   {:doc "Save the log to an EDN file."
    :schema [:tuple [:= :command/export]]}

   :command/import
   {:doc "Replace the log with one read from an EDN file."
    :schema [:tuple [:= :command/import]]}

   :command/add
   {:doc "Add the text in the new-task field as tasks, one per line."
    :schema keyword?}

   :command/clear
   {:doc "Escape: close the open field, else leave the replacement
  display, else clear the query."
    :schema keyword?}

   :command/undo
   {:doc "Undo the latest act not yet undone."
    :schema keyword?}

   :command/redo
   {:doc "Redo the latest undo."
    :schema keyword?}

   :command/paste
   {:doc "Text pasted into the new-task field. Several lines become
  several tasks."
    :schema [:tuple [:= :command/paste]]}

   ;; Commands of the stand-up board, the schedule and the replacement
   ;; display (ux-journey, Stages 3 to 5)

   :command/cursor-up
   {:doc "Move the cursor to the card above."
    :schema keyword?}

   :command/cursor-down
   {:doc "Move the cursor to the card below."
    :schema keyword?}

   :command/cursor-left
   {:doc "Move the cursor to the column on the left."
    :schema keyword?}

   :command/cursor-right
   {:doc "Move the cursor to the column on the right."
    :schema keyword?}

   :command/mark
   {:doc "Mark the task under the cursor, or unmark it (x)."
    :schema keyword?}

   :command/query
   {:doc "Move to the query bar (/)."
    :schema keyword?}

   :command/day-back
   {:doc "Move the recording day one working day back ([)."
    :schema keyword?}

   :command/day-forward
   {:doc "Move the recording day one working day forward (])."
    :schema keyword?}

   :command/show-stand-up
   {:doc "Show the stand-up board (1)."
    :schema keyword?}

   :command/show-schedule
   {:doc "Show the schedule (2)."
    :schema keyword?}

   :command/open-replace
   {:doc "Open the replacement display for the marked tasks, or the task
  under the cursor (r); from the query bar, with no old tasks and the
  query as the first title."
    :schema keyword?}

   :command/open-estimate
   {:doc "Open the estimate field on the task under the cursor (t)."
    :schema keyword?}

   :command/open-wait
   {:doc "Open the wait field on the task under the cursor (b), when it
  has no open wait."
    :schema keyword?}

   :command/open-title
   {:doc "Make the title of the task under the cursor editable (e)."
    :schema keyword?}

   :command/record
   {:doc "Record what the open field or the replacement display holds,
  as one act (Enter in a field, Cmd+Enter or Ctrl+Enter in the
  replacement display)."
    :schema keyword?}

   :command/next-line
   {:doc "Start the next new-task line in the replacement display
  (Enter)."
    :schema keyword?}

   :command/toggle-estimate
   {:doc "Move between a line's title and its estimate in the
  replacement display (Tab)."
    :schema keyword?}

   :command/state-up
   {:doc "Move the current line's state one step towards To do
  (Alt+Up)."
    :schema keyword?}

   :command/state-down
   {:doc "Move the current line's state one step towards Done
  (Alt+Down)."
    :schema keyword?}

   :command/reason-person
   {:doc "In the wait field: the reason is a person (1)."
    :schema keyword?}

   :command/reason-outside-party
   {:doc "In the wait field: the reason is an outside party (2)."
    :schema keyword?}

   :command/reason-not-in-plan
   {:doc "In the wait field: something else came first (3). Records at
  once, since there is no one to name."
    :schema keyword?}

   :command/typed
   {:doc "Text typed into an input: the query bar (:query), the open
  field on a card (:field), or a line of the replacement display
  ([:line i :title] or [:line i :estimate]). Carries the target and the
  whole text, so that every typed character is in the state."
    :schema [:tuple [:= :command/typed] any? string?]}

   :command/cursor
   {:doc "Move the cursor to the task with this id, as a click on its
  card or row does."
    :schema [:tuple [:= :command/cursor] uuid?]}

   ;; Key presses, as the shell describes them to the core

   :key/name
   {:doc "The key's name as the browser gives it, such as \"z\" or
  \"Enter\"."
    :schema string?}

   :key/ctrl
   {:doc "True when Control was held."
    :schema boolean?}

   :key/meta
   {:doc "True when Command (macOS) or the Windows key was held."
    :schema boolean?}

   :key/alt
   {:doc "True when Alt (Option on macOS) was held."
    :schema boolean?}

   :key/shift
   {:doc "True when Shift was held."
    :schema boolean?}})
