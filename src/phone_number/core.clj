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

(defprotocol Phoneable
  "This protocol is used to utilize class-based single dispatch on a phone number
  abstract passed as first argument of its functions."

  (^Phonenumber$PhoneNumber number
   [phone-number] [phone-number country-code]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and returns parsed PhoneNumber object. Second, optional argument should be a
    string containing country code which is helpful if a local number (without
    country code) was given. If the country code argument is passed and the first
    argument is already a kind of PhoneNumber then it will be ignored.")

  (^Boolean valid?
   [phone-number] [phone-number country-code]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and validates it. Returns true or false."))

(extend-protocol Phoneable

  Phonenumber$PhoneNumber

  (number
    ([phone-number] phone-number)
    ([phone-number ^String country-code] phone-number))

  (valid?
    ([obj] (valid? obj nil))
    ([obj ^String country-code]
     (try
       (.isValidNumber (util/instance) obj)
       (catch NumberParseException  e false)
       (catch NumberFormatException e false))))

  String

  (number
    ([phone-number] (number phone-number nil))
    ([phone-number ^String country-code]
     (.parse (util/instance)
             phone-number
             (when (some? country-code)
               (st/upper-case
                (if (string? country-code)
                  country-code
                  (if (ident? country-code)
                    (name country-code)
                    (str country-code))))))))

  (valid?
    ([obj](valid? obj nil))
    ([obj ^String country-code]
     (try
       (.isValidNumber (util/instance) (number obj country-code))
       (catch NumberParseException  e false)
       (catch NumberFormatException e false))))

  Number

  (number
    ([phone-number] (number (str phone-number)))
    ([phone-number ^String country-code] (number (str phone-number) country-code)))

  (valid?
    ([phone-number] (valid? (str phone-number)))
    ([phone-number ^String country-code] (valid? (str phone-number) country-code)))

  nil

  (number
    ([phone-number] phone-number)
    ([phone-number ^String country-code] phone-number))

  (valid?
    ([phone-number] false)
    ([phone-number ^String country-code] false)))

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
  it should be a valid country code used when the given phone number does not contain
  country information."
  ([^phone_number.core.Phoneable phone-number] (possible? phone-number nil))
  ([^phone_number.core.Phoneable phone-number, ^String country-code]
   (.isPossibleNumber (util/instance) (number phone-number country-code))))

(defn all-formats
  "Returns all possible phone number formats as a sequence of keywords."
  []
  (keys util/formats))

(defn all-types
  "Returns all possible phone number types as a sequence of keywords."
  []
  (keys util/types))

(defn ^String format
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns it as a formatted string. The second argument should be a format expressed
  as a keyword (use the all-formats function to list them) or a
  PhoneNumberType.

  If the third argument is present then it should be a valid country code used when
  the given phone number does not contain country information."
  ([^phone_number.core.Phoneable phone-number, ^clojure.lang.Keyword format] (format phone-number nil))
  ([^phone_number.core.Phoneable phone-number, ^clojure.lang.Keyword format, ^String country-code]
   (.format (util/instance)
            (number phone-number country-code)
            (if (keyword? format) (format util/formats) format))))

(defn ^clojure.lang.IPersistentMap all-formats
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map which keys are all possible formats expressed as keywords and values
  are string representations of the number formatted accordingly.

  If the second argument is present then it should be a valid country code used when
  the given phone number does not contain country information."
  ([^phone_number.core.Phoneable phone-number] (all-formats phone-number nil))
  ([^phone_number.core.Phoneable phone-number, ^String country-code]
   (fmap #(format phone-number % country-code) util/formats)))

(defn type
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its type as a keyword.

  If the second argument is present then it should be a valid country code used when
  the given phone number does not contain country information."
  ([^phone_number.core.Phoneable phone-number] (type phone-number nil))
  ([^phone_number.core.Phoneable phone-number, ^String country-code]
   (util/types-by-type
    (.getNumberType (util/instance) (number phone-number country-code))
    :unknown)))

(defn ^clojure.lang.IPersistentMap info
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map containing all possible information about the number with keywords as
  keys. These include: validity (:valid?), possibility of being a phone
  number (:possible?), type of the number (:type), approximate geographic location of
  a phone line (:location), carrier information (:carrier), time zone (:timezone) and
  all of the possible formats (keywords with the format namespace).

  If the second argument is present then it should be a valid country code used when
  the given phone number does not contain country information. It is acceptable to
  pass nil as a value to tell the function that there is no country information.

  If the third argument is present then it should be a string specifying locale
  information or a Locale object. It will be used during rendering strings describing
  geographic location and carrier data. When nil is passed then the default locale
  settings will be used."
  ([^phone_number.core.Phoneable phone-number]
   (info phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number, ^String country-code]
   (info phone-number country-code nil))
  ([^phone_number.core.Phoneable phone-number, ^String country-code, ^String locale-specification]
   (let [number-util      (util/instance)
         geo-coder        (PhoneNumberOfflineGeocoder/getInstance)
         carrier-mapper   (PhoneNumberToCarrierMapper/getInstance)
         timezones-mapper (PhoneNumberToTimeZonesMapper/getInstance)
         locale           (l/locale locale-specification)
         phone-obj        (number phone-number country-code)
         timezones        (seq (.getTimeZonesForNumber timezones-mapper phone-obj))]
     (merge
      (all-formats phone-obj country-code)
      {:valid?     (.isValidNumber    number-util phone-obj)
       :possible?  (.isPossibleNumber number-util phone-obj)
       :type       (util/types-by-type (.getNumberType number-util phone-obj) :unknown)
       :location   (not-empty (.getDescriptionForNumber geo-coder  phone-obj locale))
       :carrier    (not-empty (.getNameForNumber   carrier-mapper  phone-obj locale))
       :timezone   (first timezones)
       :timezones  timezones}))))
