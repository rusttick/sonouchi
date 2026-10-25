(ns sonouchi.core.chain
  "The chain behind one task: the tasks it comes after, directly or not,
  laid out in layers from left to right with few crossings (Sugiyama
  1981), for the schedule's drawing (ux-journey, \"The schedule\").

  tasks is a map from task id to a map with :task/after. The layout is
  deterministic: ties are broken by id."
  (:require [sonouchi.core.schedule :as schedule]))

(defn- fixpoint
  [f x]
  (->> (iterate f x)
       (partition 2 1)
       (drop-while (fn [[before after]] (not= before after)))
       ffirst))

(defn behind
  "The ids of the tasks that id comes after, directly or not, and id
  itself. Ids that are not tasks are left out."
  [tasks id]
  (fixpoint (fn [ids]
              (into ids (for [i ids
                              before (get-in tasks [i :task/after])
                              :when (contains? tasks before)]
                          before)))
            #{id}))

(defn- depths
  "Task id to its layer: 0 for a task with nothing behind it, else one
  more than the deepest task it comes after."
  [sub order]
  (reduce (fn [depth id]
            (assoc depth id
                   (inc (apply max -1 (keep depth (get-in sub [id :task/after]))))))
          {}
          order))

(defn depth
  "Task id to its depth in the dependency graph of tasks: 0 for a task
  that comes after nothing, else one more than the deepest task it comes
  after. Empty when the graph has a cycle. Ids in :task/after that are
  not tasks are ignored."
  [tasks]
  (let [sub (update-vals tasks (fn [t] {:task/after (set (filter tasks (:task/after t)))}))]
    (if-let [order (:schedule/order (schedule/topological-order sub))]
      (depths sub order)
      {})))

(defn- order-layer
  "A layer's ids ordered by the mean position of the tasks they come
  after in the layers already placed, ties by id. placed maps an id to
  its position in its layer, as a fraction."
  [sub placed ids]
  (vec (sort-by (fn [id]
                  (let [befores (keep placed (get-in sub [id :task/after]))]
                    [(if (seq befores) (/ (reduce + befores) (count befores)) 0.5)
                     (str id)]))
                ids)))

(defn- positions
  [layer]
  (let [n (count layer)]
    (into {} (map-indexed (fn [i id] [id (/ (+ i 0.5) n)]) layer))))

(defn layout
  "The chain behind id: {:chain/layers [[id ...] ...], the tasks in
  layers left to right with id alone in the last, and :chain/edges
  #{[before after] ...}, the comes-after links among them}. Only the
  tasks in tasks are drawn."
  [tasks id]
  (let [ids (behind tasks id)
        sub (into {} (for [i ids]
                       [i {:task/after (set (filter ids (get-in tasks [i :task/after])))}]))
        order (:schedule/order (schedule/topological-order sub))
        depth-of (depths sub order)
        by-depth (group-by depth-of order)
        layers (reduce (fn [layers d]
                         (let [placed (into {} (map positions layers))]
                           (conj layers (order-layer sub placed (by-depth d)))))
                       []
                       (range (inc (apply max 0 (vals depth-of)))))]
    {:chain/layers layers
     :chain/edges (set (for [[after {befores :task/after}] sub
                             before befores]
                         [before after]))}))
