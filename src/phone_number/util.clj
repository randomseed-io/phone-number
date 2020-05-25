(ns

    ^{:doc    "PhoneNumberUtil wrapper for Google's Libphonenumber."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.util

  (:import [com.google.i18n.phonenumbers
            PhoneNumberUtil
            geocoding.PhoneNumberOfflineGeocoder
            PhoneNumberToCarrierMapper
            PhoneNumberToTimeZonesMapper
            NumberParseException]))

(defmacro try-parse
  [& body]
  `(try ~@body
        (catch NumberParseException  e# nil)
        (catch NumberFormatException e# nil)))

(defmacro try-parse-or-false
  [& body]
  `(try ~@body
        (catch NumberParseException  e# false)
        (catch NumberFormatException e# false)))

;; Singletons

(defn ^PhoneNumberUtil              instance          [] (PhoneNumberUtil/getInstance))
(defn ^PhoneNumberOfflineGeocoder   geo-coder         [] (PhoneNumberOfflineGeocoder/getInstance))
(defn ^PhoneNumberToCarrierMapper   carrier-mapper    [] (PhoneNumberToCarrierMapper/getInstance))
(defn ^PhoneNumberToTimeZonesMapper time-zones-mapper [] (PhoneNumberToTimeZonesMapper/getInstance))
