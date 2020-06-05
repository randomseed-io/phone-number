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
       :tag clojure.lang.PersistentHashSet}
  country-coded
  "Set of formats (keywords) that should identify values containing country code information."
  #{::e164 ::international ::rfc3966})

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  not-country-coded
  "Set of formats (keywords) that should identify values containing country code information."
  (clojure.set/difference (set (keys all)) country-coded #{:raw-input}))

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
     (all (if use-infer (util/ns-infer "phone-number.format" k) k)))))

(defn valid?
  "Returns true if the given format is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.Keyword format]
  (contains? all format))

(defn country-coded?
  "Returns true if the given format contains country code information, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.Keyword format]
  (contains? country-coded format))

(defn not-country-coded?
  "Returns true if the given format does not contain country code information, false
  otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.Keyword format]
  (contains? not-country-coded format))
