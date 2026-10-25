(ns sonouchi.core.view
  "The screens: pure functions from the state to hiccup, which the shell
  renders with Replicant. Event handlers in the hiccup are data: action
  vectors, in which the keyword :field/text stands for the text of the
  input the event came from, which the shell fills in. The screens are
  described in doc/ux-journey.md."
  (:require [clojure.string :as str]
            [sonouchi.core.board :as board]
            [sonouchi.core.chain :as chain]
            [sonouchi.core.days :as days]
            [sonouchi.core.estimates :as estimates]
            [sonouchi.core.keys :as keys]
            [sonouchi.core.screen :as screen]))

(def ^:private slow-ms
  "V12: every act should respond within 0.1 s."
  100)

(def ^:private screen-names
  {:stand-up "STAND-UP" :replace "REPLACE" :schedule "SCHEDULE" :record "RECORD"})

(def ^:private reason-names
  {:person "a person" :outside-party "an outside party" :not-in-plan "not in the plan"})

(def ^:private state-names
  {:to-do "To do" :in-progress "In progress" :done "Done"})

(defn- counted
  [n word]
  (str n " " word (when (not= 1 n) "s")))

(defn- hint
  "Keys and what they do, as one line of hiccup (V11): each key first,
  in a key cap (kbd), then what it does."
  [platform pairs]
  (interpose " · " (for [[command what] pairs]
                     [:span.hint [:kbd (keys/label platform command)] (str " " what)])))

;; The frame (V9)

(def ^:private views
  "The screens the menu offers, always in this order (V10), each with
  the command that shows it, its name, and the question it answers
  (V1) with what is done there."
  [{:screen :stand-up
    :command :command/show-stand-up
    :title "Stand-up"
    :purpose (str "Which tasks have no record yet for the recording day? Record what happened: "
                  "started, finished, worked on, waiting. Replace, add or drop tasks.")}
   {:screen :schedule
    :command :command/show-schedule
    :title "Schedule"
    :purpose (str "When will each task finish, and which tasks set the last date? "
                  "Estimate tasks, and say which come after which.")}
   {:screen :record
    :command :command/show-record
    :title "Record"
    :purpose "Save the record to a file, or replace it with one from a file."}])

(defn- menu
  "The mascot and the screen's name, as one button that opens and closes
  the list of screens, each with its key, its name and its purpose."
  [{:state/keys [screen platform menu-open]}]
  [:nav.menu
   [:button.app {:title (str "Choose a view: "
                             (str/join " · " (for [{:keys [command title]} views]
                                               (str (keys/label platform command) " " title))))
                 :aria-expanded (if menu-open "true" "false")
                 :on {:click [:command/toggle-menu]}}
    [:img {:src "/tanuki-64.png" :alt "sonouchi" :width 32 :height 32}]
    [:span.part (screen-names screen "STAND-UP")]]
   (when menu-open
     [:ul.views
      (for [{:keys [command title purpose] view-screen :screen} views]
        [:li {:replicant/key view-screen}
         [:button {:class (when (= view-screen screen) "current")
                   :on {:click [command]}}
          [:kbd (keys/label platform command)]
          [:span.name title]
          [:span.purpose purpose]]])])])

(defn- day-help
  "The recording day's hover text: what it is for, and how it moves."
  [platform]
  (let [k #(keys/label platform %)]
    (str "Recording day: " (k :action/record-start) " started, " (k :action/record-finish) " finished, "
         (k :action/record-worked-on) " worked on and " (k :command/open-wait)
         " waiting are saved for this day. "
         "It starts on the last working day. " (k :command/day-back)
         " earlier catches up on missed days; " (k :command/day-forward) " later comes back.")))

(defn- day-keys
  "The keys that move the recording day, each key first and each also a
  button, so that a click does what the key does (V11). Later is off
  when there is no later-day, the day being today."
  [platform later-day]
  [:span.day-keys
   [:button.hint {:on {:click [:command/day-back]}}
    [:kbd (keys/label platform :command/day-back)] " earlier"]
   " · "
   [:button.hint (cond-> {:on {:click [:command/day-forward]}}
                   (nil? later-day) (assoc :disabled true))
    [:kbd (keys/label platform :command/day-forward)] " later"]])

(defn- frame
  [{:state/keys [query platform] :as state} shown total]
  (let [day (screen/recording-day state)
        stale? (< 1 (days/working-days-between day (screen/today state)))]
    [:header
     (menu state)
     ;; The count inside the query bar's border, so that it reads as the
     ;; bar's (common region, Palmer 1992, visual 1.3).
     [:label.search
      [:input#query {:type "text"
                     :value (or query "")
                     :autocomplete "off"
                     :placeholder "find a task, or type a new one"
                     :on {:input [:command/typed :query :field/text]}}]
      [:span.count (if (str/blank? (or query ""))
                     (counted total "task")
                     (str shown " of " (counted total "task")))]]
     [:span.day {:class (when stale? "stale")
                 :title (day-help platform)}
      [:span.small.relative (str "recording " (days/relative day (screen/today state)) ",")]
      " "
      [:span.date (days/label day)]
      (day-keys platform (screen/later-day state))]]))

