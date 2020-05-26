(ns

    ^{:doc    "Singleton wrappers and helpers for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.util

  (:import [com.google.i18n.phonenumbers
            PhoneNumberUtil
            geocoding.PhoneNumberOfflineGeocoder
            PhoneNumberToCarrierMapper
            PhoneNumberToTimeZonesMapper
            NumberParseException]))

;; Helpers

(defmacro try-parse
  "Evaluates body and if NumberParseException or NumberFormatException exception is
  caught it returns nil."
  {:added "8.12.4-0"}
  [& body]
  `(try ~@body
        (catch NumberParseException  e# nil)
        (catch NumberFormatException e# nil)))

(defmacro try-parse-or-false
  "Evaluates body and if NumberParseException or NumberFormatException exception is
  caught it returns false."
  {:added "8.12.4-0"}
  [& body]
  `(try ~@body
        (catch NumberParseException  e# false)
        (catch NumberFormatException e# false)))

(defn fmap-k
  "For each key and value of the given map m calls a function passed as the second
  argument (passing successive keys during calls to it) and generates a map with
  values updated by the results returned by the function."
  {:added "8.12.4-0" :tag clojure.lang.IPersistentMap}
  [^clojure.lang.IFn f
   ^clojure.lang.IPersistentMap m]
  (into (empty m) (for [[k v] m] [k (f k)])))

(defn fmap-k
  "For each key and value of the given map m calls a function passed as the second
  argument (passing successive keys during calls to it) and generates a map with
  values updated by the results returned by the function."
  {:added "8.12.4-0" :tag clojure.lang.IPersistentMap}
  [^clojure.lang.IFn f
   ^clojure.lang.IPersistentMap m]
  (reduce-kv
   (fn [^clojure.lang.IPersistentMap mp k v]
     (assoc mp k (f k)))
   m m))
;; Singletons

(defn instance          {:tag PhoneNumberUtil,              :added  "8.12.4-0"} [] (PhoneNumberUtil/getInstance))
(defn geo-coder         {:tag PhoneNumberOfflineGeocoder,   :added  "8.12.4-0"} [] (PhoneNumberOfflineGeocoder/getInstance))
(defn carrier-mapper    {:tag PhoneNumberToCarrierMapper,   :added  "8.12.4-0"} [] (PhoneNumberToCarrierMapper/getInstance))
(defn time-zones-mapper {:tag PhoneNumberToTimeZonesMapper, :added  "8.12.4-0"} [] (PhoneNumberToTimeZonesMapper/getInstance))
