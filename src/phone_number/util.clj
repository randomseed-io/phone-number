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
            NumberParseException]
           [java.time.format TextStyle]))

(defmacro try-parse
  [& body]
  `(try ~@body
        (catch NumberParseException  e# nil)
        (catch NumberFormatException e# nil)))

;; Singletons

(defn ^PhoneNumberUtil              instance          [] (PhoneNumberUtil/getInstance))
(defn ^PhoneNumberOfflineGeocoder   geo-coder         [] (PhoneNumberOfflineGeocoder/getInstance))
(defn ^PhoneNumberToCarrierMapper   carrier-mapper    [] (PhoneNumberToCarrierMapper/getInstance))
(defn ^PhoneNumberToTimeZonesMapper time-zones-mapper [] (PhoneNumberToTimeZonesMapper/getInstance))

;; Formats

(def ^{:added "8.12.4-0"
       :tag clojure.lang.IPersistentMap}
  formats
  "Map of possible format identifiers (keywords) to PhoneNumberFormat values."
  #:format{:e164          PhoneNumberUtil$PhoneNumberFormat/E164
           :international PhoneNumberUtil$PhoneNumberFormat/INTERNATIONAL
           :national      PhoneNumberUtil$PhoneNumberFormat/NATIONAL
           :rfc3966       PhoneNumberUtil$PhoneNumberFormat/RFC3966})

(def ^{:added "8.12.4-0"
       :tag clojure.lang.IPersistentMap}
  formats-by-format
  "Map of PhoneNumberFormat values to possible format identifiers (keywords)."
  (clojure.set/map-invert formats))

;; Types

(def ^{:added "8.12.4-0"
       :tag clojure.lang.IPersistentMap}
  types
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

(def ^{:added "8.12.4-0"
       :tag clojure.lang.IPersistentMap}
  types-by-type
  "Map of PhoneNumberType values to phone number types (keywords)."
  (clojure.set/map-invert types))

;; Time Zone Formats

(def ^{:added "8.12.4-0"
       :tag clojure.lang.IPersistentMap}
  time-zone-formats
  "Map of TextStyle objects to time zone formats (keywords) for time zone formatting."
  {:short                 TextStyle/SHORT
   :narrow                TextStyle/NARROW
   :full                  TextStyle/FULL
   :short-standalone      TextStyle/SHORT_STANDALONE
   :narrow-standalone     TextStyle/NARROW_STANDALONE
   :full-standalone       TextStyle/FULL_STANDALONE})

;; Time Zone IDs

(defn zone-id-format
  "For the given Locale object and TextStyle object renders a string describing a time
  zone identifier (given as a string) passed as the first argument."
  {:added "8.12.4-0"
   :tag String}
  [^String zone-id ^java.util.Locale l ^TextStyle style]
  (.getDisplayName (java.time.ZoneId/of zone-id) style l))
