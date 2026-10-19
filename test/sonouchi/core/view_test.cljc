(ns sonouchi.core.view-test
  (:require [clojure.test :refer [deftest is]]
            [sonouchi.core.tasks :as tasks]
            [sonouchi.core.view :as view]))

(defn- texts
  "Every string in a hiccup tree, in order."
  [hiccup]
  (filter string? (tree-seq coll? seq hiccup)))

(defn- handlers
  "Every action vector under :on in a hiccup tree."
  [hiccup]
  (for [node (tree-seq coll? seq hiccup)
        :when (map? node)
        action (vals (:on node))]
    action))

(def ^:private morning #inst "2026-10-19T00:30:00Z")

(defn- state
  [a-log]
  {:state/log a-log
   :state/platform :mac
   :state/now morning
   :state/utc-offset-minutes 540})

(def ^:private two-tasks
  [(assoc (tasks/add-act ["measure spindle runout" "order dial gauge holder"]
                         [#uuid "00000000-0000-0000-0000-000000000001"
                          #uuid "00000000-0000-0000-0000-000000000002"])
          :act/id #uuid "00000000-0000-0000-0000-0000000000a1"
          :act/at morning)])

(deftest root-shows-the-frame-and-todays-tasks
  (let [shown (set (texts (view/root (state two-tasks))))]
    (is (contains? shown "Add tasks"))
    (is (contains? shown "2 tasks"))
    (is (contains? shown "Added today (2)"))
    (is (contains? shown "measure spindle runout"))
    (is (contains? shown "09:30"))
    (is (contains? shown "▶"))))

(deftest root-shows-the-keys-for-the-platform
  (is (some #(re-find #"⌘Z undo" %) (texts (view/root (state [])))))
  (is (some #(re-find #"Ctrl\+Z undo" %)
            (texts (view/root (assoc (state []) :state/platform :other))))))

(deftest root-handlers-are-data
  (is (= #{[:command/export] [:command/import] [:command/paste]}
         (set (handlers (view/root (state [])))))))

(deftest root-shows-problems
  (is (some #{"an act needs a time at [0 :act/at]"}
            (texts (view/root (assoc (state [])
                                     :state/problems
                                     [{:problem/path [0 :act/at]
                                       :problem/message
                                       "an act needs a time"}]))))))

(deftest root-flags-a-slow-act
  (is (some #{"last act 250 ms: over 0.1 s"}
            (texts (view/root (assoc (state []) :state/last-act-ms 250)))))
  (is (some #{"last act 12 ms"}
            (texts (view/root (assoc (state []) :state/last-act-ms 12))))))
