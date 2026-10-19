(ns sonouchi.app
  "The shell: holds the one atom, keeps the log in the browser's storage,
  reads the keyboard, renders the view with Replicant, and carries out
  the commands that the view dispatches as data. It supplies everything
  the core may not make: ids, the clock and the platform."
  (:require [cljs.reader :as reader]
            [replicant.dom :as r]
            [sonouchi.core.history :as history]
            [sonouchi.core.keys :as keys]
            [sonouchi.core.log :as log]
            [sonouchi.core.tasks :as tasks]
            [sonouchi.core.view :as view]))

(def ^:private storage-key "sonouchi/log")
(def ^:private unreadable-key "sonouchi/log-unreadable")
(def ^:private before-import-key "sonouchi/log-before-import")
(def ^:private field-id "new-task")

(defonce ^:private store (atom {:state/log []}))

(defn- platform
  []
  (let [nav js/navigator
        named (or (some-> nav .-userAgentData .-platform)
                  (.-platform nav)
                  "")]
    (if (re-find #"(?i)mac|iphone|ipad" named) :mac :other)))

(defn- clock-state
  "What the core needs to know about the clock, now."
  []
  (let [now (js/Date.)]
    {:state/now now
     :state/utc-offset-minutes (- (.getTimezoneOffset now))}))

(defn- render!
  [state]
  (r/render (js/document.getElementById "app") (view/root state)))

(defn- save!
  [k a-log]
  (.setItem js/localStorage k (pr-str a-log)))

(defn- read-log
  "The log in text, or the problems that stop it being one."
  [text]
  (let [[value error] (try [(reader/read-string text)]
                           (catch :default e [nil (ex-message e)]))
        problems (if error
                   [{:problem/message (str "not readable EDN: " error)}]
                   (log/problems value))]
    (if (seq problems)
      {:state/problems problems}
      {:state/log value})))

(defn- load!
  "Reads the log saved in the browser. If it cannot be read, it is kept
  under another key, so that the next save does not lose it."
  []
  (when-let [text (.getItem js/localStorage storage-key)]
    (let [result (read-log text)]
      (when (:state/problems result)
        (.setItem js/localStorage unreadable-key text))
      (swap! store merge result))))

(defn- time-act!
  "Measures from started (performance.now) to the next painted frame,
  and shows it (V12)."
  [started]
  (js/requestAnimationFrame
   (fn []
     (js/setTimeout
      (fn []
        (let [ms (js/Math.round (- (js/performance.now) started))]
          (js/performance.measure "act" #js {:start started})
          (swap! store assoc :state/last-act-ms ms)))
      0))))

(defn- record!
  "Appends act to the log, with an id and the time."
  [act]
  (let [clock (clock-state)]
    (swap! store #(-> %
                      (merge clock)
                      (update :state/log conj
                              (assoc act
                                     :act/id (random-uuid)
                                     :act/at (:state/now clock)))))))

(defn- field
  []
  (js/document.getElementById field-id))

(defn- add-tasks!
  [text]
  (let [titles (tasks/titles text)]
    (when (seq titles)
      (record! (tasks/add-act titles (repeatedly (count titles) random-uuid)))
      (set! (.-value (field)) ""))))

(defn- run-command!
  [command]
  (case command
    :command/add (add-tasks! (.-value (field)))
    :command/clear (set! (.-value (field)) "")
    :command/undo (some-> (history/undo-act (:state/log @store)) record!)
    :command/redo (some-> (history/redo-act (:state/log @store)) record!)))

(defn- on-key!
  [e]
  (when-not (or (.-isComposing e) (= 229 (.-keyCode e)))
    (let [in-field? (identical? (field) (.-activeElement js/document))
          chord (keys/chord {:key/name (.-key e)
                             :key/ctrl (.-ctrlKey e)
                             :key/meta (.-metaKey e)
                             :key/alt (.-altKey e)
                             :key/shift (.-shiftKey e)})
          command (keys/command (:state/platform @store) chord
                                (when in-field? (.-value (field))))]
      (when command
        (.preventDefault e)
        (let [started (js/performance.now)]
          (run-command! command)
          (time-act! started))))))

(defn- export!
  [a-log]
  (let [blob (js/Blob. #js [(pr-str a-log)] #js {:type "application/edn"})
        url (js/URL.createObjectURL blob)
        link (js/document.createElement "a")
        day (subs (.toISOString (js/Date.)) 0 10)]
    (set! (.-href link) url)
    (set! (.-download link) (str "sonouchi-log-" day ".edn"))
    (.click link)
    (js/URL.revokeObjectURL url)))

(defn- import!
  "Reads the file chosen in the input, and replaces the log with it if it
  is a log. The log it replaces is kept in the browser's storage."
  [input]
  (when-let [file (aget (.-files input) 0)]
    (-> (.text file)
        (.then (fn [text]
                 (let [result (read-log text)]
                   (when (:state/log result)
                     (save! before-import-key (:state/log @store)))
                   (swap! store #(-> %
                                     (dissoc :state/problems)
                                     (merge result)))))))))

(defn- paste!
  "Several pasted lines become several tasks, with whatever was already
  in the field in front of the first. One line is left to the browser."
  [e]
  (let [pasted (.getData (.-clipboardData e) "text")]
    (when (re-find #"\n" pasted)
      (.preventDefault e)
      (let [started (js/performance.now)]
        (add-tasks! (str (.-value (field)) pasted))
        (time-act! started)))))

(defn- dispatch!
  [{:replicant/keys [dom-event]} [action]]
  (case action
    :command/export (export! (:state/log @store))
    :command/import (import! (.-target dom-event))
    :command/paste (paste! dom-event)))

(defn ^:dev/after-load reload
  "Renders the current state again. shadow-cljs calls it after each hot
  reload."
  []
  (render! @store))

(defn init
  "Starts the app. index.html loads it through shadow-cljs.edn."
  []
  (swap! store merge (clock-state) {:state/platform (platform)})
  (load!)
  (r/set-dispatch! dispatch!)
  (.addEventListener js/document "keydown" on-key!)
  (add-watch store :render
             (fn [_ _ old state]
               (when-not (= (:state/log old) (:state/log state))
                 (save! storage-key (:state/log state)))
               (render! state)))
  (reload)
  (some-> (field) .focus))
