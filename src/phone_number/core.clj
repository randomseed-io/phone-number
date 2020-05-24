(ns

    ^{:doc    "Wrappers for Google's Libphonenumber."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.core

  (:refer-clojure :exclude [format type])

  (:require [phone-number.util            :as      util]
            [clojure.string               :as        st]
            [trptr.java-wrapper.locale    :as         l]
            [clojure.algo.generic.functor :refer [fmap]])

  (:import [com.google.i18n.phonenumbers
            Phonenumber$PhoneNumber
            ShortNumberInfo
            geocoding.PhoneNumberOfflineGeocoder
            PhoneNumberToCarrierMapper
            PhoneNumberToTimeZonesMapper
            NumberParseException]))

;;
;; Protocol
;;

;; add valid-region? (for either phone or just a region code)

(defprotocol RegionCodeable
  (^String region [region-code]))

(defprotocol Phoneable
  "This protocol is used to utilize class-based single dispatch on a phone number
  abstract passed as first argument of its functions."

  (^Phonenumber$PhoneNumber number
   [phone-number] [phone-number region-code]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and returns parsed PhoneNumber object. Second, optional argument should be a
    string containing region code which is helpful if a local number (without
    region code) was given. If the region code argument is passed and the first
    argument is already a kind of PhoneNumber then it will be ignored.")

  (^Boolean valid?
   [phone-number] [phone-number region-code]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and validates it. Returns true or false."))

(extend-protocol RegionCodeable

  String

  (region [region-code] (not-empty (st/upper-case region-code)))

  clojure.lang.StringSeq

  (region [region-code] (region (apply str region-code)))

  CharSequence

  (region [region-code] (region (apply str region-code)))
  (region [region-code] (region (apply str (seq region-code))))

  clojure.lang.Keyword

  (region [region-code] (region (name region-code)))

  clojure.lang.Symbol

  (region [region-code] (region (name region-code)))

  nil

  (region [region-code] region-code))

(extend-protocol Phoneable

  Phonenumber$PhoneNumber

  (number
    ([phone-number] phone-number)
    ([phone-number ^phone_number.core.RegionCodeable region-code] phone-number))

  (valid?
    ([obj] (valid? obj nil))
    ([obj ^phone_number.core.RegionCodeable region-code]
     (try
       (.isValidNumber (util/instance) obj)
       (catch NumberParseException  e false)
       (catch NumberFormatException e false))))

  String

  (number
    ([phone-number] (number phone-number nil))
    ([phone-number ^phone_number.core.RegionCodeable region-code]
     (.parse (util/instance) phone-number (region region-code))))

  (valid?
    ([obj](valid? obj nil))
    ([obj ^phone_number.core.RegionCodeable region-code]
     (try
       (.isValidNumber (util/instance) (number obj region-code))
       (catch NumberParseException  e false)
       (catch NumberFormatException e false))))

  Number

  (number
    ([phone-number] (number (str phone-number)))
    ([phone-number ^phone_number.core.RegionCodeable region-code] (number (str phone-number) region-code)))

  (valid?
    ([phone-number] (valid? (str phone-number)))
    ([phone-number ^phone_number.core.RegionCodeable region-code] (valid? (str phone-number) region-code)))

  nil

  (number
    ([phone-number] phone-number)
    ([phone-number ^phone_number.core.RegionCodeable region-code] phone-number))

  (valid?
    ([phone-number] false)
    ([phone-number ^phone_number.core.RegionCodeable region-code] false)))

;;
;; Public functions
;;

(defn ^Boolean native?
  "Returns true if the given argument is an instance of PhoneNumber class."
  [phone-number]
  (instance? Phonenumber$PhoneNumber phone-number))

(defn ^Boolean possible?
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns true if it is a possible number in a sense defined by
  libphonenumber. Otherwise it returns false. If the second argument is present then
  it should be a valid region code used when the given phone number does not contain
  region information."
  ([^phone_number.core.Phoneable phone-number] (possible? phone-number nil))
  ([^phone_number.core.Phoneable phone-number, ^phone_number.core.RegionCodeable region-code]
   (.isPossibleNumber (util/instance) (number phone-number region-code))))

(defn formats
  "Returns all possible phone number formats as a sequence of keywords."
  []
  (keys util/formats))

(defn types
  "Returns all possible phone number types as a sequence of keywords."
  []
  (keys util/types))

(defn ^String format
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns it as a formatted string. The second argument should be a format expressed
  as a keyword (use the all-formats function to list them) or a
  PhoneNumberType.

  If the third argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  ([^phone_number.core.Phoneable phone-number, ^clojure.lang.Keyword format] (format phone-number nil))
  ([^phone_number.core.Phoneable phone-number, ^clojure.lang.Keyword format, ^phone_number.core.RegionCodeable region-code]
   (.format (util/instance)
            (number phone-number region-code)
            (if (keyword? format) (format util/formats) format))))

(defn ^clojure.lang.IPersistentMap all-formats
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map which keys are all possible formats expressed as keywords and values
  are string representations of the number formatted accordingly.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  ([^phone_number.core.Phoneable phone-number] (all-formats phone-number nil))
  ([^phone_number.core.Phoneable phone-number, ^phone_number.core.RegionCodeable region-code]
   (fmap #(format phone-number % region-code) util/formats)))

(defn type
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its type as a keyword.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  ([^phone_number.core.Phoneable phone-number] (type phone-number nil))
  ([^phone_number.core.Phoneable phone-number, ^phone_number.core.RegionCodeable region-code]
   (util/types-by-type
    (.getNumberType (util/instance) (number phone-number region-code))
    :unknown)))

(defn ^clojure.lang.IPersistentMap info
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map containing all possible information about the number with keywords as
  keys. These include: validity (:valid?), possibility of being a phone
  number (:possible?), type of the number (:type), approximate geographic location of
  a phone line (:location), carrier information (:carrier), time zone (:timezone) and
  all of the possible formats (keywords with the format namespace).

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information. It is acceptable to
  pass nil as a value to tell the function that there is no region information.

  If the third argument is present then it should be a string specifying locale
  information or a Locale object. It will be used during rendering strings describing
  geographic location and carrier data. When nil is passed then the default locale
  settings will be used."
  ([^phone_number.core.Phoneable phone-number]
   (info phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number, ^phone_number.core.RegionCodeable region-code]
   (info phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number, ^phone_number.core.RegionCodeable region-code, ^String locale-specification]
   (let [number-util      (util/instance)
         geo-coder        (PhoneNumberOfflineGeocoder/getInstance)
         carrier-mapper   (PhoneNumberToCarrierMapper/getInstance)
         timezones-mapper (PhoneNumberToTimeZonesMapper/getInstance)
         locale           (l/locale locale-specification)
         phone-obj        (number phone-number region-code)
         timezones        (seq (.getTimeZonesForNumber timezones-mapper phone-obj))]
     (merge
      (all-formats phone-obj region-code)
      {:valid?     (.isValidNumber    number-util phone-obj)
       :possible?  (.isPossibleNumber number-util phone-obj)
       :type       (util/types-by-type (.getNumberType number-util phone-obj) :unknown)
       :location   (not-empty (.getDescriptionForNumber geo-coder  phone-obj locale))
       :carrier    (not-empty (.getNameForNumber   carrier-mapper  phone-obj locale))
       :timezone   (first timezones)
       :timezones  timezones}))))
