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
            [sonouchi.core.requests :as requests]
            [sonouchi.core.screen :as screen]))

(def ^:private slow-ms
  "V12: every act should respond within 0.1 s."
  100)

(def ^:private screen-names
  {:stand-up "STAND-UP" :replace "REPLACE" :schedule "PLAN" :record "RECORD" :requests "REQUESTS"})

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
    :title "Plan"
    :purpose (str "What comes after what, when will each task finish, and which tasks set the last date? "
                  "Estimate tasks, and say which come after which.")}
   {:screen :record
    :command :command/show-record
    :title "Record"
    :purpose "Save the record to a file, or replace it with one from a file."}
   {:screen :requests
    :command :command/show-requests
    :title "Requests"
    :purpose (str "What have I promised, to whom, and when will each be done? "
                  "Record requests and promises, and say which tasks serve which.")}])

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
  "How long the last act took (V12), and the keys that undo and redo it
  (V11, V13), once there is an act."
  [{:state/keys [last-act-ms platform]}]
  [:footer
   (when last-act-ms
     (list
      (if (< slow-ms last-act-ms)
        [:span.slow (str "last act " last-act-ms " ms: over 0.1 s")]
        [:span (str "last act " last-act-ms " ms")])
      " · "
      (hint platform [[:command/undo "undo"] [:command/redo "redo"]])))])

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
  "The one small field open on a card or a row: a correction, an
  estimate, a wait, a promise, or a request's asker and words."
  [{:keys [platform today]} {:field/keys [kind text] :problem/keys [message] chosen :wait/reason}]
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
     :promise [:span "promised for "
               (field-input kind text {:placeholder "nov 6" :size 12})
               (when-let [day (days/parse-day text today)]
                 [:span.echo (str " " (days/label day))])]
     :request [:span (field-input kind text {:placeholder "who: what was asked" :size 50})]
     :request-words [:span (field-input kind text {:size 50})]
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
   [:action/set-serves "serves"]
   [:command/mark "mark"]
   [:command/open-replace "replace"]])

