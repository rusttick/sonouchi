(ns sonouchi.core.search-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [sonouchi.core.search :as search]))

(defn- words
  "A simple tokenizer for the tests: runs of letters and digits."
  [text]
  (re-seq #"\w+" text))

(def ^:private tasks
  {1 {:task/title "measure spindle runout"}
   2 {:task/title "order Spindle wrench"}
   3 {:task/title "spindle motor noise"}
   4 {:task/title "buy dial gauge"}
   5 {:problem/message "not a task"}})

(def ^:private idx (search/index words tasks))

(defn- found
  [text]
  (search/matches idx words text))

(deftest words-match-the-start-of-terms
  (is (= #{1 2 3} (found "spindle")))
  (is (= #{1 2 3} (found "spi")) "a word being typed")
  (is (= #{2} (found "wrench SPIN")) "every word, in any order and case")
  (is (= #{} (found "lathe")))
  (is (= #{1 2 3 4} (found "  ")) "no words: every task"))

(deftest a-few-typing-mistakes-are-allowed
  (is (= #{1 2 3} (found "spindel")) "two letters swapped")
  (is (= #{1 2 3} (found "spndle")) "a letter left out")
  (is (= #{} (found "dail")) "short words must be exact")
  (is (= #{} (found "pspindle")) "the first letter must be right"))

(deftest reindexing-follows-the-entities
  (testing "a new title"
    (let [retitled (assoc-in tasks [3 :task/title] "motor noise")]
      (is (= #{1 2} (search/matches (search/reindex idx words retitled [3])
                                    words "spindle")))))
  (testing "a task removed"
    (is (= #{1 3 4} (search/matches (search/reindex idx words
                                                    (dissoc tasks 2) [2])
                                    words "")))))

(def ^:private gen-entities
  "Entities 0 to 5, some with a title of a few words from a small
  vocabulary, so that titles share terms."
  (gen/let [titles (gen/vector
                    (gen/one-of
                     [(gen/return nil)
                      (gen/fmap #(str/join " " %)
                                (gen/vector (gen/elements ["spindle" "motor"
                                                           "Motor" "gauge"
                                                           "dial" "run"])
                                            1 3))])
                    6)]
    (into {} (for [[id title] (map vector (range) titles)
                   :when title]
               [id {:task/title title}]))))

(defspec reindexing-changes-equals-indexing-afresh 100
  (prop/for-all [before gen-entities
                 after gen-entities]
    (let [changed (filter #(not= (get before %) (get after %)) (range 6))]
      (= (search/index words after)
         (search/reindex (search/index words before) words after changed)))))

(defspec every-task-is-found-by-its-title 100
  (prop/for-all [entities gen-entities]
    (let [an-idx (search/index words entities)]
      (every? (fn [[id {:task/keys [title]}]]
                (contains? (search/matches an-idx words title) id))
              entities))))
