(ns

    ^{:doc    "Wrappers for Google's Libphonenumber."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.core

  (:refer-clojure :exclude [format type])

  (:require [phone-number.util            :as      util]
            [phone-number.type            :as      type]
            [phone-number.match           :as     match]
            [phone-number.format          :as    format]
            [phone-number.tz-format       :as tz-format]
            [phone-number.region          :as    region]
            [trptr.java-wrapper.locale    :as         l]
            [lazy-map.core                :refer   :all])

  (:import [phone_number.region RegionCodeable]
           [com.google.i18n.phonenumbers
            Phonenumber$PhoneNumber
            ShortNumberInfo]))

;;
;; Settings
;;

(def ^{:added "8.12.4-0"
       :dynamic true
       :tag Boolean}
  *info-removed-nils*
  "Decides whether results of info function should contain properties having nil
  values. They are removed by default."
  true)

;;
;; Constants
;;

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.PersistentHashSet}
  none
  "A set containing values considered to be none, unknown or empty."
  #{nil 0 false "" () {}
    :zero :nil :null
    :none :unknown      :etc/unknown
    ::type/unknown      ::type/none
    ::region/unknown    ::region/none
    ::format/unknown    ::format/none
    ::tz-format/unknown ::tz-format/none
    ::match/none
    "Etc/Unknown" "unknown" "none" "nil" "0"})

;;
;; Protocol
;;

(defprotocol ^{:added "8.12.4-0"} Phoneable
  "This protocol is used to utilize class-based single dispatch on a phone number
  abstract passed as a first argument of its functions."

  (^{:added "8.12.4-0" :tag Phonenumber$PhoneNumber} number
   [phone-number] [phone-number region-specification]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and returns parsed PhoneNumber object. Second, optional argument should be a
    string containing region code which is helpful if a local number (without
    region code) was given. If the region code argument is passed and the first
    argument is already a kind of PhoneNumber then it will be ignored.")

  (^{:added "8.12.4-0" :tag Boolean} valid?
   [phone-number] [phone-number region-specification]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and validates it. Returns true or false.")

  (^{:added "8.12.4-0" :tag Boolean} matches?
   [phone-number-a phone-number-b]
   [phone-number-a region-specification-a phone-number-b]
   [phone-number-a region-specification-a phone-number-b region-specification-b]
   "Compares two phone numbers with optional region codes."))

(extend-protocol Phoneable

  Phonenumber$PhoneNumber
  (number
    ([phone-number] phone-number)
    ([phone-number ^RegionCodeable region-specification] phone-number))
  (valid?
    ([obj] (valid? obj nil))
    ([obj ^RegionCodeable region-specification]
     (util/try-parse-or-false
      (.isValidNumber (util/instance) obj))))

  String
  (number
    ([phone-number] (number phone-number nil))
    ([phone-number ^RegionCodeable region-specification]
     (.parse (util/instance) phone-number (region/code region-specification))))
  (valid?
    ([obj](valid? obj nil))
    ([obj ^RegionCodeable region-specification]
     (util/try-parse-or-false
      (.isValidNumber (util/instance) (number obj region-specification)))))

  Number
  (number
    ([phone-number] (number (str phone-number)))
    ([phone-number ^RegionCodeable region-specification] (number (str phone-number) region-specification)))
  (valid?
    ([phone-number] (valid? (str phone-number)))
    ([phone-number ^RegionCodeable region-specification] (valid? (str phone-number) region-specification)))

  nil
  (number
    ([phone-number] phone-number)
    ([phone-number ^RegionCodeable region-specification] phone-number))
  (valid?
    ([phone-number] false)
    ([phone-number ^RegionCodeable region-specification] false)))

;;
;; Basic checks
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
  ([^phone_number.core.Phoneable   phone-number]
   (possible? phone-number nil))
  ([^phone_number.core.Phoneable   phone-number
    ^RegionCodeable                region-specification]
   (util/try-parse-or-false
    (.isPossibleNumber (util/instance) (number phone-number region-specification)))))

;;
;; Formatting
;;

(defn formats
  "Returns all possible phone number formats as a sequence of keywords."
  {:added "8.12.4-0" :tag clojure.lang.APersistentMap$KeySeq}
  []
  (keys format/all))

(defn format
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns it as a formatted string. The second argument should be a format expressed
  as a keyword (use the all-formats function to list them) or a
  PhoneNumberType.

  If the third argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          format-specification]
   (format phone-number nil))
  ([^phone_number.core.Phoneable   phone-number
    ^RegionCodeable                region-specification
    ^clojure.lang.Keyword          format-specification]
   (.format (util/instance)
            (number phone-number region-specification)
            (format/all format-specification format/default))))

