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

  (:import  [com.google.i18n.phonenumbers
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

(def ^{:added "8.12.4-0"
       :dynamic true
       :tag Boolean}
  *inferred-namespaces*
  "Decides whether keywords that are not fully-qualified should be automatically
  qualified (by attaching default namespaces) when passed as arguments to functions
  that operate on phone number types, phone number formats, region codes and time
  zone formats. Defaults to true."
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

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  region-codes
  "A set of all possible phone number region codes."
  (set (keys region/all)))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  formats
  "A set of all possible phone number formats as a sequence of keywords."
  (set (keys format/all)))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  types
  "A set of all possible phone number types as a sequence of keywords."
  (set (keys type/all)))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  match-types
  "A set of all possible phone number match types as a sequence of keywords."
  (set (keys match/all)))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  tz-formats
  "A set of all possible time zone formats as a sequence of keywords."
  (set (keys tz-format/all)))

;;
;; Protocol
;;

(defprotocol ^{:added "8.12.4-0"} Phoneable
  "This protocol is used to utilize class-based single dispatch on a phone number
  abstract passed as a first argument of its functions."

  (^{:added "8.12.4-0" :tag Phonenumber$PhoneNumber} number
   [phone-number] [phone-number region-code]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and returns parsed PhoneNumber object. Second, optional argument should be a
    keyword with region code which is helpful if a local number (without region code)
    was given. If the region code argument is passed and the first argument is
    already a kind of PhoneNumber then it will be ignored.")

  (^{:added "8.12.4-0" :tag Boolean} valid?
   [phone-number] [phone-number region-code]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and validates it. Returns true or false."))

(extend-protocol Phoneable

  Phonenumber$PhoneNumber
  (number
    ([phone-number] phone-number)
    ([phone-number
      ^clojure.lang.Keyword region-code]
     phone-number))
  (valid?
    ([obj] (valid? obj nil))
    ([obj
      ^clojure.lang.Keyword region-code]
     (util/try-parse-or-false
      (.isValidNumber (util/instance) obj))))

  String
  (number
    ([phone-number] (number phone-number nil))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (.parse (util/instance)
             phone-number
             (region/get region-code *inferred-namespaces*))))
  (valid?
    ([obj](valid? obj nil))
    ([obj
      ^clojure.lang.Keyword region-code]
     (util/try-parse-or-false
      (.isValidNumber
       (util/instance)
       (number obj region-code)))))

  Number
  (number
    ([phone-number] (number (str phone-number)))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (number (str phone-number) region-code)))
  (valid?
    ([phone-number] (valid? (str phone-number)))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (valid? (str phone-number) region-code)))

  nil
  (number
    ([phone-number] phone-number)
    ([phone-number
      ^clojure.lang.Keyword region-code]
     phone-number))
  (valid?
    ([phone-number] false)
    ([phone-number
      ^clojure.lang.Keyword region-code]
     false)))

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
  returns true if it is a possible number as defined by Libphonenumber. Otherwise it
  returns false. If the second argument is present then it should be a valid region
  code (a keyword) to be used when the given phone number does not contain region
  information."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable   phone-number]
   (possible? phone-number nil))
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          region-code]
   (util/try-parse-or-false
    (.isPossibleNumber
     (util/instance)
     (number phone-number region-code)))))

(defn possible-short?
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns true if it is a possible short number (like emergency etc.) as defined by
  Libphonenumber. Otherwise it returns false. If the second argument is present then
  it should be a valid region code (a keyword) to be used when the given phone number
  does not contain region information.

  In its ternary form this function takes an additional argument (region-from) that
  should be a valid region code for the origination of a possible call. That hint
  will be used to restrict the check according to rules. For example 112 may be valid
  in multiple regions but if one calls it from some particular region it might not be
  reachable number."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (util/try-parse-or-false
    (.isPossibleShortNumber
     (util/short)
     (number phone-number nil))))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (.isPossibleShortNumber
     (util/short)
     (number phone-number region-code))))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        region-from]
   (util/try-parse-or-false
    (.isPossibleShortNumberForRegion
     (util/short)
     (number phone-number region-code)
     (region/get region-from *inferred-namespaces*)))))

