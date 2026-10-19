(ns sonouchi.app
  "The shell: holds the one atom, keeps the log in the browser's storage,
  renders the view with Replicant, and carries out the commands that the
  view dispatches as data."
  (:require [cljs.reader :as reader]
            [replicant.dom :as r]
            [sonouchi.core.log :as log]
            [sonouchi.core.view :as view]))

(def ^:private storage-key "sonouchi/log")
(def ^:private unreadable-key "sonouchi/log-unreadable")
(def ^:private before-import-key "sonouchi/log-before-import")

(defonce ^:private store (atom {:state/log []}))

(defn- render!
  [state]
  (r/render (js/document.getElementById "app") (view/root state)))

(defn- save!
  [k log]
  (.setItem js/localStorage k (pr-str log)))

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

(defn- today
  []
  (subs (.toISOString (js/Date.)) 0 10))

(defn- export!
  [log]
  (let [blob (js/Blob. #js [(pr-str log)] #js {:type "application/edn"})
        url (js/URL.createObjectURL blob)
        link (js/document.createElement "a")]
    (set! (.-href link) url)
    (set! (.-download link) (str "sonouchi-log-" (today) ".edn"))
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

(defn- dispatch!
  [{:replicant/keys [dom-event]} [action]]
  (case action
    :command/export (export! (:state/log @store))
    :command/import (import! (.-target dom-event))))

(defn ^:dev/after-load reload
  "Renders the current state again. shadow-cljs calls it after each hot
  reload."
  []
  (render! @store))

(defn init
  "Starts the app. index.html loads it through shadow-cljs.edn."
  []
  (load!)
  (r/set-dispatch! dispatch!)
  (add-watch store :render
             (fn [_ _ old state]
               (when-not (= (:state/log old) (:state/log state))
                 (save! storage-key (:state/log state)))
               (render! state)))
  (reload))
