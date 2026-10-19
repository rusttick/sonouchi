(ns sonouchi.core.tasks-test
  (:require [clojure.test :refer [deftest is testing]]
            [sonouchi.core.history :as history]
            [sonouchi.core.log :as log]
            [sonouchi.core.tasks :as tasks]))

(defn- id
  "A made-up UUID ending in the digits of n."
  [n]
  (let [digits (str n)]
    (parse-uuid (str "00000000-0000-0000-0000-"
                     (apply str (repeat (- 12 (count digits)) "0"))
                     digits))))

(defn- recorded
  "act with a made-up id n and time at, as the shell would record it."
  [act n at]
  (assoc act :act/id (id n) :act/at at))

(def ^:private morning #inst "2026-10-19T00:30:00Z")
(def ^:private yesterday #inst "2026-10-18T09:00:00Z")

(deftest titles-are-lines
  (is (= ["measure runout"] (tasks/titles "  measure runout ")))
  (is (= ["a" "b"] (tasks/titles "a\r\n\n  b\n")))
  (is (= [] (tasks/titles "   "))))

(deftest added-today-is-newest-first-in-local-time
  (let [a-log [(recorded (tasks/add-act ["old"] [(id 1)]) 101 yesterday)
               (recorded (tasks/add-act ["first" "second"] [(id 2) (id 3)])
                         102 morning)]]
    (testing "09:30 in Japan is today; 18:00 yesterday is not"
      (is (= ["second" "first"]
             (map :task/title (tasks/added-today a-log morning 540)))))
    (is (= "09:30" (tasks/clock morning 540)))
    (is (= (id 102) (tasks/latest-add a-log)))
    (is (= 3 (tasks/task-count a-log)))))

(deftest undo-and-redo-adding-tasks
  (let [added [(recorded (tasks/add-act ["a"] [(id 1)]) 101 morning)
               (recorded (tasks/add-act ["b"] [(id 2)]) 102 morning)]
        undone (conj added (recorded (history/undo-act added) 103 morning))
        twice (conj undone (recorded (history/undo-act undone) 104 morning))
        redone (conj twice (recorded (history/redo-act twice) 105 morning))]
    (is (= 1 (tasks/task-count undone)) "undo removes the last task")
    (is (= 0 (tasks/task-count twice)) "a second undo removes the one before")
    (is (= 1 (tasks/task-count redone)) "redo puts back the last one undone")
    (is (= {(id 1) {:task/title "a"}} (log/entities redone)))
    (is (nil? (history/undo-act [])) "nothing to undo")
    (is (nil? (history/redo-act added)) "nothing to redo")
    (testing "a new act ends the chance to redo"
      (let [after (conj undone
                        (recorded (tasks/add-act ["c"] [(id 3)]) 104 morning))]
        (is (nil? (history/redo-act after)))))))
