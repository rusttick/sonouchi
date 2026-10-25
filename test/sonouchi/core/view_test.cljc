(ns sonouchi.core.view-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [sonouchi.core.actions :as actions]
            [sonouchi.core.screen :as screen]
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

(defn- key-caps
  "The text of every key cap (kbd) in a hiccup tree, in order."
  [hiccup]
  (for [node (tree-seq coll? seq hiccup)
        :when (and (vector? node) (= :kbd (first node)))]
    (last node)))

(defn- nodes-with-class
  [hiccup class-name]
  (for [node (tree-seq coll? seq hiccup)
        :when (and (vector? node) (map? (second node)))
        :let [c (:class (second node))]
        :when (and (string? c) (some #{class-name} (str/split c #" ")))]
    node))

(defn- id
  [n]
  (let [digits (str n)]
    (parse-uuid (str "00000000-0000-0000-0000-"
                     (apply str (repeat (- 12 (count digits)) "0"))
                     digits))))

(defn- fresh
  [state]
  (map id (iterate inc (* 100 (inc (count (:state/log state)))))))

(defn- tokenize [text] (re-seq #"\S+" text))

(def ^:private monday #inst "2026-10-26T00:30:00Z")

(def ^:private empty-state
  (actions/start {:state/log [] :state/platform :mac :state/now monday :state/utc-offset-minutes 540}
                 tokenize))

(defn- run
  [state & commands]
  (reduce (fn [s command] (actions/run s command (fresh s) tokenize)) state commands))

(defn- add
  [state & titles]
  (reduce (fn [s title]
            (run s [:command/typed :query title] [:command/open-replace :from-query] [:command/record]))
          state
          titles))

(defn- titled
  [state title]
  (some (fn [[i e]] (when (= title (:task/title e)) i)) (screen/current state)))

(defn- page [state] (view/root state tokenize))

(deftest the-frame
  (let [shown (set (texts (page (add empty-state "measure spindle runout"))))]
    (is (contains? shown "STAND-UP"))
    (is (contains? shown "1 task") "with no query, how many there are")
    (is (contains? shown "recording last working day,") "Friday, seen on a Monday")
    (is (contains? shown "Fri Oct 23"))
    (is (contains? shown "measure spindle runout")))
  (testing "the mascot opens the menu of screens, which says each one's key, name and purpose"
    (let [closed (page empty-state)
          opened (page (run empty-state [:command/toggle-menu]))
          titles (keep :title (filter map? (tree-seq coll? seq closed)))]
      (is (some #{"Choose a view: 1 Stand-up · 2 Plan · 3 Record"} titles) "the hover text")
      (let [button (first (filter #(and (vector? %) (= :button.app (first %)))
                                  (tree-seq coll? seq closed)))]
        (is (some #{"STAND-UP"} (texts button)) "the screen's name is part of the menu button"))
      (is (some #{[:command/toggle-menu]} (handlers closed)))
      (is (not (some #{[:command/show-schedule]} (handlers closed))) "no buttons for the screens")
      (is (not (some #{"Plan"} (texts closed))))
      (is (= ["1" "Stand-up" "2" "Plan" "3" "Record"]
             (filter #{"1" "2" "3" "Stand-up" "Plan" "Record"} (texts opened))))
      (is (some #(re-find #"^What comes after what" %) (texts opened)))
      (is (some #{[:command/show-stand-up]} (handlers opened)))
      (is (some #{[:command/show-schedule]} (handlers opened)))
      (is (= 1 (count (nodes-with-class opened "current"))) "the screen shown")))
  (testing "the recording day's keys, key first, also buttons, with the day explained on hover"
    (let [hiccup (page empty-state)
          day-node (first (filter #(and (vector? %) (= :span.day (first %))) (tree-seq coll? seq hiccup)))
          buttons (filter #(and (vector? %) (= :button.hint (first %))) (tree-seq coll? seq day-node))]
      (is (= ["[" "]"] (key-caps day-node)))
      (is (= [" earlier" " later"] (map last buttons)))
      (is (some #{[:command/day-back]} (handlers day-node)))
      (is (some #{[:command/day-forward]} (handlers day-node)))
      (is (= (str "Recording day: s started, f finished, w worked on and b waiting are saved for this day. "
                  "It starts on the last working day. [ earlier catches up on missed days; ] later comes back.")
             (:title (second day-node))))
      (is (not-any? :disabled (map second buttons)) "Friday can move later, to Monday")
      (let [at-today (page (run empty-state [:command/day-forward]))
            later (last (filter #(and (vector? %) (= :button.hint (first %)))
                                (tree-seq coll? seq at-today)))]
        (is (:disabled (second later)) "today cannot move later"))))
  (testing "a recording day more than a working day back is flagged"
    (is (seq (nodes-with-class (page (run empty-state [:command/day-back])) "stale")))
    (is (empty? (nodes-with-class (page empty-state) "stale")))))

(deftest the-board
  (let [state (add empty-state "one" "two")
        one (titled state "one")
        worked (run state [:command/cursor one] [:action/record-worked-on])
        hiccup (page worked)
        shown (set (texts hiccup))]
    (is (= ["To do" "In progress" "Done"] (filter #{"To do" "In progress" "Done"} (texts hiccup))))
    (is (contains? shown "no estimate"))
    (is (= 1 (count (nodes-with-class hiccup "recorded"))) "one has its record for the day")
    (is (= 1 (count (nodes-with-class hiccup "cursor"))))
    (is (contains? shown "✓"))
    (testing "the keys beside the cursor, each in a key cap and before what it does"
      (is (= ["s" "f" "w" "b" "e" "t" "a" "x" "r"]
             (key-caps (first (nodes-with-class hiccup "cursor")))))
      (is (= [:span.hint [:kbd "s"] " started"]
             (first (filter #(and (vector? %) (= :span.hint (first %))) (tree-seq coll? seq hiccup))))))
    (is (= ["[" "]"] (take-last 2 (key-caps (first (filter #(and (vector? %) (= :header (first %)))
                                                           (tree-seq coll? seq hiccup))))))
        "the recording day's keys")
    (is (some #{[:command/cursor one]} (handlers hiccup)) "a click moves the cursor")
    (testing "a waiting task shows on whom, in its column"
      (let [waiting (run state [:command/cursor one] [:command/open-wait] [:command/reason-person]
                         [:command/typed :field "Tanaka"] [:command/record])
            t (texts (page waiting))]
        (is (some #{"waiting on Tanaka since Fri Oct 23"} t))
        (is (= 2 (count (filter #{"no estimate"} t))))))
    (testing "the query hides what does not match"
      (let [t (set (texts (page (run state [:command/typed :query "tw"]))))]
        (is (contains? t "two"))
        (is (not (contains? t "one")))
        (is (contains? t "1 of 2 tasks"))))))

(deftest the-fields
  (let [state (add empty-state "one")
        estimate (page (run state [:command/open-estimate]))
        wait (page (run state [:command/open-wait]))
        chosen (page (run state [:command/open-wait] [:command/reason-person]))]
    (is (some #{" working days, as likely more as less"} (texts estimate)))
    (is (some #{[:command/typed :field :field/text]} (handlers estimate)))
    (is (some #(re-find #"1 person · 2 outside party · 3 not in the plan" %)
              (keep :placeholder (filter map? (tree-seq coll? seq wait)))))
    (is (some #{"a person"} (texts chosen)))
    (testing "a problem is said in the field"
      (is (some #{"an estimate is a number of working days"}
                (texts (page (run state [:command/open-estimate] [:command/typed :field "x"] [:command/record]))))))))

(deftest the-replacement-display
  (let [state (add empty-state "old task")
        opened (run state [:command/open-replace] [:command/typed [:line 0 :title] "new one"] [:command/next-line 0])
        hiccup (page opened)
        shown (set (texts hiccup))]
    (is (contains? shown "REPLACE"))
    (is (contains? shown "old task"))
    (is (= 2 (count (filter #{"working days"} (texts hiccup)))) "two lines")
    (is (some #{[:command/typed [:line 1 :title] :field/text]} (handlers hiccup)))
    (is (some #{[:command/paste 0]} (handlers hiccup)))
    (is (some #{"⌘Enter"} (key-caps hiccup)))
    (is (some #{"Tab"} (key-caps hiccup)))
    (is (some #{" record"} (texts hiccup)))
    (testing "an estimate on a line already in progress is kept but not counted"
      (let [t (texts (page (run opened [:command/state-down 0] [:command/typed [:line 0 :estimate] "2"])))]
        (is (some #{"estimated after its start: kept, not counted"} t))))))

(deftest the-schedule
  (let [state (add empty-state "A" "B" "C")
        a (titled state "A") b (titled state "B")
        estimate (fn [s i n] (run s [:command/cursor i] [:command/open-estimate] [:command/typed :field (str n)] [:command/record]))
        state (-> state (estimate a 3) (estimate b 5)
                  (run [:command/cursor a] [:command/mark] [:command/cursor b] [:action/set-after]
                       [:command/show-schedule] [:command/cursor b]))
        hiccup (page state)
        shown (set (texts hiccup))]
    (is (contains? shown "PLAN"))
    (is (contains? shown "finishes Wed Nov 4") "A Mon to Wed, B Thu to the next Wed")
    (is (contains? shown "Mon Oct 26 – Wed Oct 28"))
    (is (contains? shown "Thu Oct 29 – Wed Nov 4"))
    (is (contains? shown "3 wd"))
    (is (contains? shown "after A"))
    (is (= 2 (count (filter #(= :tr (first %)) (nodes-with-class hiccup "critical")))) "both rows are critical")
    (is (contains? shown "What comes after what"))
    (is (contains? shown "Not estimated"))
    (is (contains? shown "C"))
    (testing "the graph is drawn: the linked tasks with their finish days, the cursor's task marked"
      (let [svg (first (filter #(and (vector? %) (= :svg.chain (first %))) (tree-seq coll? seq hiccup)))
            t (for [node (tree-seq coll? seq svg)
                    :when (and (vector? node) (#{:text.title :text.when} (first node)))]
                (last node))]
        (is (some? svg))
        (is (= ["A" "Wed Oct 28" "B" "Wed Nov 4"] t) "C has no link, so it is not drawn")
        (is (= 1 (count (nodes-with-class svg "here"))))
        (is (= 1 (count (nodes-with-class svg "critical"))) "the one edge is on the critical path")))
    (testing "an unestimated linked task is drawn, saying so"
      (let [c (titled state "C")
            t (texts (page (run state [:command/show-stand-up] [:command/cursor b] [:command/mark]
                                [:command/cursor c] [:action/set-after] [:command/show-schedule])))]
        (is (some #{"no estimate"} t))))
    (testing "with no links, the drawing says how to make one"
      (let [t (texts (page (run (add empty-state "A") [:command/show-schedule])))]
        (is (some #{"No task comes after another yet: mark the tasks it comes after with "} t))))
    (testing "a mark shows on its row"
      (let [marked (page (run state [:command/cursor a] [:command/mark]))]
        (is (= 1 (count (nodes-with-class marked "marked"))))
        (is (some #{"✗"} (texts marked)))))
    (testing "the estimate field opens on the row, with the row's estimate hidden"
      (let [opened (page (run state [:command/cursor a] [:command/open-estimate]))]
        (is (some #{:input#field} (tree-seq coll? seq opened)))
        (is (some #{" working days, as likely more as less"} (texts opened)))
        (is (not (some #{"3 wd"} (texts opened))))
        (is (some #{"5 wd"} (texts opened)))))
    (testing "the tasks whose dates the last act moved are marked (V7)"
      (let [moved (page (estimate state a 4))]
        (is (= 2 (count (filter #(= :tr (first %)) (nodes-with-class moved "changed"))))
            "A was estimated again, and B moves with it")))
    (testing "the query hides rows that do not match"
      (let [t (set (texts (page (run state [:command/typed :query "B"]))))]
        (is (contains? t "Thu Oct 29 – Wed Nov 4"))
        (is (not (contains? t "Mon Oct 26 – Wed Oct 28")))))))

(deftest the-record-screen
  (let [state (add empty-state "one" "two")]
    (testing "export and import are not in the frame"
      (is (not (some #{[:command/export]} (handlers (page state)))))
      (is (not (some #{[:command/import]} (handlers (page state))))))
    (testing "they are on the record screen, with the record's size"
      (let [hiccup (page (run state [:command/show-record]))
            shown (set (texts hiccup))]
        (is (contains? shown "RECORD"))
        (is (contains? shown "2 acts, from 2026-10-26 to 2026-10-26; 2 tasks on the board, 0 replaced or dropped"))
        (is (some #{[:command/export]} (handlers hiccup)))
        (is (some #{[:command/import]} (handlers hiccup)))))
    (testing "an empty record says so"
      (is (some #{"The record is empty."} (texts (page (run empty-state [:command/show-record]))))))))

(deftest problems-and-the-footer
  (is (some #{"an act needs a time at [0 :act/at]"}
            (texts (page (assoc empty-state :state/problems
                                [{:problem/path [0 :act/at] :problem/message "an act needs a time"}])))))
  (is (some #{"last act 250 ms: over 0.1 s"} (texts (page (assoc empty-state :state/last-act-ms 250)))))
  (is (some #{"last act 12 ms"} (texts (page (assoc empty-state :state/last-act-ms 12)))))
  (testing "the footer shows the undo and redo keys once there is an act (V11)"
    (let [after (page (assoc empty-state :state/last-act-ms 12))]
      (is (= ["⌘Z" "⇧⌘Z"] (filter #{"⌘Z" "⇧⌘Z"} (key-caps after))))
      (is (some #{" undo"} (texts after))))
    (is (not (some #{" undo"} (texts (page empty-state)))) "nothing to undo yet")))

(deftest handlers-are-data
  (is (every? vector? (handlers (page (add empty-state "one")))))
  (is (every? qualified-keyword? (map first (handlers (page (add empty-state "one")))))))