(defn valid-short?
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns true if it is a valid short number (like emergency etc.) as defined by
  Libphonenumber. Otherwise it returns false. If the second argument is present then
  it should be a valid region code (a keyword) to be used when the given phone number
  does not contain region information.

  In its ternary form this function takes an additional argument (region-from) that
  should be a valid region code for the origination of a possible call. That hint
  will be used to restrict the check according to rules. For example 112 may be valid
  in multiple regions but if one calls it from some particular region it might not be
  reachable number."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (util/try-parse-or-false
    (.isPossibleShortNumber
     (util/short)
     (number phone-number nil))))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (.isValidShortNumber
     (util/short)
     (number phone-number region-code))))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        region-from]
   (util/try-parse-or-false
    (.isValidShortNumberForRegion
     (util/short)
     (number phone-number region-code)
     (region/get region-from *inferred-namespaces*)))))

;;
;; Formatting
;;

(defn format
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns it as a formatted string. The second argument should be a format expressed
  as a keyword (use the all-formats function to list them) or a
  PhoneNumberType.

  If the third argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        format-specification]
   (format phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        format-specification]
   (.format (util/instance)
            (number phone-number region-code)
            (format/get format-specification *inferred-namespaces*))))

(defn all-formats
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map which keys are all possible formats expressed as keywords and values
  are string representations of the number formatted accordingly.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap}
  ([^phone_number.core.Phoneable   phone-number]
   (all-formats phone-number nil))
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          region-code]
   (let [p (number phone-number region-code)]
     (util/fmap-k #(format p nil %) format/all))))

;;
;; Number type
;;

(defn type
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its type as a keyword.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^phone_number.core.Phoneable phone-number]
   (type phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (type/by-val
    (.getNumberType
     (util/instance)
     (number phone-number region-code))
    ::type/unknown)))

;;
;; Country and region
;;

(defn country-code
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its country code as an integer number.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag Integer}
  ([^phone_number.core.Phoneable    phone-number]
   (country-code phone-number nil))
  ([^phone_number.core.Phoneable    phone-number
    ^clojure.lang.Keyword           region-code]
   (.getCountryCode (number phone-number region-code))))

(defn region-code
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its region code as a string or nil if the region happens to be empty.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable  phone-number]
   (region-code phone-number nil))
  ([^phone_number.core.Phoneable  phone-number
    ^clojure.lang.Keyword         region-code]
   (region/by-val
    (not-empty
     (.getRegionCodeForNumber
      (util/instance)
      (number phone-number region-code))))))

;;
;; Location
;;

(defn location
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its possible geographic location as a string or nil if the location happens
  to be empty.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no region
  information.

  If the third argument is present then it should be a string specifying locale
  information or a java.util.Locale object. It will be used during rendering strings
  describing geographic location and carrier data. When nil is passed then the
  default locale settings will be used."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable    phone-number]
   (location phone-number nil nil))
  ([^phone_number.core.Phoneable    phone-number
    ^clojure.lang.Keyword           region-code]
   (location phone-number region-code nil))
  ([^phone_number.core.Phoneable    phone-number
    ^clojure.lang.Keyword           region-code
    ^java.util.Locale               locale-specification]
   (not-empty
    (.getDescriptionForNumber
     (util/geo-coder)
     (number phone-number region-code)
     (l/locale locale-specification)))))

;;
;; Carrier
;;

(defn carrier
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its possible carrier name as a string or nil if the carrier name happens to
  be empty.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no region
  information.

  If the third argument is present then it should be a string specifying locale
  information or a java.util.Locale object. It will be used during rendering carrier
  name. When nil is passed then the default locale settings will be used."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable   phone-number]
   (carrier phone-number nil nil))
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          region-code]
   (carrier phone-number region-code nil))
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          region-code
    ^java.util.Locale              locale-specification]
   (not-empty
    (.getNameForNumber
     (util/carrier-mapper)
     (number phone-number region-code)
     (l/locale locale-specification)))))

;;
;; Time zones
;;

