(ns

    ^{:doc    "Region handling for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.region

  (:refer-clojure :exclude [get])
  (:require [phone-number.util :as util])
  (:import  [com.google.i18n.phonenumbers
             PhoneNumberUtil
             NumberParseException]))

;;
;; Supported Regions
;;

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashMap}
  all
  "Mapping of supported regions (keywords) to region values (strings)."
  (let [tns (str (ns-name *ns*))]
    (into #::{}
          (map
           (juxt #(keyword tns (clojure.string/lower-case %)) identity)
           (.getSupportedRegions (util/instance))))))

(def ^{:added "8.12.4-0"
       :tag clojure.lang.PersistentHashMap}
  by-val
  "Mapping of supported region values (strings) to regions (keywords)."
  (clojure.set/map-invert all))

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.Keyword}
  default nil)

(def ^{:added "8.12.4-0"
       :const true
       :tag String}
  default-val (all default))

(defn get
  "Parses a region code and returns a value that can be supplied to Libphonenumber
  methods."
  {:added "8.12.4-0" :tag String}
  ([^clojure.lang.Keyword k]
   (get k true))
  ([^clojure.lang.Keyword k
    ^Boolean use-infer]
   (all (if use-infer (util/ns-infer "phone-number.region" k) k))))

(defn valid?
  "Returns true if the given region-specification is a valid region code, false
  otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.Keyword region-specification]
  (contains? all region-specification))
