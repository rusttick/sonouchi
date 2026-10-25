(ns sonouchi.core.actions
  "What each command does to the state: one map from the command's name
  to a function of the state, the command's arguments, a seq of fresh
  ids and the tokenizer, which returns the new state. A command that
  changes the record appends an act to the log; the rest change only the
  screen's own state. The shell's one dispatch function calls run with
  what only it knows: the ids, and the clock already in the state."
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [sonouchi.core.board :as board]
            [sonouchi.core.days :as days]
            [sonouchi.core.estimates :as estimates]
            [sonouchi.core.history :as history]
            [sonouchi.core.schedule :as schedule]
            [sonouchi.core.screen :as screen]
            [sonouchi.core.tasks :as tasks]))

;; Recording an act

(defn- task-of
  "The task a patch is about: the entity, or the task of the wait or
  estimate it patches."
  [entities {:patch/keys [entity] :as patch}]
  (let [e (merge (entities entity) patch)]
    (or (:wait/task e) (:estimate/task e) entity)))

(defn- record
  "state with act appended, given the first of ids and the state's
  clock; the search index updated for the tasks the act touched; and
  any problems cleared."
  [state act ids tokenize]
  (let [before (screen/current state)
        state' (-> state
                   (dissoc :state/problems)
                   (update :state/log conj (assoc act
                                                  :act/id (first ids)
                                                  :act/at (:state/now state))))
        after (screen/current state')
        changed (set (for [patch (:act/patches act)
                           entities [before after]]
                       (task-of entities patch)))]
    (assoc state' :state/index (screen/reindexed state' tokenize after changed))))

(defn- index-of
  "The position of x in v, or nil."
  [v x]
  (first (keep-indexed (fn [i y] (when (= x y) i)) v)))

(defn- undo-or-redo
  [state make ids tokenize]
  (if-let [act (make (:state/log state))]
    (record state act ids tokenize)
    state))

;; What the board shows, for the commands that act on the cursor

(defn- with-board
  "Calls f with the entities, the columns and the id under the cursor."
  [state tokenize f]
  (let [entities (screen/current state)
        shown (screen/matching state tokenize entities)
        cols (screen/columns state entities shown)]
    (f entities cols (screen/cursor state cols))))

(defn- rows
  "The ids the cursor moves over: the schedule's rows on the schedule
  screen, else the columns' ids in order."
  [state entities cols]
  (if (= :schedule (:state/screen state))
    (screen/rows state entities (screen/latest-estimates state entities))
    (vec (apply concat cols))))

