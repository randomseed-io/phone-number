(ns

    ^{:doc    "Wrappers for Google's Libphonenumber."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.core

  (:refer-clojure :exclude [format type])

  (:require [phone-number.util            :as      util]
            [clojure.string               :as        st]
            [trptr.java-wrapper.locale    :as         l])

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

(defprotocol ^{:added "8.12.4-0"} RegionCodeable
  "This protocol is used to utilize class-based single dispatch on a region code
  abstract passed as a first argument of its functions."

  (^{:added "8.12.4-0" :tag String} region
   [region-code]
   "Takes a region code (in most of the cases named a country code) expressed as an
   object that can be converted to a string and upper-cased and returns a string."))

(defprotocol ^{:added "8.12.4-0"} Phoneable
  "This protocol is used to utilize class-based single dispatch on a phone number
  abstract passed as a first argument of its functions."

  (^{:added "8.12.4-0" :tag Phonenumber$PhoneNumber} number
   [phone-number] [phone-number region-code]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and returns parsed PhoneNumber object. Second, optional argument should be a
    string containing region code which is helpful if a local number (without
    region code) was given. If the region code argument is passed and the first
    argument is already a kind of PhoneNumber then it will be ignored.")

  (^{:added "8.12.4-0" :tag Boolean} valid?
   [phone-number] [phone-number region-code]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and validates it. Returns true or false."))

(extend-protocol RegionCodeable

  String
  (region [region-code] (not-empty (st/upper-case region-code)))

  clojure.lang.StringSeq
  (region [region-code] (region (apply str region-code)))

  CharSequence
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
     (or (util/try-parse
          (.isValidNumber (util/instance) obj)) false)))

  String
  (number
    ([phone-number] (number phone-number nil))
    ([phone-number ^phone_number.core.RegionCodeable region-code]
     (.parse (util/instance) phone-number (region region-code))))
  (valid?
    ([obj](valid? obj nil))
    ([obj ^phone_number.core.RegionCodeable region-code]
     (or (util/try-parse
          (.isValidNumber (util/instance) (number obj region-code)) false))))

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

(defn native?
  "Returns true if the given argument is an instance of PhoneNumber class."
  {:added "8.12.4-0" :tag Boolean}
  [phone-number]
  (instance? Phonenumber$PhoneNumber phone-number))

(defn possible?
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns true if it is a possible number in a sense defined by
  libphonenumber. Otherwise it returns false. If the second argument is present then
  it should be a valid region code used when the given phone number does not contain
  region information."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (possible? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code]
   (or (util/try-parse
        (.isPossibleNumber (util/instance) (number phone-number region-code))) false)))

(defn formats
  "Returns all possible phone number formats as a sequence of keywords."
  {:added "8.12.4-0" :tag clojure.lang.APersistentMap$KeySeq}
  []
  (keys util/formats))

(defn types
  "Returns all possible phone number types as a sequence of keywords."
  {:added "8.12.4-0" :tag clojure.lang.APersistentMap$KeySeq}
  []
  (keys util/types))

(defn format
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns it as a formatted string. The second argument should be a format expressed
  as a keyword (use the all-formats function to list them) or a
  PhoneNumberType.

  If the third argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword format]
   (format phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword format
    ^phone_number.core.RegionCodeable region-code]
   (.format (util/instance)
            (number phone-number region-code)
            (if (keyword? format) (format util/formats) format))))

(defn all-formats
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map which keys are all possible formats expressed as keywords and values
  are string representations of the number formatted accordingly.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap}
  ([^phone_number.core.Phoneable phone-number]
   (all-formats phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code]
   (fmap #(format phone-number % region-code) util/formats)))

(defn type
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its type as a keyword.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^phone_number.core.Phoneable phone-number]
   (type phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code]
   (util/types-by-type
    (.getNumberType (util/instance) (number phone-number region-code))
    :unknown)))

(defn country-code
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its country code as an integer number.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  {:added "8.12.4-0" :tag Integer}
  ([^phone_number.core.Phoneable phone-number]
   (country-code phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code]
   (.getCountryCode (number phone-number region-code))))

(defn region-code
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its region code as a string or nil if the region happens to be empty.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable phone-number]
   (region-code phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code]
   (not-empty (.getRegionCodeForNumber (util/instance) (number phone-number region-code)))))

(defn location
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its possible geographic location as a string or nil if the location happens
  to be empty.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information. It is acceptable to
  pass nil as a value to tell the function that there is no region information.

  If the third argument is present then it should be a string specifying locale
  information or a java.util.Locale object. It will be used during rendering strings
  describing geographic location and carrier data. When nil is passed then the
  default locale settings will be used."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable phone-number]
   (location phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code]
   (location phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code
    ^java.util.Locale locale-specification]
   (not-empty
    (.getDescriptionForNumber
     (util/geo-coder)
     (number phone-number)
     (l/locale locale-specification)))))

