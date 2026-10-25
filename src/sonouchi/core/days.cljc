(ns sonouchi.core.days
  "Days as ISO strings, such as \"2026-10-26\": the next and previous
  day, working days, and counting working days between two days.

  A day is a local calendar date. Monday to Friday are working days,
  until design3's Step 6 adds calendars. The arithmetic is done here,
  with no host calendar, so that it is the same on the JVM and in the
  browser (Hinnant's civil-from-days and days-from-civil).

  Day numbers are days since 1970-01-01 and are private; everything
  public takes and returns ISO strings, except the counts."
  (:require [clojure.string :as str]))

(defn- days-from-civil
  "Days since 1970-01-01 of the date y-m-d, for years from 1970 on."
  [y m d]
  (let [y (if (<= m 2) (dec y) y)
        era (quot y 400)
        yoe (- y (* era 400))
        doy (+ (quot (+ (* 153 (+ m (if (> m 2) -3 9))) 2) 5) (dec d))
        doe (+ (* yoe 365) (quot yoe 4) (- (quot yoe 100)) doy)]
    (+ (* era 146097) doe -719468)))

(defn- civil-from-days
  "[y m d] of the day n days after 1970-01-01, for n >= 0."
  [n]
  (let [z (+ n 719468)
        era (quot z 146097)
        doe (- z (* era 146097))
        yoe (quot (+ (- doe (quot doe 1460)) (quot doe 36524) (- (quot doe 146096))) 365)
        y (+ yoe (* era 400))
        doy (- doe (+ (* 365 yoe) (quot yoe 4) (- (quot yoe 100))))
        mp (quot (+ (* 5 doy) 2) 153)
        d (inc (- doy (quot (+ (* 153 mp) 2) 5)))
        m (if (< mp 10) (+ mp 3) (- mp 9))]
    [(if (<= m 2) (inc y) y) m d]))

(defn- two-digits
  [n]
  (if (< n 10) (str "0" n) (str n)))

(defn- day-number
  "Days since 1970-01-01 of an ISO day."
  [day]
  (let [[y m d] (map parse-long (str/split day #"-"))]
    (days-from-civil y m d)))

(defn- iso
  "The ISO day of a day number."
  [n]
  (let [[y m d] (civil-from-days n)]
    (str y "-" (two-digits m) "-" (two-digits d))))

(def ^:private day-ms (* 24 60 60 1000))

(defn local-day
  "The ISO day of the instant at, in a place offset-minutes ahead of
  UTC. This is how the core learns today's date from the shell's clock."
  [at offset-minutes]
  (iso (quot (+ (inst-ms at) (* offset-minutes 60 1000)) day-ms)))

(defn next-day
  "The calendar day after day."
  [day]
  (iso (inc (day-number day))))

(defn previous-day
  "The calendar day before day."
  [day]
  (iso (dec (day-number day))))

(def ^:private weekday-names ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"])

(def ^:private month-names
  ["Jan" "Feb" "Mar" "Apr" "May" "Jun" "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"])

(defn- weekday-index
  "0 for Sunday to 6 for Saturday. 1970-01-01 was a Thursday."
  [n]
  (mod (+ n 4) 7))

(defn weekday
  "The day's weekday as \"Mon\", \"Tue\" and so on."
  [day]
  (weekday-names (weekday-index (day-number day))))

(defn label
  "The day as the owner reads it, such as \"Mon Oct 26\": weekday,
  month, day, the order in which the owner says it at the stand-up."
  [day]
  (let [n (day-number day)
        [_ m d] (civil-from-days n)]
    (str (weekday-names (weekday-index n)) " " (month-names (dec m)) " " d)))

(defn- working-number?
  [n]
  (<= 1 (weekday-index n) 5))

(defn working-day?
  "True when day is a Monday to Friday."
  [day]
  (working-number? (day-number day)))

(defn next-working-day
  "The first working day after day."
  [day]
  (->> (iterate inc (inc (day-number day)))
       (filter working-number?)
       first
       iso))

(defn previous-working-day
  "The last working day before day."
  [day]
  (->> (iterate dec (dec (day-number day)))
       (filter working-number?)
       first
       iso))

(defn add-working-days
  "The day that is n working days after day, counting day itself as
  the first when it is a working day. With n 0, the day itself, or the
  next working day if day is not one. So a task of 3 working days that
  starts on a Monday ends on add-working-days of Monday and 2, the
  Wednesday. With n negative, the working day that many working days
  before day, not counting day: -1 from a Monday is the Friday."
  [day n]
  (if (neg? n)
    (->> (iterate dec (dec (day-number day)))
         (filter working-number?)
         (drop (dec (- n)))
         first
         iso)
    (->> (iterate inc (day-number day))
         (filter working-number?)
         (drop n)
         first
         iso)))

(defn working-days-between
  "How many working days lie in [from, to): from itself when it is a
  working day, and every working day before to. 0 when to is not after
  from. A task started on a Monday and finished on the Wednesday has
  worked from Monday to the Thursday boundary: 3 working days."
  [from to]
  (let [a (day-number from)
        b (day-number to)]
    (count (filter working-number? (range a b)))))

(defn relative
  "How far day is before today, in words the owner reads beside it:
  \"today\", \"yesterday\" for the calendar day before, \"last working
  day\" for the one working day before when that is not yesterday, as
  Friday is from a Monday, and \"n working days ago\" further back.
  day is a working day on or before today."
  [day today]
  (let [n (working-days-between day today)]
    (cond
      (= day today) "today"
      (= day (previous-day today)) "yesterday"
      (= 1 n) "last working day"
      :else (str n " working days ago"))))

(defn working-days-from
  "The signed count of working days from day0 to day: how many working
  days lie in [day0, day) when day is later, and minus how many lie in
  [day, day0) when it is earlier. It is the inverse of add-working-days
  when both days are working days."
  [day0 day]
  (if (neg? (compare day day0))
    (- (working-days-between day day0))
    (working-days-between day0 day)))
