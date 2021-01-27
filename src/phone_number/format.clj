(ns

    ^{:doc    "Phone number formats for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.format

  (:require [clojure.set]
            [phone-number.util :as util])

  (:import  [com.google.i18n.phonenumbers
             PhoneNumberUtil
             PhoneNumberUtil$PhoneNumberFormat]))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  all
  "Map of possible format identifiers (keywords) to PhoneNumberFormat values
  plus an additional value indicating raw input."
  #::{:e164          PhoneNumberUtil$PhoneNumberFormat/E164
      :international PhoneNumberUtil$PhoneNumberFormat/INTERNATIONAL
      :national      PhoneNumberUtil$PhoneNumberFormat/NATIONAL
      :rfc3966       PhoneNumberUtil$PhoneNumberFormat/RFC3966
      :raw-input     ::raw})

(def ^{:added "8.12.16-1"
       :const true
       :tag clojure.lang.Keyword}
  raw ::raw-input)

(def ^{:added "8.12.16-1"
       :const true
       :tag String}
  raw-val (all raw))

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.Keyword}
  default ::international)

(def ^{:added "8.12.4-0"
       :tag PhoneNumberUtil$PhoneNumberFormat}
  default-val (all default))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentArrayMap}
  all-arg
  "Map of possible format identifiers (keywords) to PhoneNumberFormat values
  (plus an additional value indicating raw input) suitable to be used as arguments."
  all)

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentArrayMap}
  by-val
  "Map of possible PhoneNumberFormat values (plus an additional value indicating raw
  input) to format identifiers (keywords) suitable to be used as arguments."
  (clojure.set/map-invert all))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentArrayMap}
  by-val-arg
  "Map of possible PhoneNumberFormat values to format identifiers (keywords) suitable
  to be used as arguments for methods of Libphonenumber."
  (clojure.set/map-invert (dissoc all-arg :raw-input)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  all-vec
  "Vector of formats (keywords)."
  (vec (keys all)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  all-arg-vec
  "Vector of formats (keywords) suitable to be used as arguments."
  all-vec)

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentVector}
  by-val-vec
  "Vector of formats (PhoneNumberFormat values + additional value indicating raw format
  as keyword)."
  (vec (keys by-val)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  by-val-arg-vec
  "Vector of formats (PhoneNumberFormat values)."
  (vec (keys by-val-arg)))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  global
  "Set of formats (keywords) that should identify values containing country code information."
  #{::e164 ::international ::rfc3966})

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashSet}
  regional
  "Set of formats (keywords) that should identify values containing country code information."
  (clojure.set/difference (set (keys all)) global #{raw}))

(def ^{:added "8.12.4-1"
       :tag clojure.lang.PersistentVector}
  global-vec
  "Vector of formats (keywords)."
  (vec global))

(def ^{:added "8.12.4-1"
       :tag clojure.lang.PersistentVector}
  regional-vec
  "Vector of formats (keywords)."
  (vec regional))

(defn valid?
  "Returns true if the given format is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword format]
   (contains? all format))
  ([^clojure.lang.Keyword format
    ^Boolean use-infer]
   (contains? all (util/ns-infer "phone-number.format" format use-infer))))

(defn valid-arg?
  "Returns true if the given format is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword format]
   (contains? all-arg format))
  ([^clojure.lang.Keyword format
    ^Boolean use-infer]
   (contains? all-arg (util/ns-infer "phone-number.format" format use-infer))))

(defn parse
  "Parses a format specification and returns a value that can be supplied to
  Libphonenumber methods."
  {:added "8.12.4-0" :tag PhoneNumberUtil$PhoneNumberFormat}
  ([^clojure.lang.Keyword k]
   (parse k true))
  ([^clojure.lang.Keyword k
    ^Boolean use-infer]
   (if (nil? k)
     default-val
     (let [k (util/ns-infer "phone-number.format" k use-infer)]
       (assert (valid-arg? k) (str "Format specification " k " is not valid"))
       (all k)))))

(defn global?
  "Returns true if the given format contains country code information, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword format]
   (contains? global format))
  ([^clojure.lang.Keyword format
    ^Boolean use-infer]
   (contains? global (util/ns-infer "phone-number.format" format use-infer))))

(defn regional?
  "Returns true if the given format does not contain country code information, false
  otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^clojure.lang.Keyword format]
   (contains? regional format))
  ([^clojure.lang.Keyword format
    ^Boolean use-infer]
   (contains? regional (util/ns-infer "phone-number.format" format use-infer))))

;;
;; Backward compatibility
;;

(def ^{:added "8.12.4-0"
       :deprecated "8.12.4-1"
       :tag clojure.lang.PersistentHashSet}
  calling-coded
  "DEPRECATED: use `global`"
  global)

(def ^{:added "8.12.4-0"
       :deprecated "8.12.4-1"
       :tag Boolean
       :arglists '([^clojure.lang.Keyword format])}
  calling-coded?
  "DEPRECATED: use `global?`"
  global?)

(def ^{:added "8.12.4-0"
       :deprecated "8.12.4-1"
       :tag clojure.lang.PersistentHashSet}
  not-calling-coded
  "DEPRECATED: use `regional`"
  regional)

(def ^{:added "8.12.4-0"
       :deprecated "8.12.4-1"
       :tag Boolean
       :arglists '([^clojure.lang.Keyword format])}
  not-calling-coded?
  "DEPRECATED: use `regional?`"
  regional?)