(defn- wait-line
  "On whom the task waits and since when, and the day they promised,
  if any (ux-requests, \"The board\")."
  [{:wait/keys [reason on began]} promised]
  [:div.wait
   (if (= :not-in-plan reason)
     "waiting: not in the plan"
     (str "waiting on " on " since " (days/label began)
          (when promised (str ", promised " (days/label (:promise/day promised))))))])

(defn- serves-line
  "Who is expecting the task, and by when (Bellotti 2004, ux-requests,
  \"The board\"): \"for Tanaka · by Fri Nov 6\". Nil when the task
  serves no open request."
  [task {:keys [requests promises]}]
  (let [open-ids (->> (:task/serves task)
                      (filter #(and (requests %) (not (:request/closed (requests %)))))
                      (sort-by #(:request/asker (requests %))))]
    (when (seq open-ids)
      [:div.for
       (str "for " (str/join ", " (map #(:request/asker (requests %)) open-ids))
            (when-let [day (->> open-ids (keep #(:promise/day (promises %))) sort first)]
              (str " · by " (days/label day))))])))

(defn- card
  [id {:keys [entities latest waits recorded cursor marked changed platform field] :as view}]
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
     (serves-line task view)
     (when-let [wait (waits id)] (wait-line wait (get-in view [:promises (:patch/entity wait)])))
     (when (and field-here (not= :title (:field/kind field-here)))
       (card-field view field-here))
     (when (and here? (not field-here))
       [:div.keys (hint platform (cond-> card-keys
                                   (waits id) (conj [:command/open-promise "promised"])))])
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

(defn- short-title
  [title]
  (if (< 22 (count title)) (str (subs title 0 21) "…") title))

(defn- graph-drawing
  "The whole graph of the unfinished tasks that are linked by comes
  after, as SVG: layers left to right, a line for each link, in full
  ink where both ends are critical (V3), and under each title the day
  it finishes, or that it has no estimate. The cursor's task is bold.
  Nil when no task comes after another."
  [{:keys [entities cursor times critical today]}]
  (let [tasks (into {} (remove (fn [[_ t]] (:task/finished t)) (board/tasks entities)))
        {:chain/keys [layers edges]} (chain/graph tasks)
        dx 170
        dy 44
        at (into {} (for [[x layer] (map-indexed vector layers)
                          [y i] (map-indexed vector layer)]
                      [i [(+ 10 (* x dx)) (+ 20 (* y dy))]]))
        width (+ 10 (* dx (count layers)))
        height (+ 20 (* dy (apply max 1 (map count layers))))]
    (when (seq layers)
      [:svg.chain {:viewBox (str "0 0 " width " " height)
                   :width width :height height}
       (for [[from to] (sort-by str edges)
             :let [[x1 y1] (at from) [x2 y2] (at to)]]
         [:line {:x1 (+ x1 150) :y1 (- y1 5) :x2 x2 :y2 (- y2 5)
                 :class (when (and (critical from) (critical to)) "critical")}])
       (for [[i [x y]] at
             :let [finish (:schedule/finish-day (times i))]]
         [:g {:class (when (= i cursor) "here")}
          [:text.title {:x x :y y} (short-title (:task/title (entities i)))]
          [:text.when {:x x :y (+ y 14)}
           (if finish (days/label (board/last-day today finish)) "no estimate")]])])))

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

(defn- waiting-text
  "What the plan's row says of an open wait with a promise: whom, the
  day promised, and \"still waiting\" once it has passed (ux-requests,
  \"The plan\")."
  [{:keys [waits promises today]} id]
  (when-let [wait (waits id)]
    (when-let [day (:promise/day (promises (:patch/entity wait)))]
      (if (pos? (compare today day))
        (str "promised " (days/label day) ", still waiting")
        (str "waiting on " (:wait/on wait) ", promised " (days/label day))))))

(defn- schedule-row
  "One task's row, and under it the field open on it, if any. Its
  estimate is hidden while the field is open (ux-journey, Estimating)."
  [id {:keys [entities latest field times critical today] :as view}]
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
      [:td.after (str/join " · " (keep identity [(after-text entities task) (waiting-text view id)]))]]
     (when field-here
       [[:tr.field-row {:replicant/key [id :field]}
         [:td] [:td]
         [:td {:colspan 4} (card-field view field-here)]]]))))

(defn- not-estimated-line
  [id stopper {:keys [entities field] :as view}]
  (let [field-here (when (= id (:field/task field)) field)]
    [:li {:replicant/key id
          :class (line-class id view nil)
          :on {:click [:command/cursor id]}}
     [:span.mark (if ((:marked view) id) "✗" "")]
     [:span.words (:task/title (entities id))]
     (when (not= id stopper)
       [:span.quiet (str " · after " (:task/title (entities stopper)))])
     (when field-here (card-field view field-here))]))

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
        [:h3 "What comes after what"]
        (or (graph-drawing view)
            [:p.empty "No task comes after another yet: mark the tasks it comes after with "
             [:kbd (keys/label platform :command/mark)] ", then "
             [:kbd (keys/label platform :action/set-after)] " on the task."])])]))

;; The requests screen (ux-requests)

(def ^:private request-keys
  [[:command/open-request "new"]
   [:command/open-promise "promise"]
   [:action/set-serves "serves"]
   [:action/close-request "close"]
   [:command/open-request-words "edit"]
   [:command/mark-request "mark"]])

(defn- finish-text
  [today finish]
  (cond
    (number? finish) (days/label (board/last-day today finish))
    (= :done finish) "all tasks done"
    :else nil))

(defn- request-row
  "One open request's row: asker, asked, words, promised, when it will
  be done, how many tasks serve it, and its slack; and under it the
  field open on it, if any. A request with no slack or no tasks is
  urgent, the one thing that pops out (V3)."
  [id {:keys [requests promises board times today cursor marked-requests field] :as view}]
  (let [{:request/keys [asker words asked]} (requests id)
        promised (:promise/day (promises id))
        n (count (requests/serving board id))
        finish (requests/finish-day board times id)
        slack (when (and promised (number? finish)) (requests/slack today finish promised))
        urgent? (or (zero? n) (and slack (<= slack 0)))
        field-here (when (= id (:field/request field)) field)]
    (cons
     [:tr {:replicant/key id
           :class (str/join " " (keep identity [(when (= id cursor) "cursor")
                                                (when (marked-requests id) "marked")
                                                (when urgent? "urgent")]))
           :on {:click [:command/cursor-request id]}}
      [:td.mark (if (marked-requests id) "✗" "")]
      [:td.asker [:span.words asker]]
      [:td.dates (days/label asked)]
      [:td.words (or words "")]
      [:td.dates (if (and promised (not= :promise (:field/kind field-here))) (days/label promised) "—")]
      [:td.dates (or (finish-text today finish) (if (zero? n) "no tasks" "not estimated"))]
      [:td.estimate (str n)]
      [:td.slack (when slack (requests/slack-words slack))]]
     (when field-here
       [[:tr.field-row {:replicant/key [id :field]}
         [:td] [:td {:colspan 7} (card-field view field-here)]]]))))

(defn- serving-list
  "The tasks that serve the request under the cursor, in the plan's
  order, each with its state and its finish; the ones the owner could
  work on today for it are marked, as the critical path is."
  [state id {:keys [entities requests board times today latest shown]}]
  (let [ids (requests/serving board id)
        ordered (concat (filter ids (screen/rows state entities latest))
                        (filter ids (screen/shown-not-estimated state entities latest shown))
                        (sort-by str (filter #(:task/finished (board %)) ids)))
        now (requests/next-to-do board times id)
        {:request/keys [asker words]} (requests id)]
    [:div.serving
     [:h3 (str asker " asked" (when words (str " for " words)))]
     (if (seq ids)
       [:ul
        (for [tid ordered
              :let [task (board tid)
                    finish (:schedule/finish-day (times tid))]]
          [:li {:replicant/key tid :class (when (now tid) "next")}
           [:span.bar (if (now tid) "▌" "")]
           [:span.state (state-names (board/column task))]
           [:span.words (:task/title task)]
           [:span.quiet (cond
                          (:task/finished task) (str " " (days/label (:task/finished task)))
                          finish (str " " (days/label (board/last-day today finish)))
                          :else " no estimate")]])]
       [:p.quiet "No tasks serve it yet: mark tasks on the board with "
        [:kbd (keys/label (:state/platform state) :command/mark)] ", then "
        [:kbd (keys/label (:state/platform state) :action/set-serves)] " here."])]))

(defn- requests-view
  "The requests screen: the open requests as a table, the field for a
  new request, and the tasks serving the request under the cursor."
  [state {:keys [entities cursor field platform latest] :as view}]
  (let [ids (screen/request-ids state entities)
        {:schedule/keys [times]} (screen/scheduled state entities latest)
        view (assoc view
                    :board (board/tasks entities)
                    :times (or times {})
                    :today (screen/today state))]
    [:section.requests
     [:p.keys (hint platform request-keys)]
     (when (= :request (:field/kind field))
       [:div.new-request (card-field view field)])
     (if (seq ids)
       [:table.rows
        [:thead
         [:tr [:th] [:th "asker"] [:th "asked"] [:th "words"] [:th "promised"] [:th "finishes"]
          [:th "tasks"] [:th "slack"]]]
        [:tbody (mapcat #(request-row % view) ids)]]
       (when-not field
         [:p.empty "No open requests. Press " [:kbd (keys/label platform :command/open-request)]
          " to record what someone asked of you."]))
     (when cursor (serving-list state cursor view))]))

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
              :marked-requests (set (:state/marked-requests state))
              :requests (requests/all entities)
              :promises (requests/promises (:state/log state) entities)
              :today (screen/today state)
              :changed (screen/changed-by-last-act state entities)}]
    [:div.page
     (frame state (count shown) (count (board/tasks entities)))
     [:main
      (when (seq problems) (problem-list problems))
      (case screen
        :replace (replace-view state entities)
        :record (record-view state entities)
        :requests (requests-view state view)
        :schedule (schedule-view state (update view :changed into (screen/moved-by-last-act state)))
        (board-view cols view))]
     (footer state)]))