(defn time-zones
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns all possible time zones which relate to its geographical location as a lazy
  sequence of strings (representing zone identifiers in English). Returns nil if the
  list would be empty.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no region
  information."
  {:added "8.12.4-0" :tag clojure.lang.LazySeq}
  ([^phone_number.core.Phoneable  phone-number]
   (time-zones phone-number nil))
  ([^phone_number.core.Phoneable  phone-number
    ^clojure.lang.Keyword         region-code]
   (->> region-code
        (number phone-number)
        (.getTimeZonesForNumber (util/time-zones-mapper))
        util/lazy-iterator-seq
        (remove none)
        dedupe
        not-empty))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        format-specification]
   (time-zones phone-number region-code nil format-specification))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^java.util.Locale            locale-specification
    ^clojure.lang.Keyword        format-specification]
   (let [l (l/locale locale-specification)
         f (tz-format/get format-specification *inferred-namespaces*)]
     (->> (time-zones phone-number region-code)
          (map #(tz-format/transform % l f))
          dedupe
          not-empty))))

(defn time-zones-all-formats
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map which keys are all possible time zone formats expressed as keywords and values
  are sequences of the number's time zones formatted accordingly.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information. It is
  possible to pass a nil value as this argument to ignore extra processing when
  region can be inferred from the number.

  The third argument should be a Locale object or a string describing locale settings
  to be used when rendering locale-dependent time zone information. When there is no
  third argument or it is nil then default locale settings will be used."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap}
  ([^phone_number.core.Phoneable phone-number]
   (time-zones-all-formats phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (time-zones-all-formats phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^java.util.Locale            locale-specification]
   (let [l (l/locale locale-specification)
         p (number phone-number region-code)]
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

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no region
  information.

  If the third argument is present then it should be a string specifying locale
  information or a Locale object. It will be used during rendering strings describing
  geographic location, carrier data and full time zone information. When nil is
  passed then default locale settings will be used."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap}
  ([^phone_number.core.Phoneable phone-number]
   (info phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (info phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^String                      locale-specification]
   (let [number-util (util/instance)
         locale      (l/locale locale-specification)
         phone-obj   (number phone-number region-code)
         region-code phone-number.core/region-code]
     (->> #:phone-number
          {:valid?                      (valid?          phone-obj nil)
           :valid-short?                (valid?          phone-obj nil)
           :possible?                   (possible?       phone-obj nil)
           :possible-short?             (possible-short? phone-obj nil)
           :type                        (type            phone-obj nil)
           :country-code                (country-code    phone-obj nil)
           :region-code                 (region-code     phone-obj nil)
           :location                    (location        phone-obj nil locale)
           :carrier                     (carrier         phone-obj nil locale)
           ::tz-format/id               (time-zones      phone-obj nil locale ::tz-format/id)
           ::tz-format/full-standalone  (time-zones      phone-obj nil locale ::tz-format/full-standalone)
           ::tz-format/short-standalone (time-zones      phone-obj nil locale ::tz-format/short-standalone)}
          (merge (all-formats phone-obj nil))
          info-remove-nils))))

;;
;; Matching
;;

(defn match
  "Returns matching level of two numbers or nil if there is no match. Optionally each
  second argument can be a region code (if the given phone number is not a kind of
  PhoneNumber and is not prefixed by any country code )."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^phone_number.core.Phoneable phone-number-a
    ^clojure.lang.Keyword        region-code-a
    ^phone_number.core.Phoneable phone-number-b
    ^clojure.lang.Keyword        region-code-b]
   (match/by-val
    (.isNumberMatch
     (util/instance)
     (number phone-number-a region-code-a)
     (number phone-number-b region-code-b))
    ::match/none))
  ([^phone_number.core.Phoneable phone-number-a
    ^clojure.lang.Keyword        region-code-a
    ^phone_number.core.Phoneable phone-number-b]
   (match phone-number-a
          region-code-a
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
  can be a region code (if the given phone number is not a kind of PhoneNumber and is
  not prefixed by any country code)."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number-a
    ^clojure.lang.Keyword        region-code-a
    ^phone_number.core.Phoneable phone-number-b
    ^clojure.lang.Keyword        region-code-b]
   (util/try-parse-or-false
    (= ::match/exact
       (match phone-number-a
              region-code-a
              phone-number-b
              region-code-b))))
  ([^phone_number.core.Phoneable phone-number-a
    ^clojure.lang.Keyword        region-code-a
    ^phone_number.core.Phoneable phone-number-b]
   (util/try-parse-or-false
    (= ::match/exact
       (match phone-number-a
              region-code-a
              phone-number-b
              nil))))
  ([^phone_number.core.Phoneable phone-number-a
    ^phone_number.core.Phoneable phone-number-b]
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
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (contains?
     #{::type/fixed-line-or-mobile
       ::type/fixed-line ::mobile}
     (type phone-number region-code)))))

