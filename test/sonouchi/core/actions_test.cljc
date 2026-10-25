(ns sonouchi.core.actions-test
  (:require [clojure.test :refer [deftest is testing]]
            [sonouchi.core.actions :as actions]
            [sonouchi.core.board :as board]
            [sonouchi.core.estimates :as estimates]
            [sonouchi.core.log :as log]
            [sonouchi.core.screen :as screen]))

(defn- id
  [n]
  (let [digits (str n)]
    (parse-uuid (str "00000000-0000-0000-0000-"
                     (apply str (repeat (- 12 (count digits)) "0"))
                     digits))))

(defn- fresh
  "Ids for the next command: a hundred per act already in the log, so
  that no command reuses one, as the shell's fresh UUIDs never do."
  [state]
  (map id (iterate inc (* 100 (inc (count (:state/log state)))))))

(defn- tokenize
  [text]
  (re-seq #"\S+" text))

(def ^:private monday #inst "2026-10-26T00:30:00Z")

(def ^:private empty-state
  (actions/start {:state/log []
                  :state/platform :mac
                  :state/now monday
                  :state/utc-offset-minutes 540}
                 tokenize))

(defn- run
  "The state after each command in turn."
  [state & commands]
  (reduce (fn [s command] (actions/run s command (fresh s) tokenize)) state commands))

(defn- a-minute-later
  "state with its clock a minute on, so that acts have distinct times."
  [state]
  (update state :state/now
          (fn [at] (let [ms (+ (inst-ms at) 60000)]
                     #?(:clj (java.util.Date. ^long ms) :cljs (js/Date. ms))))))

(defn- add
  "state with tasks of these titles added through the replacement
  display from the query bar, one at a time, a minute apart."
  [state & titles]
  (reduce (fn [s title]
            (run (a-minute-later s)
                 [:command/typed :query title] [:command/open-replace :from-query] [:command/record]))
          state
          titles))

(defn- entities [state] (screen/current state))

(defn- titled
  "The id of the task with title in state."
  [state title]
  (some (fn [[i e]] (when (= title (:task/title e)) i)) (entities state)))

(defn- task [state title] ((entities state) (titled state title)))

(defn- last-act [state] (peek (:state/log state)))

(deftest the-recording-day-and-the-screens
  (is (= "2026-10-26" (screen/today empty-state)))
  (is (= "2026-10-23" (screen/recording-day empty-state)) "the Friday before a Monday")
  (is (= "2026-10-22" (screen/recording-day (run empty-state [:command/day-back]))))
  (is (= "2026-10-26" (screen/recording-day (run empty-state [:command/day-forward]))) "today")
  (is (= "2026-10-26" (screen/recording-day (run empty-state [:command/day-forward] [:command/day-forward])))
      "not past today")
  (is (= :stand-up (:state/screen empty-state)))
  (is (= :schedule (:state/screen (run empty-state [:command/show-schedule])))))

(deftest adding-a-task-from-the-query-bar
  (let [state (add empty-state "measure spindle runout")]
    (is (= "measure spindle runout" (:task/title (task state "measure spindle runout"))))
    (is (= :action/replace (:act/action (last-act state))))
    (is (= (:state/now state) (:act/at (last-act state))) "the state's clock")
    (is (= "" (:state/query state)) "the query is cleared")
    (is (= :stand-up (:state/screen state)))
    (is (= (titled state "measure spindle runout") (:state/cursor state)) "the cursor is on the new task")
    (is (= :to-do (board/column (task state "measure spindle runout"))))
    (testing "the index finds it"
      (is (= #{(titled state "measure spindle runout")}
             (screen/matching (assoc state :state/query "spin") tokenize (entities state)))))))

(deftest the-stand-up-keys
  (let [state (-> (add empty-state "one" "two")
                  (run [:command/cursor-down]))
        on-two (fn [s] (assoc s :state/cursor (titled s "two")))
        started (run (on-two state) [:action/record-start])
        finished (run started [:action/record-finish])
        worked (run (on-two state) [:action/record-worked-on])]
    (is (= "2026-10-23" (:task/started (task started "two"))))
    (is (= #{"2026-10-23"} (:task/worked-on (task started "two"))))
    (is (= :in-progress (board/column (task started "two"))))
    (is (= "2026-10-23" (:task/finished (task finished "two"))))
    (is (= :done (board/column (task finished "two"))))
    (is (= #{"2026-10-23"} (:task/worked-on (task worked "two"))))
    (is (nil? (:task/started (task worked "two"))) "worked on does not start")
    (testing "finishing an unstarted task starts it too"
      (let [f (run (on-two state) [:action/record-finish])]
        (is (= "2026-10-23" (:task/started (task f "two"))))))
    (testing "the recording day moved back records that day"
      (let [s (run (on-two state) [:command/day-back] [:action/record-start])]
        (is (= "2026-10-22" (:task/started (task s "two"))))))
    (testing "undo puts it back, in one act"
      (let [undone (run started [:command/undo])]
        (is (nil? (:task/started (task undone "two"))))
        (is (= :action/undo (:act/action (last-act undone))))))))

(deftest the-cursor-moves-over-the-columns
  (let [state (add empty-state "a" "b" "c")
        a (titled state "a") b (titled state "b") c (titled state "c")
        started-b (run (assoc state :state/cursor b) [:action/record-start])]
    (is (= c (:state/cursor state)) "the last added is first in To do")
    (is (= b (:state/cursor (run state [:command/cursor-down]))))
    (is (= a (:state/cursor (run state [:command/cursor-down] [:command/cursor-down] [:command/cursor-down])))
        "stops at the bottom")
    (is (= b (:state/cursor (run (assoc started-b :state/cursor c) [:command/cursor-right])))
        "right moves to In progress")
    (is (= b (:state/cursor (run (assoc started-b :state/cursor c) [:command/cursor-right] [:command/cursor-right])))
        "Done is empty, so it stays")
    (is (= c (:state/cursor (run started-b [:command/cursor-left]))))
    (is (= a (:state/cursor (run state [:command/cursor a]))) "a click")))

(deftest estimating-with-the-field
  (let [state (add empty-state "one")
        opened (run state [:command/open-estimate])]
    (is (= :estimate (get-in opened [:state/field :field/kind])))
    (is (= "" (get-in opened [:state/field :field/text])))
    (testing "a bad number stays in the field with the reason"
      (let [bad (run opened [:command/typed :field "zero"] [:command/record])]
        (is (:state/field bad))
        (is (string? (get-in bad [:state/field :problem/message])))
        (is (= 1 (count (:state/log bad))) "nothing recorded")))
    (testing "a good number is recorded and the field closes"
      (let [good (run opened [:command/typed :field "3"] [:command/record])
            latest (screen/latest-estimates good (entities good))]
        (is (nil? (:state/field good)))
        (is (= :action/estimate (:act/action (last-act good))))
        (is (= 3 (get-in latest [(titled good "one") :estimate/amount])))
        (is (= :working-days (get-in latest [(titled good "one") :estimate/unit])))))
    (testing "Escape closes the field without recording"
      (let [closed (run opened [:command/typed :field "3"] [:command/clear])]
        (is (nil? (:state/field closed)))
        (is (= 1 (count (:state/log closed))))))))

(deftest correcting-the-wording
  (let [state (add empty-state "mesure runout")
        opened (run state [:command/open-title])]
    (is (= "mesure runout" (get-in opened [:state/field :field/text])) "starts with the title")
    (let [fixed (run opened [:command/typed :field "measure runout"] [:command/record])]
      (is (= "measure runout" (:task/title (task fixed "measure runout"))))
      (is (= :action/correct-title (:act/action (last-act fixed))))
      (is (= #{(titled fixed "measure runout")}
             (screen/matching (assoc fixed :state/query "meas") tokenize (entities fixed)))
          "the index follows the correction"))
    (is (= 1 (count (:state/log (run opened [:command/record])))) "unchanged wording records nothing")))

(deftest waiting-on-someone
  (let [state (add empty-state "one")
        opened (run state [:command/open-wait])]
    (is (= :wait (get-in opened [:state/field :field/kind])))
    (testing "a person, named"
      (let [waiting (run opened [:command/reason-person] [:command/typed :field "Tanaka"] [:command/record])
            wait (get (board/open-waits (entities waiting)) (titled waiting "one"))]
        (is (= :person (:wait/reason wait)))
        (is (= "Tanaka" (:wait/on wait)))
        (is (= "2026-10-23" (:wait/began wait)))
        (is (= :to-do (board/column (task waiting "one"))) "the task keeps its column")
        (is (= #{(titled waiting "one")}
               (screen/matching (assoc waiting :state/query "tana") tokenize (entities waiting)))
            "the name finds the task")
        (testing "b again ends the wait"
          (let [ended (run waiting [:command/open-wait])]
            (is (empty? (board/open-waits (entities ended))))
            (is (= :action/end-wait (:act/action (last-act ended))))
            (is (empty? (screen/matching (assoc ended :state/query "tana") tokenize (entities ended)))
                "the name no longer finds it")))))
    (testing "not in the plan records at once"
      (let [waiting (run opened [:command/reason-not-in-plan])
            wait (get (board/open-waits (entities waiting)) (titled waiting "one"))]
        (is (nil? (:state/field waiting)))
        (is (= :not-in-plan (:wait/reason wait)))
        (is (not (contains? wait :wait/on)))))
    (testing "Enter without a reason or a name says so"
      (is (get-in (run opened [:command/record]) [:state/field :problem/message]))
      (is (get-in (run opened [:command/reason-person] [:command/record]) [:state/field :problem/message])))))

(deftest comes-after
  (let [state (add empty-state "a" "b" "c")
        a (titled state "a") b (titled state "b") c (titled state "c")
        b-after-a (run state [:command/cursor a] [:command/mark] [:command/cursor b] [:action/set-after])]
    (is (= #{a} (:task/after (task b-after-a "b"))))
    (is (empty? (:state/marked b-after-a)) "marks are cleared")
    (testing "a cycle is refused and named"
      (let [refused (run b-after-a [:command/cursor b] [:command/mark] [:command/cursor a] [:action/set-after])]
        (is (nil? (:task/after (task refused "a"))))
        (is (re-find #"cycle" (:problem/message (first (:state/problems refused)))))))
    (testing "a with nothing marked removes the predecessors"
      (let [none (run b-after-a [:command/cursor b] [:action/set-after])]
        (is (not (contains? (task none "b") :task/after)))))
    (testing "a task cannot come after itself"
      (let [self (run state [:command/cursor c] [:command/mark] [:action/set-after])]
        (is (not (contains? (task self "c") :task/after)))))))

(deftest replacing-one-task-by-three
  (let [state (add empty-state "measure spindle runout" "later")
        old-id (titled state "measure spindle runout")
        later-id (titled state "later")
        ;; later comes after old; old started Friday with an estimate of 4
        state (run state
                   [:command/cursor old-id] [:command/mark] [:command/cursor later-id] [:action/set-after]
                   [:command/cursor old-id] [:command/day-back] [:action/record-start] [:command/day-forward]
                   [:command/open-estimate] [:command/typed :field "4"] [:command/record])
        opened (run state [:command/cursor old-id] [:command/open-replace])]
    (is (= :replace (:state/screen opened)))
    (is (= #{old-id} (get-in opened [:state/draft :draft/old])))
    (is (= :in-progress (get-in opened [:state/draft :draft/lines 0 :draft/state])) "the old task's state")
    (let [typed (run opened
                     [:command/typed [:line 0 :title] "order dial gauge holder"]
                     [:command/typed [:line 0 :estimate] "1"]
                     [:command/next-line 0]
                     [:command/typed [:line 1 :title] "measure runout at 3 speeds"]
                     [:command/typed [:line 1 :estimate] "2"]
                     [:command/next-line 1]
                     [:command/typed [:line 2 :title] "compare with the 2024 log"])
          recorded (run typed [:command/record])
          ents (entities recorded)
          new-ids (map #(titled recorded %) ["order dial gauge holder" "measure runout at 3 speeds" "compare with the 2024 log"])
          latest (screen/latest-estimates recorded ents)]
      (is (= [:to-do :to-do] (map :draft/state (rest (get-in typed [:state/draft :draft/lines])))))
      (is (= :stand-up (:state/screen recorded)))
      (is (nil? (:state/draft recorded)))
      (is (= :action/replace (:act/action (last-act recorded))))
      (is (every? some? new-ids))
      (is (= (set new-ids) (:task/replaced-by (ents old-id))))
      (is (not (contains? (board/tasks ents) old-id)) "the old task leaves the board")
      (is (= "2026-10-22" (:task/started (ents (first new-ids)))) "the first line inherits the start")
      (is (not (contains? (ents (first new-ids)) :task/worked-on)) "an inherited start adds no day")
      (is (nil? (:task/started (ents (second new-ids)))))
      (is (= 1 (get-in latest [(first new-ids) :estimate/amount])))
      (is (= 2 (get-in latest [(second new-ids) :estimate/amount])))
      (is (nil? (latest (nth new-ids 2))) "no estimate typed")
      (is (= (set new-ids) (:task/after (ents later-id))) "later now comes after all three")
      (is (= (first new-ids) (:state/cursor recorded)))
      (testing "undo removes the new tasks and puts the old one back"
        (let [undone (entities (run recorded [:command/undo]))]
          (is (contains? (board/tasks undone) old-id))
          (is (not-any? #(contains? undone %) new-ids))
          (is (= #{old-id} (:task/after (undone later-id)))))))))

(deftest dropping-and-pasting
  (let [state (add empty-state "a" "b")
        a (titled state "a") b (titled state "b")
        dropped (run state [:command/cursor b] [:command/mark] [:command/cursor a] [:action/set-after]
                     [:command/cursor b] [:command/open-replace] [:command/record])]
    (is (= #{} (:task/replaced-by ((entities dropped) b))) "dropped")
    (is (not (contains? ((entities dropped) a) :task/after)) "a no longer comes after anything")
    (testing "pasting several lines makes several lines"
      (let [pasted (run empty-state [:command/open-replace :from-query]
                        [:command/typed [:line 0 :title] "x\ny\nz"])]
        (is (= ["x" "y" "z"] (map :draft/title (get-in pasted [:state/draft :draft/lines]))))))
    (testing "a draft with nothing and no old tasks records nothing"
      (let [nothing (run empty-state [:command/open-replace :from-query] [:command/record])]
        (is (empty? (:state/log nothing)))
        (is (= :stand-up (:state/screen nothing)))))))

(deftest the-schedule-rows
  (let [state (add empty-state "A" "B" "C")
        a (titled state "A") b (titled state "B") c (titled state "C")
        estimate (fn [s i n] (run s [:command/cursor i] [:command/open-estimate] [:command/typed :field (str n)] [:command/record]))
        state (-> state (estimate a 3) (estimate b 5)
                  (run [:command/cursor a] [:command/mark] [:command/cursor b] [:action/set-after]
                       [:command/cursor a] [:command/mark] [:command/cursor c] [:action/set-after]))
        ents (entities state)
        latest (screen/latest-estimates state ents)
        {:schedule/keys [times critical]} (screen/scheduled state ents latest)]
    (is (= [a b] (screen/rows state ents latest)) "C has no estimate")
    (is (= {c c} (screen/not-estimated ents latest)))
    (is (= [0 3] ((juxt :schedule/start-day :schedule/finish-day) (times a))))
    (is (= [3 8] ((juxt :schedule/start-day :schedule/finish-day) (times b))))
    (is (= #{a b} critical))
    (testing "the cursor moves along the rows on the schedule"
      (let [s (run state [:command/show-schedule] [:command/cursor a] [:command/cursor-down])]
        (is (= b (:state/cursor s)))
        (is (= b (:state/cursor (run s [:command/cursor-down]))) "stops at the end")))))

(deftest estimates-all-is-what-the-board-reads
  (let [state (add empty-state "one")
        state (run state [:command/open-estimate] [:command/typed :field "2"] [:command/record])]
    (is (= 1 (count (estimates/all (:state/log state) (log/entities (:state/log state))))))))
