(ns sonouchi.core.log-test
  (:require #?(:clj [clojure.edn :as edn]
               :cljs [cljs.reader :as edn])
            [clojure.test :refer [deftest is testing]]
            [sonouchi.core.log :as log]))

;; Attributes from the registry stand in for task attributes, which do
;; not exist yet. Only the shape of the log matters here.

(def ^:private e1 #uuid "00000000-0000-0000-0000-000000000001")
(def ^:private e2 #uuid "00000000-0000-0000-0000-000000000002")

(defn- act
  "An act with a made-up id and time."
  [n patches]
  {:act/id (parse-uuid (str "00000000-0000-0000-0000-0000000000a" n))
   :act/at #inst "2026-10-19T08:30:00Z"
   :act/action :command/export
   :act/patches patches})

(def ^:private a1 (act 1 [{:patch/entity e1 :problem/message "first"}]))
(def ^:private a2 (act 2 [{:patch/entity e1 :problem/message "second"
                           :problem/path [1]}
                          {:patch/entity e2 :problem/message "other"}]))
(def ^:private a3 (act 3 [{:patch/entity e1 :patch/retract #{:problem/path}}]))

(deftest patches-merge-in-order
  (is (= {e1 {:problem/message "second" :problem/path [1]}
          e2 {:problem/message "other"}}
         (log/entities [a1 a2])))
  (testing "a retracted attribute is gone"
    (is (= {:problem/message "second"}
           (get (log/entities [a1 a2 a3]) e1))))
  (testing "an entity with nothing left is gone"
    (is (= {} (log/entities
               [a1 (act 2 [{:patch/entity e1
                            :patch/retract #{:problem/message}}])])))))

(defn- undo
  "log with an act appended that undoes the act numbered n."
  [log-so-far n]
  (conj log-so-far
        (act 9 (log/undo-patches log-so-far (:act/id (act n []))))))

(deftest undo-restores-what-was-there-before
  (is (= (log/entities [a1]) (log/entities (undo [a1 a2] 2))))
  (testing "undoing the first act removes what it added"
    (is (= {} (log/entities (undo [a1] 1)))))
  (testing "undoing a retraction puts the value back"
    (is (= (log/entities [a1 a2]) (log/entities (undo [a1 a2 a3] 3)))))
  (testing "an attribute changed again later is left as it is"
    ;; Act 3 already removed :problem/path, so undoing act 2 leaves it
    ;; removed, puts back the first message, and removes e2, which act 2
    ;; created.
    (is (= {e1 {:problem/message "first"}}
           (log/entities (undo [a1 a2 a3] 2)))))
  (testing "an unknown act gives nil"
    (is (nil? (log/undo-patches [a1] (:act/id a2))))))

(deftest problems-explain-what-is-not-a-log
  (is (= [] (log/problems [a1 a2 a3])))
  (is (seq (log/problems {:not "a vector"})))
  (is (= [[0 :act/at]]
         (map :problem/path (log/problems [(dissoc a1 :act/at)]))))
  (is (= [[0 :act/patches 0 :patch/entity]]
         (map :problem/path
              (log/problems [(act 1 [{:problem/message "no entity"}])]))))
  (is (= ["act 00000000-0000-0000-0000-0000000000a1 appears 2 times"]
         (map :problem/message (log/problems [a1 a1])))))

(deftest a-log-survives-edn
  (let [log-value [a1 a2 a3]]
    (is (= log-value (edn/read-string (pr-str log-value))))
    (is (= [] (log/problems (edn/read-string (pr-str log-value)))))))
