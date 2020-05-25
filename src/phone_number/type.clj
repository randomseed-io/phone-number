(ns

    ^{:doc    "Phone number types for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.type

  (:import [com.google.i18n.phonenumbers
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
      :personal-number       PhoneNumberUtil$PhoneNumberType/PERSONAL_NUMBER
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
       :tag PhoneNumberUtil$PhoneNumberType}
  default PhoneNumberUtil$PhoneNumberType/UNKNOWN)

