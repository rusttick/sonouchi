(ns sonouchi.app
  "The shell: holds the one atom, keeps the log in the browser's storage,
  reads the keyboard, renders the view with Replicant, and carries out
  the commands that the view dispatches as data. It supplies everything
  the core may not make: ids, the clock, the platform and the tokenizer."
  (:require [cljs.reader :as reader]
            [clojure.walk :as walk]
            [replicant.dom :as r]
            [sonouchi.core.actions :as actions]
            [sonouchi.core.keys :as keys]
            [sonouchi.core.log :as log]
            [sonouchi.core.view :as view]))

(def ^:private storage-key "sonouchi/log")
(def ^:private unreadable-key "sonouchi/log-unreadable")
(def ^:private before-import-key "sonouchi/log-before-import")

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

(def ^:private segmenter
  (when (exists? js/Intl.Segmenter)
    (js/Intl.Segmenter. js/undefined #js {:granularity "word"})))

(defn- tokenize
  "The words of text, by Unicode word boundaries (UAX #29) when the
  browser has Intl.Segmenter, else by runs of letters and digits."
  [text]
  (if segmenter
    (for [segment (.segment segmenter text)
          :when (.-isWordLike segment)]
      (.-segment segment))
    (re-seq #"[\p{L}\p{N}]+" text)))

(defn- render!
  [state]
  (r/render (js/document.getElementById "app") (view/root state tokenize)))

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

;; Focus: which input should have it after a command, worked out from
;; the state, since the DOM is rendered from it.

(defn- focus!
  [selector]
  (some-> (js/document.querySelector selector) .focus))

(defn- focus-after!
  [[command-name & args]]
  (let [{:state/keys [field screen]} @store]
    (case command-name
      :command/query (focus! "#query")
      :command/open-replace (focus! "[data-line='0'][data-part='title']")
      :command/next-line (focus! (str "[data-line='" (inc (first args)) "'][data-part='title']"))
      (:command/open-estimate :command/open-title :command/open-wait
                              :command/reason-person :command/reason-outside-party)
      (when field (focus! "#field"))
      (:command/clear :command/record :command/reason-not-in-plan
                      :command/show-stand-up :command/show-schedule :command/show-record)
      (when-not (or field (= :replace screen))
        (some-> js/document.activeElement .blur))
      nil)))

(defn- act!
  "Runs a command through the core, with fresh ids and the clock, and
  carries out what follows: the focus and the timing."
  [command]
  (let [started (js/performance.now)]
    (swap! store #(actions/run (merge % (clock-state)) command (repeatedly random-uuid) tokenize))
    (focus-after! command)
    (time-act! started)))

;; The keyboard

(defn- focus-of
  "What has the focus, as sonouchi.core.keys/command wants it."
  []
  (let [el js/document.activeElement
        value (when el (.-value el))
        data (when el (.-dataset el))]
    (cond
      (nil? el) nil
      (= "query" (.-id el)) [:query value]
      (= "field" (.-id el)) [:field (keyword (.-kind data)) value]
      (some-> data .-line) [:line (js/parseInt (.-line data)) (keyword (.-part data)) value]
      :else nil)))

(defn- on-key!
  [e]
  (when-not (or (.-isComposing e) (= 229 (.-keyCode e)))
    (let [chord (keys/chord {:key/name (.-key e)
                             :key/ctrl (.-ctrlKey e)
                             :key/meta (.-metaKey e)
                             :key/alt (.-altKey e)
                             :key/shift (.-shiftKey e)})
          {:state/keys [screen] which :state/platform} @store
          command (keys/command which chord screen (focus-of))]
      (when command
        (.preventDefault e)
        (act! command)))))

(defn- on-click!
  "A click anywhere outside the open menu closes it. A click inside is
  the menu's own: the mascot toggles it, and an entry chooses a screen."
  [e]
  (when (and (:state/menu-open @store)
             (not (some-> (.-target e) (.closest ".menu"))))
    (act! [:command/toggle-menu])))

;; Files

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
                                     (merge result)
                                     (actions/start tokenize)))))))))

(defn- paste!
  "Several lines pasted into a title line become several tasks: the
  core splits them. One line is left to the browser."
  [e i]
  (let [pasted (.getData (.-clipboardData e) "text")]
    (when (re-find #"\n" pasted)
      (.preventDefault e)
      (act! [:command/typed [:line i :title] (str (.. e -target -value) pasted)]))))

(defn- dispatch!
  "Receives every action vector from the hiccup. The keyword :field/text
  in one stands for the text of the input the event came from."
  [{:replicant/keys [dom-event]} [action-name & args :as action]]
  (case action-name
    :command/export (export! (:state/log @store))
    :command/import (import! (.-target dom-event))
    :command/paste (paste! dom-event (first args))
    (act! (walk/postwalk-replace {:field/text (.. dom-event -target -value)} action))))

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
  (swap! store actions/start tokenize)
  (r/set-dispatch! dispatch!)
  (.addEventListener js/document "keydown" on-key!)
  (.addEventListener js/document "click" on-click!)
  (add-watch store :render
             (fn [_ _ old state]
               (when-not (= (:state/log old) (:state/log state))
                 (save! storage-key (:state/log state)))
               (render! state)))
  (reload))
