(ns

    ^{:doc    "Time zone formats for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.tz-format

  (:import [java.time.format TextStyle]))

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
       :tag nil}
  default nil)

;; Time Zone IDs

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

(defn valid?
  "Returns true if the given tz-format is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.Keyword tz-format]
  (contains? all tz-format))
