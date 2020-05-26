(ns

    ^{:doc    "Region handling for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.region

  (:refer-clojure :exclude [format type])

  (:require [phone-number.util            :as      util]
            [clojure.string               :as        st])

 (:import [com.google.i18n.phonenumbers
           PhoneNumberUtil
           NumberParseException]))

;;
;; Protocol
;;

(defprotocol ^{:added "8.12.4-0"} RegionCodeable
  "This protocol is used to utilize class-based single dispatch on a region code
  abstract passed as a first argument of its functions."

  (^{:added "8.12.4-0" :tag String} code
   [region-specification]
   "Takes a region specification (in most of the cases named a country code)
   expressed as an object that can be converted to a string and upper-cased and
   returns a string."))

(extend-protocol RegionCodeable

  String
  (code [region-specification] (not-empty (st/upper-case region-specification)))

  clojure.lang.StringSeq
  (code [region-specification] (code (apply str region-specification)))

  CharSequence
  (code [region-specification] (code (apply str (seq region-specification))))

  clojure.lang.Keyword
  (code [region-specification] (code (name region-specification)))

  clojure.lang.Symbol
  (code [region-specification] (code (name region-specification)))

  nil
  (code [region-specification] region-specification))

;;
;; Supported Regions
;;

(def ^{:added "8.12.4-0"
       :tag clojure.lang.IPersistentSet}
  all
  "Set of all supported regions."
  (set (.getSupportedRegions (util/instance))))

(defn valid?
  "Returns true if the given region-specification is a valid region code, false
  otherwise."
  {:added "8.12.4-0" :tag Boolean}
  [region-specification]
  (contains? all (code region-specification)))
