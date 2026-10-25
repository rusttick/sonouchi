(ns sonouchi.core.screen
  "The values the screens are computed from: what the views and the
  commands both need from the state, such as today, the recording day,
  the entities, which tasks the query leaves visible, the columns, and
  the schedule. Everything is derived from the state; nothing here is
  stored."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [sonouchi.core.board :as board]
            [sonouchi.core.chain :as chain]
            [sonouchi.core.days :as days]
            [sonouchi.core.estimates :as estimates]
            [sonouchi.core.log :as log]
            [sonouchi.core.schedule :as schedule]
            [sonouchi.core.search :as search]))

(defn today
  "Today's ISO day, from the shell's clock in the state."
  [{:state/keys [now utc-offset-minutes]}]
  (days/local-day now utc-offset-minutes))

(defn recording-day
  "The day the stand-up keys record: the one the owner chose, or the
  last working day before today."
  [state]
  (or (:state/recording-day state)
      (days/previous-working-day (today state))))

(defn current
  "Entity id to its current map, from the log in the state."
  [state]
  (log/entities (:state/log state)))

(defn all-estimates
  "What sonouchi.core.estimates/all returns for the state's log."
  [state entities]
  (estimates/all (:state/log state) entities))

(defn matching
  "The set of ids of the tasks on the board that match the query:
  every task when the query is blank. tokenize splits the query into
  words as the index was built."
  [{:state/keys [query index]} tokenize entities]
  (let [on-board (set (keys (board/tasks entities)))]
    (if (or (str/blank? (or query "")) (nil? index))
      on-board
      (set/intersection on-board (search/matches index tokenize query)))))

(def column-names
  "The columns, left to right, with what they are called."
  [[:to-do "To do"] [:in-progress "In progress"] [:done "Done"]])

(defn columns
  "Three vectors of task ids, To do, In progress and Done, each in the
  board's order, with only the tasks in shown, a set of ids."
  [state entities shown]
  (let [by-column (->> (board/ordered (:state/log state) entities)
                       (filter shown)
                       (group-by #(board/column (entities %))))]
    (mapv #(vec (by-column (first %))) column-names)))

(defn position
  "[column row] of id in cols, as columns returns them, or nil when it
  is not there."
  [cols id]
  (first (for [[c ids] (map-indexed vector cols)
               [r i] (map-indexed vector ids)
               :when (= i id)]
           [c r])))

(defn cursor
  "The id under the cursor: the state's, when it is in cols, else the
  first id there is, else nil."
  [state cols]
  (let [id (:state/cursor state)]
    (if (and id (position cols id))
      id
      (first (apply concat cols)))))

(defn latest-estimates
  "Task id to its latest estimate, for the state."
  [state entities]
  (estimates/latest (all-estimates state entities)))

(defn scheduled
  "The schedule of the board's tasks from today: what
  sonouchi.core.schedule/schedule returns for the scheduler's input, or
  nil when nothing can be scheduled."
  [state entities latest]
  (let [input (board/scheduler-input (board/tasks entities) latest (today state))]
    (when (seq input)
      (schedule/schedule input))))

(defn not-estimated
  "Task id to the id of the unestimated task that keeps it off the
  schedule."
  [entities latest]
  (board/not-estimated (board/tasks entities) latest))

(defn rows
  "The ids of the schedule's rows: the scheduled tasks that are not
  done, in the order of the dependency graph, with ties in the board's
  order (ux-journey, \"The schedule\")."
  [state entities latest]
  (let [{:schedule/keys [times]} (scheduled state entities latest)
        depth (chain/depth (update-vals (board/tasks entities) #(select-keys % [:task/after])))
        place (zipmap (board/ordered (:state/log state) entities) (range))]
    (->> (keys times)
         (remove #(:task/finished (entities %)))
         (sort-by (juxt depth place))
         vec)))

(defn changed-by-last-act
  "The ids of the tasks the last act in the log changed: the tasks it
  patched, and the tasks of the waits and estimates it patched (V7)."
  [{:state/keys [log]} entities]
  (set (for [{:patch/keys [entity] :as patch} (:act/patches (peek log))
             :let [e (merge (entities entity) patch)]]
         (or (:wait/task e) (:estimate/task e) entity))))

(defn index-of
  "The search index of the board's tasks, built afresh from entities
  with tokenize."
  [tokenize entities]
  (search/index-texts tokenize (board/search-texts entities)))

(defn reindexed
  "The state's index with the tasks in ids indexed again, or built
  afresh when there is none."
  [state tokenize entities ids]
  (if-let [idx (:state/index state)]
    (search/reindex-texts idx tokenize (board/search-texts entities) ids)
    (index-of tokenize entities)))
