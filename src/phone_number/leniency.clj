(ns

    ^{:doc    "Leniency types for phone-number"
      :author "Paweł Wilk"
      :added  "8.12.4-3"}

    phone-number.leniency

  (:require [phone-number.util :as util])
  (:import [com.google.i18n.phonenumbers
            PhoneNumberUtil
            PhoneNumberUtil$Leniency]))

(def ^{:added "8.12.4-3"
       :tag clojure.lang.PersistentArrayMap}
  all
  "Map of leniencies (keywords) to Leniency values."
  #::{:exact    PhoneNumberUtil$Leniency/EXACT_GROUPING
      :possible PhoneNumberUtil$Leniency/POSSIBLE
      :strict   PhoneNumberUtil$Leniency/STRICT_GROUPING
      :valid    PhoneNumberUtil$Leniency/VALID})

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  all-arg
  "Vector of leniencies (keywords) to be used as arguments."
  all)

(def ^{:added "8.12.4-3"
       :tag clojure.lang.PersistentArrayMap}
  by-val
  "Map of Leniency values to leniencies (keywords)."
  (clojure.set/map-invert all))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentArrayMap}
  by-val-arg
  "Map of Leniency values to leniencies (keywords) to be used as method arguments."
  (clojure.set/map-invert all-arg))

(def ^{:added "8.12.4-3"
       :tag clojure.lang.PersistentVector}
  all-vec
  "Vector of leniencies (keywords)."
  (vec (keys all)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  all-arg-vec
  "Vector of leniencies (keywords) to be used as arguments."
  (vec (keys all-arg)))

(def ^{:added "8.12.4-3"
       :tag clojure.lang.PersistentVector}
  by-val-vec
  "Vector of leniencies (Leniency values)."
  (vec (keys by-val)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  by-val-arg-vec
  "Vector of leniencies (Leniency values) to be used as method arguments."
  (vec (keys by-val-arg)))

(def ^{:added "8.12.4-3"
       :const true
       :tag clojure.lang.Keyword}
  default ::valid)

(def ^{:added "8.12.4-3"
       :tag PhoneNumberUtil$Leniency}
  default-val (all default))

(defn valid?
  "Returns true if the given leniency is a valid leniency, false
  otherwise. In its binary form it uses namespace inference."
  {:added "8.12.4-3" :tag Boolean}
  ([^clojure.lang.Keyword leniency]
   (contains? all leniency))
  ([^clojure.lang.Keyword leniency
    ^Boolean use-infer]
   (contains? all (util/ns-infer "phone-number.leniency" leniency use-infer))))

(defn valid-arg?
  "Returns true if the given leniency is a valid leniency to be used as an argument,
  false otherwise. In its binary form it uses namespace inference."
  {:added "8.12.16-1" :tag Boolean}
  ([^clojure.lang.Keyword leniency]
   (contains? all-arg leniency))
  ([^clojure.lang.Keyword leniency
    ^Boolean use-infer]
   (contains? all-arg (util/ns-infer "phone-number.leniency" leniency use-infer))))

(defn parse
  "Parses a leniency and returns a value that can be supplied to
  Libphonenumber methods."
  {:added "8.12.4-3" :tag PhoneNumberUtil$Leniency}
  ([^clojure.lang.Keyword k]
   (parse k true))
  ([^clojure.lang.Keyword k
    ^Boolean use-infer]
   (if (nil? k)
     default-val
     (let [k (util/ns-infer "phone-number.leniency" k use-infer)]
       (assert (valid-arg? k) (str "Leniency specification " k " is not valid"))
       (all k)))))

(defn generate-sample
  "Generates random leniency."
  {:added "8.12.4-3" :tag clojure.lang.Keyword}
  ([] (rand-nth all-vec))
  ([^java.util.Random rng] (util/get-rand-nth all-vec rng)))

(defn generate-sample-arg
  "Generates random leniency (suitable as an argument)."
  {:added "8.12.16-1" :tag clojure.lang.Keyword}
  ([] (rand-nth all-arg-vec))
  ([^java.util.Random rng] (util/get-rand-nth all-arg-vec rng)))

(defn generate-sample-val
  "Generates random leniency (string value)."
  {:added "8.12.4-3" :tag String}
  ([] (rand-nth by-val-vec))
  ([^java.util.Random rng] (util/get-rand-nth by-val-vec rng)))

(defn generate-sample-val-arg
  "Generates random leniency (string value, suitable as an argument)."
  {:added "8.12.16-1" :tag String}
  ([] (rand-nth by-val-arg-vec))
  ([^java.util.Random rng] (util/get-rand-nth by-val-arg-vec rng)))
