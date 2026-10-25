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
    (is (contains? shown "1 of 1"))
    (is (contains? shown "recording Fri 23 Oct"))
    (is (contains? shown "1 Stand-up"))
    (is (contains? shown "2 Schedule"))
    (is (contains? shown "measure spindle runout")))
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
    (is (some #(re-find #"s started · f finished · w worked on" %) (texts hiccup)) "the keys beside the cursor")
    (is (some #{[:command/cursor one]} (handlers hiccup)) "a click moves the cursor")
    (testing "a waiting task shows on whom, in its column"
      (let [waiting (run state [:command/cursor one] [:command/open-wait] [:command/reason-person]
                         [:command/typed :field "Tanaka"] [:command/record])
            t (texts (page waiting))]
        (is (some #{"waiting on Tanaka since Fri 23 Oct"} t))
        (is (= 2 (count (filter #{"no estimate"} t))))))
    (testing "the query hides what does not match"
      (let [t (set (texts (page (run state [:command/typed :query "tw"]))))]
        (is (contains? t "two"))
        (is (not (contains? t "one")))
        (is (contains? t "1 of 2"))))))

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
    (is (some #(re-find #"⌘Enter record" %) (texts hiccup)))
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
    (is (contains? shown "SCHEDULE"))
    (is (contains? shown "finishes Wed 4 Nov") "A Mon to Wed, B Thu to the next Wed")
    (is (contains? shown "Mon 26 Oct – Wed 28 Oct"))
    (is (contains? shown "Thu 29 Oct – Wed 4 Nov"))
    (is (contains? shown "3 wd"))
    (is (contains? shown "after A"))
    (is (= 2 (count (nodes-with-class hiccup "critical"))))
    (is (contains? shown "B comes after"))
    (is (contains? shown "Not estimated"))
    (is (contains? shown "C"))
    (is (some #(= :svg.chain (first %)) (filter vector? (tree-seq coll? seq hiccup))) "the chain is drawn")))

(deftest problems-and-the-footer
  (is (some #{"an act needs a time at [0 :act/at]"}
            (texts (page (assoc empty-state :state/problems
                                [{:problem/path [0 :act/at] :problem/message "an act needs a time"}])))))
  (is (some #{"last act 250 ms: over 0.1 s"} (texts (page (assoc empty-state :state/last-act-ms 250)))))
  (is (some #{"last act 12 ms"} (texts (page (assoc empty-state :state/last-act-ms 12))))))

(deftest handlers-are-data
  (is (every? vector? (handlers (page (add empty-state "one")))))
  (is (every? qualified-keyword? (map first (handlers (page (add empty-state "one")))))))
