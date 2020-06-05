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
            [phone-number.cost            :as      cost]
            [phone-number.calling-code    :as    c-code]
            [clojure.string               :as    string]
            [trptr.java-wrapper.locale    :as         l]
            [lazy-map.core                :refer   :all])

  (:import  [com.google.i18n.phonenumbers
             Phonenumber$PhoneNumber]))

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
  regions
  "A set of all possible phone number region codes."
  (set region/all-vec))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  calling-codes
  "A set of all possible phone number region codes."
  c-code/all)

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  formats
  "A set of all possible phone number formats as a sequence of keywords."
  (set (keys format/all)))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  types
  "A set of all possible phone number types as a sequence of keywords."
  (set type/all-vec))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  match-types
  "A set of all possible phone number match types as a sequence of keywords."
  (set (keys match/all)))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  tz-formats
  "A set of all possible time zone formats as a sequence of keywords."
  (set (keys tz-format/all)))

;;
;; Protocol helper
;;

(defn- inf-get
  {:added "8.12.4-0"}
  ([^clojure.lang.IPersistentMap coll
    ^clojure.lang.Keyword k]
   (inf-get coll k nil))
  ([^clojure.lang.IPersistentMap coll
    ^clojure.lang.Keyword k
    default]
   (if *inferred-namespaces*
     (util/inferred-get coll k)
     (k coll default))))

(defn- inf-contains?
  {:added "8.12.4-0"}
  [^clojure.lang.IPersistentMap coll
   ^clojure.lang.Keyword k]
  (if *inferred-namespaces*
    (util/inferred-contains? coll k)
    (and (contains? coll k))))

(def ^{:added "8.12.4-0" :tag clojure.lang.LazySeq :private true}
  country-coded-formats-simple
  (map (comp keyword name) format/country-coded))

(def ^{:added "8.12.4-0" :tag clojure.lang.LazySeq :private true}
  not-country-coded-formats-simple
  (map (comp keyword name) format/not-country-coded))

(defn from-map
  "Tries to apply a function to a phone number obtained from a map using known keys."
  {:added "8.12.4-0" :tag Phonenumber$PhoneNumber}
  ([^clojure.lang.IFn            f
    ^clojure.lang.IPersistentMap m]
   (from-map f m nil))
  ([^clojure.lang.IFn            f
    ^clojure.lang.IPersistentMap m
    ^clojure.lang.Keyword        region-code]
   ;; try phone number object
   (if (inf-contains? m :phone-number/number)
     (f (inf-get m :phone-number/number) nil)
     ;; try phone number info map
     (if (inf-contains? m :phone-number/info)
       (from-map (inf-get m :phone-number/info))
       ;; try phone number formats containing region code information
       (if-some [t (some m format/country-coded)]
         (f t nil)
         (if-some [t (when *inferred-namespaces* (some m country-coded-formats-simple))]
           (f t nil)
           ;; try phone number formats without any region code information
           ;; obtain region from:
           ;; - country code number (:phone-number/country-code)
           ;; - different key (:phone-number/region or :region)
           ;; - or passed as an argument (region-code)
           (let [c (inf-get m :phone-number/country-code)
                 r (if (some? c) nil (inf-get m :phone-number/region region-code))]
             (if (or (some? c) (some? r))
               (if-some [t (some m format/not-country-coded)]
                 (if (some? c) (f (str "+" c t) nil) (f t r))
                 (if-some [t (when *inferred-namespaces* (some m not-country-coded-formats-simple))]
                   (if (some? c) (f (str "+" c t) nil) (f t r))
                   (if (some? c) (f nil nil) (f nil r))))
               (f nil nil)))))))))

;;
;; Protocol
;;

