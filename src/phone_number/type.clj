(ns

    ^{:doc    "Phone number types for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.type

  (:refer-clojure :exclude [get])
  (:require [phone-number.util :as util])
  (:import  [com.google.i18n.phonenumbers
             PhoneNumberUtil
             PhoneNumberUtil$PhoneNumberType]))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashMap}
  all
  "Map of phone number types (keywords) to PhoneNumberType values."
  #::{:fixed-line            PhoneNumberUtil$PhoneNumberType/FIXED_LINE
      :mobile                PhoneNumberUtil$PhoneNumberType/MOBILE
      :fixed-line-or-mobile  PhoneNumberUtil$PhoneNumberType/FIXED_LINE_OR_MOBILE
      :toll-free             PhoneNumberUtil$PhoneNumberType/TOLL_FREE
      :premium-rate          PhoneNumberUtil$PhoneNumberType/PREMIUM_RATE
      :shared-cost           PhoneNumberUtil$PhoneNumberType/SHARED_COST
      :voip                  PhoneNumberUtil$PhoneNumberType/VOIP
      :personal              PhoneNumberUtil$PhoneNumberType/PERSONAL_NUMBER
      :pager                 PhoneNumberUtil$PhoneNumberType/PAGER
      :uan                   PhoneNumberUtil$PhoneNumberType/UAN
      :voicemail             PhoneNumberUtil$PhoneNumberType/VOICEMAIL
      :unknown               PhoneNumberUtil$PhoneNumberType/UNKNOWN})

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashMap}
  by-val
  "Map of PhoneNumberType values to phone number types (keywords)."
  (clojure.set/map-invert all))

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.Keyword}
  default ::fixed-line)

(def ^{:added "8.12.4-0"
       :tag PhoneNumberUtil$PhoneNumberType}
  default-val (all default))

(defn get
  "Parses a type and returns a value that can be supplied to Libphonenumber methods. If
  nil is given it returns the default value."
  {:added "8.12.4-0" :tag PhoneNumberUtil$PhoneNumberType}
  ([^clojure.lang.Keyword k]
   (get k true))
  ([^clojure.lang.Keyword k
    ^Boolean use-infer]
   (if (nil? k)
     default-val
     (all (if use-infer (util/ns-infer "phone-number.type" k) k)))))

(defn valid?
  "Returns true if the given number-type is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.Keyword number-type]
  (contains? all number-type))