(defn- problem-list
  [problems]
  [:ul.problems
   (for [{:problem/keys [path message]} problems]
     [:li (str message (when (seq path) (str " at " (pr-str path))))])])

(defn- footer
  [{:state/keys [last-act-ms]}]
  [:footer
   (when last-act-ms
     (if (< slow-ms last-act-ms)
       [:span.slow (str "last act " last-act-ms " ms: over 0.1 s")]
       [:span (str "last act " last-act-ms " ms")]))])

;; Cards and their fields

(defn- field-input
  [kind text attrs]
  [:input#field (merge {:type "text"
                        :value text
                        :autocomplete "off"
                        :data-kind (name kind)
                        :on {:input [:command/typed :field :field/text]}}
                       attrs)])

(defn- card-field
  "The one small field open on a card: a correction, an estimate or a
  wait."
  [platform {:field/keys [kind text] :problem/keys [message] chosen :wait/reason}]
  [:div.field
   (case kind
     :estimate [:span
                (field-input kind text {:inputmode "decimal" :size 4})
                " working days, as likely more as less"]
     :wait [:span "waiting on "
            (if chosen
              [:span [:span.reason (reason-names chosen)] ": "
               (field-input kind text {:placeholder "whom" :size 16})]
              (field-input :wait-reason "" {:placeholder "1 person · 2 outside party · 3 not in the plan"
                                            :size 40}))]
     nil)
   [:span.keys (hint platform [[:command/next-line "record"] [:command/clear "cancel"]])]
   (when message [:span.problem message])])

(def ^:private card-keys
  [[:action/record-start "started"]
   [:action/record-finish "finished"]
   [:action/record-worked-on "worked on"]
   [:command/open-wait "waiting"]
   [:command/open-title "edit"]
   [:command/open-estimate "estimate"]
   [:action/set-after "after"]
   [:command/mark "mark"]
   [:command/open-replace "replace"]])

(defn- wait-line
  [{:wait/keys [reason on began]}]
  [:div.wait
   (if (= :not-in-plan reason)
     "waiting: not in the plan"
     (str "waiting on " on " since " (days/label began)))])

(defn- card
  [id {:keys [entities latest waits recorded cursor marked changed platform field]}]
  (let [task (entities id)
        here? (= id cursor)
        field-here (when (= id (:field/task field)) field)]
    [:div.card {:replicant/key id
                :class (str/join " " (keep identity
                                           [(when here? "cursor")
                                            (when (marked id) "marked")
                                            (when (recorded id) "recorded")
                                            (when (changed id) "changed")]))
                :on {:click [:command/cursor id]}}
     [:div.title
      [:span.mark (cond (marked id) "✗" (recorded id) "✓" :else "")]
      (if (= :title (:field/kind field-here))
        (field-input :title (:field/text field-here) {:size 40})
        [:span.words (:task/title task)])]
     (when-not (latest id) [:div.no-estimate "no estimate"])
     (when-let [wait (waits id)] (wait-line wait))
     (when (and field-here (not= :title (:field/kind field-here)))
       (card-field platform field-here))
     (when (and here? (not field-here))
       [:div.keys (hint platform card-keys)])
     (when (= :title (:field/kind field-here))
       [:div.keys (hint platform [[:command/next-line "record"] [:command/clear "cancel"]])])]))

;; Stage 3: the stand-up board

(defn- board-view
  [cols view]
  [:section.board
   (for [[[col-key col-name] ids] (map vector screen/column-names cols)]
     [:div.column {:replicant/key col-key}
      [:h2 col-name [:span.count (str " " (count ids))]]
      (for [id ids] (card id view))])
   (when (every? empty? cols)
     [:p.empty "No tasks. Type a title in the bar above and press " [:kbd "Enter"] "."])])

;; Stage 4: replacing N tasks by M

(defn- line-note
  [{:draft/keys [state estimate-text]}]
  (let [{:problem/keys [message]} (when-not (str/blank? estimate-text)
                                    (estimates/parse estimate-text))]
    (cond
      message [:span.problem message]
      (and (not= :to-do state) (not (str/blank? estimate-text)))
      [:span.note "estimated after its start: kept, not counted"]
      :else nil)))