(defn all-formats
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map which keys are all possible formats expressed as keywords and values
  are string representations of the number formatted accordingly.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap}
  ([^phone_number.core.Phoneable   phone-number]
   (all-formats phone-number nil))
  ([^phone_number.core.Phoneable   phone-number
    ^RegionCodeable                region-specification]
   (let [p (number phone-number region-specification)]
     (util/fmap-k #(format p nil %) format/all))))

;;
;; Number type
;;

(defn types
  "Returns all possible phone number types as a sequence of keywords."
  {:added "8.12.4-0" :tag clojure.lang.APersistentMap$KeySeq}
  []
  (keys type/all))

(defn type
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its type as a keyword.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^phone_number.core.Phoneable      phone-number]
   (type phone-number nil))
  ([^phone_number.core.Phoneable      phone-number
    ^RegionCodeable                   region-specification]
   (type/by-val
    (.getNumberType (util/instance) (number phone-number region-specification))
    ::type/unknown)))

;;
;; Country and region
;;

(defn country-code
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its country code as an integer number.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  {:added "8.12.4-0" :tag Integer}
  ([^phone_number.core.Phoneable      phone-number]
   (country-code phone-number nil))
  ([^phone_number.core.Phoneable      phone-number
    ^RegionCodeable                   region-specification]
   (.getCountryCode (number phone-number region-specification))))

(defn region-code
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its region code as a string or nil if the region happens to be empty.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable  phone-number]
   (region-code phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification]
   (not-empty
    (.getRegionCodeForNumber
     (util/instance)
     (number phone-number region-specification)))))


;;
;; Location
;;

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
  ([^phone_number.core.Phoneable      phone-number]
   (location phone-number nil nil))
  ([^phone_number.core.Phoneable      phone-number
    ^RegionCodeable                   region-specification]
   (location phone-number region-specification nil))
  ([^phone_number.core.Phoneable      phone-number
    ^RegionCodeable                   region-specification
    ^java.util.Locale                 locale-specification]
   (not-empty
    (.getDescriptionForNumber
     (util/geo-coder)
     (number phone-number)
     (l/locale locale-specification)))))

;;
;; Carrier
;;

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
  ([^phone_number.core.Phoneable      phone-number]
   (carrier phone-number nil nil))
  ([^phone_number.core.Phoneable      phone-number
    ^RegionCodeable                   region-specification]
   (carrier phone-number region-specification nil))
  ([^phone_number.core.Phoneable      phone-number
    ^RegionCodeable                   region-specification
    ^java.util.Locale                 locale-specification]
   (not-empty
    (.getNameForNumber
     (util/carrier-mapper)
     (number phone-number)
     (l/locale locale-specification)))))

;;
;; Time zones
;;

