(ns

    ^{:doc    "Phone number formats for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.format

  (:import [com.google.i18n.phonenumbers
            PhoneNumberUtil
            PhoneNumberUtil$PhoneNumberFormat]))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  all
  "Map of possible format identifiers (keywords) to PhoneNumberFormat values."
  #::{:e164          PhoneNumberUtil$PhoneNumberFormat/E164
      :international PhoneNumberUtil$PhoneNumberFormat/INTERNATIONAL
      :national      PhoneNumberUtil$PhoneNumberFormat/NATIONAL
      :rfc3966       PhoneNumberUtil$PhoneNumberFormat/RFC3966})

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  by-val
  "Map of PhoneNumberFormat values to possible format identifiers (keywords)."
  (clojure.set/map-invert all))

(def ^{:added "8.12.4-0"
       :tag PhoneNumberUtil$PhoneNumberFormat}
  default PhoneNumberUtil$PhoneNumberFormat/INTERNATIONAL)

(defn valid?
  "Returns true if the given format is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.Keyword format]
  (contains? all format))
