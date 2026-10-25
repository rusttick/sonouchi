(ns sonouchi.core.keys
  "Keyboard shortcuts as data: which key does what on each screen and
  platform, and how each key is shown on screen (V11)."
  (:require [clojure.string :as str]))

(def ^:private undo-redo
  "Platform to chord to command. Redo follows each platform's habit."
  {:mac {"Meta+Z" :command/undo
         "Meta+Shift+Z" :command/redo}
   :other {"Ctrl+Z" :command/undo
           "Ctrl+Y" :command/redo
           "Ctrl+Shift+Z" :command/redo}})

(def ^:private board-keys
  "Chord to command when no field has the focus, on the board."
  {"ArrowUp" [:command/cursor-up]
   "ArrowDown" [:command/cursor-down]
   "ArrowLeft" [:command/cursor-left]
   "ArrowRight" [:command/cursor-right]
   "S" [:action/record-start]
   "F" [:action/record-finish]
   "W" [:action/record-worked-on]
   "B" [:command/open-wait]
   "E" [:command/open-title]
   "T" [:command/open-estimate]
   "A" [:action/set-after]
   "X" [:command/mark]
   "R" [:command/open-replace]
   "V" [:action/set-serves]
   "P" [:command/open-promise]
   "/" [:command/query]
   "[" [:command/day-back]
   "]" [:command/day-forward]
   "1" [:command/show-stand-up]
   "2" [:command/show-schedule]
   "3" [:command/show-record]
   "4" [:command/show-requests]})

(def ^:private schedule-keys
  "Chord to command when no field has the focus, on the schedule: the
  board's acts that ux-journey's Stage 5 gives it, t, a, x, s and f, on
  a cursor that moves up and down, and the frame's keys."
  (select-keys board-keys ["ArrowUp" "ArrowDown" "S" "F" "T" "A" "X" "/" "[" "]" "1" "2" "3" "4"]))

(def ^:private request-keys
  "Chord to command when no field has the focus, on the requests screen
  (ux-requests, \"The requests screen\"): a cursor that moves up and
  down the rows, n, p, v, c, e and x on the request under it, and the
  frame's keys."
  (merge (select-keys board-keys ["ArrowUp" "ArrowDown" "V" "P" "/" "[" "]" "1" "2" "3" "4"])
         {"N" [:command/open-request]
          "C" [:action/close-request]
          "E" [:command/open-request-words]
          "X" [:command/mark-request]}))

(def ^:private record-keys
  "Chord to command when no field has the focus, on the record screen:
  only the keys that choose a screen, since nothing there has a cursor."
  (select-keys board-keys ["1" "2" "3" "4"]))

(def ^:private reasons
  {"1" [:command/reason-person]
   "2" [:command/reason-outside-party]
   "3" [:command/reason-not-in-plan]})

(def ^:private labels
  "Command to how its key is shown, the same on every platform."
  {:action/record-start "s"
   :action/record-finish "f"
   :action/record-worked-on "w"
   :command/open-wait "b"
   :command/open-title "e"
   :command/open-estimate "t"
   :action/set-after "a"
   :command/mark "x"
   :command/open-replace "r"
   :command/query "/"
   :command/day-back "["
   :command/day-forward "]"
   :command/show-stand-up "1"
   :command/show-schedule "2"
   :command/show-record "3"
   :command/show-requests "4"
   :action/set-serves "v"
   :command/open-promise "p"
   :command/open-request "n"
   :action/close-request "c"
   :command/open-request-words "e"
   :command/mark-request "x"
   :command/clear "Esc"
   :command/next-line "Enter"})

(def ^:private platform-labels
  {:mac {:command/undo "⌘Z" :command/redo "⇧⌘Z" :command/record "⌘Enter"
         :command/state-up "⌥↑" :command/state-down "⌥↓"}
   :other {:command/undo "Ctrl+Z" :command/redo "Ctrl+Y" :command/record "Ctrl+Enter"
           :command/state-up "Alt+↑" :command/state-down "Alt+↓"}})

(defn label
  "How the key for command is shown on platform (:mac or :other)."
  [platform command]
  (or (get-in platform-labels [platform command]) (labels command)))

(defn chord
  "The name of a key press, such as \"Ctrl+Shift+Z\" or \"Enter\", from
  a map with :key/name and the flags :key/ctrl, :key/meta, :key/alt and
  :key/shift. A single letter is named in upper case."
  [{key-name :key/name :key/keys [ctrl alt shift] held-meta :key/meta}]
  (str/join "+" (concat (keep identity [(when ctrl "Ctrl")
                                        (when held-meta "Meta")
                                        (when alt "Alt")
                                        (when shift "Shift")])
                        [(if (= 1 (count key-name))
                           (str/upper-case key-name)
                           key-name)])))

(defn- record-chord?
  [platform chord-name]
  (= chord-name (if (= :mac platform) "Meta+Enter" "Ctrl+Enter")))

(defn command
  "The command, as a vector, that the key press named chord-name asks
  for on platform, given which screen is shown and what has the focus,
  or nil to leave the key to the browser.

  focus is nil when no input has the focus; [:query text] in the query
  bar; [:field kind text] in a card's or a row's field, where kind is
  :title, :estimate, :wait, :wait-reason while the wait's reason is not
  yet chosen, :request, :promise or :request-words; or [:line i part text] on line i of the replacement display,
  part being :title or :estimate.

  Escape always clears. Undo and redo act only when no input with text
  has the focus, so that in one they undo typing, as usual. The board's
  and the schedule's keys act only when nothing has the focus."
  [platform chord-name screen focus]
  (let [[where kind-or-i _ _] focus
        text (last (filter string? focus))
        undo (get-in undo-redo [platform chord-name])]
    (cond
      (= "Escape" chord-name) [:command/clear]
      undo (when (str/blank? (or text "")) [undo])
      (nil? focus) (case screen
                     :stand-up (board-keys chord-name)
                     :schedule (schedule-keys chord-name)
                     :requests (request-keys chord-name)
                     :record (record-keys chord-name)
                     nil)
      (= :query where) (when (and (= "Enter" chord-name) (not (str/blank? text)))
                         [:command/open-replace :from-query])
      (= :field where) (cond
                         (= "Enter" chord-name) [:command/record]
                         (= :wait-reason kind-or-i) (reasons chord-name)
                         :else nil)
      (= :line where) (cond
                        (record-chord? platform chord-name) [:command/record]
                        (= "Enter" chord-name) [:command/next-line kind-or-i]
                        (= "Alt+ArrowUp" chord-name) [:command/state-up kind-or-i]
                        (= "Alt+ArrowDown" chord-name) [:command/state-down kind-or-i]
                        :else nil)
      :else nil)))
