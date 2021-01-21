(ns

    ^{:doc    "Match types for phone-number"
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.match

  (:require [lazy-map.core :refer :all])
  (:import [com.google.i18n.phonenumbers
            PhoneNumberUtil
            PhoneNumberUtil$MatchType
            PhoneNumberMatch]))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  all
  "Map of phone number matches (keywords) to MatchType values."
  #::{:exact      PhoneNumberUtil$MatchType/EXACT_MATCH
      :not-number PhoneNumberUtil$MatchType/NOT_A_NUMBER
      :short-nsn  PhoneNumberUtil$MatchType/SHORT_NSN_MATCH
      :nsn        PhoneNumberUtil$MatchType/NSN_MATCH
      :none       PhoneNumberUtil$MatchType/NO_MATCH})

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  by-val
  "Map of MatchType values to phone number matches (keywords)."
  (clojure.set/map-invert all))

(defn mapper
  "Generates a map from PhoneNumberMatch object."
  {:added "8.12.4-0"
   :tag lazy_map.core.LazyMap}
  [^PhoneNumberMatch m]
  (let [num (.number m)
        num (locking num (if (.hasRawInput num) num (.setRawInput num (.rawString m))))]
    (merge
     (lazy-map #::{})
     #::{:phone-number/number num
         :start               (.start     m)
         :end                 (.end       m)
         :raw-string          (.rawString m)})))
