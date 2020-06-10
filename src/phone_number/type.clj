(ns

    ^{:doc    "Phone number types for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.type

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
  all-arg
  "Map of PhoneNumberType values to phone number types (keywords) suitable to be
  passed as arguments."
  (dissoc all ::unknown))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashMap}
  by-val
  "Map of PhoneNumberType values to phone number types (keywords)."
  (clojure.set/map-invert all))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashMap}
  by-val-arg
  "Map of PhoneNumberType values to phone number types (keywords)."
  (clojure.set/map-invert all-arg))

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.Keyword}
  default ::fixed-line)

(def ^{:added "8.12.4-0"
       :tag PhoneNumberUtil$PhoneNumberType}
  default-val (all default))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  all-vec
  "Vector of types (keywords)."
  (vec (keys all)))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  all-arg-vec
  "Vector of types (keywords)."
  (vec (keys all-arg)))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  by-val-vec
  "Vector of types (PhoneNumberType values)."
  (vec (keys by-val)))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  by-val-arg-vec
  "Vector of types (PhoneNumberType values)."
  (vec (keys by-val-arg)))

(defn valid?
  "Returns true if the given number-type is valid, false otherwise.
  Use valid-arg? for argument testing when building phone numbers."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword number-type]
   (contains? all number-type))
  ([^clojure.lang.Keyword number-type
    ^Boolean use-infer]
   (contains? all (util/ns-infer "phone-number.type" number-type use-infer))))

(defn valid-arg?
  "Returns true if the given number-type is valid, false otherwise.
  Excludes unknown type from the valid list."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword number-type]
   (contains? all-arg number-type))
  ([^clojure.lang.Keyword number-type
    ^Boolean use-infer]
   (contains? all-arg (util/ns-infer "phone-number.type" number-type use-infer))))

(defn parse
  "Parses a type and returns a value that can be supplied to Libphonenumber methods. If
  nil is given it returns the default value."
  {:added "8.12.4-0" :tag PhoneNumberUtil$PhoneNumberType}
  ([^clojure.lang.Keyword k]
   (parse k true))
  ([^clojure.lang.Keyword k
    ^Boolean use-infer]
   (if (nil? k)
     default-val
     (let [k (util/ns-infer "phone-number.type" k use-infer)]
       (assert (valid-arg? k) (str "Type " k " is not valid"))
       (all-arg k)))))

(defn generate-sample
  "Generates random number type."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([] (rand-nth all-vec))
  ([^java.util.Random rng] (util/get-rand-nth all-vec rng)))

(defn generate-sample-val
  "Generates random number type (PhoneNumberType value)."
  {:added "8.12.4-0" :tag PhoneNumberUtil$PhoneNumberType}
  ([] (rand-nth by-val-vec))
  ([^java.util.Random rng] (util/get-rand-nth by-val-vec rng)))

(defn generate-arg-sample
  "Generates random number type suitable to be used as an argument."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([] (rand-nth all-vec))
  ([^java.util.Random rng] (util/get-rand-nth all-arg-vec rng)))

(defn generate-sample-val
  "Generates random number type (PhoneNumberType value) suitable to be used as an
  argument."
  {:added "8.12.4-0" :tag PhoneNumberUtil$PhoneNumberType}
  ([] (rand-nth by-val-vec))
  ([^java.util.Random rng] (util/get-rand-nth by-val-arg-vec rng)))
