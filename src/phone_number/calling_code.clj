(ns

    ^{:doc    "Network calling codes handling for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.calling-code

  (:require [phone-number.util :as util])
  (:import  [com.google.i18n.phonenumbers
             PhoneNumberUtil
             NumberParseException]))

;;
;; Supported Global Network Calling Codes
;;

(def ^{:added "8.12.4-0"
       :const true
       :tag Integer}
  unknown 0)

(def ^{:added "8.12.4-0"
       :const true
       :tag Integer}
  unknown-val unknown)

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  all
  "Set of supported global network calling codes."
  (set (.getSupportedGlobalNetworkCallingCodes (util/instance))))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  by-val all)

(def ^{:added "8.12.4-0"
       :const true
       :tag Integer}
  default 0)

(def ^{:added "8.12.4-0"
       :const true
       :tag Integer}
  default-val 0)

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  all-vec
  "Vector of all supported global network calling codes."
  (vec all))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  by-val-vec
  "Vector of all supported global network calling codes."
  all-vec)

(defn parse
  "Parses a network calling code and returns a value that can be supplied to
  Libphonenumber methods."
  {:added "8.12.4-0" :tag Integer}
  ([^Integer calling-code]
   (when calling-code) calling-code))

(defn valid?
  "Returns true if the given region-specification is a valid region code, false
  otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^Integer calling-code]
  (contains? all calling-code))

(defn generate-sample
  "Generates random calling code."
  {:added "8.12.4-0" :tag Integer}
  []
  (rand-nth all-vec))

(defn generate-sample-val
  "Generates random region code (string value)."
  {:added "8.12.4-0" :tag Integer}
  []
  (rand-nth by-val-vec))
