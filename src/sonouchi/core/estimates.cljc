(ns sonouchi.core.estimates
  "Each task's estimates: every one is kept as an entity of its own,
  with the time it was made. The latest is the one the schedule uses,
  and design3 counts an estimate only if it was made before the task
  started."
  (:require [clojure.string :as str]
            [sonouchi.core.days :as days]))

(defn all
  "Estimate id to its current map with :act/at, the time it was made,
  for every estimate in log that is still in entities, the map that
  sonouchi.core.log/entities returns for the same log."
  [log entities]
  (let [made-at (reduce (fn [m {:act/keys [at patches]}]
                          (reduce (fn [m {:patch/keys [entity] :as patch}]
                                    (if (and (:estimate/task patch) (not (m entity)))
                                      (assoc m entity at)
                                      m))
                                  m
                                  patches))
                        {}
                        log)]
    (into {} (for [[id at] made-at
                   :let [estimate (entities id)]
                   :when estimate]
               [id (assoc estimate :act/at at)]))))

(defn latest
  "Task id to its latest estimate, a map with :estimate/amount,
  :estimate/unit and :act/at, from the estimates that all returns."
  [estimates]
  (->> (vals estimates)
       (sort-by (comp inst-ms :act/at))
       (reduce (fn [m estimate] (assoc m (:estimate/task estimate) estimate)) {})))

(defn counts?
  "True when design3 counts estimate for task: it was made on or before
  the local day the task started, or the task has not started. The
  local day comes from :act/at and offset-minutes, as in
  sonouchi.core.days/local-day."
  [estimate task offset-minutes]
  (let [started (:task/started task)]
    (or (nil? started)
        (<= (compare (days/local-day (:act/at estimate) offset-minutes) started) 0))))

(defn parse
  "What the owner typed as an estimate: {:estimate/amount n} when text
  is a positive number, or {:problem/message why} when it is not. A
  whole number stays whole on both platforms."
  [text]
  (let [t (str/trim text)
        n (or (parse-long t) (parse-double t))]
    (cond
      (nil? n) {:problem/message "an estimate is a number of working days"}
      (not (pos? n)) {:problem/message "an estimate is more than 0 working days"}
      :else {:estimate/amount n})))

(defn show
  "An estimate's amount as the owner reads it, with its unit: \"3 wd\"
  or \"2.5 wd\"."
  [{:estimate/keys [amount]}]
  (str amount " wd"))

(defn act
  "The act, without :act/id and :act/at, that records amount as a new
  estimate of task-id, as the entity estimate-id, in working days."
  [task-id estimate-id amount]
  {:act/action :action/estimate
   :act/patches [{:patch/entity estimate-id
                  :estimate/task task-id
                  :estimate/amount amount
                  :estimate/unit :working-days}]})
