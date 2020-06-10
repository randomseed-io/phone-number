(ns

    ^{:doc    "Phone number formats for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.format

  (:require [phone-number.util :as util])
  (:import  [com.google.i18n.phonenumbers
             PhoneNumberUtil
             PhoneNumberUtil$PhoneNumberFormat]))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  all
  "Map of possible format identifiers (keywords) to PhoneNumberFormat values."
  #::{:e164          PhoneNumberUtil$PhoneNumberFormat/E164
      :international PhoneNumberUtil$PhoneNumberFormat/INTERNATIONAL
      :national      PhoneNumberUtil$PhoneNumberFormat/NATIONAL
      :rfc3966       PhoneNumberUtil$PhoneNumberFormat/RFC3966
      :raw-input     :raw})

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  all-vec
  "Vector of formats (keywords)."
  (vec (keys all)))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  calling-coded
  "Set of formats (keywords) that should identify values containing country code information."
  #{::e164 ::international ::rfc3966})

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  not-calling-coded
  "Set of formats (keywords) that should identify values containing country code information."
  (clojure.set/difference (set (keys all)) calling-coded #{::raw-input}))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  by-val
  "Map of PhoneNumberFormat values to possible format identifiers (keywords)."
  (clojure.set/map-invert all))

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.Keyword}
  default ::international)

(def ^{:added "8.12.4-0"
       :tag PhoneNumberUtil$PhoneNumberFormat}
  default-val (all default))

(defn valid?
  "Returns true if the given format is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword format]
   (contains? all format))
  ([^clojure.lang.Keyword format
    ^Boolean use-infer]
   (contains? all (util/ns-infer "phone-number.format" format use-infer))))

(defn parse
  "Parses a format specification and returns a value that can be supplied to
  Libphonenumber methods."
  {:added "8.12.4-0" :tag PhoneNumberUtil$PhoneNumberFormat}
  ([^clojure.lang.Keyword k]
   (parse k true))
  ([^clojure.lang.Keyword k
    ^Boolean use-infer]
   (if (nil? k)
     default-val
     (let [k (util/ns-infer "phone-number.format" k use-infer)]
       (assert (valid? k) (str "Format specification " k " is not valid"))
       (all k)))))

(defn calling-coded?
  "Returns true if the given format contains country code information, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.Keyword format]
  (contains? calling-coded format))

(defn not-calling-coded?
  "Returns true if the given format does not contain country code information, false
  otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.Keyword format]
  (contains? not-calling-coded format))
