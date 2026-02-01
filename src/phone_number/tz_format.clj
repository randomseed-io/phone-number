(ns

    ^{:doc    "Time zone formats for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.tz-format

  (:refer-clojure :exclude [get])

  (:require [clojure.set]
            [phone-number.util :as util])

  (:import  [java.time.format TextStyle]))

;; Time Zone Formats

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  all
  "Map of time zone formats (keywords) to TextStyle objects for time zone formatting."
  #::{:id                nil
      :short             TextStyle/SHORT
      :narrow            TextStyle/NARROW
      :full              TextStyle/FULL
      :short-standalone  TextStyle/SHORT_STANDALONE
      :narrow-standalone TextStyle/NARROW_STANDALONE
      :full-standalone   TextStyle/FULL_STANDALONE})

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.Keyword}
  default ::id)

(def ^{:added "8.12.4-0"
       :tag TextStyle}
  default-val (all default))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentArrayMap}
  by-val
  "Map of time zone formats to TextStyle objects."
  (clojure.set/map-invert all))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentArrayMap}
  all-arg
  "Map of TextStyle objects to time zone formats (keywords) for time zone
  formatting (to be used as arguments)."
  all)

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentArrayMap}
  by-val-arg
  "Map of time zone formats as TextStyle objects mapped to keywords (values accepted by
  Libphonenumber methods)."
  (dissoc by-val default-val))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  all-vec
  "Vector of time zone formats (keywords)."
  (vec (keys all)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  all-arg-vec
  "Vector of time zone formats (keywords) to be used as arguments."
  (vec (keys all-arg)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  by-val-vec
  "Vector of time zone values."
  (vec (keys by-val)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  by-val-arg-vec
  "Vector of time zone values suitable to be passed as arguments to methods of
  Libphonenumber."
  (vec (keys by-val-arg)))

;; Time Zone IDs

(defn valid?
  "Returns true if the given tz-format is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword tz-format]
   (contains? all tz-format))
  ([^clojure.lang.Keyword tz-format
    ^Boolean use-infer]
   (contains? all (util/ns-infer "phone-number.tz-format" tz-format use-infer))))

(defn valid-arg?
  "Returns true if the given tz-format is valid as an argument, false otherwise."
  {:added "8.12.16-1" :tag Boolean}
  ([^clojure.lang.Keyword tz-format]
   (contains? all-arg tz-format))
  ([^clojure.lang.Keyword tz-format
    ^Boolean use-infer]
   (contains? all-arg (util/ns-infer "phone-number.tz-format" tz-format use-infer))))

(defn transform
  "For the given `Locale` object and `TextStyle` object renders a string describing a
  time zone identifier (given as a string) passed as the first argument. If the style
  is nil then the original object passed as a `zone-id` argument is returned."
  {:added "8.12.4-0"
   :tag String}
  [^String           zone-id
   ^java.util.Locale l
   ^TextStyle        style]
  (if (or (nil? style) (= default-val style))
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
       (when-not (valid-arg? k)
         (throw (ex-info (str "Time zone format " k " is not valid")
                         {:phone-number/error    :phone-number.tz-format/invalid
                          :phone-number/value    k
                          :phone-number/value-type (clojure.core/type k)
                          :phone-number/tz-format k})))
       (all k)))))