(defprotocol ^{:added "8.12.4-0"} Phoneable
  "This protocol is used to utilize class-based single dispatch on a phone number
  abstract passed as a first argument of its functions."

  (^{:added "8.12.4-0" :tag Boolean} valid-input?
   [phone-number]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and returns true if it is not empty or nil. Otherwise it returns nil.")

  (^{:added "8.12.4-0" :tag Phonenumber$PhoneNumber} number
   [phone-number] [phone-number region-code]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and returns parsed PhoneNumber object. Second, optional argument should be a
    keyword with region code which is helpful if a local number (without region code)
    was given. If the region code argument is passed and the first argument is
    already a kind of PhoneNumber then it will be ignored.")

  (^{:added "8.12.4-0" :tag Phonenumber$PhoneNumber} number-noraw
   [phone-number] [phone-number region-code]
   "Like number but does not preserve raw input.")

  (^{:added "8.12.4-0" :tag String} raw-input
   [phone-number] [phone-number region-code]
   "Returns a string used to initialize phone number object with the number
   function. For strings and numbers it returns a string representation. For nil
   values it returns nil.")

  (^{:added "8.12.4-0" :tag Boolean} valid?
   [phone-number] [phone-number region-code]
   "Takes a phone number represented as a string, a number or a PhoneNumber object
    and validates it. Returns true or false."))

(extend-protocol Phoneable

  Phonenumber$PhoneNumber
  (valid-input? [phone-number]
    true)
  (number-noraw
    ([phone-number] phone-number)
    ([phone-number ^clojure.lang.Keyword region-code] phone-number))
  (number
    ([phone-number] phone-number)
    ([phone-number ^clojure.lang.Keyword region-code] phone-number))
  (raw-input
    ([phone-number]
     (when (some? phone-number)
       (not-empty (.getRawInput phone-number))))
    ([phone-number ^clojure.lang.Keyword region-code]
     (raw-input phone-number)))
  (valid?
    ([obj] (valid? obj nil))
    ([obj
      ^clojure.lang.Keyword region-code]
     (util/try-parse-or-false
      (when (some? obj)
        (.isValidNumber (util/instance) obj)))))

  String
  (valid-input?
    [phone-number]
    (some? (not-empty phone-number)))
  (number-noraw
    ([phone-number] (number-noraw phone-number nil))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (when (valid-input? phone-number)
       (.parse
        (util/instance)
        phone-number
        (region/parse region-code *inferred-namespaces*)))))
  (number
    ([phone-number] (number phone-number nil))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (when (valid-input? phone-number)
       (.parseAndKeepRawInput
        (util/instance)
        phone-number
        (region/parse region-code *inferred-namespaces*)))))
  (raw-input
    ([phone-number] (not-empty phone-number))
    ([phone-number ^clojure.lang.Keyword region-code] (raw-input phone-number)))
  (valid?
    ([obj](valid? obj nil))
    ([obj
      ^clojure.lang.Keyword region-code]
     (util/try-parse-or-false
      (when (valid-input? obj)
        (.isValidNumber
         (util/instance)
         (number-noraw obj region-code))))))

  Number
  (valid-input?
    [phone-number] true)
  (number-noraw
    ([phone-number] (number-noraw (str phone-number)))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (number-noraw (str phone-number) region-code)))
  (number
    ([phone-number] (number (str phone-number)))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (number (str phone-number) region-code)))
  (raw-input
    ([phone-number] (raw-input (str phone-number)))
    ([phone-number ^clojure.lang.Keyword region-code] (raw-input phone-number)))
  (valid?
    ([phone-number] (valid? (str phone-number)))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (valid? (str phone-number) region-code)))

  clojure.lang.IPersistentMap
  (valid-input?
    [phone-number] (from-map (fn [p _] (valid-input? p)) phone-number nil))
  (number-noraw
    ([phone-number] (from-map number-noraw phone-number nil))
    ([phone-number ^clojure.lang.Keyword region-code] (from-map number-noraw phone-number region-code)))
  (number
    ([phone-number] (from-map number phone-number nil))
    ([phone-number ^clojure.lang.Keyword region-code] (from-map number phone-number region-code)))
  (raw-input
    ([phone-number] (from-map raw-input phone-number nil))
    ([phone-number ^clojure.lang.Keyword region-code] (from-map raw-input phone-number)))
  (valid?
    ([phone-number]  (from-map valid? phone-number nil))
    ([phone-number ^clojure.lang.Keyword region-code] (from-map valid? phone-number region-code)))

  nil
  (valid-input?
    [phone-number] false)
  (number-noraw
    ([phone-number] phone-number)
    ([phone-number ^clojure.lang.Keyword region-code] phone-number))
  (number
    ([phone-number] phone-number)
    ([phone-number ^clojure.lang.Keyword region-code] phone-number))
  (raw-input
    ([phone-number] phone-number)
    ([phone-number ^clojure.lang.Keyword region-code] phone-number))
  (valid?
    ([phone-number] false)
    ([phone-number ^clojure.lang.Keyword region-code] false))

  Object
  (valid-input?
    [phone-number] false)
  (number-noraw
    ([phone-number] phone-number)
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (number-noraw (str phone-number))))
  (number
    ([phone-number] phone-number)
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (number (str phone-number))))
  (raw-input
    ([phone-number] phone-number)
    ([phone-number ^clojure.lang.Keyword region-code]
     (raw-input (str phone-number))))
  (valid?
    ([phone-number] false)
    ([phone-number
      ^clojure.lang.Keyword region-code]
     false)))

