(ns sonouchi.core.search
  "Searching task titles with an inverted index that is a value.

  The index maps each term to the tasks whose titles contain it, in a
  sorted map, so that the terms starting with a word are a range of it.
  Words are split by tokenize, a function from a string to the words in
  it, which the caller passes in. The shell passes one that follows
  Unicode word boundaries (UAX #29); the tests pass a simpler one. A
  term is a word in lower case.

  The index is a map of this namespace's own. Read it only through the
  functions here."
  (:require [clojure.set :as set]
            [clojure.string :as str]))

(def ^:private letters-per-edit
  "How forgiving a search is: one typing mistake is allowed for each
  this many letters of a word typed, so short words must be exact."
  5)

(def ^:private empty-index
  {:terms (sorted-map) :docs {}})

(defn- terms-of
  [tokenize text]
  (set (map str/lower-case (tokenize text))))

(defn- drop-doc
  [idx id]
  (reduce (fn [i term]
            (let [ids (disj (get-in i [:terms term]) id)]
              (if (empty? ids)
                (update i :terms dissoc term)
                (assoc-in i [:terms term] ids))))
          (update idx :docs dissoc id)
          (get-in idx [:docs id])))

(defn- add-doc
  [idx id terms]
  (if (empty? terms)
    idx
    (reduce (fn [i term] (update-in i [:terms term] (fnil conj #{}) id))
            (assoc-in idx [:docs id] terms)
            terms)))

(defn reindex
  "idx with each task whose id is in ids indexed again from entities, a
  map of entity id to its current map such as sonouchi.core.log/entities
  returns. A task without a :task/title in entities leaves the index."
  [idx tokenize entities ids]
  (reduce (fn [i id]
            (let [title (get-in entities [id :task/title])]
              (cond-> (drop-doc i id)
                title (add-doc id (terms-of tokenize title)))))
          idx
          ids))

(defn index
  "The index of every :task/title in entities, a map of entity id to its
  current map, split into words by tokenize."
  [tokenize entities]
  (reindex empty-index tokenize entities (keys entities)))

(defn- next-row
  "Row i of the edit distance table from a to b, from the two rows
  before it. A swap of two neighbouring letters counts as one edit."
  [a b i before-last last-row]
  (let [ai (nth a (dec i))]
    (reduce (fn [row j]
              (let [bj (nth b (dec j))
                    d (min (inc (nth last-row j))
                           (inc (peek row))
                           (+ (nth last-row (dec j)) (if (= ai bj) 0 1)))]
                (conj row (if (and (< 1 i) (< 1 j)
                                   (= ai (nth b (- j 2)))
                                   (= (nth a (- i 2)) bj))
                            (min d (inc (nth before-last (- j 2))))
                            d))))
            [i]
            (range 1 (inc (count b))))))

(defn- prefix-distance
  "The fewest edits that turn word into the start of term."
  [word term]
  (->> (range 1 (inc (count word)))
       (reduce (fn [[before-last last-row] i]
                 [last-row (next-row word term i before-last last-row)])
               [nil (vec (range (inc (count term))))])
       second
       (apply min)))

(defn- starting-with
  "The entries of terms whose term starts with prefix."
  [terms prefix]
  (take-while #(str/starts-with? (key %) prefix) (subseq terms >= prefix)))

(defn- word-ids
  "The ids of the tasks with a term that starts with word, or nearly
  does. The first letter must be right."
  [{:keys [terms]} word]
  (let [edits (quot (count word) letters-per-edit)
        found (if (zero? edits)
                (starting-with terms word)
                (filter #(<= (prefix-distance word (key %)) edits)
                        (starting-with terms (subs word 0 1))))]
    (into #{} (mapcat val) found)))

(defn matches
  "The set of ids of the tasks in idx with a match for every word of
  text, split by tokenize: a term that starts with the word, or does
  with a few typing mistakes. With no words in text, every task in idx."
  [idx tokenize text]
  (let [words (terms-of tokenize text)]
    (if (empty? words)
      (set (keys (:docs idx)))
      (apply set/intersection (map #(word-ids idx %) words)))))
