(ns

    ^{:doc    "Wrappers for Google's Libphonenumber."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.cost

  (:require  [phone-number.util :as util])

  (:import [com.google.i18n.phonenumbers
            ShortNumberInfo
            ShortNumberInfo$ShortNumberCost]))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashMap}
  all
  "Map of phone number cost (keywords) to ShortNumberCost values."
  #::{:toll-free ShortNumberInfo$ShortNumberCost/TOLL_FREE
      :standard  ShortNumberInfo$ShortNumberCost/STANDARD_RATE
      :premium   ShortNumberInfo$ShortNumberCost/PREMIUM_RATE
      :unknown   ShortNumberInfo$ShortNumberCost/UNKNOWN_COST})

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashMap}
  by-val
  "Map of ShortNumberCost values to phone number costs (keywords)."
  (clojure.set/map-invert all))

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.Keyword}
  default ::standard)

(def ^{:added "8.12.4-0"
       :tag ShortNumberInfo$ShortNumberCost}
  default-val (all default))

(defn valid?
  "Returns true if the given cost is valid, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.Keyword cost]
  (contains? all cost))
