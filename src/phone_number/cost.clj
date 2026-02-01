(ns

    ^{:doc    "Cost classes of short numbers for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.cost

  (:require  [clojure.set]
             [phone-number.util :as util])

  (:import (com.google.i18n.phonenumbers ShortNumberInfo
                                         ShortNumberInfo$ShortNumberCost)))

(def ^{:added "8.12.4-0"
       :tag   clojure.lang.PersistentArrayMap}
  all
  "Map of phone number cost (keywords) to ShortNumberCost values."
  #::{:toll-free ShortNumberInfo$ShortNumberCost/TOLL_FREE
      :standard  ShortNumberInfo$ShortNumberCost/STANDARD_RATE
      :premium   ShortNumberInfo$ShortNumberCost/PREMIUM_RATE
      :unknown   ShortNumberInfo$ShortNumberCost/UNKNOWN_COST})

(def ^{:added "8.12.16-1"
       :const true
       :tag clojure.lang.Keyword}
  unknown ::unknown)

(def ^{:added "8.12.16-1"
       :const true
       :tag String}
  unknown-val (all unknown))

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.Keyword}
  default ::standard)

(def ^{:added "8.12.4-0"
       :tag ShortNumberInfo$ShortNumberCost}
  default-val (all default))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  all-arg
  "Map of ShortNumberCost values to phone number costs (keywords) suitable to be passed
  as arguments."
  (dissoc all ::unknown))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  by-val
  "Map of ShortNumberCost values to phone number costs (keywords)."
  (clojure.set/map-invert all))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentArrayMap}
  by-val-arg
  "Map of ShortNumberCost values to phone number costs (keywords) suitable to be passed
  as method argument."
  (clojure.set/map-invert all-arg))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  all-vec
  "Vector of costs (keywords)."
  (vec (keys all)))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  all-arg-vec
  "Vector of costs (keywords) suitable as arguments."
  (vec (keys all-arg)))

(def ^{:added "8.12.4-0"
       :tag ShortNumberInfo$ShortNumberCost}
  by-val-vec
  "Vector of costs (ShortNumberCost values)."
  (vec (keys by-val)))

(def ^{:added "8.12.16-1"
       :tag ShortNumberInfo$ShortNumberCost}
  by-val-arg-vec
  "Vector of costs (ShortNumberCost values)."
  (vec (keys by-val-arg)))

(defn valid?
  "Returns true if the given cost is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword cost]
   (contains? all cost))
  ([^clojure.lang.Keyword cost
    use-infer]
   (contains? all (util/ns-infer "phone-number.cost" cost use-infer))))

(defn valid-arg?
  "Returns true if the given cost is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword cost]
   (contains? all-arg cost))
  ([^clojure.lang.Keyword cost
    use-infer]
   (contains? all-arg (util/ns-infer "phone-number.cost" cost use-infer))))

(defn parse
  "Parses a cost and returns a value that can be supplied to Libphonenumber methods. If
  nil is given it returns the default value."
  {:added "8.12.4-0" :tag ShortNumberInfo$ShortNumberCost}
  ([^clojure.lang.Keyword k]
   (parse k true))
  ([^clojure.lang.Keyword k
    use-infer]
   (if (nil? k)
     default-val
     (let [k (util/ns-infer "phone-number.cost" k use-infer)]
       (when-not (valid-arg? k)
         (throw (ex-info (str "Cost class " k " is not valid")
                         {:phone-number/error      :phone-number.cost/invalid
                          :phone-number/value      k
                          :phone-number/value-type (clojure.core/type k)
                          :phone-number/cost       k})))
       (all-arg k)))))

(defn generate-sample
  "Generates random number cost."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([] (rand-nth all-vec))
  ([^java.util.Random rng] (util/get-rand-nth all-vec rng)))

(defn generate-sample-val
  "Generates random number cost (ShortNumberCost value)."
  {:added "8.12.4-0" :tag ShortNumberInfo$ShortNumberCost}
  ([] (rand-nth by-val-vec))
  ([^java.util.Random rng] (util/get-rand-nth by-val-vec rng)))
