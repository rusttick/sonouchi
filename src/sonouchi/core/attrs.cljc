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

   ;; Tasks

   :task/title
   {:doc "What is on the post-it: a few words describing the task. Never
  blank."
    :schema [:and string? [:fn #(re-find #"\S" %)]]}

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
   {:doc "Empty the new-task field."
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