(defn- move
  "The cursor moved dc columns and dr rows, staying on a card. On the
  schedule, dr moves along the rows and dc does nothing."
  [state tokenize dc dr]
  (with-board
    state tokenize
    (fn [entities cols cursor]
      (cond
        (nil? cursor) state

        (= :schedule (:state/screen state))
        (let [ids (rows state entities cols)
              i (or (index-of ids cursor) 0)
              i' (-> (+ (max i 0) dr) (max 0) (min (dec (count ids))))]
          (if (seq ids) (assoc state :state/cursor (ids i')) state))

        :else
        (let [[c r] (screen/position cols cursor)
              c' (if (zero? dc)
                   c
                   (first (filter #(seq (cols %))
                                  (if (pos? dc) (range (inc c) 3) (range (dec c) -1 -1)))))
              col (cols (or c' c))
              r' (-> (+ r dr) (max 0) (min (dec (count col))))]
          (assoc state :state/cursor (col r')))))))

(defn- on-cursor
  "state after f is applied to the entities and the id under the cursor,
  or state unchanged when there is no cursor."
  [state tokenize f]
  (with-board state tokenize
    (fn [entities _ cursor]
      (if cursor (f entities cursor) state))))

;; The stand-up acts

(defn- worked
  [task day]
  (conj (set (:task/worked-on task)) day))

(defn- start-patch
  [id task day]
  {:patch/entity id :task/started day :task/worked-on (worked task day)})

(defn- finish-patch
  [id task day]
  (cond-> {:patch/entity id :task/finished day :task/worked-on (worked task day)}
    (not (:task/started task)) (assoc :task/started day)))

(defn- worked-patch
  [id task day]
  {:patch/entity id :task/worked-on (worked task day)})

(defn- cursor-act
  "Records an act of action on the task under the cursor, with one patch
  from make, given the id, the task and the recording day."
  [action make]
  (fn [state _ ids tokenize]
    (on-cursor state tokenize
               (fn [entities cursor]
                 (record state
                         {:act/action action
                          :act/patches [(make cursor (entities cursor) (screen/recording-day state))]}
                         ids tokenize)))))

;; Fields on a card

(defn- open-field
  [kind text-of]
  (fn [state _ _ tokenize]
    (on-cursor state tokenize
               (fn [entities cursor]
                 (assoc state :state/field {:field/kind kind
                                            :field/task cursor
                                            :field/text (text-of (entities cursor))})))))

(defn- begin-wait
  [state task reason on day ids tokenize]
  (-> state
      (dissoc :state/field)
      (record {:act/action :action/begin-wait
               :act/patches [(cond-> {:patch/entity (first ids)
                                      :wait/task task
                                      :wait/reason reason
                                      :wait/began day}
                               (seq on) (assoc :wait/on on))]}
              (rest ids) tokenize)))

(defn- open-wait
  "Ends the open wait on the task under the cursor, or opens the wait
  field when it has none."
  [state _ ids tokenize]
  (on-cursor state tokenize
             (fn [entities cursor]
               (if-let [wait (get (board/open-waits entities) cursor)]
                 (record state
                         {:act/action :action/end-wait
                          :act/patches [{:patch/entity (:patch/entity wait)
                                         :wait/ended (screen/recording-day state)}]}
                         ids tokenize)
                 (assoc state :state/field {:field/kind :wait :field/task cursor :field/text ""})))))

(defn- reason
  [r]
  (fn [state _ ids tokenize]
    (let [{:field/keys [kind task]} (:state/field state)]
      (cond
        (not= :wait kind) state
        (= :not-in-plan r) (begin-wait state task r nil (screen/recording-day state) ids tokenize)
        :else (-> state
                  (assoc-in [:state/field :wait/reason] r)
                  (update :state/field dissoc :problem/message))))))

(defn- problem-in-field
  [state message]
  (assoc-in state [:state/field :problem/message] message))

(defn- record-field
  "Records what the open field holds, or says in the field why it cannot."
  [state ids tokenize]
  (let [{:field/keys [kind task text] chosen :wait/reason} (:state/field state)
        entities (screen/current state)
        day (screen/recording-day state)
        trimmed (str/trim (or text ""))]
    (case kind
      :title (if (or (str/blank? trimmed) (= trimmed (:task/title (entities task))))
               (dissoc state :state/field)
               (-> state
                   (dissoc :state/field)
                   (record {:act/action :action/correct-title
                            :act/patches [{:patch/entity task :task/title trimmed}]}
                           ids tokenize)))
      :estimate (let [{:estimate/keys [amount] :problem/keys [message]} (estimates/parse trimmed)]
                  (if message
                    (problem-in-field state message)
                    (-> state
                        (dissoc :state/field)
                        (record (estimates/act task (first ids) amount) (rest ids) tokenize))))
      :wait (cond
              (nil? chosen) (problem-in-field state "press 1, 2 or 3 for the reason")
              (str/blank? trimmed) (problem-in-field state "on whom?")
              :else (begin-wait state task chosen trimmed day ids tokenize))
      state)))

;; Replacing N tasks by M

(def ^:private states
  "A new line's states, from To do to Done."
  [:to-do :in-progress :done])

(defn- new-line
  [title line-state]
  {:draft/title title :draft/state line-state :draft/estimate-text ""})

(defn- open-replace
  "Opens the replacement display for the marked tasks or the task under
  the cursor; with :from-query, for no old tasks and the query as the
  first title."
  [state [from] _ tokenize]
  (with-board state tokenize
    (fn [entities _ cursor]
      (let [from-query? (= :from-query from)
            marked (set (:state/marked state))
            old (cond from-query? #{}
                      (seq marked) marked
                      cursor #{cursor}
                      :else #{})
            line-state (if (= 1 (count old)) (board/column (entities (first old))) :to-do)
            title (if from-query? (str/trim (or (:state/query state) "")) "")]
        (if (and (not from-query?) (empty? old))
          state
          (cond-> (assoc state
                         :state/screen :replace
                         :state/marked #{}
                         :state/draft {:draft/old old
                                       :draft/lines [(new-line title line-state)]})
            from-query? (assoc :state/query "")))))))

(defn- next-line
  [state [i] _ _]
  (let [lines (get-in state [:state/draft :draft/lines])
        i (min (or i 0) (dec (count lines)))]
    (assoc-in state [:state/draft :draft/lines]
              (vec (concat (subvec lines 0 (inc i))
                           [(new-line "" :to-do)]
                           (subvec lines (inc i)))))))

(defn- step-state
  [d]
  (fn [state [i] _ _]
    (if (get-in state [:state/draft :draft/lines i])
      (update-in state [:state/draft :draft/lines i :draft/state]
                 (fn [s]
                   (states (-> (index-of states s) (+ d) (max 0) (min 2)))))
      state)))

(defn- typed-lines
  "Lines i of the draft replaced by one line per line of text, each with
  line i's state, so that pasting several lines makes several tasks."
  [lines i text]
  (let [titles (tasks/titles text)
        {:draft/keys [state] :as line} (lines i)]
    (vec (concat (subvec lines 0 i)
                 (if (< 1 (count titles))
                   (map #(new-line % state) titles)
                   [(assoc line :draft/title text)])
                 (subvec lines (inc i))))))

(defn- typed
  "Text typed into the query bar, the open field, or a line of the
  replacement display: target is :query, :field, or [:line i :title] or
  [:line i :estimate]."
  [state [target text] _ _]
  (let [text (or text "")]
    (cond
      (= :query target) (assoc state :state/query text)
      (= :field target) (-> state
                            (assoc-in [:state/field :field/text] text)
                            (update :state/field dissoc :problem/message))
      (and (vector? target) (get-in state [:state/draft :draft/lines (second target)]))
      (let [[_ i part] target]
        (if (= :title part)
          (update-in state [:state/draft :draft/lines] typed-lines i text)
          (assoc-in state [:state/draft :draft/lines i :draft/estimate-text] text)))
      :else state)))

(defn- new-task-patch
  [id {:draft/keys [title state]} after start day]
  (cond-> {:patch/entity id :task/title (str/trim title)}
    (seq after) (assoc :task/after after)
    (#{:in-progress :done} state) (assoc :task/started start)
    (and (#{:in-progress :done} state) (= start day)) (assoc :task/worked-on #{day})
    (= :done state) (assoc :task/finished day :task/worked-on #{day})))

(defn- dependent-patch
  "The new :task/after of a task that came after one of the old tasks."
  [id task old new-ids]
  (let [after (set/union (set/difference (set (:task/after task)) old) new-ids)]
    (if (seq after)
      {:patch/entity id :task/after after}
      {:patch/entity id :patch/retract #{:task/after}})))

(defn replacement-act
  "The act, without :act/id and :act/at, that replaces the old tasks of
  draft by its lines with a title, on day the recording day: each new
  task with its title, state and dates; a new estimate for each line
  whose estimate parses; each old task's :task/replaced-by; and the
  :task/after of every task that came after an old task. New tasks come
  after what the old tasks came after, and inherit the start of the one
  old task that had started, if exactly one had (ux-journey, Stage 4).
  Uses 2n of ids, for n new tasks."
  [entities {:draft/keys [old lines]} day ids]
  (let [lines (filter #(seq (str/trim (:draft/title %))) lines)
        n (count lines)
        task-ids (vec (take n ids))
        estimate-ids (vec (take n (drop n ids)))
        started (filter #(:task/started (entities %)) old)
        start (if (= 1 (count started)) (:task/started (entities (first started))) day)
        after (set/difference (set (mapcat #(:task/after (entities %)) old)) old)
        new-ids (set task-ids)]
    {:act/action :action/replace
     :act/patches (vec (concat
                        (map #(new-task-patch %1 %2 after start day) task-ids lines)
                        (for [[line task-id estimate-id] (map vector lines task-ids estimate-ids)
                              :let [{:estimate/keys [amount]} (estimates/parse (:draft/estimate-text line))]
                              :when amount]
                          {:patch/entity estimate-id
                           :estimate/task task-id
                           :estimate/amount amount
                           :estimate/unit :working-days})
                        (for [o (sort-by str old)]
                          {:patch/entity o :task/replaced-by new-ids})
                        (for [[id task] (sort-by (comp str key) (board/tasks entities))
                              :when (and (not (old id)) (seq (set/intersection (set (:task/after task)) old)))]
                          (dependent-patch id task old new-ids))))}))

(defn- record-replacement
  [state ids tokenize]
  (let [entities (screen/current state)
        draft (:state/draft state)
        act (replacement-act entities draft (screen/recording-day state) ids)
        n (count (filter #(seq (str/trim (:draft/title %))) (:draft/lines draft)))
        first-new (:patch/entity (first (:act/patches act)))]
    (if (and (zero? n) (empty? (:draft/old draft)))
      (-> state (dissoc :state/draft) (assoc :state/screen :stand-up))
      (cond-> (-> state
                  (dissoc :state/draft)
                  (assoc :state/screen :stand-up)
                  (record act (drop (* 2 n) ids) tokenize))
        first-new (assoc :state/cursor first-new)))))

(defn- record-command
  "Enter in a field, or Cmd+Enter in the replacement display."
  [state _ ids tokenize]
  (cond
    (= :replace (:state/screen state)) (record-replacement state ids tokenize)
    (:state/field state) (record-field state ids tokenize)
    :else state))

;; Comes after

(defn- set-after
  "The task under the cursor comes after exactly the marked tasks. Not
  recorded if that would make a cycle; the problem names the cycle."
  [state _ ids tokenize]
  (on-cursor state tokenize
             (fn [entities cursor]
               (let [on-board (board/tasks entities)
                     marked (set/intersection (disj (set (:state/marked state)) cursor)
                                              (set (keys on-board)))
                     proposed (-> (update-vals on-board
                                               (fn [t] {:task/after (set (filter on-board (:task/after t)))}))
                                  (assoc-in [cursor :task/after] marked))
                     {looped :schedule/cycle} (schedule/topological-order proposed)]
                 (if looped
                   (assoc state :state/problems
                          [{:problem/message
                            (str "that would make a cycle: "
                                 (str/join ", " (sort (map #(:task/title (entities %)) looped))))}])
                   (-> state
                       (assoc :state/marked #{})
                       (record {:act/action :action/set-after
                                :act/patches [(if (seq marked)
                                                {:patch/entity cursor :task/after marked}
                                                {:patch/entity cursor :patch/retract #{:task/after}})]}
                               ids tokenize)))))))

;; The frame

(defn- clear
  "Escape: closes the open field, else leaves the replacement display,
  else clears the query."
  [state _ _ _]
  (cond
    (:state/field state) (dissoc state :state/field)
    (= :replace (:state/screen state)) (-> state (dissoc :state/draft) (assoc :state/screen :stand-up))
    (seq (:state/query state)) (assoc state :state/query "")
    :else state))

(defn- show
  [screen-name]
  (fn [state _ _ _]
    (-> state (dissoc :state/field) (assoc :state/screen screen-name))))

(defn- day-back
  [state _ _ _]
  (assoc state :state/recording-day (days/previous-working-day (screen/recording-day state))))

(defn- day-forward
  "One working day forward, but not past today."
  [state _ _ _]
  (let [next-day (days/next-working-day (screen/recording-day state))]
    (if (pos? (compare next-day (screen/today state)))
      state
      (assoc state :state/recording-day next-day))))

(defn- mark
  [state _ _ tokenize]
  (on-cursor state tokenize
             (fn [_ cursor]
               (update state :state/marked #(if (contains? (set %) cursor)
                                              (disj (set %) cursor)
                                              (conj (set %) cursor))))))

(defn- cursor-to
  [state [id] _ _]
  (if id (assoc state :state/cursor id) state))

(def handlers
  "Command name to the function that carries it out: of the state, the
  command's arguments, the ids and the tokenizer."
  {:command/cursor-up (fn [state _ _ tokenize] (move state tokenize 0 -1))
   :command/cursor-down (fn [state _ _ tokenize] (move state tokenize 0 1))
   :command/cursor-left (fn [state _ _ tokenize] (move state tokenize -1 0))
   :command/cursor-right (fn [state _ _ tokenize] (move state tokenize 1 0))
   :command/cursor cursor-to
   :command/mark mark
   :command/query (fn [state [text] _ _] (if text (assoc state :state/query text) state))
   :command/typed typed
   :command/day-back day-back
   :command/day-forward day-forward
   :command/show-stand-up (show :stand-up)
   :command/show-schedule (show :schedule)
   :command/clear clear
   :command/open-replace open-replace
   :command/open-estimate (open-field :estimate (constantly ""))
   :command/open-title (open-field :title :task/title)
   :command/open-wait open-wait
   :command/reason-person (reason :person)
   :command/reason-outside-party (reason :outside-party)
   :command/reason-not-in-plan (reason :not-in-plan)
   :command/record record-command
   :command/next-line next-line
   :command/state-up (step-state -1)
   :command/state-down (step-state 1)
   :command/undo (fn [state _ ids tokenize] (undo-or-redo state history/undo-act ids tokenize))
   :command/redo (fn [state _ ids tokenize] (undo-or-redo state history/redo-act ids tokenize))
   :action/record-start (cursor-act :action/record-start start-patch)
   :action/record-finish (cursor-act :action/record-finish finish-patch)
   :action/record-worked-on (cursor-act :action/record-worked-on worked-patch)
   :action/set-after set-after})

(defn run
  "The state after command, a vector that names the command first, such
  as [:command/typed :query \"runout\"]. ids is a seq of fresh UUIDs,
  of which the command takes what it needs; tokenize splits text into
  words for the search index. The state's :state/now is the time of
  any act recorded. A command that is not known leaves the state as it
  is."
  [state [command-name & args] ids tokenize]
  (if-let [handler (handlers command-name)]
    (handler state args ids tokenize)
    state))

(defn start
  "state made ready for the screens: the search index built, and the
  stand-up board shown unless a screen is already chosen."
  [state tokenize]
  (cond-> (assoc state :state/index (screen/index-of tokenize (screen/current state)))
    (nil? (:state/screen state)) (assoc :state/screen :stand-up)))