(defn time-zones
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns all possible time zones which relate to its geographical location as a lazy
  sequence of strings (representing zone identifiers in English). Returns nil if the
  list would be empty.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information. It is acceptable to
  pass nil as a value to tell the function that there is no region information."
  {:added "8.12.4-0" :tag clojure.lang.LazySeq}
  ([^phone_number.core.Phoneable      phone-number]
   (time-zones phone-number nil))
  ([^phone_number.core.Phoneable      phone-number
    ^RegionCodeable                   region-specification]
   (->> phone-number
        number
        (.getTimeZonesForNumber (util/time-zones-mapper))
        util/lazy-iterator-seq
        (remove none)
        dedupe
        not-empty))
  ([^phone_number.core.Phoneable      phone-number
    ^RegionCodeable                   region-specification
    ^clojure.lang.Keyword             format-specification]
   (time-zones phone-number region-specification nil format-specification))
  ([^phone_number.core.Phoneable      phone-number
    ^RegionCodeable                   region-specification
    ^java.util.Locale                 locale-specification
    ^clojure.lang.Keyword             format-specification]
   (let [l (l/locale locale-specification)
         f (tz-format/all format-specification tz-format/default)]
     (->> (time-zones phone-number region-specification)
          (map #(tz-format/transform % l f))
          dedupe
          not-empty))))

(defn time-zones-all-formats
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map which keys are all possible time zone formats expressed as keywords and values
  are sequences of the number's time zones formatted accordingly.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information. It is possible to pass
  a nil value as this argument to ignore extra processing when region can be inferred
  from the number.

  The third argument should be a Locale object or a string describing locale settings
  to be used when rendering locale-dependent time zone information. When there is no
  third argument or it is nil then default locale settings will be used."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap}
  ([^phone_number.core.Phoneable phone-number]
   (time-zones-all-formats phone-number nil nil))
  ([^phone_number.core.Phoneable      phone-number
    ^RegionCodeable                   region-specification]
   (time-zones-all-formats phone-number region-specification nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification
    ^java.util.Locale            locale-specification]
   (let [l (l/locale locale-specification)
         p (number phone-number region-specification)]
     (util/fmap-k #(time-zones p nil l %) tz-format/all))))


;;
;; Generic reporting
;;

(defn- ^clojure.lang.PersistentArrayMap info-remove-nils
  [^clojure.lang.PersistentArrayMap m]
  (if *info-removed-nils*
    (util/remove-empty-vals m)
    m))

(defn info
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map containing all possible information about the number with keywords as
  keys. These include:
  * validity (:phone-number/valid?)
  * possibility of being a phone number (:phone-number/possible?),
  * numerical country code (:phone-number/country-code),
  * short region code (:phone-number/region-code),
  * type of the number (:phone-number/type),
  * approximate geographic location of  a phone line (:phone-number/location),
  * carrier information (:phone-number/carrier),
  * time zones (:phone-number.tz-format/id,
                :phone-number.tz-format/short-standalone,
                :phone-number.tz-format/full-standalone) and
  * all of the possible formats (keywords with the :phone-number.format/ namespace).

  Keys with nil values assigned will be removed from the map unless the dynamic
  variable *info-removed-nils* is rebound to false.

  If the second argument is present then it should be a valid region code used when
  the given phone number does not contain region information. It is acceptable to
  pass nil as a value to tell the function that there is no region information.

  If the third argument is present then it should be a string specifying locale
  information or a Locale object. It will be used during rendering strings describing
  geographic location, carrier data and full time zone information. When nil is
  passed then default locale settings will be used."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap}
  ([^phone_number.core.Phoneable  phone-number]
   (info phone-number nil nil))
  ([^phone_number.core.Phoneable  phone-number
    ^RegionCodeable               region-specification]
   (info phone-number region-specification nil))
  ([^phone_number.core.Phoneable  phone-number
    ^RegionCodeable               region-specification
    ^String                       locale-specification]
   (let [number-util (util/instance)
         locale      (l/locale            locale-specification)
         phone-obj   (number phone-number region-specification)]
     (->> #:phone-number
          {:valid?                      (valid?       phone-obj nil)
           :possible?                   (possible?    phone-obj nil)
           :type                        (type         phone-obj nil)
           :country-code                (country-code phone-obj nil)
           :region-code                 (region-code  phone-obj nil)
           :location                    (location     phone-obj nil locale)
           :carrier                     (carrier      phone-obj nil locale)
           ::tz-format/id               (time-zones   phone-obj nil locale ::tz-format/id)
           ::tz-format/full-standalone  (time-zones   phone-obj nil locale ::tz-format/full-standalone)
           ::tz-format/short-standalone (time-zones   phone-obj nil locale ::tz-format/short-standalone)}
          (merge (all-formats phone-obj nil))
          info-remove-nils))))

;;
;; Matching
;;

(defn match
  "Returns matching level of two numbers or nil if there is no match. Optionally each
  second argument can be a region code (if the given phone number is not a kind of
  PhoneNumber)."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^phone_number.core.Phoneable      phone-number-a
    ^RegionCodeable                   region-specification-a
    ^phone_number.core.Phoneable      phone-number-b
    ^RegionCodeable                   region-specification-b]
   (match/by-val
    (.isNumberMatch
     (util/instance)
     (number phone-number-a region-specification-a)
     (number phone-number-b region-specification-b))
    ::match/none))
  ([^phone_number.core.Phoneable      phone-number-a
    ^RegionCodeable                   region-specification-a
    ^phone_number.core.Phoneable      phone-number-b]
   (match phone-number-a
          region-specification-a
          phone-number-b
          nil))
  ([^phone_number.core.Phoneable phone-number-a
    ^phone_number.core.Phoneable phone-number-b]
   (match phone-number-a
          nil
          phone-number-b
          nil)))

(defn match?
  "Returns true if two numbers match, false otherwise. Optionally each second argument
  can be a region code (if the given phone number is not a kind of PhoneNumber)."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable      phone-number-a
    ^RegionCodeable                   region-specification-a
    ^phone_number.core.Phoneable      phone-number-b
    ^RegionCodeable                   region-specification-b]
   (util/try-parse-or-false
    (= ::match/exact
       (match phone-number-a
              region-specification-a
              phone-number-b
              region-specification-b))))
  ([^phone_number.core.Phoneable      phone-number-a
    ^RegionCodeable                   region-specification-a
    ^phone_number.core.Phoneable      phone-number-b]
   (util/try-parse-or-false
    (= ::match/exact
       (match phone-number-a
              region-specification-a
              phone-number-b
              nil))))
  ([^phone_number.core.Phoneable      phone-number-a
    ^phone_number.core.Phoneable      phone-number-b]
   (util/try-parse-or-false
    (= ::match/exact
       (match phone-number-a
              nil
              phone-number-b
              nil)))))

;;
;; Number type checking
;;

(util/gen-ises  ; Auto-generated is-(type)? functions.
 (remove #{::type/fixed-line-or-mobile} (keys type/all))
 type)

(defn is-fixed-line-or-mobile?
  "Returns true if the given number is a kind of fixed-line number or a mobile number,
  false otherwise. Returns true also when there is a chance that a number is either
  mobile or fixed-line but it cannot be certainly decided."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (is-fixed-line-or-mobile? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification]
   (util/try-parse-or-false
    (contains?
     #{::type/fixed-line-or-mobile
       ::type/fixed-line ::mobile}
     (type phone-number)))))

