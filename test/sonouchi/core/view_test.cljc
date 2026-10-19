(ns sonouchi.core.view-test
  (:require [clojure.test :refer [deftest is]]
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

(def ^:private one-act
  [{:act/id #uuid "00000000-0000-0000-0000-0000000000a1"
    :act/at #inst "2026-10-19T08:30:00Z"
    :act/action :command/export
    :act/patches [{:patch/entity #uuid "00000000-0000-0000-0000-000000000017"
                   :problem/message "any attribute"}]}])

(deftest root-counts-acts-and-entities
  (is (some #{"0 acts recorded, 0 entities"}
            (texts (view/root {:state/log []}))))
  (is (some #{"1 acts recorded, 1 entities"}
            (texts (view/root {:state/log one-act})))))

(deftest root-offers-export-and-import-as-data
  (is (= #{[:command/export] [:command/import]}
         (set (handlers (view/root {:state/log []}))))))

(deftest root-shows-problems
  (is (some #{"an act needs a time at [0 :act/at]"}
            (texts (view/root {:state/log []
                               :state/problems
                               [{:problem/path [0 :act/at]
                                 :problem/message "an act needs a time"}]})))))
