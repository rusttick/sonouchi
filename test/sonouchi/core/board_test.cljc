(ns sonouchi.core.board-test
  (:require [clojure.test :refer [deftest is testing]]
            [sonouchi.core.board :as board]
            [sonouchi.core.estimates :as estimates]
            [sonouchi.core.log :as log]
            [sonouchi.core.schedule :as schedule]
            [sonouchi.core.search :as search]
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

(defn- patch-act
  [action & patches]
  {:act/action action :act/patches (vec patches)})

(def ^:private t1 #inst "2026-10-20T01:00:00Z")
(def ^:private t2 #inst "2026-10-21T01:00:00Z")
(def ^:private t3 #inst "2026-10-22T01:00:00Z")
(def ^:private t4 #inst "2026-10-23T01:00:00Z")

(def ^:private a-log
  [(recorded (tasks/add-act ["one" "two" "three"] [(id 1) (id 2) (id 3)]) 101 t1)
   (recorded (patch-act :action/record-start
                        {:patch/entity (id 1) :task/started "2026-10-20"
                         :task/worked-on #{"2026-10-20"}})
             102 t2)
   (recorded (patch-act :action/record-start
                        {:patch/entity (id 2) :task/started "2026-10-21"
                         :task/worked-on #{"2026-10-21"}})
             103 t3)
   (recorded (patch-act :action/record-finish
                        {:patch/entity (id 1) :task/finished "2026-10-22"
                         :task/worked-on #{"2026-10-20" "2026-10-22"}})
             104 t4)
   (recorded (patch-act :action/begin-wait
                        {:patch/entity (id 21) :wait/task (id 2) :wait/reason :person
                         :wait/on "Tanaka" :wait/began "2026-10-22"})
             105 t4)
   (recorded (patch-act :action/correct-title {:patch/entity (id 3) :task/title "three!"})
             106 t4)
   (recorded (patch-act :action/replace
                        {:patch/entity (id 3) :task/replaced-by #{}})
             107 t4)])

(def ^:private entities (log/entities a-log))

(deftest the-columns
  (is (= #{(id 1) (id 2)} (set (keys (board/tasks entities)))) "a dropped task is off the board")
  (is (= :done (board/column (entities (id 1)))))
  (is (= :in-progress (board/column (entities (id 2)))))
  (is (= :to-do (board/column {:task/title "x"})))
  (is (= [(id 1) (id 2)] (board/ordered a-log entities))
      "one arrived in Done after two arrived in In progress"))

(deftest a-title-correction-does-not-move-a-card
  (let [moved (conj a-log (recorded (patch-act :action/correct-title
                                               {:patch/entity (id 2) :task/title "two!"})
                                    108 #inst "2026-10-24T01:00:00Z"))]
    (is (= [(id 1) (id 2)] (board/ordered moved (log/entities moved))))))

(deftest open-waits-and-search-text
  (let [waits (board/open-waits entities)]
    (is (= "Tanaka" (get-in waits [(id 2) :wait/on])))
    (is (= (id 21) (get-in waits [(id 2) :patch/entity])))
    (is (nil? (waits (id 1))))
    (is (= {(id 1) "one" (id 2) "two Tanaka"} (board/search-texts entities)))
    (testing "typing the name finds the task waiting on that person"
      (let [tokenize #(re-seq #"\w+" %)
            idx (search/index-texts tokenize (board/search-texts entities))]
        (is (= #{(id 2)} (search/matches idx tokenize "tanaka")))
        (is (= #{(id 1)} (search/matches idx tokenize "one")))))))

(deftest recorded-days
  (let [estimated (conj a-log (recorded (estimates/act (id 2) (id 22) 4) 108 t4))
        ents (log/entities estimated)
        days (board/recorded-days ents (estimates/all estimated ents) 540)]
    (is (= #{"2026-10-20" "2026-10-22"} (days (id 1))))
    (is (= #{"2026-10-21" "2026-10-22" "2026-10-23"} (days (id 2)))
        "worked on, the wait began, and estimated on the 23rd")))

(deftest the-schedulers-input
  (testing "the running example from Monday 26 October, with A finished and B started"
    (let [board {:A {:task/title "A" :task/started "2026-10-21" :task/finished "2026-10-23"}
                 :B {:task/title "B" :task/after #{:A} :task/started "2026-10-23"}
                 :C {:task/title "C" :task/after #{:A}}
                 :D {:task/title "D" :task/after #{:B :C}}
                 :E {:task/title "E" :task/after #{:D}}
                 :F {:task/title "F" :task/after #{:E}}
                 :G {:task/title "G"}}
          latest {:B {:estimate/amount 5} :C {:estimate/amount 4} :D {:estimate/amount 2}
                  :F {:estimate/amount 1}}
          input (board/scheduler-input board latest "2026-10-26")]
      (is (= {:E :E :F :E :G :G} (board/not-estimated board latest))
          "E has no estimate, F comes after E, G has none")
      (is (= {:task/after #{} :task/actual-start-day -3 :task/actual-finish-day 0}
             (:A input)) "A ran Wednesday to Friday, finishing at the start of Monday")
      (is (= {:task/after #{:A} :task/actual-start-day -1 :task/estimate-days 5}
             (:B input)))
      (is (= {:task/after #{:A} :task/estimate-days 4} (:C input)))
      (is (= #{:A :B :C :D} (set (keys input))))
      (let [{:schedule/keys [times critical]} (schedule/schedule input)]
        (is (= [-1 4] ((juxt :schedule/start-day :schedule/finish-day) (:B times))))
        (is (= [0 4] ((juxt :schedule/start-day :schedule/finish-day) (:C times))))
        (is (= [4 6] ((juxt :schedule/start-day :schedule/finish-day) (:D times))))
        (is (= #{:A :B :C :D} critical))
        (is (= "2026-10-26" (board/start-day "2026-10-26" 0)))
        (is (= "2026-10-30" (board/start-day "2026-10-26" 4)) "D starts Friday")
        (is (= "2026-11-02" (board/last-day "2026-10-26" 6)) "D ends the Monday after")
        (is (= "2026-10-23" (board/start-day "2026-10-26" -1)) "B started Friday"))))
  (testing "a weekend today counts from Monday"
    (is (= "2026-10-26" (board/day-zero "2026-10-24")))))
