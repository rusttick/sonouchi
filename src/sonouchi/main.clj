(ns sonouchi.main
  "Command line entry point: read tasks from an EDN file, compute their
  schedule, and print it.

    clojure -M -m sonouchi.main examples/running-example.edn"
  (:require [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [sonouchi.core.schedule :as schedule]))

(defn -main
  "Prints the schedule of the tasks in the EDN file at path."
  [path]
  (let [tasks (edn/read-string (slurp path))]   ; gather
    (-> (schedule/schedule tasks)              ; compute
        (pprint/pprint))))                     ; effect
