(ns sonouchi.core.keys-test
  (:require [clojure.test :refer [deftest is testing]]
            [sonouchi.core.keys :as keys]))

(deftest chords-are-named
  (is (= "Meta+Shift+Z" (keys/chord {:key/name "z" :key/meta true :key/shift true})))
  (is (= "Enter" (keys/chord {:key/name "Enter"})))
  (is (= "S" (keys/chord {:key/name "s"})))
  (is (= "/" (keys/chord {:key/name "/"}))))

(deftest the-board-keys-act-when-nothing-has-the-focus
  (is (= [:action/record-start] (keys/command :mac "S" :stand-up nil)))
  (is (= [:command/cursor-down] (keys/command :mac "ArrowDown" :schedule nil)))
  (is (= [:command/open-replace] (keys/command :other "R" :stand-up nil)))
  (is (= [:command/show-schedule] (keys/command :mac "2" :stand-up nil)))
  (is (nil? (keys/command :mac "S" :replace nil)) "not on the replacement display")
  (is (nil? (keys/command :mac "S" :stand-up [:query "s"])) "typing in the query bar")
  (is (nil? (keys/command :mac "Q" :stand-up nil)) "an unbound key goes to the browser"))

(deftest the-schedule-takes-its-own-acts
  (testing "t, a, x, s and f, as ux-journey's Stage 5 gives them"
    (is (= [:command/open-estimate] (keys/command :mac "T" :schedule nil)))
    (is (= [:action/set-after] (keys/command :mac "A" :schedule nil)))
    (is (= [:command/mark] (keys/command :mac "X" :schedule nil)))
    (is (= [:action/record-start] (keys/command :mac "S" :schedule nil)))
    (is (= [:action/record-finish] (keys/command :mac "F" :schedule nil))))
  (testing "and the frame's keys"
    (is (= [:command/show-stand-up] (keys/command :mac "1" :schedule nil)))
    (is (= [:command/query] (keys/command :mac "/" :schedule nil))))
  (testing "not the board's other acts"
    (is (nil? (keys/command :mac "W" :schedule nil)))
    (is (nil? (keys/command :mac "B" :schedule nil)))
    (is (nil? (keys/command :mac "E" :schedule nil)))
    (is (nil? (keys/command :mac "R" :schedule nil)))
    (is (nil? (keys/command :mac "ArrowLeft" :schedule nil)) "the rows are one column")))

(deftest the-record-screen-takes-only-the-screen-keys
  (is (= [:command/show-record] (keys/command :mac "3" :stand-up nil)))
  (is (= [:command/show-record] (keys/command :mac "3" :schedule nil)))
  (is (= [:command/show-stand-up] (keys/command :mac "1" :record nil)))
  (is (= [:command/show-schedule] (keys/command :mac "2" :record nil)))
  (is (nil? (keys/command :mac "S" :record nil)))
  (is (nil? (keys/command :mac "ArrowDown" :record nil)))
  (is (= "3" (keys/label :mac :command/show-record))))

(deftest escape-undo-and-redo
  (is (= [:command/clear] (keys/command :mac "Escape" :replace [:line 0 :title "x"])))
  (is (= [:command/undo] (keys/command :mac "Meta+Z" :stand-up nil)))
  (is (= [:command/redo] (keys/command :mac "Meta+Shift+Z" :stand-up nil)))
  (is (= [:command/undo] (keys/command :other "Ctrl+Z" :stand-up nil)))
  (is (= [:command/redo] (keys/command :other "Ctrl+Y" :stand-up nil)))
  (is (= [:command/undo] (keys/command :mac "Meta+Z" :stand-up [:query ""])) "an empty field")
  (is (nil? (keys/command :mac "Meta+Z" :stand-up [:query "runout"])) "typing is undone by the browser")
  (is (nil? (keys/command :mac "Ctrl+Z" :stand-up nil)) "not the Mac key"))

(deftest keys-in-the-query-bar-and-the-fields
  (is (= [:command/open-replace :from-query] (keys/command :mac "Enter" :stand-up [:query "new task"])))
  (is (nil? (keys/command :mac "Enter" :stand-up [:query "  "])))
  (is (= [:command/record] (keys/command :mac "Enter" :stand-up [:field :estimate "3"])))
  (testing "the wait's reason is chosen with a digit, then the digits type"
    (is (= [:command/reason-person] (keys/command :mac "1" :stand-up [:field :wait-reason ""])))
    (is (= [:command/reason-not-in-plan] (keys/command :mac "3" :stand-up [:field :wait-reason ""])))
    (is (nil? (keys/command :mac "1" :stand-up [:field :wait "Tanaka"])))
    (is (nil? (keys/command :mac "1" :stand-up [:field :estimate ""])))))

(deftest keys-on-a-line-of-the-replacement-display
  (is (= [:command/next-line 1] (keys/command :mac "Enter" :replace [:line 1 :title "x"])))
  (is (= [:command/record] (keys/command :mac "Meta+Enter" :replace [:line 1 :title "x"])))
  (is (= [:command/record] (keys/command :other "Ctrl+Enter" :replace [:line 1 :estimate "2"])))
  (is (nil? (keys/command :other "Meta+Enter" :replace [:line 1 :title "x"])))
  (is (= [:command/state-up 0] (keys/command :mac "Alt+ArrowUp" :replace [:line 0 :title ""])))
  (is (= [:command/state-down 2] (keys/command :mac "Alt+ArrowDown" :replace [:line 2 :title ""])))
  (is (nil? (keys/command :mac "Tab" :replace [:line 0 :title ""])) "Tab is the browser's"))

(deftest labels
  (is (= "⌘Z" (keys/label :mac :command/undo)))
  (is (= "Ctrl+Y" (keys/label :other :command/redo)))
  (is (= "s" (keys/label :mac :action/record-start)))
  (is (= "⌥↑" (keys/label :mac :command/state-up)))
  (is (= "Alt+↑" (keys/label :other :command/state-up))))
