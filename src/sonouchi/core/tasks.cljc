(ns sonouchi.core.tasks
  "Titles from text, and the act that adds tasks by title, which the
  quick-entry page used and whose logs still load."
  (:require [clojure.string :as str]
            [sonouchi.core.log :as log]))

(defn titles
  "The task titles in text: one per line, trimmed, with blank lines left
  out."
  [text]
  (->> (str/split-lines text)
       (map str/trim)
       (remove str/blank?)
       vec))

(defn add-act
  "The act, without :act/id and :act/at, that adds one task for each
  title in new-titles, with the entity ids in ids, one per title."
  [new-titles ids]
  {:act/action :action/add-task
   :act/patches (mapv (fn [title id] {:patch/entity id :task/title title})
                      new-titles
                      ids)})

(defn task-count
  "How many tasks there are: entities with a :task/title."
  [log]
  (count (filter :task/title (vals (log/entities log)))))
