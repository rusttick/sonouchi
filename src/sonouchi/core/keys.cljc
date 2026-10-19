(ns sonouchi.core.keys
  "Keyboard shortcuts as data: which key does what on each platform, and
  how each key is shown on screen (V11)."
  (:require [clojure.string :as str]))

(def ^:private keymaps
  "Platform to chord to command. Redo follows each platform's habit."
  {:mac {"Enter" :command/add
         "Escape" :command/clear
         "Meta+Z" :command/undo
         "Meta+Shift+Z" :command/redo}
   :other {"Enter" :command/add
           "Escape" :command/clear
           "Ctrl+Z" :command/undo
           "Ctrl+Y" :command/redo
           "Ctrl+Shift+Z" :command/redo}})

(def ^:private labels
  "Platform to command to how its key is shown."
  {:mac {:command/add "Enter" :command/clear "Esc"
         :command/undo "⌘Z" :command/redo "⇧⌘Z"}
   :other {:command/add "Enter" :command/clear "Esc"
           :command/undo "Ctrl+Z" :command/redo "Ctrl+Y"}})

(defn label
  "How the key for command is shown on platform (:mac or :other)."
  [platform command]
  (get-in labels [platform command]))

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

(defn command
  "The command that the key press named chord-name asks for on platform,
  or nil to leave the key to the browser. field-text is the text in the
  new-task field when that field has the focus, and nil otherwise.

  Enter and Esc act only in the field, and only when it has text. Undo
  and redo act only when the field is empty or not focused, so that in a
  field with text they undo typing, as usual."
  [platform chord-name field-text]
  (let [found (get-in keymaps [platform chord-name])]
    (case found
      :command/add (when (and field-text (not (str/blank? field-text)))
                     found)
      :command/clear (when (seq field-text) found)
      (:command/undo :command/redo) (when (empty? field-text) found)
      nil)))
