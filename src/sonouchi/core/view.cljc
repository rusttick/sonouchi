(ns sonouchi.core.view
  "The view: a pure function from the state to hiccup, which the shell
  renders with Replicant. Event handlers in the hiccup are data (action
  vectors), never functions."
  (:require [clojure.string :as str]
            [sonouchi.core.keys :as keys]
            [sonouchi.core.tasks :as tasks]))

(def ^:private slow-ms
  "V12: every act should respond within 0.1 s."
  100)

(defn- header
  "The frame shared by every part (V9): the app, the part, the count."
  [{:state/keys [log]} part]
  [:header
   [:span.app "sonouchi"]
   [:span.part part]
   [:span.count (let [n (tasks/task-count log)]
                  (str n (if (= 1 n) " task" " tasks")))]
   [:span.tools
    [:button {:on {:click [:command/export]}} "Export to EDN"]
    [:label.import "Import from EDN "
     [:input {:type "file" :accept ".edn"
              :on {:change [:command/import]}}]]]])

(defn- key-hints
  [platform]
  (str/join " · "
            [(str (keys/label platform :command/add) " add")
             (str (keys/label platform :command/clear) " clear")
             (str (keys/label platform :command/undo) " undo, "
                  (keys/label platform :command/redo) " redo"
                  " (when the field is empty)")
             "paste several lines for several tasks"]))

(defn- quick-entry
  [{:state/keys [platform]}]
  [:section.quick-entry
   [:label {:for "new-task"} "New task"]
   [:input#new-task {:type "text"
                     :autofocus true
                     :autocomplete "off"
                     :placeholder "a few words describing a task"
                     :on {:paste [:command/paste]}}]
   [:p.keys (key-hints platform)]])

(defn- added-today
  [{:state/keys [log now utc-offset-minutes]}]
  (let [rows (tasks/added-today log now utc-offset-minutes)
        latest (tasks/latest-add log)]
    [:section.added
     [:h2 (str "Added today (" (count rows) ")")]
     [:ol.added
      (for [{:act/keys [id at] :patch/keys [entity] :task/keys [title]} rows
            :let [just? (= id latest)]]
        [:li {:replicant/key entity :class (when just? "just-added")}
         [:span.mark (if just? "▶" "")]
         [:span.title title]
         [:span.time (tasks/clock at utc-offset-minutes)]])]]))

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

(defn root
  "The whole page for state: a map with :state/log, :state/platform,
  :state/now and :state/utc-offset-minutes, and optionally
  :state/problems and :state/last-act-ms."
  [{:state/keys [problems] :as state}]
  [:div.page
   (header state "Add tasks")
   [:main
    (quick-entry state)
    (when (seq problems)
      (problem-list problems))
    (added-today state)]
   (footer state)])
