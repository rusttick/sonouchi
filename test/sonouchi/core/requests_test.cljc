(ns sonouchi.core.requests-test
  (:require [clojure.test :refer [deftest is testing]]
            [sonouchi.core.requests :as requests]))

(def ^:private tanaka #uuid "00000000-0000-0000-0000-000000000001")
(def ^:private sato #uuid "00000000-0000-0000-0000-000000000002")
(def ^:private closed #uuid "00000000-0000-0000-0000-000000000003")
(def ^:private p1 #uuid "00000000-0000-0000-0000-000000000011")
(def ^:private p2 #uuid "00000000-0000-0000-0000-000000000012")
(def ^:private w #uuid "00000000-0000-0000-0000-000000000021")

(def ^:private log
  [{:act/at #inst "2026-10-19T01:00:00Z" :act/action :action/add-request
    :act/patches [{:patch/entity tanaka :request/asker "Tanaka" :request/words "the integrated system"
                   :request/asked "2026-10-19"}]}
   {:act/at #inst "2026-10-19T01:01:00Z" :act/action :action/add-request
    :act/patches [{:patch/entity closed :request/asker "Ito" :request/asked "2026-10-19"}]}
   {:act/at #inst "2026-10-22T01:00:00Z" :act/action :action/add-request
    :act/patches [{:patch/entity sato :request/asker "Sato" :request/words "the new front end"
                   :request/asked "2026-10-22"}]}
   {:act/at #inst "2026-10-22T01:02:00Z" :act/action :action/promise
    :act/patches [{:patch/entity p1 :promise/about tanaka :promise/day "2026-11-04"}]}
   {:act/at #inst "2026-10-23T01:00:00Z" :act/action :action/promise
    :act/patches [{:patch/entity p2 :promise/about tanaka :promise/day "2026-11-06"}]}
   {:act/at #inst "2026-10-23T01:01:00Z" :act/action :action/close-request
    :act/patches [{:patch/entity closed :request/closed "2026-10-23"}]}])

(def ^:private entities
  {tanaka {:request/asker "Tanaka" :request/words "the integrated system" :request/asked "2026-10-19"}
   closed {:request/asker "Ito" :request/asked "2026-10-19" :request/closed "2026-10-23"}
   sato {:request/asker "Sato" :request/words "the new front end" :request/asked "2026-10-22"}
   p1 {:promise/about tanaka :promise/day "2026-11-04"}
   p2 {:promise/about tanaka :promise/day "2026-11-06"}
   w {:wait/task :B :wait/reason :person :wait/on "Sato" :wait/began "2026-10-20"}})

(def ^:private board
  "design3's running example: Tanaka asked for the integrated system,
  Sato for the front end alone."
  {:A {:task/title "design" :task/serves #{tanaka} :task/started "2026-10-26"}
   :B {:task/title "build back end" :task/after #{:A} :task/serves #{tanaka}}
   :C {:task/title "build front end" :task/after #{:A} :task/serves #{tanaka sato}}
   :D {:task/title "integrate" :task/after #{:B :C} :task/serves #{tanaka}}
   :E {:task/title "test" :task/after #{:D} :task/serves #{tanaka}}
   :F {:task/title "write it up" :task/finished "2026-10-23" :task/serves #{closed}}})

(def ^:private times
  {:A {:schedule/start-day 0 :schedule/finish-day 3}
   :B {:schedule/start-day 3 :schedule/finish-day 8}
   :C {:schedule/start-day 3 :schedule/finish-day 7}
   :D {:schedule/start-day 8 :schedule/finish-day 10}
   :E {:schedule/start-day 10 :schedule/finish-day 13}})

(deftest open-requests-in-the-order-asked
  (is (= [tanaka sato] (requests/open log entities)) "the closed one is left out")
  (is (= #{tanaka closed sato} (set (keys (requests/all entities))))))

(deftest the-promise-in-force-is-the-latest
  (let [in-force (requests/promises log entities)]
    (is (= "2026-11-06" (get-in in-force [tanaka :promise/day])))
    (is (= #inst "2026-10-23T01:00:00Z" (get-in in-force [tanaka :act/at])))
    (is (nil? (in-force sato)))))

(deftest which-tasks-serve-a-request
  (is (= #{:A :B :C :D :E} (requests/serving board tanaka)))
  (is (= #{:C} (requests/serving board sato)))
  (is (= #{} (requests/serving board p1))))

(deftest when-a-request-is-done
  (is (= 13 (requests/finish-day board times tanaka)))
  (is (= 7 (requests/finish-day board times sato)))
  (is (= :done (requests/finish-day board times closed)) "all its tasks have finished")
  (is (nil? (requests/finish-day board times p1)) "no tasks")
  (is (nil? (requests/finish-day board (dissoc times :E) tanaka)) "a task without dates"))

(deftest slack-against-the-promise
  (let [today "2026-10-26"]
    (is (= -3 (requests/slack today 13 "2026-11-06")) "finishes Wed Nov 11, promised Fri Nov 6")
    (is (= 3 (requests/slack today 7 "2026-11-06")) "finishes Tue Nov 3")
    (is (= 0 (requests/slack today 10 "2026-11-06")) "finishes on the Friday promised")
    (is (= 0 (requests/slack today 10 "2026-11-07")) "a Saturday promised is the Friday before")
    (is (= "can slip 3 working days" (requests/slack-words 3)))
    (is (= "can slip 1 working day" (requests/slack-words 1)))
    (is (= "no slack" (requests/slack-words 0)))
    (is (= "3 working days late" (requests/slack-words -3)))))

(deftest what-to-do-now-for-a-request
  (is (= #{:A} (requests/next-to-do board times tanaka)) "A has started and sets the chain")
  (is (= #{:A} (requests/next-to-do board times sato)) "C waits for A")
  (is (= #{} (requests/next-to-do board times p1)))
  (testing "once A is done, B and C can both be worked on"
    (let [later-board (assoc-in board [:A :task/finished] "2026-10-28")
          later-times (assoc times :B {:schedule/start-day 0 :schedule/finish-day 5}
                             :C {:schedule/start-day 0 :schedule/finish-day 4}
                             :D {:schedule/start-day 5 :schedule/finish-day 7}
                             :E {:schedule/start-day 7 :schedule/finish-day 10})]
      (is (= #{:B :C} (requests/next-to-do later-board later-times tanaka))))))

(deftest a-request-typed-on-one-line
  (is (= {:request/asker "Tanaka" :request/words "the integrated system"}
         (requests/parse-request "Tanaka: the integrated system")))
  (is (= {:request/asker "Tanaka"} (requests/parse-request " Tanaka ")))
  (is (= {:request/asker "Tanaka" :request/words "a: b"} (requests/parse-request "Tanaka: a: b"))
      "only the first colon splits")
  (is (:problem/message (requests/parse-request ": words")))
  (is (:problem/message (requests/parse-request "")))
  (is (= "Tanaka: the integrated system"
         (requests/show-request {:request/asker "Tanaka" :request/words "the integrated system"})))
  (is (= "Tanaka" (requests/show-request {:request/asker "Tanaka"}))))
