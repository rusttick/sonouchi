(ns sonouchi.core.chain
  "The dependency graph of tasks laid out in layers from left to right
  with few crossings (Sugiyama 1981), for the plan's drawing (ux-journey,
  \"The plan\"): either the whole graph of linked tasks, or the chain
  behind one task.

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

(defn linked
  "The ids of the tasks that come after a task, or that a task comes
  after. Ids in :task/after that are not tasks are ignored."
  [tasks]
  (set (for [[id {befores :task/after}] tasks
             before befores
             :when (contains? tasks before)
             i [id before]]
         i)))

(defn- depths
  "Task id to its layer: 0 for a task with nothing behind it, else one
  more than the deepest task it comes after."
  [sub order]
  (reduce (fn [depth id]
            (assoc depth id
                   (inc (apply max -1 (keep depth (get-in sub [id :task/after]))))))
          {}
          order))

(defn- subgraph
  "tasks restricted to ids, each with only the :task/after links among
  ids."
  [tasks ids]
  (into {} (for [i ids]
             [i {:task/after (set (filter ids (get-in tasks [i :task/after])))}])))

(defn depth
  "Task id to its depth in the dependency graph of tasks: 0 for a task
  that comes after nothing, else one more than the deepest task it comes
  after. Empty when the graph has a cycle. Ids in :task/after that are
  not tasks are ignored."
  [tasks]
  (let [sub (subgraph tasks (set (keys tasks)))]
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

(defn- layered
  "The layout of sub, a subgraph: {:chain/layers [[id ...] ...], the
  tasks in layers left to right, and :chain/edges #{[before after] ...},
  the comes-after links among them}. Empty layers when sub has a cycle."
  [sub]
  (let [order (:schedule/order (schedule/topological-order sub))
        depth-of (depths sub order)
        by-depth (group-by depth-of order)
        layers (reduce (fn [layers d]
                         (let [placed (into {} (map positions layers))]
                           (conj layers (order-layer sub placed (by-depth d)))))
                       []
                       (range (inc (apply max -1 (vals depth-of)))))]
    {:chain/layers layers
     :chain/edges (set (for [[after {befores :task/after}] sub
                             before befores]
                         [before after]))}))

(defn layout
  "The chain behind id: {:chain/layers [[id ...] ...], the tasks in
  layers left to right with id alone in the last, and :chain/edges
  #{[before after] ...}, the comes-after links among them}. Only the
  tasks in tasks are drawn."
  [tasks id]
  (layered (subgraph tasks (behind tasks id))))

(defn graph
  "The whole graph of linked tasks: {:chain/layers [[id ...] ...], the
  tasks that come after a task or that a task comes after, in layers
  left to right, and :chain/edges #{[before after] ...}, every
  comes-after link among them}. A task with no links is left out, and
  the layers are empty when the graph has a cycle."
  [tasks]
  (layered (subgraph tasks (linked tasks))))