(defmacro when-valid-input
  [phone-num & body]
  `(when (valid-input? ~phone-num)
     ~@body))

;;
;; Basic functions
;;

(defn native?
  "Returns true if the given argument is an instance of PhoneNumber class."
  {:added "8.12.4-0" :tag Boolean}
  [phone-number]
  (instance? Phonenumber$PhoneNumber phone-number))

(def
  ^{:added "8.12.4-0" :tag Boolean
    :arglists '([^phone_number.core.Phoneable phone-number]
                [^phone_number.core.Phoneable phone-number
                 ^clojure.lang.Keyword        region-code])}
  invalid?
  "Returns true if the given phone number (expressed as a string, a number or a
  PhoneNumber object) is not valid."
  (complement valid?))

(defn possible?
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns true if it is a possible number as defined by Libphonenumber. Otherwise it
  returns false. If the second argument is present then it should be a valid region
  code (a keyword) to be used when the given phone number does not contain region
  information."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (possible? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword         region-code]
   (util/try-parse-or-false
    (when-valid-input phone-number
      (.isPossibleNumber
       (util/instance)
       (number-noraw phone-number region-code))))))

(def
  ^{:added "8.12.4-0" :tag Boolean
    :arglists '([^phone_number.core.Phoneable phone-number]
                [^phone_number.core.Phoneable phone-number
                 ^clojure.lang.Keyword        region-code])}
  impossible?
  "Returns true if the given phone number (expressed as a string, a number or a
  PhoneNumber object) is not possible."
  (complement possible?))

(defn valid-for-region?
  "Like valid? but checks whether the given number is valid for a certain region. It
  only makes sense to use it when the country calling code for a number is not the
  same as the country calling code for the region."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable  phone-number
    ^clojure.lang.Keyword          region-code
    ^clojure.lang.Keyword        tested-region]
   (util/try-parse-or-false
    (when-valid-input phone-number
      (.isValidNumberForRegion
       (util/instance)
       (number-noraw phone-number region-code)
       (region/parse tested-region *inferred-namespaces*))))))

;;
;; Formatting
;;

(defn format
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns it as a formatted string. The last argument should be a format expressed
  as a keyword (use the all-formats function to list them) or a
  PhoneNumberType.

  If the second argument is present (and there are 3 arguments) then it should be a
  valid region code (a keyword) to be used when the given phone number does not
  contain region information."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable phone-number]
   (format phone-number nil format/default))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (format phone-number region-code format/default))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        format-specification]
   (let [f (format/parse format-specification *inferred-namespaces*)]
     (when-valid-input phone-number
       (not-empty
        (if (= :raw f)
          (if (native? phone-number)
            (raw-input phone-number)
            (str phone-number))
          (.format (util/instance)
                   (number-noraw phone-number region-code)
                   f)))))))

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
   (when-valid-input phone-number
     (type/by-val
      (.getNumberType
       (util/instance)
       (number-noraw phone-number region-code))
      ::type/unknown))))

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
   (when-valid-input phone-number
     (.getCountryCode
      (number-noraw phone-number region-code)))))

(defn region
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its region code as a string or nil if the region happens to be empty.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable  phone-number]
   (region phone-number nil))
  ([^phone_number.core.Phoneable  phone-number
    ^clojure.lang.Keyword         region-code]
   (region/by-val
    (not-empty
     (when-valid-input phone-number
       (.getRegionCodeForNumber
        (util/instance)
        (number-noraw phone-number region-code)))))))

;;
;; Location
;;

(defn location
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its possible geographic location as a string or nil if the location happens
  to be empty.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no explicit
  region information and it should extract it from a number.

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
   (when-valid-input phone-number
     (not-empty
      (.getDescriptionForNumber
       (util/geo-coder)
       (number-noraw phone-number region-code)
       (l/locale locale-specification))))))

(defn geographical?
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns true if it is a geographical number as defined by Libphonenumber. Otherwise it
  returns false. If the second argument is present then it should be a valid region
  code (a keyword) to be used when the given phone number does not contain region
  information."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable   phone-number]
   (geographical? phone-number nil))
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          region-code]
   (util/try-parse-or-false
    (when-valid-input phone-number
      (.isNumberGeographical
       (util/instance)
       (number-noraw phone-number region-code))))))

;;
;; Carrier
;;

(defn carrier
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns its possible carrier name as a string or nil if the carrier name happens to
  be empty.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no explicit
  region information and it should extract it from a number.

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
   (when-valid-input phone-number
     (not-empty
      (.getNameForNumber
       (util/carrier-mapper)
       (number-noraw phone-number region-code)
       (l/locale locale-specification))))))

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
  acceptable to pass nil as a value to tell the function that there is no explicit
  region information and it should extract it from a number."
  {:added "8.12.4-0" :tag clojure.lang.LazySeq}
  ([^phone_number.core.Phoneable  phone-number]
   (time-zones phone-number nil))
  ([^phone_number.core.Phoneable  phone-number
    ^clojure.lang.Keyword         region-code]
   (when-valid-input phone-number
     (->> region-code
          (number-noraw phone-number)
          (.getTimeZonesForNumber (util/time-zones-mapper))
          util/lazy-iterator-seq
          (remove none)
          distinct
          not-empty)))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        format-specification]
   (time-zones phone-number region-code nil format-specification))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^java.util.Locale            locale-specification
    ^clojure.lang.Keyword        format-specification]
   (let [l (l/locale locale-specification)
         f (tz-format/parse format-specification *inferred-namespaces*)]
     (->> (time-zones phone-number region-code)
          (map #(tz-format/transform % l f))
          distinct
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
         p (number-noraw phone-number region-code)]
     (util/fmap-k #(time-zones p nil l %) tz-format/all))))

;;
;; Short number specific
;;

(defn short-possible?
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns true if it is a possible short number (like emergency etc.) as defined by
  Libphonenumber. Otherwise it returns false. If the second argument is present then
  it should be a valid region code (a keyword) to be used when the given phone number
  does not contain region information.

  In its ternary form this function takes an additional argument (region-from) that
  should be a valid region code for the origination of a possible call. That hint
  will be used to restrict the check according to rules. For example 112 may be valid
  in multiple regions but if one calls it from some particular region it might not be
  reachable."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (short-possible? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (when-valid-input phone-number
      (.isPossibleShortNumber
       (util/short)
       (number-noraw phone-number region-code)))))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        region-from]
   (if (nil? region-from)
     (short-possible? phone-number region-code)
     (util/try-parse-or-false
      (when-valid-input phone-number
        (.isPossibleShortNumberForRegion
         (util/short)
         (number-noraw phone-number region-code)
         (region/parse region-from *inferred-namespaces*)))))))

(defn short-valid?
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns true if it is a valid short number (like emergency etc.) as defined by
  Libphonenumber. Otherwise it returns false. If the second argument is present then
  it should be a valid region code (a keyword) to be used when the given phone number
  does not contain region information.

  In its ternary form this function takes an additional argument (region-from) that
  should be a valid region code for the origination of a possible call. That hint
  will be used to restrict the check according to rules. For example 112 may be valid
  in multiple regions but if one calls it from some particular region it might not be
  reachable."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (short-valid? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (when-valid-input phone-number
      (.isValidShortNumber
       (util/short)
       (number-noraw phone-number region-code)))))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        region-from]
   (if (nil? region-from)
     (short-valid? phone-number region-code)
     (util/try-parse-or-false
      (when-valid-input phone-number
        (.isValidShortNumberForRegion
         (util/short)
         (number-noraw phone-number region-code)
         (region/parse region-from *inferred-namespaces*)))))))

(defn short-cost
  "Takes a short (like an emergency) phone number (expressed as a string, a number or a
  PhoneNumber object) and returns the expected cost class of that number as a
  keyword.

  The second, optional argument should be a valid region code (a keyword) to be used
  when the given phone number does not contain region information. It is acceptable
  to pass nil as a value to tell the function that there is no explicit region
  information and it should extract it from a number.

  If the third argument is present then it should be a valid region code for the
  origination of a possible call. That hint will be used to restrict the check
  according to rules. For example 112 may be valid in multiple regions but if one
  calls it from some particular region it might not be reachable."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^phone_number.core.Phoneable phone-number]
   (short-cost phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (when-valid-input phone-number
     (cost/by-val
      (.getExpectedCost
       (util/short)
       (number-noraw phone-number region-code)))))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        region-from]
   (if (nil? region-from)
     (short-cost phone-number region-code)
     (when-valid-input phone-number
       (cost/by-val
        (.getExpectedCostForRegion
         (util/short)
         (number-noraw phone-number region-code)
         (region/parse region-from *inferred-namespaces*)))))))

(defn short-emergency?
  "Takes a short (like an emergency) phone number (expressed as a string!) and returns
  true if it is exactly the emergency number. The second argument should be a valid
  region code (a keyword).

  When the region-code argument is nil it returns nil."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^String                phone-number
    ^clojure.lang.Keyword  region-code]
   (when (some? region-code)
     (util/try-parse-or-false
      (when-valid-input phone-number
        (.isEmergencyNumber
         (util/short)
         (str phone-number)
         (region/parse region-code *inferred-namespaces*)))))))

(defn short-to-emergency?
  "Takes a short (like an emergency) phone number (expressed as a string!) and returns
  true if it can be used to connect to emergency services. The second argument should
  be a valid region code (a keyword).

  When the region-code argument is nil it returns nil."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^String                phone-number
    ^clojure.lang.Keyword  region-code]
   (when (some? region-code)
     (util/try-parse-or-false
      (when-valid-input phone-number
        (.connectsToEmergencyNumber
         (util/short)
         (str phone-number)
         (region/parse region-code *inferred-namespaces*)))))))

(defn short-carrier-specific?
  "Takes a short phone number (expressed as a string, a number or a PhoneNumber object),
  optional region code (or nil) and optional calling region code. Returns true if it
  is a carrier-specific number."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (short-carrier-specific? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (when-valid-input phone-number
      (.isCarrierSpecific
       (util/short)
       (number-noraw phone-number region-code)))))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        region-from]
   (if (nil? region-from)
     (short-carrier-specific? phone-number region-code)
     (util/try-parse-or-false
      (when-valid-input phone-number
        (.isCarrierSpecificForRegion
         (util/short)
         (number-noraw phone-number region-code)
         (region/parse region-from *inferred-namespaces*)))))))

(defn short-sms-service?
  "Takes a short phone number (expressed as a string, a number or a PhoneNumber object),
  optional region code (or nil) and a calling region code. Returns true if SMS is
  supported."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        region-from]
   (util/try-parse-or-false
    (when-valid-input phone-number
      (.isSmsServiceForRegion
       (util/short)
       (number-noraw phone-number region-code)
       (region/parse region-from *inferred-namespaces*))))))

;;
;; Generic reporting
;;

(defn- ^clojure.lang.PersistentArrayMap info-remove-nils
  [^clojure.lang.PersistentArrayMap m]
  (if *info-removed-nils*
    (util/remove-empty-vals m)
    m))

(defn short-info
  "Takes a short (like an emergency) phone number (expressed as a string, a number or a
  PhoneNumber object) and returns a map containing all possible information about the
  number with keywords as keys.

  Keys with nil values assigned will be removed from the map unless the dynamic
  variable *info-removed-nils* is rebound to false.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no explicit
  region information and it should extract it from a number.

  If the third argument is present then it should be a valid region code for the
  origination of a possible call. That hint will be used to restrict the check
  according to rules. For example 112 may be valid in multiple regions but if one
  calls it from some particular region it might not be reachable."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap}
  ([^phone_number.core.Phoneable phone-number]
   (short-info phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (short-info phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        region-from]
   (when-valid-input phone-number
     (let [region-from  (region/parse region-from *inferred-namespaces*)
           phone-obj    (number-noraw phone-number region-code)
           region-code  (region phone-obj nil)
           phone-number (raw-input phone-number)
           phone-number (if (nil? phone-number)
                          (format phone-obj nil ::format/national) ;; fallback
                          phone-number)
           sh-possible   (short-possible? phone-obj nil region-from)
           sh-valid      (short-valid?    phone-obj nil region-from)]
       (if (or sh-valid sh-possible)
         (->> #:phone-number.short
              {:valid?            sh-valid
               :possible?         sh-possible
               :calling-region    (region/by-val           region-from)
               :carrier-specific? (short-carrier-specific? phone-obj nil region-from)
               :cost              (short-cost              phone-obj nil region-from)
               :sms-service?      (short-sms-service?      phone-obj nil region-from)
               :emergency?        (short-emergency?        phone-number region-code)
               :to-emergency?     (short-to-emergency?     phone-number region-code)}
              info-remove-nils)
         #:phone.number.short
         {:possible? false
          :valid?    false})))))

(defn info
  "Takes a phone number (expressed as a string, a number or a PhoneNumber object) and
  returns a map containing all possible information about the number with keywords as
  keys. These include:
  * validity (:phone-number/valid?)
  * possibility of being a phone number (:phone-number/possible?),
  * numerical country code (:phone-number/country),
  * region code (:phone-number/region),
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
  acceptable to pass nil as a value to tell the function that there is no explicit
  region information and it should extract it from a number.

  If the third argument is present then it should be a string specifying locale
  information or a Locale object. It will be used during rendering strings describing
  geographic location, carrier data and full time zone information. When nil is
  passed then default locale settings will be used.

  If there are four arguments then the last one should be a calling region code
  intended to be used with short numbers (like 112 etc.). It describes originating
  region to help validate the possibility of reaching the destination number."
  {:added "8.12.4-0" :tag clojure.lang.PersistentHashMap}
  ([^phone_number.core.Phoneable phone-number]
   (info phone-number nil nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (info phone-number region-code nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^String                      locale-specification]
   (info phone-number region-code locale-specification nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^String                      locale-specification
    ^clojure.lang.Keyword        region-from]
   (when-valid-input phone-number
     (let [locale       (l/locale locale-specification)
           phone-obj    (number phone-number region-code)
           region-code  (region phone-obj nil)
           phone-number (raw-input phone-number)
           phone-number (if (nil? phone-number) phone-obj phone-number)]
       (->> #:phone-number
            {:region                      region-code
             :valid?                      (valid?        phone-obj nil)
             :possible?                   (possible?     phone-obj nil)
             :geographical?               (geographical? phone-obj nil)
             :type                        (type          phone-obj nil)
             :country-code                (country-code  phone-obj nil)
             :location                    (location      phone-obj nil locale)
             :carrier                     (carrier       phone-obj nil locale)
             ::tz-format/id               (time-zones    phone-obj nil locale ::tz-format/id)
             ::tz-format/full-standalone  (time-zones    phone-obj nil locale ::tz-format/full-standalone)
             ::tz-format/short-standalone (time-zones    phone-obj nil locale ::tz-format/short-standalone)}
            (merge (all-formats phone-obj nil))
            (merge (short-info phone-number region-code region-from))
            info-remove-nils)))))

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
   (when-valid-input phone-number-a
     (when-valid-input phone-number-b
       (match/by-val
        (.isNumberMatch
         (util/instance)
         (number-noraw phone-number-a region-code-a)
         (number-noraw phone-number-b region-code-b))
        ::match/none))))
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

(def is-short? short-valid?)
(def is-maybe-short? short-possible?)

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

(defn has-region?
  "For the given phone number returns true if the region code is present in it, false
  otherwise. The region code can be explicit part of a number (as its prefix) or can
  be inferred by making use of the region-code argument."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-region? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (not (contains? none (region phone-number region-code))))))

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
  The whole map is put under the :phone-number/info key and it is a delay
  object, automatically dereferenced when accessed due to lazy map structure used
  under the hood."
  {:added "8.12.4-0" :tag lazy_map.core.LazyMap}
  [^java.util.Locale      locale-specification
   ^clojure.lang.Keyword  region-from
   ^lazy_map.core.LazyMap m]
  (if-some [n (:phone-number/number m)]
    (assoc m :phone-number/info (delay (info (:phone-number/number m) nil locale-specification region-from)))
    m))

(defn find-numbers
  "Searches for phone numbers in the given text. Returns a lazy sequence of maps where
  each element is a map representing a match and having the following keys:

  * :phone-number.match/start       - start index of a phone number substring
  * :phone-number.match/end         - end index of a phone number substring
  * :phone-number.match/raw-string  - phone number substring
  * :phone-number/number            - phone number object
  * :phone-number/info              - phone number properties map

  Phone number object is suitable to be used with majority of functions from
  core. The info key is associated with a map holding all information rendered by
  calling info function. This map is lazily evaluated (as a whole), meaning the info
  function will be called when the value is accessed.

  Optional, but highly recommended, second argument should be a region code to be
  used as a hint when looking for numbers without any country code prefix.

  The third optional argument should be a locale specification (Locale object or any
  other object that can initialize one, like a string with language and/or
  dialect). It will be used to render a value associated with
  the :phone-number/info key."
  {:added "8.12.4-0" :tag clojure.lang.LazySeq}
  ([^String               text]
   (find-numbers text nil nil))
  ([^String               text
    ^clojure.lang.Keyword region-code]
   (find-numbers text region-code nil))
  ([^String               text
    ^clojure.lang.Keyword region-code
    ^java.util.Locale     locale-specification]
   (find-numbers text region-code locale-specification nil))
  ([^String               text
    ^clojure.lang.Keyword region-code
    ^java.util.Locale     locale-specification
    ^clojure.lang.Keyword region-from]
   (->> *inferred-namespaces*
        (region/parse region-code)
        (.findNumbers (util/instance) text)
        util/lazy-iterator-seq
        (map (comp
              (partial enrich-match
                       (l/locale locale-specification)
                       (region/parse region-from *inferred-namespaces*))
              match/mapper)))))

;;
;; Example numbers generation
;;

(defn invalid-example
  {:added "8.12.4-0" :tag Phonenumber$PhoneNumber}
  [^clojure.lang.Keyword region-code]
  (when-some [rcode (region/parse region-code *inferred-namespaces*)]
    (util/try-null
     (.getInvalidExampleNumber
      (util/instance)
      rcode))))

(defn example-non-geo
  {:added "8.12.4-0" :tag Phonenumber$PhoneNumber}
  ([^Integer calling-code]
   (when (some? calling-code)
     (util/try-null
      (.getExampleNumberForNonGeoEntity
       (util/instance)
       calling-code)))))

(defn example
  {:added "8.12.4-0" :tag Phonenumber$PhoneNumber}
  ([^clojure.lang.Keyword region-code]
   (example region-code nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type]
   (when-some [rcode (region/parse region-code *inferred-namespaces*)]
     (when-some [ptype (type/parse number-type *inferred-namespaces*)]
       (util/try-null
        (.getExampleNumberForType
         (util/instance)
         rcode
         ptype))))))

(defn- gen-sample
  "Internal phone number samples generator."
  {:added "8.12.4-0" :tag Phonenumber$PhoneNumber}
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^Number                      keep-digits
    ^Number                      retries
    ^clojure.lang.IFn            validator]
   (when-some [template (number-noraw phone-number region-code)]
     (let [template-num (format template nil ::format/e164)
           template-len (unchecked-int (count template-num))
           ccode        (str (country-code template nil))
           cnt-country  (unchecked-inc-int (count ccode))
           num-national (not-empty (subs template-num cnt-country))
           cnt-national (unchecked-int (count num-national))
           keep-digits  (unchecked-int (if (nil? keep-digits) 0 keep-digits))
           keep-digits  (if (> keep-digits cnt-national) cnt-national keep-digits)
           prefix       (str "+" ccode (if (zero? keep-digits) nil (subs num-national 0 keep-digits)))
           max-len-gen  (unchecked-subtract-int cnt-national keep-digits)
           template     (if (validator template) template nil)]
       (if (some? retries)
         (loop [last-valid template
                iteration  (unchecked-long retries)
                gen-length (unchecked-int 0)
                cur-prefix template-num
                valid-hits (unchecked-long 0)]
           (if (or (zero? iteration) (and (> gen-length max-len-gen) (some? last-valid)))
             [(unchecked-subtract retries iteration) (unchecked-dec-int gen-length) valid-hits last-valid]
             (let [test-number (number-noraw (str cur-prefix (util/gen-digits gen-length)) nil)
                   have-valid  (validator test-number)
                   shorten     (or have-valid (and (nil? last-valid) (not= gen-length max-len-gen)))
                   new-length  (if shorten (unchecked-inc-int gen-length) gen-length)
                   new-prefix  (if shorten (subs cur-prefix 0 (unchecked-subtract-int template-len new-length)) cur-prefix)]
               (recur  (if have-valid test-number last-valid)
                       (unchecked-dec iteration)
                       new-length
                       new-prefix
                       (if have-valid (unchecked-inc valid-hits) valid-hits)))))
         (loop [last-valid template
                gen-length (unchecked-int 0)
                cur-prefix template-num
                retries    1
                valid-hits 0]
           (if (and (> gen-length max-len-gen) (some? last-valid))
             [retries (unchecked-dec-int gen-length) valid-hits last-valid]
             (let [test-number (number-noraw (str cur-prefix (util/gen-digits gen-length)) nil)
                   have-valid  (validator test-number)
                   shorten     (or have-valid (and (nil? last-valid) (not= gen-length max-len-gen)))
                   new-length  (if shorten (unchecked-inc-int gen-length) gen-length)
                   new-prefix  (if shorten (subs cur-prefix 0 (unchecked-subtract-int template-len new-length)) cur-prefix)]
               (recur  (if have-valid test-number last-valid)
                       new-length
                       new-prefix
                       (inc retries)
                       (if have-valid (inc valid-hits) valid-hits))))))))))

(defn generate
  "Generates sample phone number in a form of a map with two keys:
  - :phone-number/number      - PhoneNumber object
  - :phone-number/info        - a map with phone number information (evaluated on access)
  - :phone-number.sample/hits - a number of valid hits encountered during sampling
  - :phone-number.sample/random-digits - a number of digits that are randomly generated
  - :phone-number.sample/samples - a number of samples processed before the result was formed

  It is important to note that the result may be valid or invalid phone number. To
  get only valid number pass the valid? predicate function as the third
  argument (described later).

  Without any arguments it generates any geographical number of any possible region
  and type.

  When the first argument is present it should be a valid region code and the result
  will be a number that belongs to that region. It is possible to pass nil as a value
  (in order to make use of other positional arguments). In such case the region will
  be picked up randomly.

  When the second argument is present it should be a valid number type and the result
  will be a number that is of that type. It is possible to pass nil as a value
  (in order to make use of other positional arguments). In such case the type will be
  picked up randomly.

  When the third argument is present it should be a predicate function used by
  samples generator to look for a number for which the function returns truthy
  value (not false and not nil). It is possible to pass nil as a value to disable
  this check.

  When the fourth argument is present it should be a maximal number of attempts the
  internal sampler will perform to get the desired sample. By default it will try to
  get the sample that meets the criteria (country code, type and a custom predicate)
  in 1000 attempts but when the supplied predicate makes it too improbable to get the
  desired result the operation may fail and this number should be increased. It is
  possible to pass nil as a value. In such case the default will be used. It is also
  possible to pass false as a value. In such case the sampler will continue
  indefinitely which poses the risk of freezing the program for complicated or
  impossible conditions.

  It is important to know that even relatively low retry counts will produce valid
  results in most cases. This is due to shrinking strategy the internal sampler
  uses. It starts by taking an initial, template number returned by the example
  function. This number is valid but may not fulfill additional criteria. If it
  fulfills them it is memorized and the next, more fuzzed variant is tried with last
  digit replaced by a randomly generated one. If such number is also valid it is
  memorized and the shrinking continues until all digits (except the country code
  plus the static part described later) are randomized. When that happens the result
  is returned if it fulfills all of the validation criteria or the number of retries
  reaches the given maximal value. If the final result (after all the trials) is not
  valid then the memorized number is returned.

  When the fifth argument is present it should be a number of digits that will remain
  constant when sampling is performed. If nil is given or the argument is not passed
  then this parameter will be dynamically incremented to increase the chance of
  meeting validation criteria.

  When the sixth argument is present it should be a valid locale specification or a
  java.util.Locale object that will be passed to the info function in order to render
  localized versions of time zone information and geographical locations."
  {:added "8.12.4-0" :tag lazy_map.core.LazyMap}
  ([]
   (generate nil nil nil nil nil nil))
  ([^clojure.lang.Keyword region-code]
   (generate region-code nil nil nil nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type]
   (generate region-code number-type nil nil nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate]
   (generate region-code number-type predicate nil nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Number               retries]
   (generate region-code number-type predicate retries nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Number               retries
    ^Number               keep-digits]
   (generate region-code number-type predicate retries keep-digits nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Number               retries
    ^Number               keep-digits
    ^java.util.Locale     locale-specification]
   (let [predicate            (if (nil? predicate) any? predicate)
         retries              (if (nil? retries) 1000 (if (false? retries) nil retries))
         locale-specification (l/locale locale-specification)]
     (loop [region-code region-code
            number-type number-type
            template-tries (unchecked-dec-int
                            (unchecked-multiply-int
                             (if (nil? region-code) (count regions) 1)
                             (if (nil? number-type) (count types)   1)))]
       (let [number-type' (if (some? number-type) number-type (type/generate-sample))
             region-code' (if (some? region-code) region-code (region/generate-sample))]
         (if-some [template (example region-code' number-type')]
           (let [number-type'  (if (some? number-type) (type   template) number-type')
                 region-code'  (if (some? region-code) (region template) region-code')
                 valid-type?   (if (some? number-type) #(= number-type' (type   %)) any?)
                 valid-region? (if (some? region-code) #(= region-code' (region %)) any?)]
             (let [[samples random-digits valid-hits phone-number]
                   (gen-sample template
                               nil
                               keep-digits
                               retries
                               (every-pred predicate valid-type? valid-region?))]
               (when (some? phone-number)
                 (assoc
                  (lazy-map {:phone-number/info (info phone-number nil locale-specification)})
                  :phone-number/number               phone-number
                  :phone-number.sample/samples       samples
                  :phone-number.sample/random-digits random-digits
                  :phone-number.sample/hits          valid-hits))))
           (when-not (zero? template-tries)
             ;; some combinations of type and region are not suited to produce a valid template
             ;; in such cases we have to retry if there is a chance to do that
             ;; (at least a region or a number type are random)
             (when (or (nil? region-code) (nil? number-type))
               (recur region-code number-type (unchecked-dec-int template-tries))))))))))

(defn generate-valid
  "Works the same as generate but returns only valid numbers."
  {:added "8.12.4-0" :tag lazy_map.core.LazyMap}
  ([]
   (generate nil nil valid? nil nil nil))
  ([^clojure.lang.Keyword region-code]
   (generate region-code nil valid? nil nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type]
   (generate region-code number-type valid? nil nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate]
   (generate region-code number-type
             (if (nil? predicate) valid? (every-pred valid? predicate))
             nil nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Number               retries]
   (generate region-code number-type
             (if (nil? predicate) valid? (every-pred valid? predicate))
             retries nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Number               retries
    ^Number               keep-digits]
   (generate region-code number-type
             (if (nil? predicate) valid? (every-pred valid? predicate))
             retries keep-digits nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Number               retries
    ^Number               keep-digits
    ^java.util.Locale     locale-specification]
   (generate region-code number-type
             (if (nil? predicate) valid? (every-pred valid? predicate))
             retries keep-digits locale-specification)))

(defn generate-invalid
  "Works the same as generate but returns only invalid numbers."
  {:added "8.12.4-0" :tag lazy_map.core.LazyMap}
  ([]
   (generate nil nil invalid? nil nil nil))
  ([^clojure.lang.Keyword region-code]
   (generate region-code nil invalid? nil nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type]
   (generate region-code number-type invalid? nil nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate]
   (generate region-code number-type
             (if (nil? predicate) invalid? (every-pred invalid? predicate))
             nil nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Number               retries]
   (generate region-code number-type
             (if (nil? predicate) invalid? (every-pred invalid? predicate))
             retries nil nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Number               retries
    ^Number               keep-digits]
   (generate region-code number-type
             (if (nil? predicate) invalid? (every-pred invalid? predicate))
             retries keep-digits nil))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Number               retries
    ^Number               keep-digits
    ^java.util.Locale     locale-specification]
   (generate region-code number-type
             (if (nil? predicate) invalid? (every-pred invalid? predicate))
             retries keep-digits locale-specification)))
