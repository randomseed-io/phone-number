(ns

    ^{:doc    "Country calling codes handling for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.country-code

  (:require [clojure.set]
            [phone-number.util :as util])

  (:import  [com.google.i18n.phonenumbers
             PhoneNumberUtil
             NumberParseException]))

;;
;; Supported Country Calling Codes
;;

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  all
  "Set of supported country codes."
  (clojure.set/difference
   (set (.getSupportedCallingCodes              (util/instance)))
   (set (.getSupportedGlobalNetworkCallingCodes (util/instance)))))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  all-arg
  "Set of supported country codes (to be used as arguments)."
  all)

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  by-val
  "Set of supported values of country codes."
  all)

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  by-val-arg
  "Set of supported values of country codes (to be used as method arguments)."
  all-arg)

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  all-vec
  "Vector of all supported country codes."
  (vec all))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  by-val-vec
  "Vector of all supported country codes."
  (vec by-val))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  all-arg-vec
  "Vector of all supported country codes (valid as args)."
  (vec all-arg))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  by-val-arg-vec
  "Vector of all supported country codes (valid as args)."
  (vec by-val-arg))

(defn valid?
  "Returns true if the given region-specification is a valid region code, false
  otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^Integer calling-code]
  (contains? all calling-code))

(defn valid-arg?
  "Returns true if the given region-specification is a valid region code (to be used as
  an argument), false otherwise."
  {:added "8.12.16-1" :tag Boolean}
  [^Integer calling-code]
  (contains? all-arg calling-code))

(defn parse
  "Parses a country calling code and returns a value that can be supplied to
  Libphonenumber methods."
  {:added "8.12.4-0" :tag Integer}
  ([^Integer calling-code]
   (when-not (valid-arg? calling-code)
     (throw (ex-info (str "Country code " calling-code " is not valid")
                     {:phone-number/error        :phone-number.country-code/invalid
                      :phone-number/value        calling-code
                      :phone-number/value-type   (clojure.core/type calling-code)
                      :phone-number/country-code calling-code})))
   calling-code))

(defn generate-sample
  "Generates a random country calling code."
  {:added "8.12.4-0" :tag Integer}
  ([] (rand-nth all-vec))
  ([^java.util.Random rng] (util/get-rand-nth all-vec rng)))

(defn generate-sample-val
  "Generates a random country calling code."
  {:added "8.12.16-1" :tag Integer}
  ([] (rand-nth by-val-vec))
  ([^java.util.Random rng] (util/get-rand-nth by-val-vec rng)))

(defn generate-arg-sample
  "Generates a random country calling code."
  {:added "8.12.16-1" :tag Integer}
  ([] (rand-nth all-arg-vec))
  ([^java.util.Random rng] (util/get-rand-nth all-arg-vec rng)))

(defn generate-arg-sample-val
  "Generates a random country calling code."
  {:added "8.12.16-1" :tag Integer}
  ([] (rand-nth by-val-arg-vec))
  ([^java.util.Random rng] (util/get-rand-nth by-val-arg-vec rng)))
