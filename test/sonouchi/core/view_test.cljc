(ns sonouchi.core.view-test
  (:require [clojure.test :refer [deftest is]]
            [sonouchi.core.view :as view]))

(defn- texts
  "Every string in a hiccup tree, in order."
  [hiccup]
  (filter string? (tree-seq coll? seq hiccup)))

(deftest root-counts-the-facts
  (is (some #{"0 facts recorded"} (texts (view/root {:state/facts []}))))
  (is (some #{"2 facts recorded"}
            (texts (view/root {:state/facts [{} {}]})))))