(defn is-uncertain-fixed-line-or-mobile?
  "Returns true if the given number belongs to a class of numbers that cannot be
  certainly decided as being mobile or fixed-line, false otherwise. Please note that
  it will return false for mobile or fixed-line numbers that are certainly classified
  as such."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (is-uncertain-fixed-line-or-mobile? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (= ::type/fixed-line-or-mobile
       (type phone-number region-code)))))

(defn is-maybe-mobile?
  "Returns true if the given number is a kind of a mobile number or a number that
  belongs to a class where it cannot be fully decided whether it is mobile or
  fixed-line. Returns false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (is-maybe-mobile? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (contains?
     #{::type/fixed-line-or-mobile
       ::type/mobile}
     (type phone-number region-code)))))

(defn is-maybe-fixed-line?
  "Returns true if the given number is a kind of a fixed-line number or a number that
  belongs to a class where it cannot be fully decided whether it is mobile or
  fixed-line. Returns false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (is-maybe-fixed-line? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (contains?
     #{::type/fixed-line-or-mobile
       ::type/fixed-line}
     (type phone-number region-code)))))

(defn has-known-type?
  "Returns true if the given number is of a known type, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-known-type? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (not (contains? none (type phone-number region-code))))))

;;
;; Region and country checking
;;

(defn has-region-code?
  "For the given phone number returns true if the region code is present in it, false
  otherwise. The region code can be explicit part of a number (as its prefix) or can
  be inferred by making use of the region-code argument."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-region-code? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (not (contains? none (region-code phone-number region-code))))))

(defn has-country-code?
  "For the given phone number returns true if the country code is present in it, false
  otherwise. The region code can be explicit part of a number (as its prefix) or can
  be inferred by making use of the region-code argument."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-country-code? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (not (contains? none (country-code phone-number region-code))))))

(defn has-location?
  "For the given phone number returns true if the approximate geographic location is
  present in it, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-location? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (not (contains? none (location phone-number region-code))))))

(defn has-time-zone?
  "For the given phone number returns true if any time zone information is present in
  it, false otherwise."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-time-zone? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (not (contains? none (time-zones phone-number region-code))))))

;;
;; Finding numbers
;;

(defn- enrich-match
  "Used to enrich the results of find-numbers with phone number information map.
  The whole map is put under the :phone-number.match/info key and it is a delay
  object, automatically dereferenced when accessed due to lazy map structure used
  under the hood."
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

  Optional, but highly recommended, second argument should be a region code to be
  used as a hint when looking for numbers without any country code prefix.

  The third optional argument should be a locale specification (Locale object or any
  other object that can initialize one, like a string with language and/or
  dialect). It will be used to render a value associated with
  the :phone-number.match/info key."
  {:added "8.12.4-0" :tag clojure.lang.LazySeq}
  ([^String               text]
   (find-numbers text nil nil))
  ([^String               text
    ^clojure.lang.Keyword region-code]
   (find-numbers text region-code nil))
  ([^String               text
    ^clojure.lang.Keyword region-code
    ^java.util.Locale     locale-specification]
   (->> *inferred-namespaces*
        (region/get region-code)
        (.findNumbers (util/instance) text)
        util/lazy-iterator-seq
        (map (comp
              (partial enrich-match locale-specification)
              match/mapper)))))

;;
;; Example numbers generation
;;

(defn invalid-example
  [])

(defn example-non-geo
  ([^Integer calling-code]
   (.getExampleNumberForNonGeoEntity
    (util/instance)
    calling-code)))

(defn example
  ([^clojure.lang.Keyword region-code]
   (example region-code nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type]
   (.getExampleNumberForType
    (util/instance)
    (region/get region-code *inferred-namespaces*)
    (type/get   number-type *inferred-namespaces*))))