(defn is-uncertain-fixed-line-or-mobile?
  "Returns true if the given number belongs to a class of numbers that cannot be
  certainly decided as being mobile or fixed-line, false otherwise. Please note that
  it will return false for mobile or fixed-line numbers that are certainly classified
  as such."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (is-uncertain-fixed-line-or-mobile? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification]
   (util/try-parse-or-false
    (= ::type/fixed-line-or-mobile
       (type phone-number region-specification)))))

(defn is-maybe-mobile?
  "Returns true if the given number is a kind of a mobile number or a number that
  belongs to a class where it cannot be fully decided whether it is mobile or
  fixed-line. Returns false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (is-maybe-mobile? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification]
   (util/try-parse-or-false
    (contains?
     #{::type/fixed-line-or-mobile
       ::type/mobile}
     (type phone-number region-specification)))))

(defn is-maybe-fixed-line?
  "Returns true if the given number is a kind of a fixed-line number or a number that
  belongs to a class where it cannot be fully decided whether it is mobile or
  fixed-line. Returns false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (is-maybe-fixed-line? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification]
   (util/try-parse-or-false
    (contains?
     #{::type/fixed-line-or-mobile
       ::type/fixed-line}
     (type phone-number region-specification)))))

(defn has-known-type?
  "Returns true if the given number is of a known type, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-known-type? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification]
   (util/try-parse-or-false
    (not (contains? none (type phone-number region-specification))))))

;;
;; Region and country checking
;;

(defn has-region-code?
  "For the given phone number returns true if the region code is present in it, false
  otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-region-code? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification]
   (util/try-parse-or-false
    (not (contains? none (region-code phone-number region-specification))))))

(defn has-country-code?
  "For the given phone number returns true if the country code is present in it, false
  otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-country-code? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification]
   (util/try-parse-or-false
    (not (contains? none (country-code phone-number region-specification))))))

(defn has-location?
  "For the given phone number returns true if the approximate geographic location is
  present in it, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-location? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification]
   (util/try-parse-or-false
    (not (contains? none (location phone-number region-specification))))))

(defn has-time-zone?
  "For the given phone number returns true if any time zone information is present in
  it, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-time-zone? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^RegionCodeable              region-specification]
   (util/try-parse-or-false
    (not (contains? none (time-zones phone-number region-specification))))))

;;
;; Finding numbers
;;

(defn- enrich-match
  "Used to enrich the results of find-numbers with phone number information map."
  {:added "8.12.4-0" :tag lazy_map.core.LazyMap}
  [^java.util.Locale      locale-specification
   ^lazy_map.core.LazyMap m]
  (if-some [n (::match/number m)]
    (assoc m ::match/info (delay (info (::match/number m) nil locale-specification)))
    m))

(defn find-numbers
  "Searches for phone numbers in the given text. Returns a lazy sequence of maps where
  each element is a map representing a match and having the following keys:

  * :phone-number.match/start       - start index of a phone number substring
  * :phone-number.match/end         - end index of a phone number substring
  * :phone-number.match/raw-string  - phone number substring
  * :phone-number.match/number      - phone number object
  * :phone-number.match/info        - phone number properties map

  Phone number object is suitable to be used with majority of functions from
  core. The info key is associated with a map holding all information rendered by
  calling info function. This map is lazily evaluated (as a whole), meaning the info
  function will be called when the value is accessed.

  Optional, but highly recommended, second argument should be a region specification
  used as a hint when looking for numbers without any country code prefix.

  The third optional argument should be a locale specification (Locale object or any
  other object that can initialize one, like a string with language and/or
  dialect). It will be used to render a value associated with
  the :phone-number.match/info key."
  {:added "8.12.4-0" :tag clojure.lang.LazySeq}
  ([^String           text]
   (find-numbers text nil nil))
  ([^String           text
    ^RegionCodeable   region-specification]
   (find-numbers text region-specification nil))
  ([^String           text
    ^RegionCodeable   region-specification
    ^java.util.Locale locale-specification]
   (->> region-specification
        region/code
        (.findNumbers (util/instance) text)
        util/lazy-iterator-seq
        (map (comp
              (partial enrich-match locale-specification)
              match/mapper)))))
