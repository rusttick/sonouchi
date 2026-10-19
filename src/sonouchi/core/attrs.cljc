(ns sonouchi.core.attrs
  "Every attribute this program uses, defined once.

  An attribute is a qualified keyword whose meaning holds in any map it
  appears in. bin/check fails on a qualified keyword that is not listed
  here, so a misspelt key cannot pass silently.

  Entries are only ever added. Never remove one or change its meaning;
  if the meaning changes, add a new name. Only the owner changes this
  file.

  These are the prototype's attributes. Which of them soonish adopts is
  decided later (TODO.md, section 4).

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
   {:doc "The :act/id of the act that this act undoes."
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
    :schema [:tuple [:= :command/import]]}})