(defn- replace-view
  [{:state/keys [platform draft]} entities]
  (let [{:draft/keys [old lines]} draft]
    [:section.replace
     [:div.old
      (if (seq old)
        (for [id (sort-by str old)]
          [:div.old-task {:replicant/key id} (:task/title (entities id))])
        [:div.old-task.none "nothing"])]
     [:div.arrow "→"]
     [:div.lines
      (for [[i {:draft/keys [title state estimate-text] :as line}] (map-indexed vector lines)]
        [:div.line {:replicant/key i}
         [:span.state (state-names state)]
         [:input.title {:type "text"
                        :value title
                        :autocomplete "off"
                        :placeholder "a few words describing the task"
                        :data-line i
                        :data-part "title"
                        :on {:input [:command/typed [:line i :title] :field/text]
                             :paste [:command/paste i]}}]
         [:input.estimate {:type "text"
                           :value estimate-text
                           :autocomplete "off"
                           :inputmode "decimal"
                           :size 4
                           :data-line i
                           :data-part "estimate"
                           :on {:input [:command/typed [:line i :estimate] :field/text]}}]
         [:span.unit "working days"]
         (line-note line)])
      [:p.keys (hint platform [[:command/next-line "next line"]
                               [:command/state-up "state"]
                               [:command/record "record"]
                               [:command/clear "cancel"]])
       " · " [:span.hint [:kbd "Tab"] " estimate"]]]]))

;; Stage 5: the schedule

(defn- chain-drawing
  "The chain behind id, as SVG: layers left to right, a line for each
  comes-after link."
  [entities id]
  (let [{:chain/keys [layers edges]} (chain/layout (board/tasks entities) id)
        dx 170
        dy 32
        at (into {} (for [[x layer] (map-indexed vector layers)
                          [y i] (map-indexed vector layer)]
                      [i [(+ 10 (* x dx)) (+ 20 (* y dy))]]))
        width (+ 10 (* dx (count layers)))
        height (+ 20 (* dy (apply max 1 (map count layers))))]
    [:svg.chain {:viewBox (str "0 0 " width " " height)
                 :width width :height height}
     (for [[from to] (sort-by str edges)
           :let [[x1 y1] (at from) [x2 y2] (at to)]]
       [:line {:x1 (+ x1 150) :y1 (- y1 5) :x2 x2 :y2 (- y2 5)}])
     (for [[i [x y]] at]
       [:text {:x x :y y :class (when (= i id) "here")}
        (let [title (:task/title (entities i))]
          (if (< 22 (count title)) (str (subs title 0 21) "…") title))])]))

(def ^:private schedule-keys
  "The keys the schedule's cursor takes (ux-journey, Stage 5)."
  [[:command/open-estimate "estimate"]
   [:action/set-after "after"]
   [:command/mark "mark"]
   [:action/record-start "started"]
   [:action/record-finish "finished"]])

(defn- line-class
  "The classes of a row or a line of the schedule: the cursor, the
  marks, and what the last act changed or moved (V7)."
  [id {:keys [cursor marked changed]} extra]
  (str/join " " (keep identity [extra
                                (when (= id cursor) "cursor")
                                (when (marked id) "marked")
                                (when (changed id) "changed")])))

