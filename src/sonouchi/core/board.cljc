(ns sonouchi.core.board
  "The stand-up board, computed from the facts: which tasks are on it,
  each task's column and its place in it, its open wait, whether it has
  a record for a day, and the tasks turned into the scheduler's input
  with today as day 0.

  Nothing about columns or positions is stored (ux-design, \"The
  concept\"). entities is the map sonouchi.core.log/entities returns."
  (:require [clojure.string :as str]
            [sonouchi.core.days :as days]))

(defn tasks
  "Task id to task, for the tasks on the board: those with a title that
  have not been replaced."
  [entities]
  (into {} (filter (fn [[_ e]]
                     (and (:task/title e) (not (contains? e :task/replaced-by))))
                   entities)))

(defn column
  "The column a task is in: :done when it has finished, :in-progress
  when it has started, :to-do otherwise."
  [task]
  (cond
    (:task/finished task) :done
    (:task/started task) :in-progress
    :else :to-do))

(def ^:private moving
  "The attributes whose change moves a card to another column."
  #{:task/started :task/finished})

(defn- arrivals
  "Task id to the :act/at of the act that last moved it into its column:
  the last act that set or removed a moving attribute, or else the act
  that first gave it a title."
  [log]
  (reduce (fn [m {:act/keys [at patches]}]
            (reduce (fn [m {:patch/keys [entity retract] :as patch}]
                      (cond
                        (or (some moving (keys patch)) (some moving retract))
                        (assoc m entity at)

                        (and (:task/title patch) (not (m entity)))
                        (assoc m entity at)

                        :else m))
                    m
                    patches))
          {}
          log))

(defn ordered
  "The ids of the tasks on the board, the one that last arrived in its
  column first (ux-journey, Stage 3), with ties broken by id so that the
  order is the same on both platforms."
  [log entities]
  (let [arrived (arrivals log)]
    (vec (sort-by (fn [id] [(- (inst-ms (arrived id))) (str id)])
                  (keys (tasks entities))))))

(defn open-waits
  "Task id to its open wait: the wait on it with no :wait/ended, with
  the wait's id under :patch/entity. When several are open, the one that
  began last."
  [entities]
  (->> entities
       (keep (fn [[id e]]
               (when (and (:wait/task e) (not (:wait/ended e)))
                 (assoc e :patch/entity id))))
       (sort-by (juxt :wait/began (comp str :patch/entity)))
       (reduce (fn [m wait] (assoc m (:wait/task wait) wait)) {})))

(defn recorded-days
  "Task id to the set of days on which it has a record: the days it was
  worked on, the days a wait on it began or ended, and the local days on
  which it was estimated. estimates is what sonouchi.core.estimates/all
  returns."
  [entities estimates offset-minutes]
  (let [worked (into {} (for [[id e] entities :when (:task/title e)]
                          [id (set (:task/worked-on e))]))
        waited (for [[_ e] entities :when (:wait/task e)
                     day (keep e [:wait/began :wait/ended])]
                 [(:wait/task e) day])
        estimated (for [e (vals estimates)]
                    [(:estimate/task e) (days/local-day (:act/at e) offset-minutes)])]
    (reduce (fn [m [id day]] (update m id (fnil conj #{}) day))
            worked
            (concat waited estimated))))

(defn search-texts
  "Task id to the text it is found by: its title, whom its open wait is
  on, and the asker and words of each request it serves."
  [entities]
  (let [waits (open-waits entities)]
    (into {} (for [[id task] (tasks entities)]
               [id (str/join " " (keep identity
                                       (concat [(:task/title task)
                                                (get-in waits [id :wait/on])]
                                               (for [r (sort-by str (:task/serves task))
                                                     k [:request/asker :request/words]]
                                                 (get-in entities [r k])))))]))))

(defn not-estimated
  "Task id to the id of the task without an estimate that keeps it off
  the schedule: itself, or a task it comes after, directly or not.
  latest is what sonouchi.core.estimates/latest returns. Finished tasks
  need no estimate."
  [board latest]
  (let [own (into {} (for [[id t] board
                           :when (and (not (:task/finished t)) (not (latest id)))]
                       [id id]))
        step (fn [m]
               (into m (for [[id t] board
                             :when (not (m id))
                             :let [stopper (some m (sort-by str (:task/after t)))]
                             :when stopper]
                         [id stopper])))]
    (->> (iterate step own)
         (partition 2 1)
         (drop-while (fn [[before after]] (not= before after)))
         ffirst)))

(defn day-zero
  "Day 0 of the schedule: today, or the next working day when today is
  not one."
  [today]
  (days/add-working-days today 0))

(defn scheduler-input
  "The tasks of board that can be scheduled, in the shape
  sonouchi.core.schedule/schedule takes, with days counted in working
  days from day 0, the start of day-zero of today. A finished task has
  its actual start and finish; a started one its actual start and its
  latest estimate; the rest their latest estimate. Tasks in
  not-estimated are left out."
  [board latest today]
  (let [day0 (day-zero today)
        out (not-estimated board latest)
        boundary #(days/working-days-from day0 %)]
    (into {} (for [[id {:task/keys [after started finished]}] board
                   :when (not (out id))]
               [id (cond-> {:task/after (set (remove out after))}
                     started (assoc :task/actual-start-day (boundary started))
                     finished (assoc :task/actual-finish-day
                                     (boundary (days/next-working-day finished)))
                     (not finished) (assoc :task/estimate-days
                                           (get-in latest [id :estimate/amount])))]))))

(defn start-day
  "The day on which a task with the scheduled :schedule/start-day n
  starts, from day 0 of today."
  [today n]
  (days/add-working-days (day-zero today) n))

(defn last-day
  "The last working day of a task with the scheduled :schedule/finish-day
  n, from day 0 of today: the finish is a boundary, so this is the
  working day before it."
  [today n]
  (days/add-working-days (day-zero today) (dec n)))
