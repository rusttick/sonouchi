(ns sonouchi.core.chain-test
  (:require [clojure.test :refer [deftest is]]
            [sonouchi.core.chain :as chain]))

(def running-example
  "The five tasks of design3's running example, by what they come after."
  {:A {}
   :B {:task/after #{:A}}
   :C {:task/after #{:A}}
   :D {:task/after #{:B :C}}
   :E {:task/after #{:D}}})

(deftest the-chain-behind-a-task
  (is (= #{:A :B :C :D} (chain/behind running-example :D)))
  (is (= #{:A} (chain/behind running-example :A)))
  (is (= {:chain/layers [[:A] [:B :C] [:D]]
          :chain/edges #{[:A :B] [:A :C] [:B :D] [:C :D]}}
         (chain/layout running-example :D)))
  (is (= {:chain/layers [[:A]] :chain/edges #{}}
         (chain/layout running-example :A)))
  (is (= [[:A] [:B :C] [:D] [:E]] (:chain/layers (chain/layout running-example :E)))))

(deftest depth-of-every-task
  (is (= {:A 0 :B 1 :C 1 :D 2 :E 3} (chain/depth running-example)))
  (is (= {} (chain/depth {:A {:task/after #{:B}} :B {:task/after #{:A}}})) "a cycle"))

(deftest a-task-that-skips-a-layer
  (let [tasks {:A {} :B {:task/after #{:A}} :C {:task/after #{:A :B}}}]
    (is (= {:chain/layers [[:A] [:B] [:C]]
            :chain/edges #{[:A :B] [:A :C] [:B :C]}}
           (chain/layout tasks :C)))))

(deftest the-whole-graph
  (is (= #{:A :B :C :D :E} (chain/linked running-example)))
  (is (= {:chain/layers [[:A] [:B :C] [:D] [:E]]
          :chain/edges #{[:A :B] [:A :C] [:B :D] [:C :D] [:D :E]}}
         (chain/graph running-example)))
  (is (= {:chain/layers [[:A] [:B]] :chain/edges #{[:A :B]}}
         (chain/graph {:A {} :B {:task/after #{:A}} :alone {} :after-gone {:task/after #{:gone}}}))
      "tasks with no links are left out")
  (is (= {:chain/layers [] :chain/edges #{}} (chain/graph {:A {} :B {}})))
  (is (= [] (:chain/layers (chain/graph {:A {:task/after #{:B}} :B {:task/after #{:A}}}))) "a cycle"))

(deftest ids-that-are-not-tasks-are-left-out
  (is (= {:chain/layers [[:B]] :chain/edges #{}}
         (chain/layout {:B {:task/after #{:gone}}} :B))))
