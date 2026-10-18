(ns guard.main
  "Runs the project's own rules over src and test:

    clojure -M:guard

  The owner, and only the owner, accepts new counts with:

    clojure -M:guard --update-baseline"
  (:require [clj-kondo.core :as kondo]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.walk :as walk]
            [guard.check :as check]))

(def ^:private rules-path "guard/rules.edn")
(def ^:private baseline-path "guard/baseline.edn")

(defn- choose-branch
  "The branch of a reader conditional for platform, else its :default.
  A splicing #?@ branch stays one vector, which is enough for the checks."
  [platform form]
  (if (reader-conditional? form)
    (let [branches (apply hash-map (:form form))]
      (get branches platform (get branches :default)))
    form))

(defn- resolve-form
  "A top-level form with its conditionals resolved for platform. It keeps
  the form's metadata, which holds its line number."
  [platform form]
  (let [resolved (walk/prewalk (partial choose-branch platform) form)]
    (if (instance? clojure.lang.IObj resolved)
      (with-meta resolved (meta form))
      resolved)))

(defn- read-forms
  "Every top-level form in text, read without evaluating anything, with
  reader conditionals resolved for platform (:clj or :cljs). Tagged
  literals such as #js are kept as data.

  Clojure's reader always matches :clj, so the conditionals are kept as
  they are while reading, and resolved here."
  [^String text platform]
  (let [eof (Object.)
        reader (clojure.lang.LineNumberingPushbackReader.
                (java.io.StringReader. text))]
    (binding [*ns* (create-ns (symbol check/reading-ns-name))
              *read-eval* false
              *default-data-reader-fn* tagged-literal]
      (into []
            (comp (take-while #(not (identical? eof %)))
                  (map #(resolve-form platform %)))
            (repeatedly #(read {:eof eof :read-cond :preserve} reader))))))

(defn- platforms
  "The platforms a file is read for, the first one counted by the
  ratchet: .clj for the JVM, .cljs for the browser, .cljc for both."
  [^java.io.File file]
  (condp re-find (.getName file)
    #"\.cljc$" [:clj :cljs]
    #"\.cljs$" [:cljs]
    [:clj]))

(defn- read-file
  [area ^java.io.File file]
  (let [text (slurp file)
        [platform other] (platforms file)
        base {:file/path (.getPath file) :file/area area :file/text text}]
    (try
      (cond-> (assoc base :file/forms (read-forms text platform))
        other (assoc :file/other-forms (read-forms text other)))
      (catch Exception e
        (assoc base :file/error (ex-message e))))))

(defn- source-files
  [dir area]
  (->> (file-seq (io/file dir))
       (filter #(re-find #"\.clj[cs]?$" (.getName ^java.io.File %)))
       (sort-by #(.getPath ^java.io.File %))
       (mapv #(read-file area %))))

(defn- analyse
  "clj-kondo's analysis of every definition and use in src and test."
  []
  (:analysis (kondo/run! {:lint ["src" "test"] :config {:analysis true}})))

(defn- describe
  [{:violation/keys [path line rule message]}]
  (str path (when line (str ":" line)) "  " (name rule) "  " message))

(defn- update-baseline!
  [counts]
  (spit baseline-path
        (str ";; Written by `clojure -M:guard --update-baseline`."
             " Only the owner runs it.\n"
             (with-out-str (pprint/pprint (into (sorted-map) counts)))))
  (println "Baseline written:" (into (sorted-map) counts)))

(defn- report!
  [rules registry baseline files counts]
  (let [readable (remove :file/error files)
        ;; distinct: a .cljc file is read for both platforms, and
        ;; clj-kondo analyses it for both, so a finding can appear twice.
        violations (distinct
                    (concat (check/registry-violations rules registry)
                            (mapcat #(check/file-violations rules registry %)
                                    files)
                            (check/unused-public-vars rules (analyse))
                            (check/ratchet-violations baseline counts)))]
    (run! (comp println describe) violations)
    (run! #(println (str "Ratchet: " (name (:ratchet/measure %)) " fell to "
                         (:ratchet/now %) " from " (:ratchet/baseline %)
                         ". The owner may lower the baseline."))
          (check/ratchet-decreases baseline counts))
    (println "Sizes:" (check/sizes rules readable))
    (if (seq violations)
      (do (println (count violations) "guard violations.")
          (System/exit 1))
      (println "Guard: no violations."))))

(defn -main
  "Checks src and test, and exits with status 1 on any violation."
  [& args]
  (let [rules (edn/read-string (slurp rules-path))
        registry @(requiring-resolve (:registry-var rules))
        files (into (source-files "src" :src) (source-files "test" :test))
        counts (check/counts rules (remove :file/error files))]
    (if (some #{"--update-baseline"} args)
      (update-baseline! counts)
      (report! rules registry (edn/read-string (slurp baseline-path))
               files counts))))
