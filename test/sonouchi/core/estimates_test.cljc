(ns sonouchi.core.estimates-test
  (:require [clojure.test :refer [deftest is testing]]
            [sonouchi.core.estimates :as estimates]
            [sonouchi.core.history :as history]
            [sonouchi.core.log :as log]
            [sonouchi.core.tasks :as tasks]))

(defn- id
  [n]
  (let [digits (str n)]
    (parse-uuid (str "00000000-0000-0000-0000-"
                     (apply str (repeat (- 12 (count digits)) "0"))
                     digits))))

(defn- recorded
  [act n at]
  (assoc act :act/id (id n) :act/at at))

(def ^:private monday #inst "2026-10-26T01:00:00Z")
(def ^:private tuesday #inst "2026-10-27T01:00:00Z")

(def ^:private a-log
  [(recorded (tasks/add-act ["a" "b"] [(id 1) (id 2)]) 101 monday)
   (recorded (estimates/act (id 1) (id 11) 3) 102 monday)
   (recorded (estimates/act (id 1) (id 12) 5) 103 tuesday)
   (recorded (estimates/act (id 2) (id 21) 2) 104 tuesday)])

(deftest the-latest-estimate-of-each-task
  (let [all (estimates/all a-log (log/entities a-log))
        latest (estimates/latest all)]
    (is (= 3 (count all)))
    (is (= tuesday (get-in all [(id 12) :act/at])) "an estimate carries the time it was made")
    (is (= 5 (get-in latest [(id 1) :estimate/amount])) "the later one wins")
    (is (= 2 (get-in latest [(id 2) :estimate/amount])))
    (is (= :working-days (get-in latest [(id 2) :estimate/unit])))
    (is (= "5 wd" (estimates/show (latest (id 1)))))))

(deftest an-undone-estimate-is-gone
  (let [undone (conj a-log (recorded (history/undo-act a-log) 105 tuesday))
        latest (estimates/latest (estimates/all undone (log/entities undone)))]
    (is (nil? (latest (id 2))))
    (is (= 5 (get-in latest [(id 1) :estimate/amount])))))

(deftest design3-counts-estimates-made-before-the-start
  (let [made-tuesday {:act/at tuesday}]
    (is (estimates/counts? made-tuesday {} 540) "not started")
    (is (estimates/counts? made-tuesday {:task/started "2026-10-27"} 540) "the same day")
    (is (estimates/counts? made-tuesday {:task/started "2026-10-28"} 540))
    (is (not (estimates/counts? made-tuesday {:task/started "2026-10-26"} 540)) "after the start")
    (testing "the local day decides: 01:00 UTC on Tuesday is still Monday in New York"
      (is (estimates/counts? made-tuesday {:task/started "2026-10-26"} -300)))))

(deftest what-the-owner-typed
  (is (= {:estimate/amount 3} (estimates/parse " 3 ")))
  (is (= {:estimate/amount 2.5} (estimates/parse "2.5")))
  (is (:problem/message (estimates/parse "")))
  (is (:problem/message (estimates/parse "three")))
  (is (:problem/message (estimates/parse "0")))
  (is (:problem/message (estimates/parse "-2"))))
