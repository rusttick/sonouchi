(ns sonouchi.core.view
  "The view: a pure function from the state to hiccup, which the shell
  renders with Replicant. Event handlers in the hiccup are data (action
  vectors), never functions.")

(defn root
  "The whole page for state, a map holding :state/facts."
  [{:state/keys [facts]}]
  [:main
   [:h1 "sonouchi"]
   [:p (str (count facts) " facts recorded")]])
