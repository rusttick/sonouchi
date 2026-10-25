(ns sonouchi.core.days-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [sonouchi.core.days :as days]))

(deftest the-calendar
  (is (= "2026-10-27" (days/next-day "2026-10-26")))
  (is (= "2026-10-25" (days/previous-day "2026-10-26")))
  (testing "month and year ends"
    (is (= "2026-11-01" (days/next-day "2026-10-31")))
    (is (= "2027-01-01" (days/next-day "2026-12-31")))
    (is (= "2026-12-31" (days/previous-day "2027-01-01"))))
  (testing "leap days"
    (is (= "2028-02-29" (days/next-day "2028-02-28")))
    (is (= "2100-03-01" (days/next-day "2100-02-28")) "2100 is not a leap year")
    (is (= "2000-02-29" (days/next-day "2000-02-28")) "2000 is")))

(deftest weekdays
  (is (= "Thu" (days/weekday "1970-01-01")))
  (is (= "Sun" (days/weekday "2026-10-25")))
  (is (= "Mon" (days/weekday "2026-10-26")))
  (is (= "Mon 26 Oct" (days/label "2026-10-26")))
  (is (= "Fri 6 Nov" (days/label "2026-11-06")))
  (is (days/working-day? "2026-10-26"))
  (is (days/working-day? "2026-10-30"))
  (is (not (days/working-day? "2026-10-31")))
  (is (not (days/working-day? "2026-10-25"))))

(deftest working-days
  (is (= "2026-10-26" (days/next-working-day "2026-10-23")) "Friday to Monday")
  (is (= "2026-10-26" (days/next-working-day "2026-10-25")) "Sunday to Monday")
  (is (= "2026-10-23" (days/previous-working-day "2026-10-26")) "Monday to Friday")
  (is (= "2026-10-23" (days/previous-working-day "2026-10-24")) "Saturday to Friday")
  (testing "the running example from Monday 26 October 2026"
    (is (= "2026-10-26" (days/add-working-days "2026-10-26" 0)))
    (is (= "2026-10-28" (days/add-working-days "2026-10-26" 2)) "A ends Wednesday")
    (is (= "2026-10-29" (days/add-working-days "2026-10-26" 3)) "B and C start Thursday")
    (is (= "2026-11-04" (days/add-working-days "2026-10-26" 7)) "B ends Wednesday")
    (is (= "2026-11-11" (days/add-working-days "2026-10-26" 12)) "E ends Wednesday"))
  (is (= "2026-10-26" (days/add-working-days "2026-10-24" 0)) "a weekend start moves to Monday")
  (is (= 3 (days/working-days-between "2026-10-26" "2026-10-29")))
  (is (= 5 (days/working-days-between "2026-10-26" "2026-11-02")) "a weekend is not counted")
  (is (= 0 (days/working-days-between "2026-10-26" "2026-10-26")))
  (is (= 0 (days/working-days-between "2026-10-29" "2026-10-26")) "to before from"))

(deftest today-from-the-clock
  (is (= "2026-10-19" (days/local-day #inst "2026-10-19T00:30:00Z" 540)) "09:30 in Japan")
  (is (= "2026-10-18" (days/local-day #inst "2026-10-19T00:30:00Z" -300)) "still the 18th in New York")
  (is (= "2026-10-19" (days/local-day #inst "2026-10-18T16:00:00Z" 540)) "01:00 the next day in Japan"))

(def gen-day
  "A day between 1970 and about 2240."
  (gen/fmap #(days/add-working-days "1970-01-01" %) (gen/choose 0 70000)))

(defspec next-and-previous-undo-each-other 200
  (prop/for-all [day gen-day]
    (and (= day (days/previous-day (days/next-day day)))
         (= day (days/next-day (days/previous-day day))))))

(defspec adding-working-days-counts-them 200
  (prop/for-all [day gen-day
                 n (gen/choose 0 100)]
    (let [end (days/add-working-days day n)]
      (and (days/working-day? end)
           (= n (days/working-days-between day end))))))

(defspec a-week-has-five-working-days 100
  (prop/for-all [day gen-day]
    (= 5 (days/working-days-between day (nth (iterate days/next-day day) 7)))))
