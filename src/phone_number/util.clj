(ns

    ^{:doc    "PhoneNumberUtil wrapper for Google's Libphonenumber."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.util

  (:import [com.google.i18n.phonenumbers
            PhoneNumberUtil
            PhoneNumberUtil$PhoneNumberFormat
            PhoneNumberUtil$PhoneNumberType
            geocoding.PhoneNumberOfflineGeocoder
            PhoneNumberToCarrierMapper
            PhoneNumberToTimeZonesMapper
            NumberParseException]))

(defmacro try-parse
  [& body]
  `(try ~@body
        (catch NumberParseException  e# nil)
        (catch NumberFormatException e# nil)))

(defn ^PhoneNumberUtil              instance         [] (PhoneNumberUtil/getInstance))
(defn ^PhoneNumberOfflineGeocoder   geo-coder        [] (PhoneNumberOfflineGeocoder/getInstance))
(defn ^PhoneNumberToCarrierMapper   carrier-mapper   [] (PhoneNumberToCarrierMapper/getInstance))
(defn ^PhoneNumberToTimeZonesMapper timezones-mapper [] (PhoneNumberToTimeZonesMapper/getInstance))

(def formats
  "Map of possible format identifiers (keywords) to PhoneNumberFormat values."
  #:format{:e164          PhoneNumberUtil$PhoneNumberFormat/E164
           :international PhoneNumberUtil$PhoneNumberFormat/INTERNATIONAL
           :national      PhoneNumberUtil$PhoneNumberFormat/NATIONAL
           :rfc3966       PhoneNumberUtil$PhoneNumberFormat/RFC3966})

(def formats-by-format
  "Map of PhoneNumberFormat values to possible format identifiers (keywords)."
  (clojure.set/map-invert formats))

(def types
  "Map of phone number types (keywords) to PhoneNumberType values."
  {:fixed-line            PhoneNumberUtil$PhoneNumberType/FIXED_LINE
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

(def types-by-type
  "Map of PhoneNumberType values to phone number types (keywords)."
  (clojure.set/map-invert types))
