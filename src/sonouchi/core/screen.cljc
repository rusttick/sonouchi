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
            [sonouchi.core.requests :as requests]
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

(defn later-day
  "The working day after the recording day, or nil when it is after
  today: the recording day moves forward, but never past today."
  [state]
  (let [next-day (days/next-working-day (recording-day state))]
    (when-not (pos? (compare next-day (today state)))
      next-day)))

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

(defn- places
  "Task id to its place in the board's order."
  [state entities]
  (zipmap (board/ordered (:state/log state) entities) (range)))

(defn rows
  "The ids of the schedule's rows: the scheduled tasks that are not
  done, in the order of the dependency graph, with ties in the board's
  order (ux-journey, \"The schedule\")."
  [state entities latest]
  (let [{:schedule/keys [times]} (scheduled state entities latest)
        depth (chain/depth (update-vals (board/tasks entities) #(select-keys % [:task/after])))
        place (places state entities)]
    (->> (keys times)
         (remove #(:task/finished (entities %)))
         (sort-by (juxt depth place))
         vec)))

(defn shown-rows
  "The schedule's rows that the query leaves visible, shown being the
  set of ids that match it. The schedule is computed from every task."
  [state entities latest shown]
  (filterv shown (rows state entities latest)))

(defn shown-not-estimated
  "The ids of the tasks the schedule lists as not estimated, in the
  board's order: those in not-estimated that have not finished and that
  the query leaves visible, shown being the set of ids that match it."
  [state entities latest shown]
  (->> (keys (not-estimated entities latest))
       (filter shown)
       (remove #(:task/finished (entities %)))
       (sort-by (places state entities))
       vec))

(defn schedule-ids
  "The ids the cursor moves over on the schedule, in the order shown:
  its visible rows, then the visible tasks not estimated."
  [state entities latest shown]
  (into (shown-rows state entities latest shown)
        (shown-not-estimated state entities latest shown)))

(defn request-ids
  "The ids the cursor moves over on the requests screen: the open
  requests in the order asked."
  [state entities]
  (requests/open (:state/log state) entities))

(defn- first-or-cursor
  [state ids]
  (let [id (:state/cursor state)]
    (if (some #{id} ids) id (first ids))))

(defn cursor-on-screen
  "The id under the cursor on the screen the state shows. On the
  schedule it is the state's cursor when that is in schedule-ids, else
  the first of them; on the requests screen, likewise over request-ids;
  elsewhere it is what cursor gives for cols. shown is the set of ids
  matching the query, and cols the board's columns."
  [state entities shown cols]
  (case (:state/screen state)
    :schedule (first-or-cursor state (schedule-ids state entities (latest-estimates state entities) shown))
    :requests (first-or-cursor state (request-ids state entities))
    (cursor state cols)))

(defn- scheduled-dates
  "Task id to its first and last scheduled day, as ISO days."
  [state]
  (let [entities (current state)
        day (today state)]
    (update-vals (:schedule/times (scheduled state entities (latest-estimates state entities)))
                 (fn [{:schedule/keys [start-day finish-day]}]
                   [(board/start-day day start-day) (board/last-day day finish-day)]))))

(defn moved-by-last-act
  "The ids of the tasks whose scheduled dates the last act in the log
  moved, or gave them (V7)."
  [{:state/keys [log] :as state}]
  (if (seq log)
    (let [before (scheduled-dates (update state :state/log pop))]
      (set (for [[id dates] (scheduled-dates state)
                 :when (not= dates (before id))]
             id)))
    #{}))

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
