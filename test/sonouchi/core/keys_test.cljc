(ns sonouchi.core.keys-test
  (:require [clojure.test :refer [deftest is testing]]
            [sonouchi.core.keys :as keys]))

(deftest chords-are-named-the-same-way-everywhere
  (is (= "Enter" (keys/chord {:key/name "Enter"})))
  (is (= "Meta+Z" (keys/chord {:key/name "z" :key/meta true})))
  (is (= "Ctrl+Shift+Z" (keys/chord {:key/name "Z" :key/ctrl true
                                     :key/shift true}))))

(deftest enter-and-escape-act-only-in-the-field-with-text
  (is (= :command/add (keys/command :mac "Enter" "measure runout")))
  (is (nil? (keys/command :mac "Enter" "   ")))
  (is (nil? (keys/command :mac "Enter" nil)) "Enter elsewhere is the browser's")
  (is (= :command/clear (keys/command :other "Escape" "x")))
  (is (nil? (keys/command :other "Escape" ""))))

(deftest undo-and-redo-follow-the-platform
  (testing "macOS"
    (is (= :command/undo (keys/command :mac "Meta+Z" "")))
    (is (= :command/redo (keys/command :mac "Meta+Shift+Z" nil)))
    (is (nil? (keys/command :mac "Ctrl+Z" ""))))
  (testing "Windows and Linux"
    (is (= :command/undo (keys/command :other "Ctrl+Z" "")))
    (is (= :command/redo (keys/command :other "Ctrl+Y" "")))
    (is (= :command/redo (keys/command :other "Ctrl+Shift+Z" ""))))
  (testing "in a field with text, they undo typing instead"
    (is (nil? (keys/command :mac "Meta+Z" "half a tit")))))

(deftest every-shown-key-is-a-bound-key
  (is (= "⌘Z" (keys/label :mac :command/undo)))
  (is (= "Ctrl+Y" (keys/label :other :command/redo))))
