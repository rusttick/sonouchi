(ns sonouchi.core.log
  "The fact log: a vector of acts, oldest first, only ever appended.

  An act is a map with :act/id, :act/at, :act/action and :act/patches.
  A patch is a map with :patch/entity and the attributes of that entity
  which the act changed, and optionally :patch/retract, the attributes
  it removed. An entity's current map is its patches merged in order.")

(def ^:private patch-keys
  "The keys of a patch that are not attributes of its entity."
  [:patch/entity :patch/retract])

(defn- patch-attrs
  "Every attribute a patch sets or removes."
  [patch]
  (concat (keys (apply dissoc patch patch-keys)) (:patch/retract patch)))

(defn- apply-patch
  [entities {:patch/keys [entity retract] :as patch}]
  (let [current (as-> (get entities entity) e
                  (merge e (apply dissoc patch patch-keys))
                  (apply dissoc e retract))]
    (if (empty? current)
      (dissoc entities entity)
      (assoc entities entity current))))

(defn entities
  "Entity id to its current map, from the acts of log in order. An
  entity whose attributes have all been removed is left out."
  [log]
  (reduce apply-patch {} (mapcat :act/patches log)))

(defn undo-patches
  "The patches that undo the act with id act-id in log. Each attribute
  the act changed goes back to its value before the act, or is removed if
  it had none. An attribute that a later act changed again is left as it
  is. Returns nil when no act in log has that id."
  [log act-id]
  (let [[before [act & after]] (split-with #(not= act-id (:act/id %)) log)]
    (when act
      (let [then (entities before)
            changed-later (set (for [later after
                                     patch (:act/patches later)
                                     attr (patch-attrs patch)]
                                 [(:patch/entity patch) attr]))]
        (vec (for [{:patch/keys [entity] :as patch} (:act/patches act)
                   :let [attrs (remove #(changed-later [entity %])
                                       (patch-attrs patch))
                         old (get then entity)
                         absent (set (remove #(contains? old %) attrs))]
                   :when (seq attrs)]
               (cond-> (merge {:patch/entity entity} (select-keys old attrs))
                 (seq absent) (assoc :patch/retract absent))))))))

(defn- problem
  [path message]
  {:problem/path path :problem/message message})

(defn- patch-problems
  [path patch]
  (if-not (map? patch)
    [(problem path "a patch must be a map")]
    (concat
     (when-not (uuid? (:patch/entity patch))
       [(problem (conj path :patch/entity) "a patch needs a UUID entity")])
     (when-let [retract (:patch/retract patch)]
       (when-not (and (set? retract) (every? qualified-keyword? retract))
         [(problem (conj path :patch/retract)
                   "retract must be a set of attribute names")])))))

(defn- act-problems
  [path act]
  (if-not (map? act)
    [(problem path "an act must be a map")]
    (concat
     (when-not (uuid? (:act/id act))
       [(problem (conj path :act/id) "an act needs a UUID id")])
     (when-not (inst? (:act/at act))
       [(problem (conj path :act/at) "an act needs a time")])
     (when-not (qualified-keyword? (:act/action act))
       [(problem (conj path :act/action) "an act needs an action")])
     (if (vector? (:act/patches act))
       (mapcat #(patch-problems (conj path :act/patches %1) %2)
               (range)
               (:act/patches act))
       [(problem (conj path :act/patches)
                 "an act needs a vector of patches")]))))

(defn problems
  "Why x is not a log, as a vector of maps with :problem/path and
  :problem/message. Empty when x is a log."
  [x]
  (vec
   (if-not (vector? x)
     [(problem [] "a log must be a vector of acts")]
     (concat
      (mapcat #(act-problems [%1] %2) (range) x)
      (for [[id n] (frequencies (keep :act/id (filter map? x)))
            :when (< 1 n)]
        (problem [] (str "act " id " appears " n " times")))))))
