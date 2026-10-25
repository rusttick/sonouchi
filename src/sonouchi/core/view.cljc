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
  {:stand-up "STAND-UP" :replace "REPLACE" :schedule "SCHEDULE"})

(def ^:private reason-names
  {:person "a person" :outside-party "an outside party" :not-in-plan "not in the plan"})

(def ^:private state-names
  {:to-do "To do" :in-progress "In progress" :done "Done"})

(defn- hint
  "Keys and what they do, as one line (V11)."
  [platform pairs]
  (str/join " · " (for [[command what] pairs]
                    (str (keys/label platform command) " " what))))

;; The frame (V9)

(defn- frame
  [{:state/keys [screen query platform] :as state} shown total]
  (let [day (screen/recording-day state)
        stale? (< 1 (days/working-days-between day (screen/today state)))]
    [:header
     [:span.app "sonouchi"]
     [:span.part (screen-names screen "STAND-UP")]
     [:input#query {:type "text"
                    :value (or query "")
                    :autocomplete "off"
                    :placeholder "find a task, or type a new one"
                    :on {:input [:command/typed :query :field/text]}}]
     [:span.count (str shown " of " total)]
     [:span.day {:class (when stale? "stale")}
      (str "recording " (days/label day))
      [:span.keys (str " " (keys/label platform :command/day-back)
                       " " (keys/label platform :command/day-forward))]]
     [:nav.actions
      [:button {:class (when (= :stand-up screen) "current")
                :on {:click [:command/show-stand-up]}}
       (str (keys/label platform :command/show-stand-up) " Stand-up")]
      [:button {:class (when (= :schedule screen) "current")
                :on {:click [:command/show-schedule]}}
       (str (keys/label platform :command/show-schedule) " Schedule")]]
     [:span.tools
      [:button {:on {:click [:command/export]}} "Export"]
      [:label.import "Import "
       [:input {:type "file" :accept ".edn" :on {:change [:command/import]}}]]]]))

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
     [:p.empty "No tasks. Type a title in the bar above and press Enter."])])

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
       " · Tab estimate"]]]))

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

(defn- schedule-view
  [{:state/keys [platform] :as state} entities latest cursor]
  (let [{:schedule/keys [times critical] looped :schedule/cycle} (screen/scheduled state entities latest)
        today (screen/today state)
        row-ids (screen/rows state entities latest)
        unscheduled (screen/not-estimated entities latest)
        last-finish (apply max 0 (map :schedule/finish-day (vals times)))
        shown (filter (set row-ids) (keys times))]
    [:section.schedule
     (cond
       looped [:p.problem (str "these tasks come after each other in a circle: "
                               (str/join ", " (sort (map #(:task/title (entities %)) looped))))]
       (empty? times) [:p.empty "Nothing to schedule yet: estimate a task with t."]
       :else
       [:div
        [:p.summary
         (str "finishes " (days/label (board/last-day today last-finish)))
         [:span.critical-note
          (str " · critical: "
               (str/join ", " (map #(:task/title (entities %))
                                   (filter critical row-ids))))]]
        [:table.rows
         [:tbody
          (for [id row-ids
                :let [{:schedule/keys [start-day finish-day]} (times id)
                      task (entities id)]]
            [:tr {:replicant/key id
                  :class (str/join " " (keep identity [(when (critical id) "critical")
                                                       (when (= id cursor) "cursor")]))
                  :on {:click [:command/cursor id]}}
             [:td.bar (if (critical id) "▌" "")]
             [:td.title (:task/title task)]
             [:td.estimate (estimates/show (latest id))]
             [:td.dates (str (days/label (board/start-day today start-day))
                             " – " (days/label (board/last-day today finish-day)))]
             [:td.after (when (seq (:task/after task))
                          (str "after " (str/join ", " (sort (map #(:task/title (entities %))
                                                                  (:task/after task))))))]])]]
        (when (and cursor ((set shown) cursor))
          [:div.behind
           [:h3 (str (:task/title (entities cursor)) " comes after")]
           (chain-drawing entities cursor)])
        (when cursor
          [:p.keys (hint platform [[:command/open-estimate "estimate"]
                                   [:action/set-after "after"]
                                   [:command/mark "mark"]
                                   [:action/record-start "started"]
                                   [:action/record-finish "finished"]])])])
     (when (seq unscheduled)
       [:div.not-estimated
        [:h3 "Not estimated"]
        [:ul
         (for [[id stopper] (sort-by (comp str key) unscheduled)]
           [:li {:replicant/key id}
            (:task/title (entities id))
            (when (not= id stopper)
              [:span.quiet (str " · after " (:task/title (entities stopper)))])])]])]))

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
        cursor (screen/cursor state cols)
        latest (screen/latest-estimates state entities)
        view {:entities entities
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
        :schedule (schedule-view state entities latest
                                 (let [row-ids (screen/rows state entities latest)
                                       id (:state/cursor state)]
                                   (if ((set row-ids) id) id (first row-ids))))
        (board-view cols view))]
     (footer state)]))
