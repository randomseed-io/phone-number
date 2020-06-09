(ns

    ^{:doc    "Time zone formats for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.tz-format

  (:refer-clojure :exclude [get])
  (:require [phone-number.util :as util])
  (:import  [java.time.format TextStyle]))

;; Time Zone Formats

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  all
  "Map of TextStyle objects to time zone formats (keywords) for time zone formatting."
  #::{:id                nil
      :short             TextStyle/SHORT
      :narrow            TextStyle/NARROW
      :full              TextStyle/FULL
      :short-standalone  TextStyle/SHORT_STANDALONE
      :narrow-standalone TextStyle/NARROW_STANDALONE
      :full-standalone   TextStyle/FULL_STANDALONE})


(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  all-vec
  "Vector of time zone formats (keywords)."
  (vec (keys all)))

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.Keyword}
  default ::id)

(def ^{:added "8.12.4-0"
       :tag TextStyle}
  default-val (all default))

;; Time Zone IDs

(defn valid?
  "Returns true if the given tz-format is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword tz-format]
   (contains? all tz-format))
  ([^clojure.lang.Keyword tz-format
    ^Boolean use-infer]
   (contains? all (util/ns-infer "phone-number.tz-format" tz-format use-infer))))

(defn transform
  "For the given Locale object and TextStyle object renders a string describing a time
  zone identifier (given as a string) passed as the first argument. If the style is nil
  then the original zone-id object is returned."
  {:added "8.12.4-0"
   :tag String}
  [^String           zone-id
   ^java.util.Locale l
   ^TextStyle        style]
  (if (nil? style)
    zone-id
    (.getDisplayName (java.time.ZoneId/of zone-id) style l)))

(defn parse
  "Parses a time zone format specification and returns a value that can be supplied to
  phone-number functions.  If nil is given it returns the default value."
  {:added "8.12.4-0" :tag TextStyle}
  ([^clojure.lang.Keyword k]
   (parse k true))
  ([^clojure.lang.Keyword k
    ^Boolean use-infer]
   (if (nil? k)
     default-val
     (let [k (util/ns-infer "phone-number.tz-format" k use-infer)]
       (assert (valid? k) (str "Time zone format " k " is not valid"))
       (all k)))))
