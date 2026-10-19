(ns sonouchi.core.tasks
  "Adding tasks, and the tasks added today."
  (:require [clojure.string :as str]
            [sonouchi.core.log :as log]))

(defn titles
  "The task titles in text: one per line, trimmed, with blank lines left
  out."
  [text]
  (->> (str/split-lines text)
       (map str/trim)
       (remove str/blank?)
       vec))

(defn add-act
  "The act, without :act/id and :act/at, that adds one task for each
  title in new-titles, with the entity ids in ids, one per title."
  [new-titles ids]
  {:act/action :action/add-task
   :act/patches (mapv (fn [title id] {:patch/entity id :task/title title})
                      new-titles
                      ids)})

(def ^:private day-ms (* 24 60 60 1000))

(defn- local-ms
  [at offset-minutes]
  (+ (inst-ms at) (* offset-minutes 60 1000)))

(defn- local-day
  [at offset-minutes]
  (quot (local-ms at offset-minutes) day-ms))

(defn- two-digits
  [n]
  (if (< n 10) (str "0" n) (str n)))

(defn clock
  "The local time of day of at, as \"HH:MM\", for a place offset-minutes
  ahead of UTC."
  [at offset-minutes]
  (let [minutes (quot (mod (local-ms at offset-minutes) day-ms) 60000)]
    (str (two-digits (quot minutes 60)) ":" (two-digits (mod minutes 60)))))

(defn added-today
  "The tasks added on the same local day as now, newest first, that
  still have a title. Each is a map with :patch/entity, the task's
  current :task/title, and the :act/id and :act/at of the act that added
  it."
  [log now offset-minutes]
  (let [today (local-day now offset-minutes)
        current (log/entities log)]
    (for [{:act/keys [id at action patches]} (rseq (vec log))
          :when (= :action/add-task action)
          :when (= today (local-day at offset-minutes))
          {:patch/keys [entity]} (reverse patches)
          :let [title (get-in current [entity :task/title])]
          :when title]
      {:patch/entity entity :task/title title :act/id id :act/at at})))

(defn latest-add
  "The :act/id of the last act in log, when it added tasks. Its tasks are
  the ones just added (V7)."
  [log]
  (let [{:act/keys [id action]} (peek (vec log))]
    (when (= :action/add-task action)
      id)))

(defn task-count
  "How many tasks there are: entities with a :task/title."
  [log]
  (count (filter :task/title (vals (log/entities log)))))