(defn- after-text
  [entities task]
  (when (seq (:task/after task))
    (str "after " (str/join ", " (sort (map #(:task/title (entities %)) (:task/after task)))))))

(defn- schedule-row
  "One task's row, and under it the field open on it, if any. Its
  estimate is hidden while the field is open (ux-journey, Estimating)."
  [id {:keys [entities latest field platform times critical today] :as view}]
  (let [{:schedule/keys [start-day finish-day]} (times id)
        task (entities id)
        field-here (when (= id (:field/task field)) field)]
    (cons
     [:tr {:replicant/key id
           :class (line-class id view (when (critical id) "critical"))
           :on {:click [:command/cursor id]}}
      [:td.bar (if (critical id) "▌" "")]
      [:td.mark (if ((:marked view) id) "✗" "")]
      [:td.title [:span.words (:task/title task)]]
      [:td.estimate (when-not field-here (estimates/show (latest id)))]
      [:td.dates (str (days/label (board/start-day today start-day))
                      " – " (days/label (board/last-day today finish-day)))]
      [:td.after (after-text entities task)]]
     (when field-here
       [[:tr.field-row {:replicant/key [id :field]}
         [:td] [:td]
         [:td {:colspan 4} (card-field platform field-here)]]]))))

(defn- not-estimated-line
  [id stopper {:keys [entities field platform] :as view}]
  (let [field-here (when (= id (:field/task field)) field)]
    [:li {:replicant/key id
          :class (line-class id view nil)
          :on {:click [:command/cursor id]}}
     [:span.mark (if ((:marked view) id) "✗" "")]
     [:span.words (:task/title (entities id))]
     (when (not= id stopper)
       [:span.quiet (str " · after " (:task/title (entities stopper)))])
     (when field-here (card-field platform field-here))]))

(defn- schedule-view
  "The schedule: the rows and the tasks not estimated that the query
  leaves visible, with the cursor, the marks and the open field drawn on
  them as on the board's cards. view is the map root makes."
  [state {:keys [entities latest cursor shown platform] :as view}]
  (let [{:schedule/keys [times critical] looped :schedule/cycle} (screen/scheduled state entities latest)
        today (screen/today state)
        row-ids (screen/shown-rows state entities latest shown)
        unscheduled (screen/not-estimated entities latest)
        loose (screen/shown-not-estimated state entities latest shown)
        last-finish (apply max 0 (map :schedule/finish-day (vals times)))
        view (assoc view :times times :critical (set critical) :today today)]
    [:section.schedule
     (cond
       looped [:p.problem (str "these tasks come after each other in a circle: "
                               (str/join ", " (sort (map #(:task/title (entities %)) looped))))]
       (empty? times) [:p.empty "Nothing to schedule yet: estimate a task with "
                       [:kbd (keys/label platform :command/open-estimate)] "."]
       :else
       [:div
        [:p.summary
         (str "finishes " (days/label (board/last-day today last-finish)))
         [:span.critical-note
          (str " · critical: "
               (str/join ", " (map #(:task/title (entities %))
                                   (filter (set critical) row-ids))))]]
        [:table.rows
         [:tbody (mapcat #(schedule-row % view) row-ids)]]])
     (when (seq loose)
       [:div.not-estimated
        [:h3 "Not estimated"]
        [:ul (for [id loose] (not-estimated-line id (unscheduled id) view))]])
     (when cursor
       [:div.behind
        [:p.keys (hint platform schedule-keys)]
        [:h3 (str (:task/title (entities cursor)) " comes after")]
        (chain-drawing entities cursor)])]))

;; The record: export and import

(defn- record-view
  "Export and import of the log, with its size and the days it spans,
  so that one file can be told from another."
  [{:state/keys [log utc-offset-minutes]} entities]
  (let [day-of #(days/local-day (:act/at %) utc-offset-minutes)
        replaced (count (filter #(contains? % :task/replaced-by) (vals entities)))]
    [:section.record
     [:p.size
      (if (seq log)
        (str (counted (count log) "act") ", from " (day-of (first log)) " to " (day-of (peek log)) "; "
             (counted (count (board/tasks entities)) "task") " on the board, "
             replaced " replaced or dropped")
        "The record is empty.")]
     [:div.export
      [:h3 "Export"]
      [:p "Saves the whole record to a file named by today's date, as EDN. The record stays in this browser too."]
      [:button {:on {:click [:command/export]}} "Export"]]
     [:div.import
      [:h3 "Import"]
      [:p (str "Replaces the record with the one in a file. The record it replaces is kept in this browser "
               "first. A file that does not hold a record changes nothing, and says why.")]
      [:input {:type "file" :accept ".edn" :on {:change [:command/import]}}]]]))

;; The page

(defn root
  "The whole page for state, with tokenize, the function that splits
  text into words for the search. state holds :state/log,
  :state/platform, :state/now and :state/utc-offset-minutes, and the
  screen's own keys as the commands set them."
  [{:state/keys [problems screen] :as state} tokenize]
  (let [entities (screen/current state)
        shown (screen/matching state tokenize entities)
        cols (screen/columns state entities shown)
        cursor (screen/cursor-on-screen state entities shown cols)
        latest (screen/latest-estimates state entities)
        view {:entities entities
              :shown shown
              :platform (:state/platform state)
              :field (:state/field state)
              :latest latest
              :waits (board/open-waits entities)
              :recorded (let [days (board/recorded-days
                                    entities (screen/all-estimates state entities)
                                    (:state/utc-offset-minutes state))
                              day (screen/recording-day state)]
                          (fn [id] (contains? (days id) day)))
              :cursor cursor
              :marked (set (:state/marked state))
              :changed (screen/changed-by-last-act state entities)}]
    [:div.page
     (frame state (count shown) (count (board/tasks entities)))
     [:main
      (when (seq problems) (problem-list problems))
      (case screen
        :replace (replace-view state entities)
        :record (record-view state entities)
        :schedule (schedule-view state (update view :changed into (screen/moved-by-last-act state)))
        (board-view cols view))]
     (footer state)]))
