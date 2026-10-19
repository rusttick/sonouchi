(ns sonouchi.core.history
  "Undo and redo, worked out from the log alone. Undo appends an act that
  undoes the latest act not yet undone; redo appends an act that undoes
  the latest undo. A new act of any other kind ends the chance to redo,
  as in any editor."
  (:require [sonouchi.core.log :as log]))

(defn- step
  [{:keys [done undone] :as stacks} {:act/keys [id action]}]
  (case action
    :action/undo {:done (if (seq done) (pop done) done)
                  :undone (conj undone id)}
    :action/redo {:done (conj done id)
                  :undone (if (seq undone) (pop undone) undone)}
    (assoc stacks :done (conj done id) :undone [])))

(defn- stacks
  [log]
  (reduce step {:done [] :undone []} log))

(defn- undoing
  [log action target]
  (when target
    {:act/action action
     :act/undoes target
     :act/patches (log/undo-patches log target)}))

(defn undo-act
  "The act, without :act/id and :act/at, that undoes the latest act in
  log not yet undone. nil when there is nothing to undo."
  [log]
  (undoing log :action/undo (peek (:done (stacks log)))))

(defn redo-act
  "The act, without :act/id and :act/at, that redoes the latest undo in
  log. nil when there is nothing to redo."
  [log]
  (undoing log :action/redo (peek (:undone (stacks log)))))
