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

   :state/facts
   {:doc "The fact log: every fact recorded, oldest first. Facts are only
  ever appended."
    :schema [:vector map?]}})