(defn carrier
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its possible carrier name as a string or nil if the carrier name happens to
  be empty.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information. It is acceptable to
  pass nil as a value to tell the function that there is no region information.

  If the third argument is present then it should be a string specifying locale
  information or a java.util.Locale object. It will be used during rendering carrier
  name. When nil is passed then the default locale settings will be used."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable phone-number]
   (carrier phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code]
   (carrier phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code
    ^java.util.Locale locale-specification]
   (not-empty
    (.getNameForNumber
     (util/carrier-mapper)
     (number phone-number)
     (l/locale locale-specification)))))

(defn time-zones
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns all possible time zones which relate to its geographical location as a lazy
  sequence of strings (representing zone identifiers in English). Returns nil if the
  list would be empty.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information. It is acceptable to
  pass nil as a value to tell the function that there is no region information."
  {:added "8.12.4-0" :tag clojure.lang.Cons}
  ([^phone_number.core.Phoneable phone-number]
   (time-zones phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code]
   (->> phone-number
        number
        (.getTimeZonesForNumber (util/time-zones-mapper))
        (remove #{"Etc/Unknown"})
        dedupe
        seq))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code
    ^java.util.Locale locale-specification
    ^clojure.lang.Keyword format-specification]
   (let [l (l/locale locale-specification)
         f (util/time-zone-formats format-specification)]
     (->> (time-zones phone-number region-code)
          (map #(util/zone-id-format % l f))
          dedupe
          seq))))

(defn info
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map containing all possible information about the number with keywords as
  keys. These include:
  * validity (:valid?)
  * possibility of being a phone number (:possible?),
  * numerical country code (:country-code),
  * short region code (:region-code),
  * type of the number (:type),
  * approximate geographic location of  a phone line (:location),
  * carrier information (:carrier),
  * time zones (:time-zone/full, :time-zone/short, :time-zone/ids) and
  * all of the possible formats (keywords with the format namespace).

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information. It is acceptable to
  pass nil as a value to tell the function that there is no region information.

  If the third argument is present then it should be a string specifying locale
  information or a Locale object. It will be used during rendering strings describing
  geographic location, carrier data and full time zone information. When nil is
  passed then the default locale settings will be used."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap}
  ([^phone_number.core.Phoneable phone-number]
   (info phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code]
   (info phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^phone_number.core.RegionCodeable region-code
    ^String locale-specification]
   (let [number-util (util/instance)
         locale    (l/locale locale-specification)
         region    (region region-code)
         rcode-fn  phone-number.core/region-code
         phone-obj (number phone-number region)]
     (merge
      (all-formats phone-obj region)
      {:valid?           (valid?       phone-obj nil)
       :possible?        (possible?    phone-obj nil)
       :type             (type         phone-obj nil)
       :country-code     (country-code phone-obj nil)
       :region-code      (rcode-fn     phone-obj nil)
       :location         (location     phone-obj nil locale)
       :carrier          (carrier      phone-obj nil locale)
       :time-zones/full  (time-zones   phone-obj nil locale :full-standalone)
       :time-zones/short (time-zones   phone-obj nil locale :short-standalone)
       :time-zones/ids   (time-zones   phone-obj nil)}))))
