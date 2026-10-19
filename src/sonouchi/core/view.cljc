(ns sonouchi.core.view
  "The view: a pure function from the state to hiccup, which the shell
  renders with Replicant. Event handlers in the hiccup are data (action
  vectors), never functions."
  (:require [sonouchi.core.log :as log]))

(defn- problem-list
  [problems]
  [:ul.problems
   (for [{:problem/keys [path message]} problems]
     [:li (str message (when (seq path) (str " at " (pr-str path))))])])

(defn root
  "The whole page for state, a map holding :state/log and optionally
  :state/problems."
  [{:state/keys [log problems]}]
  [:main
   [:h1 "sonouchi"]
   [:p (str (count log) " acts recorded, "
            (count (log/entities log)) " entities")]
   [:p
    [:button {:on {:click [:command/export]}} "Export to EDN"]
    " "
    [:label "Import from EDN: "
     [:input {:type "file" :accept ".edn"
              :on {:change [:command/import]}}]]]
   (when (seq problems)
     (problem-list problems))])
