(ns sonouchi.core.requests
  "Requests and promises, computed from the facts (doc/ux-requests.md):
  which requests are open and in what order, the promise in force about
  a request or a wait, the tasks that serve a request, when a request
  will be done, and its slack against the day promised. entities is the
  map sonouchi.core.log/entities returns."
  (:require [clojure.string :as str]
            [sonouchi.core.chain :as chain]
            [sonouchi.core.days :as days]))

(defn all
  "Request id to request, for every request in entities."
  [entities]
  (into {} (filter (fn [[_ e]] (:request/asker e)) entities)))

(defn- first-appearance
  "Entity id to the index in log of the act that first gave it the
  attribute attr."
  [log attr]
  (reduce (fn [m [i {:act/keys [patches]}]]
            (reduce (fn [m {:patch/keys [entity] :as patch}]
                      (if (and (attr patch) (not (m entity))) (assoc m entity i) m))
                    m
                    patches))
          {}
          (map-indexed vector log)))

(defn open
  "The ids of the open requests, in the order they were asked, newest
  last: by :request/asked, then by the order of the acts that made
  them (ux-requests, \"The requests screen\")."
  [log entities]
  (let [made (first-appearance log :request/asker)]
    (->> (all entities)
         (remove (fn [[_ r]] (:request/closed r)))
         (sort-by (fn [[id r]] [(:request/asked r) (made id)]))
         (mapv first))))

(defn promises
  "The id of a request or a wait to the promise in force about it: the
  latest promise made, with :act/at, the time it was made, from log and
  the entities of the same log."
  [log entities]
  (reduce (fn [m {:act/keys [at patches]}]
            (reduce (fn [m {:patch/keys [entity]}]
                      (if-let [p (entities entity)]
                        (if (:promise/about p) (assoc m (:promise/about p) (assoc p :act/at at)) m)
                        m))
                    m
                    patches))
          {}
          log))

(defn serving
  "The ids of the tasks on board that serve request-id, as a set. board
  is what sonouchi.core.board/tasks returns."
  [board request-id]
  (set (for [[id t] board :when (contains? (:task/serves t) request-id)] id)))

(defn finish-day
  "When request-id will be done, from times, the schedule's
  :schedule/times: the latest :schedule/finish-day among its unfinished
  tasks on board; :done when it has tasks and all have finished; nil
  when it has no tasks, or a task of it has no dates."
  [board times request-id]
  (let [ids (serving board request-id)
        unfinished (remove #(:task/finished (board %)) ids)]
    (cond
      (empty? ids) nil
      (empty? unfinished) :done
      (every? times unfinished) (apply max (map #(get-in times [% :schedule/finish-day]) unfinished))
      :else nil)))

(defn slack
  "How many working days a request finishing at the schedule's
  finish-day n, counted from day 0 of today, can slip before the day
  promised is missed: positive when there is room, 0 when it finishes
  on the promised day, negative when it is late."
  [today n promised]
  (- (days/working-days-from (days/add-working-days today 0) (days/next-working-day promised))
     n))

(defn slack-words
  "slack as the owner reads it: \"can slip 2 working days\", \"no slack\"
  or \"3 working days late\"."
  [n]
  (let [unit (fn [k] (str k " working day" (when (not= 1 k) "s")))]
    (cond
      (pos? n) (str "can slip " (unit n))
      (zero? n) "no slack"
      :else (str (unit (- n)) " late"))))

(defn next-to-do
  "The ids of the tasks the owner could work on today for request-id:
  the unfinished tasks behind the one that sets its finish, whose
  scheduled start is not after today. Empty when the request has no
  finish."
  [board times request-id]
  (let [n (finish-day board times request-id)]
    (if (number? n)
      (let [ids (serving board request-id)
            last-task (->> ids
                           (remove #(:task/finished (board %)))
                           (sort-by (fn [id] [(- (get-in times [id :schedule/finish-day])) (str id)]))
                           first)]
        (set (for [id (chain/behind board last-task)
                   :when (and (not (:task/finished (board id)))
                              (<= (get-in times [id :schedule/start-day] 1) 0))]
               id)))
      #{})))

(defn parse-request
  "What the owner typed for a new request, \"Tanaka: the integrated
  system\": {:request/asker ... :request/words ...}, the words absent
  when none were typed, or {:problem/message why} when there is no
  asker."
  [text]
  (let [[asker words] (map str/trim (str/split (or text "") #":" 2))]
    (if (str/blank? asker)
      {:problem/message "who asked? as \"Tanaka: the integrated system\""}
      (cond-> {:request/asker asker}
        (not (str/blank? words)) (assoc :request/words words)))))

(defn show-request
  "A request's asker and words as one line, the form parse-request
  reads."
  [{:request/keys [asker words]}]
  (if words (str asker ": " words) asker))
