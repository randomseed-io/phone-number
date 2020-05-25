(ns

    ^{:doc    "Match types for phone-number"
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.match

  (:import [com.google.i18n.phonenumbers
            PhoneNumberUtil
            PhoneNumberUtil$MatchType]))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  all
  "Map of phone number matches (keywords) to MatchType values."
  #::{:exact      PhoneNumberUtil$MatchType/EXACT_MATCH
      :not-number PhoneNumberUtil$MatchType/NOT_A_NUMBER
      :short-nsn  PhoneNumberUtil$MatchType/SHORT_NSN_MATCH
      :none       PhoneNumberUtil$MatchType/NO_MATCH})

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  by-val
  "Map of MatchType values to phone number matches (keywords)."
  (clojure.set/map-invert all))
