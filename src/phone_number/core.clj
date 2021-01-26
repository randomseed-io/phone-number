(ns

    ^{:doc    "Clojure interface to Libphonenumber."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.core

  (:refer-clojure :exclude [format type])

  (:require [phone-number                 :as             PN]
            [phone-number.util            :as           util]
            [phone-number.type            :as           type]
            [phone-number.match           :as          match]
            [phone-number.format          :as         format]
            [phone-number.tz-format       :as      tz-format]
            [phone-number.leniency        :as       leniency]
            [phone-number.locale          :as         locale]
            [phone-number.region          :as         region]
            [phone-number.cost            :as           cost]
            [phone-number.net-code        :as       net-code]
            [phone-number.country-code    :as   country-code]
            [phone-number.calling-code    :as   calling-code]
            [phone-number.short           :as          short]
            [phone-number.sample          :as         sample]
            [phone-number.dialing-region  :as dialing-region]
            [lazy-map.core                :refer        :all])

  (:import  [com.google.i18n.phonenumbers
             Phonenumber$PhoneNumber]))

;;
;; Settings
;;

(def ^{:added "8.12.4-0"
       :dynamic true
       :tag Boolean}
  *info-removed-nils*
  "Decides whether the results of the info function should contain properties having
  nil values. They are removed by default due to `true` value of this switch."
  true)

(def ^{:added "8.12.4-1"
       :dynamic true
       :tag Boolean}
  *info-dialing-region-derived*
  "Decides whether some of the results of the info function should be calculated using
  dialing region code derived from the given region code if the dialing region was
  not passed as an argument nor obtained from the `*default-dialing-region*` dynamic
  variable. Default is `true`."
  true)

(def ^{:added "8.12.4-1"
       :dynamic true
       :tag Boolean}
  *default-dialing-region*
  "Sets a default dialing region (from where calls are originating) for functions that
  explicitly require dialing region code to be present (usually operating on short
  numbers). Default is nil."
  nil)

(def ^{:added "8.12.4-0"
       :dynamic true
       :tag Boolean}
  *inferred-namespaces*
  "Decides whether keywords which are not fully-qualified should be automatically
  qualified (by attaching default namespaces) when passed as arguments to functions
  that operate on phone number types, phone number formats, region codes and time
  zone formats. Defaults to `true`."
  true)

;;
;; Constants
;;

(def ^{:added "8.12.4-0"
       :const true
       :tag clojure.lang.PersistentHashSet}
  none
  "A set containing values considered to be none, unknown or empty in the domain of
  processing phone numbers and codes."
  #{nil 0 false "" () {}
    :zero :nil :null
    :none :unknown      :etc/unknown
    ::type/unknown      ::type/none
    ::region/unknown    ::region/none
    ::format/unknown    ::format/none
    ::tz-format/unknown ::tz-format/none
    ::match/none
    "Etc/Unknown" "unknown" "none" "nil" "0" "ZZ"})

(def ^{:added "8.12.4-1" :tag clojure.lang.PersistentHashSet :const true}
  required-first-input-characters
  "A set of required first characters in a phone number which is a string."
  (util/char-ranges->set [\0 \9]))

(def ^{:added "8.12.4-1" :tag clojure.lang.PersistentHashSet :const true}
  allowed-removable-characters
  "A set of removable (like punctuation) characters in a phone number which is a
  string. Used during input validation."
  #{\space\tab\-\—\–\.\,\=\*\~\(\)\[\]\/})

(def ^{:added "8.12.4-1" :tag clojure.lang.PersistentHashSet :const true}
  allowed-input-characters
  "A set of allowed characters in a phone number which is a string
  (applied to all characters except the first 3 on a string cleaned up from removable
  characters)."
  (clojure.set/union
   (util/char-ranges->set [\0 \9] [\A \Z] [\a \z])))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  regions
  "A set of all possible phone number region codes."
  (set region/all-vec))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  net-codes
  "A set of all possible global network calling codes."
  net-code/all)

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  country-codes
  "A set of all possible country calling codes."
  country-code/all)

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  calling-codes
  "A set of all possible country calling codes."
  calling-code/all)

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  formats
  "A set of all possible phone number formats as a sequence of keywords."
  (set (keys format/all)))

(def ^{:added "8.12.4-3" :tag clojure.lang.PersistentHashSet}
  leniencies
  "A set of all possible phone number leniencies as a sequence of keywords."
  (set (keys leniency/all)))

(def ^{:added "8.12.4-3" :tag clojure.lang.PersistentHashSet}
  locales
  "A set of all possible phone number locales as a sequence of keywords."
  (set (keys locale/all)))

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

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentHashSet}
  costs
  "A set of all possible phone number cost classes as a sequence of keywords."
  (set cost/all-vec))

;;
;; Protocol
;;

(defprotocol ^{:added "8.12.4-0"} Phoneable
  "This protocol is used to utilize class-based single dispatch on a phone number
  abstract."

  (^{:added "8.12.4-0" :tag Boolean} valid-input?
   [phone-number]
   "Takes a phone number represented as a string, a number, a map or a `PhoneNumber`
    object and returns true if it is a valid input to be parsed (must be not empty
    and not nil and have some minimal number of digits). Otherwise it returns false.")

  (^{:added "8.12.4-0" :tag Phonenumber$PhoneNumber} number
   [phone-number] [phone-number region-code]
   "Takes a phone number represented as a string, a number, a map or a `PhoneNumber`
   object and returns parsed `PhoneNumber` object. Second, optional argument should
   be a keyword with region code which is helpful if a local number (without region
   code) was given. If the region code argument is passed and the first argument is
   already a kind of PhoneNumber then it will be ignored.

   It is important to realize that certain properties of so called short
   numbers (like an emergency numbers) can only be successfully calculated by other
   functions if the unprocessed form of a number (a string or a natural number) does
   not contain country code and so it is delivered as it would be dialed. It is
   advised to pass a region code as the second argument when short numbers are
   in use.")

  (^{:added "8.12.4-0" :tag Phonenumber$PhoneNumber} number-noraw
   [phone-number] [phone-number region-code]
   "Like number but does not preserve raw input.")

  (^{:added "8.12.4-0" :tag String} raw-input
   [phone-number] [phone-number region-code]
   "Returns a string used to initialize phone number object with the number
   function. For strings and numbers it short-circuits on the given argument and
   ignores any given region code. Returns a string representation. For nil values it
   returns nil.")

  (^{:added "8.12.4-0" :tag Boolean} valid?
   [phone-number] [phone-number region-code] [phone-number region-code dialing-region]
   "Takes a phone number represented as a string, a number, a map or a `PhoneNumber`
   object and validates it. Returns true or false.

   When 3 arguments are given the last one should be a source region code for which
   the test is performed. It only makes sense to use it when the calling code for a
   number is not the same as the dialing code for the region. If that argument is nil
   then a value stored in the dynamic variable `*default-dialing-region*` will be
   used. If this value is also nil then the function will fall back to checking a
   number without any dialing region.

   One special case is when validating an info map (the result of calling `info`
   function). When there will not be dialing region given (or its value will be
   `nil`) then this function will try to obtain the source region information from an
   entry stored under the key `:phone-number/dialing-region` (or possibly
   `:dialing-region` when namespace inference is enabled). It will fetch it only when
   the key `:phone-number.dialing-region/derived?` is not holding a truthy
   value. When this fail then it will default to `*default-dialing-region*`.

   This function will NOT make use of `*info-dialing-region-derived*`, hence the
   prefix `info-` (this variable is to control only the `info` function). If you need
   a dialing region code to be derived from the region code of a number, please parse
   a number and supply the result of the `region` function to `valid?` as its last
   argument."))

;;
;; Protocol helpers
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

(def ^{:added "8.12.4-1" :tag clojure.lang.LazySeq :private true}
  global-formats-simple
  (map (comp keyword name) format/global))

(def ^{:added "8.12.4-1" :tag clojure.lang.LazySeq :private true}
  regional-formats-simple
  (map (comp keyword name) format/regional))

(defn- fetch-from-info-map
  [^clojure.lang.Fn             fn-valid-arg?
   ^clojure.lang.Fn             fn-valid?
   plain-value
   ^clojure.lang.IPersistentMap info-map
   ^clojure.lang.Keyword        k
   default]
  (if (some? plain-value) plain-value
      (when-some [map-value (inf-get info-map k default)]
        (if (fn-valid-arg? map-value *inferred-namespaces*) map-value
            (when-not (fn-valid? map-value *inferred-namespaces*) map-value)))))

(defn- ^clojure.lang.Keyword prep-dialing-region
  ([^clojure.lang.Keyword        region-code]
   (if (nil? region-code) *default-dialing-region* region-code))
  ([^clojure.lang.Keyword        region-code
    ^phone_number.core.Phoneable phone-number]
   (if region-code region-code
       (if (and (map? phone-number)
                (not (inf-get phone-number ::dialing-region/derived? false)))
         (fetch-from-info-map region/valid-arg?
                              region/valid?
                              region-code
                              phone-number
                              ::PN/dialing-region
                              *default-dialing-region*)
         *default-dialing-region*))))

(declare region)
(declare format)
(declare native?)

(defn- first-nil?
  {:added "8.12.16-1" :tag Boolean}
  [e _]
  (nil? e))

(defn- phoneable-map-apply
  "Tries to apply the given function to a phone number obtained from a map using known
  keys."
  {:added "8.12.4-0"}
  ([] nil)
  ([^clojure.lang.IFn            f]
   (phoneable-map-apply f {} nil false))
  ([^clojure.lang.IFn            f
    ^clojure.lang.IPersistentMap m]
   (phoneable-map-apply f m nil false))
  ([^clojure.lang.IFn            f
    ^clojure.lang.IPersistentMap m
    ^clojure.lang.Keyword        region-code]
   (phoneable-map-apply f m region-code false))
  ([^clojure.lang.IFn            f
    ^clojure.lang.IPersistentMap m
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        dialing-region]
   (let [more        (when-not (false? dialing-region) (cons (prep-dialing-region dialing-region m) nil))
         region-code (fetch-from-info-map region/valid-arg? region/valid? region-code m ::PN/region nil)
         number-obj  (fetch-from-info-map native? first-nil? nil m ::PN/number nil)
         region-code (if (some? region-code) region-code (region number-obj nil))]
     ;; try phone number object
     (if (some? number-obj)
       (apply f number-obj region-code more)
       ;; try phone number info map
       (if (inf-contains? m ::PN/info)
         (apply phoneable-map-apply f (inf-get m ::PN/info) region-code more)
         ;; try raw input
         (if-some [t (inf-get m ::format/raw-input)]
           (apply f t region-code more)
           ;; try phone number formats without region code + region code we already have
           (if-some [t (when (some? region-code) (some m format/regional))]
             (apply f t region-code more)
             ;; retry with simplified format key
             (if-some [t (when (some? region-code) (when *inferred-namespaces* (some m regional-formats-simple)))]
               (apply f t region-code more)
               ;; try phone number formats containing region code information
               (if-some [t (some m format/global)]
                 (apply f t region-code more)
                 (if-some [t (when *inferred-namespaces* (some m global-formats-simple))]
                   (apply f t nil more)
                   ;; try phone number formats without any region code information
                   ;; obtain region from:
                   ;; - calling code number (:phone-number/calling-code)
                   ;; - region code passed as an argument (region-code) or taken from a map before
                   ;;
                   ;; [remember to validate dirty return values from info function]
                   ;;
                   (let [c (fetch-from-info-map calling-code/valid-arg?
                                                calling-code/valid?
                                                nil
                                                m
                                                ::PN/calling-code
                                                nil)
                         r (when (nil? c) region-code)]
                     (if (and (nil? c) (nil? r))
                       (apply f nil nil more)
                       (if-some [t (some m format/regional)]
                         (if (some? c) (apply f (str "+" c t) region-code more) (apply f t r more))
                         (if-some [t (when *inferred-namespaces* (some m regional-formats-simple))]
                           (if (some? c) (apply f (str "+" c t) region-code more) (apply f t r more))
                           (if (some? c) (apply f nil nil more) (apply f region-code r more))))))))))))))))

;;
;; Protocol implementation
;;

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
    ([phone-number] (not-empty (.getRawInput phone-number)))
    ([phone-number ^clojure.lang.Keyword region-code]
     (raw-input phone-number)))
  (valid?
    ([obj]
     (util/try-parse-or-false
      (when (some? obj)
        (.isValidNumber (util/instance) obj))))
    ([obj ^clojure.lang.Keyword region-code]
     (valid? obj))
    ([obj
      ^clojure.lang.Keyword region-code
      ^clojure.lang.Keyword dialing-region]
     (if-some [dialing-region (prep-dialing-region dialing-region)]
       (util/try-parse-or-false
        (when (some? obj)
          (.isValidNumberForRegion
           (util/instance)
           obj
           (region/parse dialing-region *inferred-namespaces*))))
       (valid? obj dialing-region))))

  String
  (valid-input?
    [phone-number]
    (and
     (> (count phone-number) 1)
     (= 2 (count (take 2 (filter required-first-input-characters phone-number))))))
  (number-noraw
    ([phone-number]
     (number-noraw phone-number nil))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (assert (valid-input? phone-number)
             "Phone number string should begin with at least 3 digits")
     (when (some? phone-number)
       (.parse
        (util/instance)
        phone-number
        (region/parse region-code *inferred-namespaces*)))))
  (number
    ([phone-number]
     (number phone-number nil))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (assert (valid-input? phone-number)
             "Phone number string should begin with at least 3 digits")
     (when (some? phone-number)
       (.parseAndKeepRawInput
        (util/instance)
        phone-number
        (region/parse region-code *inferred-namespaces*)))))
  (raw-input
    ([phone-number]
     (not-empty phone-number))
    ([phone-number ^clojure.lang.Keyword region-code]
     (not-empty phone-number)))
  (valid?
    ([obj](valid? obj nil))
    ([obj
      ^clojure.lang.Keyword region-code]
     (util/try-parse-or-false
      (when (valid-input? obj)
        (.isValidNumber
         (util/instance)
         (number-noraw obj region-code)))))
    ([obj
      ^clojure.lang.Keyword region-code
      ^clojure.lang.Keyword dialing-region]
     (if-some [dialing-region (prep-dialing-region dialing-region)]
       (util/try-parse-or-false
        (when (some? obj)
          (.isValidNumberForRegion
           (util/instance)
           (number-noraw obj region-code)
           (region/parse dialing-region *inferred-namespaces*))))
       (valid? obj region-code))))

  Number
  (valid-input?
    [phone-number]
    (and (nat-int? phone-number) (> phone-number 9)))
  (number-noraw
    ([phone-number]
     (assert false "Numeric phone number must have region code argument supplied"))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (assert (valid-input? phone-number)
             "Numeric phone number should be a positive natural number having at least 2 digits")
     (assert (region/valid? region-code *inferred-namespaces*) "Region code must be valid")
     (number-noraw (str phone-number) region-code)))
  (number
    ([phone-number]
     (assert false "Numeric phone number must have some region code argument supplied"))
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (assert (valid-input? phone-number)
             "Numeric phone number should be a positive natural number having at least 2 digits")
     (assert (region/valid? region-code *inferred-namespaces*) "Region code must be valid")
     (number (str phone-number) region-code)))
  (raw-input
    ([phone-number]
     (raw-input (str phone-number)))
    ([phone-number ^clojure.lang.Keyword region-code]
     (raw-input (str phone-number) region-code)))
  (valid?
    ([phone-number] false)
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (and (valid-input? phone-number)
          (valid? (str phone-number) region-code)))
    ([phone-number
      ^clojure.lang.Keyword region-code
      ^clojure.lang.Keyword dialing-region]
     (and (valid-input? phone-number)
          (valid? (str phone-number) region-code dialing-region))))

  clojure.lang.IPersistentMap
  (valid-input?
    [phone-number]
    (or (valid-input? (inf-get phone-number ::format/raw-input))
        (if (phoneable-map-apply (fn [p _] (valid-input? p))
                                 phone-number
                                 nil)
          true false)))
  (number-noraw
    ([phone-number]
     (phoneable-map-apply number-noraw phone-number nil))
    ([phone-number ^clojure.lang.Keyword region-code]
     (phoneable-map-apply number-noraw phone-number region-code)))
  (number
    ([phone-number]
     (phoneable-map-apply number phone-number nil))
    ([phone-number ^clojure.lang.Keyword region-code]
     (phoneable-map-apply number phone-number region-code)))
  (raw-input
    ([phone-number]
     (or (inf-get phone-number ::format/raw-input)
         (phoneable-map-apply raw-input phone-number nil)))
    ([phone-number ^clojure.lang.Keyword region-code]
     (or (inf-get phone-number ::format/raw-input)
         (phoneable-map-apply raw-input phone-number region-code))))
  (valid?
    ([phone-number]
     (phoneable-map-apply valid? phone-number nil nil))
    ([phone-number ^clojure.lang.Keyword region-code]
     (phoneable-map-apply valid? phone-number region-code nil))
    ([phone-number
      ^clojure.lang.Keyword region-code
      ^clojure.lang.Keyword dialing-region]
     (phoneable-map-apply valid? phone-number region-code dialing-region)))

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
    ([phone-number ^clojure.lang.Keyword region-code] false)
    ([phone-number
      ^clojure.lang.Keyword region-code
      ^clojure.lang.Keyword dialing-region] false))

  Object
  (valid-input?
    [phone-number] false)
  (number-noraw
    ([phone-number] phone-number)
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (assert false ("Unable to create phone number from "
                    (clojure.core/type phone-number)
                    " type object"))))
  (number
    ([phone-number] phone-number)
    ([phone-number
      ^clojure.lang.Keyword region-code]
     (assert false ("Unable to create phone number from "
                    (clojure.core/type phone-number)
                    " type object"))))
  (raw-input
    ([phone-number] phone-number)
    ([phone-number ^clojure.lang.Keyword region-code]
     (assert false ("Unable to create phone number from "
                    (clojure.core/type phone-number)
                    " type object"))))
  (valid?
    ([phone-number] false)
    ([phone-number
      ^clojure.lang.Keyword region-code]
     false)
    ([phone-number
      ^clojure.lang.Keyword region-code
      ^clojure.lang.Keyword dialing-region]
     false)))

;;
;; Other generic helpers
;;

(defmacro when-some-input
  [phone-num & body]
  `(when (some? ~phone-num)
     ~@body))

(defn- prep-raw-or-national+region
  {:added "8.12.4-2" :tag clojure.lang.PersistentVector}
  [^phone_number.core.Phoneable phone-number
   ^clojure.lang.Keyword         region-code]
  (let [region-code (if (nil? region-code) (region phone-number nil) region-code)]
    [(or (raw-input phone-number) (format phone-number region-code ::format/national))
     (region/parse region-code *inferred-namespaces*)]))

;;
;; Basic functions
;;

(defn native?
  "Returns true if the given argument is an instance of PhoneNumber class."
  {:added "8.12.4-0" :tag Boolean}
  ([phone-number _]
   (instance? Phonenumber$PhoneNumber phone-number))
  ([phone-number]
   (instance? Phonenumber$PhoneNumber phone-number)))

(def ^{:added "8.12.4-0" :tag Boolean
       :arglists '([^phone_number.core.Phoneable phone-number]
                   [^phone_number.core.Phoneable phone-number
                    ^clojure.lang.Keyword        region-code])}
  invalid?
  "Returns true if the given phone number (expressed as a string, a number,
  a map or a `PhoneNumber` object) is not valid."
  (complement valid?))

(defn possible?
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns true if it is a possible number as defined by Libphonenumber.
  Otherwise it returns false.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (possible? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword         region-code]
   (util/try-parse-or-false
    (when-some-input phone-number
      (.isPossibleNumber
       (util/instance)
       (number-noraw phone-number region-code))))))

(def ^{:added "8.12.4-0" :tag Boolean
       :arglists '([^phone_number.core.Phoneable phone-number]
                   [^phone_number.core.Phoneable phone-number
                    ^clojure.lang.Keyword        region-code])}
  impossible?
  "Returns true if the given phone number (expressed as a string, a number, a map or a
  `PhoneNumber` object) is not possible."
  (complement possible?))

(defn valid-for-region?
  "DEPRECATED: Please use ternary version of the `valid?` function."
  {:added "8.12.4-0" :deprecated "8.12.4-1" :tag Boolean}
  [^phone_number.core.Phoneable  phone-number
   ^clojure.lang.Keyword          region-code
   ^clojure.lang.Keyword       dialing-region]
  (valid? phone-number region-code dialing-region))

(defn has-raw-input?
  "Checks whether raw input string can be obtained from the given phone number."
  {:added "8.12.4-1" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-raw-input? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword         region-code]
   (util/try-parse-or-false
    (when-some-input phone-number
      (when-some [r (raw-input phone-number region-code)]
        (string? r))))))

;;
;; Formatting
;;

(defn format
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns it as a formatted string. The last argument should be a format
  expressed as a keyword (use the all-formats function to list them) or a
  PhoneNumberType.

  If the second argument is present (and there are 3 arguments) then it should be a
  valid region code (a keyword) to be used when the given phone number does not
  contain region information.

  If there are 2 arguments and the second argument is a valid format specification it
  will be used without setting the region. If the format specification doesn't look
  like a valid format then it will be assumed it is a region code and format will be
  set to a default."
  {:added "8.12.4-0" :tag String
   :arglists '([^phone_number.core.Phoneable phone-number]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        format-specification]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code
                ^clojure.lang.Keyword        format-specification])}
  ([^phone_number.core.Phoneable phone-number]
   (format phone-number nil format/default))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code-or-format-spec]
   (if (nil? region-code-or-format-spec)
     (format phone-number nil format/default)
     (if (format/valid? region-code-or-format-spec *inferred-namespaces*)
       (format phone-number nil region-code-or-format-spec)               ;; region code
       (format phone-number region-code-or-format-spec format/default)))) ;; format spec
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        format-specification]
   (let [f (format/parse format-specification *inferred-namespaces*)]
     (when-some-input phone-number
       (not-empty
        (when-some [p (number-noraw phone-number region-code)]
          (if (= :raw f)
            (raw-input p)
            (.format (util/instance) p f))))))))

(defn all-formats
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns a map which keys are all possible formats expressed as keywords
  and values are string representations of the number formatted accordingly.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap}
  ([^phone_number.core.Phoneable   phone-number]
   (all-formats phone-number nil))
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          region-code]
   (when-some-input phone-number
     (when-some [p (number phone-number region-code)]
       (util/fmap-k #(format p nil %) format/all)))))

(defn- all-formats-into
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns a map which keys are all possible formats expressed as keywords
  and values are string representations of the number formatted accordingly.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-1" :tag clojure.lang.PersistentArrayMap}
  [^phone_number.core.Phoneable    phone-number
   ^clojure.lang.Keyword           region-code
   ^clojure.lang.PersistentHashMap dst]
  (let [p (number phone-number region-code)]
    (util/fmap-k #(format p nil %) format/all dst)))

;;
;; Number type
;;

(defn type
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns its type as a keyword.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^phone_number.core.Phoneable phone-number]
   (type phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (when-some-input phone-number
     (type/by-val
      (.getNumberType
       (util/instance)
       (number-noraw phone-number region-code))
      ::type/unknown))))

;;
;; Country and region
;;

(defn calling-code
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns its calling code as an integer number.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag Integer}
  ([^phone_number.core.Phoneable    phone-number]
   (calling-code phone-number nil))
  ([^phone_number.core.Phoneable    phone-number
    ^clojure.lang.Keyword           region-code]
   (when-some-input phone-number
     (.getCountryCode
      (number-noraw phone-number region-code)))))

(defn region
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns its region code as a string or nil if the region happens to be
  empty.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-0" :tag String}
  ([^phone_number.core.Phoneable  phone-number]
   (region phone-number nil))
  ([^phone_number.core.Phoneable  phone-number
    ^clojure.lang.Keyword         region-code]
   (region/by-val
    (not-empty
     (when-some-input phone-number
       (.getRegionCodeForNumber
        (util/instance)
        (number-noraw phone-number region-code)))))))

;;
;; Numeric representation
;;

(defn numeric
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns its regional part as an integer, positive number of type Long.

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information."
  {:added "8.12.4-1" :tag Long}
  ([^phone_number.core.Phoneable phone-number]
   (numeric phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (when-some [phone-obj (number-noraw phone-number region-code)]
     (when-some [as-string (format phone-obj nil ::format/e164)]
       (let [ccode (calling-code phone-obj nil)
             digits (if (or (nil? ccode) (<= ccode 0)) 1 (unchecked-inc (util/count-digits ccode)))]
         (when-some [regional-number (not-empty (subs as-string digits))]
           (Long/valueOf regional-number)))))))

;;
;; Location
;;

(defn location
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns its possible geographic location as a string or nil if the
  location happens to be empty.

  If the second argument is present then it may be a valid region code (a keyword) to
  be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no explicit
  region information and it should extract it from a number.

  If the third argument is present then it should be a string specifying locale
  information or a `java.util.Locale` object. It will be used during rendering
  strings describing geographic location and carrier data. When nil is passed then
  the default locale settings will be used.

  If there are 2 arguments and the second argument is a keyword but IS NOT a
  **fully-qualified, valid locale specification** (`locale-specification-FQ` having
  namespace set to `phone-number.locale`) then it will be treated as a **region
  code**. Using namespaced keyword for a locale (or using object other than keyword)
  is required to avoid ambiguity since simple region codes and locale specs can be
  expressed using the very same keyword names. Optionally (but a bad habit) you may
  use simple keyword with locale and variant (e.g. `:pl_PL`) that will not match any
  region but match locale object."
  {:added "8.12.4-0" :tag String
   :arglists '([^phone_number.core.Phoneable phone-number]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        locale-specification-FQ]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code
                ^clojure.lang.Keyword        locale-specification])}
  ([^phone_number.core.Phoneable    phone-number]
   (location phone-number nil nil))
  ([^phone_number.core.Phoneable    phone-number
    ^clojure.lang.Keyword           region-code-or-locale-spec]
   (if (nil? region-code-or-locale-spec)
     (location phone-number nil nil)
     (if (region/valid? region-code-or-locale-spec *inferred-namespaces*)
       (location phone-number region-code-or-locale-spec nil)     ;; region code
       (location phone-number nil region-code-or-locale-spec))))  ;; locale spec
  ([^phone_number.core.Phoneable    phone-number
    ^clojure.lang.Keyword           region-code
    ^clojure.lang.Keyword  locale-specification]
   (when-some-input phone-number
     (not-empty
      (.getDescriptionForNumber
       (util/geo-coder)
       (number-noraw phone-number region-code)
       (locale/parse locale-specification *inferred-namespaces*))))))

(defn geographical?
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns true if it is a geographical number as defined by
  Libphonenumber. Otherwise it returns false. If the second argument is present then
  it should be a valid region code (a keyword) to be used when the given phone number
  does not contain region information."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable   phone-number]
   (geographical? phone-number nil))
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          region-code]
   (util/try-parse-or-false
    (when-some-input phone-number
      (.isNumberGeographical
       (util/instance)
       (number-noraw phone-number region-code))))))

;;
;; Carrier
;;

(defn carrier
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns its possible carrier name as a string or nil if the carrier
  name happens to be empty.

  If the second argument is present then it may be a valid region code (a keyword) to
  be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no explicit
  region information and it should extract it from a number.

  If the third argument is present then it should be a string specifying locale
  information or a `java.util.Locale` object. It will be used during rendering carrier
  name. When nil is passed then the default locale settings will be used.

  If there are 2 arguments and the second argument is a keyword but IS NOT a
  **fully-qualified, valid locale specification** (`locale-specification-FQ` having
  namespace set to `phone-number.locale`) then it will be treated as a **region
  code**. Using namespaced keyword for a locale (or using object other than keyword)
  is required to avoid ambiguity since simple region codes and locale specs can be
  expressed using the very same keyword names. Optionally (but a bad habit) you may
  use simple keyword with locale and variant (e.g. `:pl_PL`) that will not match any
  region but match locale object."
  {:added "8.12.4-0" :tag String
   :arglists '([^phone_number.core.Phoneable phone-number]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        locale-specification-FQ]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code
                ^clojure.lang.Keyword        locale-specification])}
  ([^phone_number.core.Phoneable   phone-number]
   (carrier phone-number nil nil))
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          region-code-or-locale-spec]
   (if (nil? region-code-or-locale-spec)
     (carrier phone-number nil nil)
     (if (region/valid? region-code-or-locale-spec *inferred-namespaces*)
       (carrier phone-number region-code-or-locale-spec nil)    ;; region code
       (carrier phone-number nil region-code-or-locale-spec)))) ;; locale spec
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          region-code
    ^clojure.lang.Keyword locale-specification]
   (when-some-input phone-number
     (not-empty
      (.getNameForNumber
       (util/carrier-mapper)
       (number-noraw phone-number region-code)
       (locale/parse locale-specification *inferred-namespaces*))))))

;;
;; Time zones
;;

(defn- time-zones-core
  {:added "8.12.4-3" :tag clojure.lang.LazySeq}
  [^phone_number.core.Phoneable  phone-number
   ^clojure.lang.Keyword         region-code]
  (when-some-input phone-number
    (->> region-code
         (number-noraw phone-number)
         (.getTimeZonesForNumber (util/time-zones-mapper))
         util/lazy-iterator-seq
         (remove none)
         distinct
         not-empty)))

(defn time-zones
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns all possible time zones which relate to its geographical
  location as a lazy sequence of strings (representing zone identifiers in
  English). Returns nil if the list would be empty.

  If the second argument is present then it may be a valid region code (a keyword) to
  be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no explicit
  region information and it should extract it from a number. It may also be a format
  specifier (also a keyword).

  The third argument may be a format specification or a locale specification (both as
  keywords).

  If there are 4 arguments then the format specification should be the last and
  locale specification should be last but one."
  {:added "8.12.4-0" :tag clojure.lang.LazySeq
   :arglists '([^phone_number.core.Phoneable phone-number]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        format-specification]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code
                ^clojure.lang.Keyword        format-specification]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code
                ^clojure.lang.Keyword        locale-specification]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code
                ^clojure.lang.Keyword        locale-specification
                ^clojure.lang.Keyword        format-specification])}
  ([^phone_number.core.Phoneable  phone-number]
   (time-zones phone-number nil))
  ([^phone_number.core.Phoneable  phone-number
    ^clojure.lang.Keyword         region-code-or-format-spec]
   (if (nil? region-code-or-format-spec)
     (time-zones phone-number nil nil nil)
     (if (tz-format/valid? region-code-or-format-spec *inferred-namespaces*)
       (time-zones phone-number nil nil region-code-or-format-spec)    ;; format spec
       (time-zones phone-number region-code-or-format-spec nil nil)))) ;; region code
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        format-spec-or-locale-spec]
   (if (nil? format-spec-or-locale-spec)
     (time-zones phone-number region-code nil nil)
     (if (tz-format/valid? format-spec-or-locale-spec *inferred-namespaces*)
       (time-zones phone-number region-code nil format-spec-or-locale-spec)    ;; format spec
       (time-zones phone-number region-code format-spec-or-locale-spec nil)))) ;; region code
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        locale-specification
    ^clojure.lang.Keyword        format-specification]
   (let [l (locale/parse locale-specification *inferred-namespaces*)
         f (tz-format/parse format-specification *inferred-namespaces*)]
     (->> (time-zones-core phone-number region-code)
          (map #(tz-format/transform % l f))
          distinct
          not-empty))))

(defn time-zones-all-formats
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns a map which keys are all possible time zone formats expressed
  as keywords and values are sequences of the number's time zones formatted
  accordingly. If the given number is nil, invalid or time zone information is
  unavailable for it this function returns nil.

  If the second argument is present then it may be a valid region code (a keyword) to
  be used when the given phone number does not contain region information. It is
  possible to pass a nil value as this argument to ignore extra processing when
  region can be inferred from the number.

  If there are 2 arguments and the second argument is a keyword but IS NOT a
  **fully-qualified, valid locale specification** (`locale-specification-FQ` having
  namespace set to `phone-number.locale`) then it will be treated as a **region
  code**. Using namespaced keyword for a locale (or using object other than keyword)
  is required to avoid ambiguity since simple region codes and locale specs can be
  expressed using the very same keyword names. Optionally (but a bad habit) you may
  use simple keyword with locale and variant (e.g. `:pl_PL`) that will not match any
  region but match locale object.

  The third argument should be a Locale object or a string describing locale settings
  to be used when rendering locale-dependent time zone information. When there is no
  third argument or it is nil then default locale settings will be used."
  {:added "8.12.4-0" :tag clojure.lang.PersistentArrayMap
   :arglists '([^phone_number.core.Phoneable phone-number]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        locale-specification-FQ]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code
                ^clojure.lang.Keyword        locale-specification])}
  ([^phone_number.core.Phoneable phone-number]
   (time-zones-all-formats phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code-or-locale-spec]
   (if (nil? region-code-or-locale-spec)
     (time-zones-all-formats phone-number nil nil)
     (if (region/valid? region-code-or-locale-spec *inferred-namespaces*)
       (time-zones-all-formats phone-number region-code-or-locale-spec nil)    ;; region-code
       (time-zones-all-formats phone-number nil region-code-or-locale-spec)))) ;; locale spec
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        locale-specification]
   (when-some-input phone-number
     (let [p (number-noraw phone-number region-code)]
       (when (some? (time-zones p nil ::tz-format/id))
         (let [l (locale/parse locale-specification *inferred-namespaces*)]
           (util/fmap-k #(time-zones p nil l %) tz-format/all)))))))

;;
;; Short number specific
;;

(defn short-possible?
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns true if it is a possible short number (like emergency etc.) as
  defined by Libphonenumber. Otherwise it returns false. If the second argument is
  present then it should be a valid region code (a keyword) to be used when the given
  phone number does not contain region information.

  If the `default-dialing-region*` dynamic variable is set then it will be used as
  a default dialing region if it is not passed as an argument.

  In its ternary form this function takes an additional argument (dialing-region)
  that should be a valid region code for the origination of a possible call. That
  hint will be used to restrict the check according to rules. For example 112 may be
  valid in multiple regions but if one calls it from some particular region it might
  not be reachable. When this argument is missing or its value is nil and the dynamic
  variable `*default-dialing-region*` is not nil then its value will be used to set
  the dialing region. If this argument is missing or is nil and there is no default
  the binary variant of this function is called (without a source region)."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (short-possible? phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (short-possible? phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        dialing-region]
   (util/try-parse-or-false
    (when-some-input phone-number
      (let [dialing-region (prep-dialing-region dialing-region phone-number)]
        (if (nil? dialing-region)
          (.isPossibleShortNumber
           (util/short)
           (number-noraw phone-number region-code))
          (.isPossibleShortNumberForRegion
           (util/short)
           (number-noraw phone-number region-code)
           (region/parse dialing-region *inferred-namespaces*))))))))

(defn short-valid?
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns true if it is a valid short number (like emergency etc.) as
  defined by Libphonenumber. Otherwise it returns false. If the second argument is
  present then it should be a valid region code (a keyword) to be used when the given
  phone number does not contain region information.

  If the `default-dialing-region*` dynamic variable is set then it will be used as
  a default dialing region if it is not passed as an argument.

  In its ternary form this function takes an additional argument (dialing-region)
  that should be a valid region code for the origination of a possible call. That
  hint will be used to restrict the check according to rules. For example 112 may be
  valid in multiple regions but if one calls it from some particular region it might
  not be reachable. When this argument is missing or its value is nil and the dynamic
  variable `*default-dialing-region*` is not nil then its value will be used to set
  the dialing region. If this argument is missing or is nil and there is no default
  the binary variant of this function is called (without a source region)."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (short-valid? phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (short-valid? phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        dialing-region]
   (util/try-parse-or-false
    (when-some-input phone-number
      (if-some [dialing-region (prep-dialing-region dialing-region phone-number)]
        (.isValidShortNumberForRegion
         (util/short)
         (number-noraw phone-number region-code)
         (region/parse dialing-region *inferred-namespaces*))
        (.isValidShortNumber
         (util/short)
         (number-noraw phone-number region-code)))))))

(defn short-cost
  "Takes a short (like an emergency) phone number (expressed as a string, a number, a
  map or a `PhoneNumber` object) and returns the expected cost class of that number
  as a keyword.

  The second, optional argument should be a valid region code (a keyword) to be used
  when the given phone number does not contain region information. It is acceptable
  to pass nil as a value to tell the function that there is no explicit region
  information and it should extract it from a number.

  If the `default-dialing-region*` dynamic variable is set then it will be used as
  a default dialing region if it is not passed as an argument.

  If the third argument is present then it should be a valid region code for the
  origination of a possible call. That hint will be used to restrict the check
  according to rules. For example 112 may be valid in multiple regions but if one
  calls it from some particular region it might not be reachable. When this argument
  is missing or its value is nil and the dynamic variable `*default-dialing-region*`
  is not nil then its value will be used to set the dialing region. If this argument
  is missing or is nil and there is no default the binary variant of this function is
  called (without a source region).

  It is important to realize that certain properties of short numbers can only be
  successfully calculated if the unprocessed form of a number (a string or a natural
  number) does not contain country code and so it is delivered as it would be
  dialed. It is advised to pass a region code as the second argument when short
  numbers are tested. It is also advised to submit a dialing region code when more
  precise checking should be performed."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^phone_number.core.Phoneable phone-number]
   (short-cost phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (short-cost phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        dialing-region]
   (when-some-input phone-number
     (cost/by-val
      (if-some [dialing-region (prep-dialing-region dialing-region phone-number)]
        (.getExpectedCostForRegion
         (util/short)
         (number-noraw phone-number region-code)
         (region/parse dialing-region *inferred-namespaces*))
        (.getExpectedCost
         (util/short)
         (number-noraw phone-number region-code)))))))

(defn short-emergency?
  "Takes a short (like an emergency) phone number (expressed as a string!) and returns
  true if it is exactly the emergency number. The second argument should be a valid
  region code (a keyword).

  It is important to realize that certain properties of short numbers can only be
  successfully calculated if the unprocessed form of a number (a string or a natural
  number) does not contain country code and so it is delivered as it would be
  dialed. It is advised to pass a region code as the second argument when short
  numbers are tested."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (short-emergency? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword         region-code]
   (util/try-parse-or-false
    (when-some-input phone-number
      (let [[^String phone-string
             ^String region-string] (prep-raw-or-national+region
                                     phone-number
                                     region-code)]
        (.isEmergencyNumber
         (util/short)
         phone-string
         region-string))))))

(defn short-to-emergency?
  "Takes a short (like an emergency) phone number (expressed as a string!) and returns
  true if it can be used to connect to emergency services. The second argument should
  be a valid region code (a keyword).

  It is important to realize that certain properties of short numbers can only be
  successfully calculated if the unprocessed form of a number (a string or a natural
  number) does not contain country code and so it is delivered as it would be
  dialed. It is advised to pass a region code as the second argument when short
  numbers are tested."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (short-to-emergency? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword         region-code]
   (util/try-parse-or-false
    (when-some-input phone-number
      (let [[^String phone-string
             ^String region-string] (prep-raw-or-national+region
                                     phone-number
                                     region-code)]
        (.connectsToEmergencyNumber
         (util/short)
         phone-string
         region-string))))))

(defn short-carrier-specific?
  "Takes a short phone number (expressed as a string, a number, a map or a
  `PhoneNumber` object), optional region code (or nil) and optional dialing region
  code. Returns true if it is a carrier-specific number.

  If the `default-dialing-region*` dynamic variable is set then it will be used as
  a default dialing region if it is not passed as an argument.

  It is important to realize that certain properties of short numbers can only be
  successfully calculated if the unprocessed form of a number (a string or a natural
  number) does not contain country code and so it is delivered as it would be
  dialed. It is advised to pass a region code as the second argument when short
  numbers are tested."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (short-carrier-specific? phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (short-carrier-specific? phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        dialing-region]
   (util/try-parse-or-false
    (when-some-input phone-number
      (if-some [dialing-region (prep-dialing-region dialing-region phone-number)]
        (.isCarrierSpecificForRegion
         (util/short)
         (number-noraw phone-number region-code)
         (region/parse dialing-region *inferred-namespaces*))
        (.isCarrierSpecific
         (util/short)
         (number-noraw phone-number region-code)))))))

(defn short-sms-service?
  "Takes a short phone number (expressed as a string, a number, a map or a
  `PhoneNumber` object), optional region code (or nil) and a dialing region
  code (uses `*default-dialing-code*` if not given). Returns true if SMS is
  supported, false otherwise.

  If the `default-dialing-region*` dynamic variable is set then it will be used as
  a default dialing region if it is not passed as an argument.

  It is important to realize that certain properties of short numbers can only be
  successfully calculated if the unprocessed form of a number (a string or a natural
  number) does not contain country code and so it is delivered as it would be
  dialed. It is advised to pass a region code as the second argument when short
  numbers are tested."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (short-sms-service? phone-number nil nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (short-sms-service? phone-number region-code nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        dialing-region]
   (let [dialing-region (prep-dialing-region dialing-region phone-number)]
     (assert (region/valid? dialing-region *inferred-namespaces*)
             "Dialing region code must be valid and not nil")
     (util/try-parse-or-false
      (when-some-input phone-number
        (.isSmsServiceForRegion
         (util/short)
         (number-noraw phone-number region-code)
         (region/parse dialing-region *inferred-namespaces*)))))))

;;
;; Generic reporting
;;

(defn- ^clojure.lang.PersistentHashMap info-remove-nils
  [^clojure.lang.PersistentHashMap m]
  (if *info-removed-nils*
    (util/remove-empty-vals m)
    m))

(defn- ^clojure.lang.Keyword calc-dialing-region
  [^clojure.lang.Keyword        region-code
   ^clojure.lang.Keyword        dialing-region
   ^phone_number.core.Phoneable phone-number]
  (if dialing-region
    [dialing-region false false]
    (if (and (map? phone-number)
             (inf-contains? phone-number ::PN/dialing-region)
             (not (inf-get  phone-number ::dialing-region/derived?   false))
             (not (inf-get  phone-number ::dialing-region/defaulted? false)))
      ;; we have a map and it has dialing region
      ;; that is not derived nor default
      [(inf-get phone-number ::PN/dialing-region *default-dialing-region*) false false]
      (if (some? *default-dialing-region*)
        [*default-dialing-region* false true]
        (if (and (false? dialing-region) (some? region-code))
          [region-code true false]
          (if (map? phone-number)
            [(inf-get phone-number ::PN/dialing-region)
             (inf-get phone-number ::dialing-region/derived?)
             (inf-get phone-number ::dialing-region/refaulted?)]
            [nil nil nil]))))))

(defn- short-info-core
  "Internal short-info logic. Supports additional argument which should be string
  version of a phone number and a destination map to avoid merging."
  {:added "8.12.4-1" :tag clojure.lang.PersistentHashMap}
  [^phone_number.core.Phoneable    phone-number
   ^clojure.lang.Keyword           region-code
   ^clojure.lang.Keyword           dialing-region
   ^phone_number.core.Phoneable    phone-orig
   ^clojure.lang.PersistentHashMap dst]
  (when-some-input phone-number
    (let [phone-obj         (number-noraw phone-number region-code)
          region-from-obj   (region phone-obj nil)
          region-code       (if (nil? region-from-obj) region-code region-from-obj)
          [dialing-region
           dialing-derived
           dialing-default] (calc-dialing-region region-code dialing-region (if (nil? phone-orig) phone-number phone-orig))
          region-code       (if (and (nil? region-code) (some? dialing-region)) dialing-region region-code)
          sh-possible       (short-possible? phone-obj nil dialing-region)
          sh-valid          (short-valid?    phone-obj nil dialing-region)
          have-from         (some? dialing-region)
          build-map-fn      (if (nil? dst) hash-map (partial assoc dst))]
      (if-not (or sh-valid sh-possible)
        (build-map-fn ::short/possible? false
                      ::short/valid?    false)
        (let [phone-string   (if (string? phone-orig) phone-orig (raw-input phone-orig))
              phone-string   (if (and (nil? phone-string) (string? phone-number)) phone-number phone-string)
              phone-string   (if (valid-input? phone-string) phone-string nil)
              phone-number   (if (nil? phone-string) (raw-input phone-number region-code) phone-string)
              phone-number   (if (nil? phone-number) (format phone-obj nil ::format/national) phone-number) ;; fallback
              dialing-region (util/ns-infer "phone-number.region" dialing-region *inferred-namespaces*)
              add-dialing-fn (if (some? dst) identity
                                 #(assoc
                                   %
                                   ::PN/dialing-region         dialing-region
                                   ::dialing-region/derived?   dialing-derived
                                   ::dialing-region/defaulted? dialing-default))
              remove-nils-fn (if (nil? dst) info-remove-nils identity)]
          (-> (build-map-fn
               ::short/valid?            sh-valid
               ::short/possible?         sh-possible
               ::short/carrier-specific? (short-carrier-specific? phone-obj nil dialing-region)
               ::short/cost              (short-cost              phone-obj nil dialing-region)
               ::short/emergency?        (short-emergency?        phone-number region-code)
               ::short/to-emergency?     (short-to-emergency?     phone-number region-code)
               ::short/sms-service?      (and have-from (short-sms-service? phone-obj nil dialing-region)))
              add-dialing-fn
              remove-nils-fn))))))

(defn short-info
  "Takes a short (like an emergency) phone number (expressed as a string, a number, a
  map or a `PhoneNumber` object) and returns a map containing all possible information
  about the number with keywords as keys.

  Required keys:

       :phone-number.short/valid?
       :phone-number.short/possible?

  Optional keys:

       :phone-number.short/carrier-specific?
       :phone-number.short/cost
       :phone-number.short/emergency?
       :phone-number.short/sms-service?
       :phone-number.short/to-emergency?
       :phone-number/dialing-region
       :phone-number.dialing-region/derived?
       :phone-number.dialing-region/defaulted?

  If the second argument is present then it should be a valid region code (a keyword)
  to be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no explicit
  region information and it should extract it from a number.

  If the third argument is present then it should be a valid region code for the
  origination of a possible call. That hint will be used to restrict the check
  according to rules. For example 112 may be valid in multiple regions but if one
  calls it from some particular region it might not be reachable. When this argument
  is missing or its value is nil and the dynamic variable `*default-dialing-region*`
  is not nil then its value will be used to set the dialing region. If the dynamic
  variable is also nil (which is the default) then the dynamic variable
  `*info-dialing-region-derived*` is checked to be set to truthy value (not nil and
  not `false`). If that is true then the dialing region will be derived from a region
  code of the number.

  It is important to realize that certain properties of short numbers can only be
  successfully calculated if the unprocessed form of a number (a string or a natural
  number) does not contain country code and so it is delivered as it would be
  dialed. It is advised to pass a region code as the second argument when short
  numbers are tested. It is also advised to submit a dialing region code when more
  precise checking is required."
  {:added "8.12.4-0" :tag clojure.lang.PersistentHashMap}
  ([^phone_number.core.Phoneable    phone-number]
   (short-info-core phone-number nil false nil nil))
  ([^phone_number.core.Phoneable    phone-number
    ^clojure.lang.Keyword           region-code]
   (short-info-core phone-number region-code false nil nil))
  ([^phone_number.core.Phoneable    phone-number
    ^clojure.lang.Keyword           region-code
    ^clojure.lang.Keyword           dialing-region]
   (short-info-core phone-number region-code dialing-region nil nil)))

(defn info
  "Takes a phone number (expressed as a string, a number, a map or a `PhoneNumber`
  object) and returns a map containing all possible information about the number with
  keywords as keys.

  Required keys:

      :phone-number/calling-code
      :phone-number/geographical?
      :phone-number/possible?
      :phone-number/valid?
      :phone-number/type
      :phone-number.short/valid?
      :phone-number.short/possible?

  Optional keys:

      :phone-number/region
      :phone-number/location
      :phone-number/carrier
      :phone-number/dialing-region
      :phone-number.dialing-region/derived?
      :phone-number.dialing-region/defaulted?
      :phone-number.dialing-region/valid-for?
      :phone-number.format/e164
      :phone-number.format/international
      :phone-number.format/national
      :phone-number.format/rfc3966
      :phone-number.format/raw-input
      :phone-number.tz-format/narrow-global
      :phone-number.tz-format/full
      :phone-number.tz-format/short
      :phone-number.tz-format/narrow
      :phone-number.tz-format/full-global
      :phone-number.tz-format/short-global
      :phone-number.tz-format/id
      :phone-number.short/carrier-specific?
      :phone-number.short/cost
      :phone-number.short/emergency?
      :phone-number.short/sms-service?
      :phone-number.short/to-emergency?

  Keys with nil values assigned will be removed from the map unless the dynamic
  variable `*info-removed-nils*` is bound to false.

  If the second argument is present then it may be a valid region code (a keyword) to
  be used when the given phone number does not contain region information. It is
  acceptable to pass nil as a value to tell the function that there is no explicit
  region information and it should extract it from a number.

  If the second argument is a locale specification (`locale-specification-FQ`) then
  it should be a **fully-qualified keyword** using `phone-number.locale`
  namespace (or an **object that is not a keyword**) in order to distinguish it from
  a region code (which is favored as this argument). Optionally (but a bad habit) you
  may use simple keyword with locale and variant (e.g. `:pl_PL`) that will not match
  any region but match locale object.

  If the third argument is present then it may be a string specifying locale
  information or a Locale object. It will be used during rendering strings describing
  geographic location, carrier data and full time zone information. When nil is
  passed then default locale settings will be used.

  If the third argument is a dialing region code (`dialing-region-FQ`) then it should
  be a **fully-qualified keyword** (using `phone-number.region` namespace) in order
  to distinguish it from a locale specification (which is favored as this argument).

  If there are four arguments then the last one should be a dialing region code
  intended to be used with short numbers (like 112 etc.). It describes originating
  region to help validate the possibility of reaching the destination number. When
  this argument is missing or its value is nil and the dynamic variable
  `*default-dialing-region*` is not nil then its value will be used to set the
  dialing region. If the dynamic variable is also nil (which is the default) then the
  dynamic variable `*info-dialing-region-derived*` is checked to be set to truthy
  value (not nil and not `false`). If that is true then the dialing region will be
  derived from a region code of the number.

  The `:phone-number/valid?` key holds the return value of `valid?` function call
  without any dialing region applied (even if it is passed as an argument or
  extracted from a map given as input to the `info` function). There is also the
  `:phone-number.dialing-region/valid-for?` key which holds the validity information
  taking dialing region (passed, extracted or default) into account.

  It is important to realize that certain properties of short numbers can only be
  successfully calculated if the unprocessed form of a number (a string or a natural
  number) does not contain country code and so it is delivered as it would be
  dialed. It is advised to pass a region code as the second argument when short
  numbers are analysed. It is also advised to submit a dialing region code when more
  precise analysis is required."
  {:added "8.12.4-0" :tag clojure.lang.PersistentHashMap
   :arglists '([^phone_number.core.Phoneable phone-number]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        locale-specification-FQ]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code
                ^clojure.lang.Keyword        locale-specification]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code
                ^clojure.lang.Keyword        dialing-region-FQ]
               [^phone_number.core.Phoneable phone-number
                ^clojure.lang.Keyword        region-code
                ^clojure.lang.Keyword        locale-specification
                ^clojure.lang.Keyword        dialing-region])}
  ([^phone_number.core.Phoneable phone-number]
   (info phone-number nil nil false))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code-or-locale-spec]
   (if (nil? region-code-or-locale-spec)
     (info phone-number nil nil false)
     (if (region/valid? region-code-or-locale-spec *inferred-namespaces*)
       (info phone-number region-code-or-locale-spec nil false)               ;; region code
       (info phone-number nil region-code-or-locale-spec false))))            ;; locale spec
  ([^phone_number.core.Phoneable   phone-number
    ^clojure.lang.Keyword          region-code
    ^clojure.lang.Keyword          locale-spec-or-dialing-region]
   (if (nil? locale-spec-or-dialing-region)
     (info phone-number region-code nil false)
     (if (region/valid? locale-spec-or-dialing-region false)
       (info phone-number region-code nil locale-spec-or-dialing-region)       ;; dialing region code
       (info phone-number region-code locale-spec-or-dialing-region false))))  ;; locale spec (favored)
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^clojure.lang.Keyword        locale-specification
    ^clojure.lang.Keyword        dialing-region]
   (when-some-input phone-number
     (let [locale            (locale/parse locale-specification *inferred-namespaces*)
           phone-obj         (number phone-number region-code)
           region-from-obj   (region phone-obj nil)
           region-code       (if (nil? region-from-obj) region-code region-from-obj)
           [dialing-region
            dialing-derived
            dialing-default] (calc-dialing-region region-code dialing-region phone-number)
           dialing-region    (util/ns-infer "phone-number.region" dialing-region *inferred-namespaces*)]
       (->> #::PN
            {:region                      region-code
             :dialing-region              dialing-region
             :valid?                      (valid?        phone-obj nil)
             :possible?                   (possible?     phone-obj nil)
             :geographical?               (geographical? phone-obj nil)
             :type                        (type          phone-obj nil)
             :calling-code                (calling-code  phone-obj nil)
             :location                    (location      phone-obj nil locale)
             :carrier                     (carrier       phone-obj nil locale)
             ::tz-format/id               (time-zones    phone-obj nil locale ::tz-format/id)
             ::tz-format/full-standalone  (time-zones    phone-obj nil locale ::tz-format/full-standalone)
             ::tz-format/short-standalone (time-zones    phone-obj nil locale ::tz-format/short-standalone)
             ::dialing-region/valid-for?  (when (some? dialing-region) (valid? phone-obj nil dialing-region))
             ::dialing-region/derived?    dialing-derived
             ::dialing-region/defaulted?  dialing-default}
            (all-formats-into phone-obj nil)
            (short-info-core  phone-obj region-code dialing-region phone-number)
            info-remove-nils)))))

;;
;; Matching
;;

(defn match
  "Returns matching level of two numbers or nil if there is no match. Optionally each
  second argument can be a region code (if the given phone number is not a kind of
  PhoneNumber and is not prefixed by any calling code )."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^phone_number.core.Phoneable phone-number-a
    ^clojure.lang.Keyword        region-code-a
    ^phone_number.core.Phoneable phone-number-b
    ^clojure.lang.Keyword        region-code-b]
   (when-some-input phone-number-a
     (when-some-input phone-number-b
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
  not prefixed by any calling code)."
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

(def ^{:added "8.12.4-0" :tag Boolean}
  is-short?
  "Same as short-valid?"
  short-valid?)

(def ^{:added "8.12.4-0" :tag Boolean}
  is-maybe-short?
  "Same as short-possible?"
  short-possible?)

(def ^{:added "8.12.4-0" :tag Boolean}
  short-invalid?
  "Logical negation of short-valid?"
  (complement short-valid?))

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

(defn has-calling-code?
  "For the given phone number returns true if the calling code is present in it, false
  otherwise. The region code can be explicit part of a number (as its prefix) or can
  be inferred by making use of the region-code argument.

  This function will always return `true` if a phone number was successfully parsed."
  {:added "8.12.4-0" :tag Boolean}
  ([^phone_number.core.Phoneable phone-number]
   (has-calling-code? phone-number nil))
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code]
   (util/try-parse-or-false
    (when-some-input phone-number
      (not
       (contains? none
                  (.getCountryCode
                   (number-noraw phone-number region-code))))))))

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
  The whole map is put under the :phone-number/info key and it is a delay object,
  automatically dereferenced when accessed due to lazy map structure used under the
  hood."
  {:added "8.12.4-0" :tag lazy_map.core.LazyMap}
  [^clojure.lang.Keyword  locale-specification
   ^clojure.lang.Keyword  dialing-region
   ^lazy_map.core.LazyMap m]
  (when-some [m (match/mapper m)]
    (if (= locale-specification false) m
        (if-some [n (::PN/number m)]
          (assoc m ::PN/info (delay (info n nil locale-specification dialing-region)))
          m))))

(defn find-numbers
  "Searches for phone numbers in the given text. Returns a lazy sequence of maps where
  each element is a map representing a match and having the following keys:

  * `:phone-number.match/start`       - start index of a phone number substring
  * `:phone-number.match/end`         - end index of a phone number substring
  * `:phone-number.match/raw-string`  - phone number substring
  * `:phone-number/number`            - phone number object
  * `:phone-number/info`              - optional info object (lazily evaluated)

  Phone number object is suitable to be used with majority of functions from
  the core. It is a mutable Java object so be careful!

  Optional but highly recommended `region-code` argument should be a region code to
  be used as a hint when looking for numbers without any calling code prefix.

  The `leniency` argument sets the matching leniency level during searching. Possible
  levels are:

  * `:phone-number.leniency/exact` – accepts valid phone numbers that are formatted
  in a standard way (or as a single block)

  * `:phone-number.leniency/possible` – accepts possible phone numbers (including
  invalid ones)

  * `:phone-number.leniency/strict` –accepts valid phone numbers that are possible
  for the locale (and its formatting rules)

  * `:phone-number.leniency/valid` – accepts possible AND valid phone numbers (this
  is default)

  If it's nil then it will be set to a default (`:phone-number.leniency/valid`).

  The `max-tries` argument tells the searching engine to deliver only certain number
  of matches. If it's not given then the maximum value of type `Long` will be used.

  The `locale-specification` and `dialing-region` are not used during searching but
  when the map under `:phone-number/info` key is generated. They are passed to the
  `info` function. Note that setting `dialing-region` explicitly to nil will disable
  it from being derived from the detected region (yet it will still default to
  current value of the `phone-number/*default-dialing-region*` if it's set) value. To
  preserve default behaviour (derivation plus using the default from a dynamic
  variable) and explicitly pass this argument, use `false`.

  All keyword arguments except `locale-specification` (which can also be of other
  type) are namespace inferred if `*inferred-namespace*` dynamic variable is set in
  the calling context.

  Some arities can have different variants (depending on the given arguments). It's
  possible to detect when keywords describing different things are not
  conflicting. The exception is when we have two final arguments which cannot be
  easily distinguished without namespacing since they may have overlapping
  values (like `:pl` as a dialing region and `:pl` as a locale specification). To use
  shortened arity with them and allow function to properly detect what kind of data
  it's dealing with, please use **fully-qualified keywords** to describe **a dialing
  region**. Keyword without a proper namespace will be treated as locale
  specification on this argument position.

  For the sake of efficiency it is possible to entirely disable generation of a map
  under the `:phone-number/info` key. To do that just use arity with
  `locale-specification` and set the value of this argument to `false`. For argument
  values that you wish to be kept as default, use nil."
  {:added "8.12.4-0" :tag clojure.lang.LazySeq
   :arglists '([^String text]
               [^String               text
                ^clojure.lang.Keyword region-code]
               [^String               text
                ^clojure.lang.Keyword leniency]
               [^String               text
                ^clojure.lang.Keyword region-code
                ^clojure.lang.Keyword leniency]
               [^String               text
                ^clojure.lang.Keyword region-code
                ^clojure.lang.Keyword locale-specification]
               [^String               text
                ^clojure.lang.Keyword region-code
                ^long                 max-tries]
               [^String               text
                ^clojure.lang.Keyword leniency
                ^long                 max-tries]
               [^String               text
                ^clojure.lang.Keyword leniency
                ^clojure.lang.Keyword locale-specification]
               [^String               text
                ^clojure.lang.Keyword region-code
                ^clojure.lang.Keyword leniency
                ^Long                 max-tries]
               [^String               text
                ^clojure.lang.Keyword region-code
                ^clojure.lang.Keyword locale-specification
                ^Long                 dialing-region]
               [^String               text
                ^clojure.lang.Keyword region-code
                ^clojure.lang.Keyword leniency
                ^Long                 max-tries
                ^clojure.lang.Keyword locale-specification]
               [^String               text
                ^clojure.lang.Keyword region-code
                ^clojure.lang.Keyword leniency
                ^Long                 max-tries
                ^clojure.lang.Keyword dialing-region-FQ]
               [^String               text
                ^clojure.lang.Keyword region-code
                ^clojure.lang.Keyword leniency
                ^Long                 max-tries
                ^clojure.lang.Keyword locale-specification
                ^clojure.lang.Keyword dialing-region])}
  ([]
   nil)
  ([^String               text]
   (find-numbers text nil nil nil nil false))
  ([^String               text
    ^clojure.lang.Keyword region-code-or-leniency]
   (if (nil? region-code-or-leniency)
     (find-numbers text nil nil nil nil false)
     (if (leniency/valid? region-code-or-leniency *inferred-namespaces*)
       (find-numbers text nil region-code-or-leniency nil nil false)     ;; region code
       (find-numbers text region-code-or-leniency nil nil nil false))))  ;; leniency
  ([^String               text
    ^clojure.lang.Keyword region-code-or-leniency
    ^clojure.lang.Keyword leniency-or-max-tries-or-locale-spec]
   (if (nil? leniency-or-max-tries-or-locale-spec)
     (find-numbers text region-code-or-leniency)
     (if (region/valid? region-code-or-leniency *inferred-namespaces*)
       (if (leniency/valid? leniency-or-max-tries-or-locale-spec *inferred-namespaces*)
         (find-numbers text region-code-or-leniency leniency-or-max-tries-or-locale-spec nil nil false)         ;; region code, leniency
         (if (number? leniency-or-max-tries-or-locale-spec)
           (find-numbers text region-code-or-leniency nil leniency-or-max-tries-or-locale-spec nil false)       ;; region code, max-tries
           (find-numbers text region-code-or-leniency nil nil leniency-or-max-tries-or-locale-spec false)))     ;; region code, locale spec
       (if (number? leniency-or-max-tries-or-locale-spec)
         (find-numbers text nil region-code-or-leniency leniency-or-max-tries-or-locale-spec nil false)         ;; leniency, max-tries
         (find-numbers text nil region-code-or-leniency nil leniency-or-max-tries-or-locale-spec false))))) ;; leniency, locale spec
  ([^String               text
    ^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword leniency-or-locale-spec
    ^Long                 max-tries-or-dialing-region]
   (if (and                                    ; backward compatibility
        (number? max-tries-or-dialing-region)
        (leniency/valid? leniency-or-locale-spec *inferred-namespaces*))
     (find-numbers text region-code leniency-or-locale-spec max-tries-or-dialing-region nil false)               ;; max-tries, leniency
     (find-numbers text region-code nil  nil leniency-or-locale-spec max-tries-or-dialing-region)))              ;; locale spec, dialing region code
  ([^String               text
    ^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword leniency
    ^Long                 max-tries
    ^clojure.lang.Keyword locale-specification-or-dialing-region]
   (if (nil? locale-specification-or-dialing-region)
     (find-numbers text region-code leniency max-tries nil false)
     (if (region/valid? locale-specification-or-dialing-region false)
       (find-numbers text region-code leniency max-tries nil locale-specification-or-dialing-region)             ;; dialing region code
       (find-numbers text region-code leniency max-tries locale-specification-or-dialing-region false))))        ;; locale specification
  ([^String               text
    ^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword leniency
    ^Long                 max-tries
    ^clojure.lang.Keyword locale-specification
    ^clojure.lang.Keyword dialing-region]
   (when (some? text)
     (->> (.findNumbers (util/instance)
                        text
                        (region/parse   region-code *inferred-namespaces*)
                        (leniency/parse leniency    *inferred-namespaces*)
                        (long (if (nil? max-tries) Long/MAX_VALUE max-tries)))
          util/lazy-iterator-seq
          (map #(enrich-match locale-specification dialing-region %))
          seq))))

;;
;; Example numbers generation
;;

(defn invalid-example
  "For the given region code returns the example phone number that is invalid (being a
  `PhoneNumber` kind of object). This is not a random number generator; it will
  always generate the same example number for the same arguments."
  {:added "8.12.4-0" :tag Phonenumber$PhoneNumber}
  [^clojure.lang.Keyword region-code]
  (when-some [rcode (region/parse region-code *inferred-namespaces*)]
    (util/try-null
     (.getInvalidExampleNumber
      (util/instance)
      rcode))))

(defn example-non-geo
  "For the given calling code (given as a positive, natural number) returns the example
  phone number that is valid (being a `PhoneNumber` kind of object). This is not a
  random number generator; it will always generate the same example number for the
  same arguments."
  {:added "8.12.4-0" :tag Phonenumber$PhoneNumber}
  ([^Integer calling-code _]
   (example-non-geo calling-code))
  ([^Integer calling-code]
   (when (some? calling-code)
     (util/try-null
      (.getExampleNumberForNonGeoEntity
       (util/instance)
       (net-code/parse calling-code))))))

(defn example
  "For the given region code and optional number type returns an example phone number
  that is valid (being a `PhoneNumber` kind of object). This is not a random number
  generator; it will always generate the same example number for the same arguments."
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

(defn- number-g
  [^phone_number.core.Phoneable phone-number
   ^clojure.lang.Keyword        region-code]
  (if (native? phone-number)
    phone-number
    (.parseAndKeepRawInput
     (util/instance) phone-number
     (region/parse region-code *inferred-namespaces*))))

(defn- number-g-noraw
  [^phone_number.core.Phoneable phone-number
   ^clojure.lang.Keyword        region-code]
  (if (native? phone-number)
    phone-number
    (.parse
     (util/instance) phone-number
     (region/parse region-code *inferred-namespaces*))))

(defn- gen-sample
  "Internal phone number samples generator."
  {:added "8.12.4-0" :tag Phonenumber$PhoneNumber}
  ([^phone_number.core.Phoneable phone-number
    ^clojure.lang.Keyword        region-code
    ^Long                        retries
    ^Long                        min-digits
    ^clojure.lang.IFn            validator
    ^java.util.Random            rng
    ^Boolean                     early-shrinking
    ^Boolean                     preserve-raw]
   (let [number-fn (if preserve-raw number-g number-g-noraw)]
     (when-some [template (number-fn phone-number region-code)]
       (let [min-digits     (if (nil? min-digits) 3 min-digits)
             calling-code   (.getCountryCode template)
             prefix         (subs (.format (util/instance) template (format/all ::format/e164))
                                  (unchecked-inc (util/count-digits calling-code)))
             calling-code   (str "+" calling-code)
             total-len      (unchecked-long (count prefix))
             retries        (unchecked-long retries)
             auto-shrink    (unchecked-long (if early-shrinking 0 (* retries 0.75)))
             despair-shrink (unchecked-long (* retries 0.25))
             template       (if (validator template) template nil)]
         (loop [last-valid  template
                last-static prefix
                last-random nil
                prefix      prefix
                iteration   (unchecked-long 1)
                valid-hits  (unchecked-long 0)]
           (if (or (and (some? retries) (= iteration retries))
                   (and (nil? prefix) (some? last-valid)))
             {::PN/number      last-valid
              ::sample/hits    valid-hits
              ::sample/samples iteration
              ::sample/digits  [(not-empty calling-code) (not-empty last-static) (not-empty last-random)]}
             (let [prefix-len    (unchecked-long (count prefix))
                   fuzzed-len    (unchecked-subtract total-len prefix-len)
                   shrink-now    (or  (> iteration auto-shrink) (and (nil? last-valid) (> iteration despair-shrink)))
                   random-len    (util/random-digits-len fuzzed-len iteration shrink-now rng)
                   random-digits (util/gen-digits random-len rng)
                   regional-part (str prefix random-digits)
                   skip-test     (< (count regional-part) min-digits)
                   test-number   (when-not skip-test (util/try-parse (number-fn (str calling-code regional-part) nil)))
                   have-valid    (and (some? test-number) (validator test-number))
                   ;; retest is needed since there are zero-prefixed regional numbers
                   ;; which are not valid input when used without country calling code but with region argument
                   have-valid    (if (and have-valid (= \0 (first regional-part)))
                                   (and (some? (region test-number))
                                        (native? (util/try-parse-or-false (number-g-noraw regional-part (region test-number)))))
                                   have-valid)
                   shorten       (or have-valid (nil? last-valid))
                   new-prefix    (if (empty? prefix) nil (if shorten (subs prefix 0 (dec prefix-len)) prefix))]
               (recur (if have-valid test-number   last-valid)
                      (if have-valid prefix        last-static)
                      (if have-valid random-digits last-random)
                      new-prefix
                      (unchecked-inc iteration)
                      (if have-valid (unchecked-inc valid-hits) valid-hits))))))))))

(def ^{:added "8.12.4-0" :tag clojure.lang.PersistentVector :private true}
  region-net-code-mix
  "Vector of supported global network codes mixed with region codes."
  (into region/all-arg-vec net-code/all-arg-vec))

(defn- region-or-code-sample
  "Random sampler of region codes mixed with global network calling codes."
  {:added "8.12.4-0"}
  [^java.util.Random rng]
  (util/get-rand-nth region-net-code-mix rng))

(defn generate
  "Generates random phone number in a form of a map with the following keys:

  * `:phone-number/number`        - a `PhoneNumber` object
  * `:phone-number/info`          - a map with phone number information (evaluated on access)
  * `:phone-number.sample/hits`   - a number of valid hits encountered during sampling
  * `:phone-number.sample/digits` - a vector with used calling code prefix, template part and random part
  * `:phone-number.sample/max-samples` - a maximum number of samples declared
  * `:phone-number.sample/samples` - a number of samples processed before the result was formed
  * `:phone-number.sample/random-seed` – random seed used to initialize pseudo-random generator

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
  results in most cases. This is due to randomization strategy the internal sampler
  uses. It starts by taking an initial, template number returned by the example
  function. This number is valid but may not fulfill additional criteria. If it
  fulfills them it is memorized and the next, more fuzzed variant is tried with last
  digit replaced by a randomly generated one. If such number is also valid it is
  memorized and the randomization continues until all digits (except the country code
  plus the static part described later) are randomized. When that happens the result
  is returned if it fulfills all of the validation criteria or the number of retries
  reaches the given maximal value. If the final result (after all the trials) is not
  valid then the memorized number is returned.

  The fifth argument, when present, should be a minimal number of digits in regional
  part of the number that are accepted as a result. If it is not given the default of
  3 is assumed.

  When the sixth argument is present it should be a valid locale specification or a
  `java.util.Locale` object that will be passed to the info function in order to render
  localized versions of time zone information and geographical locations.

  The seventh argument should be a long value that will seed the pseudo-random number
  generator used to produce digits and to choose region and/or phone number type when
  not given. It can be used to create a deterministic sequence of samples.

  The eight, optional argument enables more aggressive shrinking of randomly generated
  part. If it is set to a truthy value (not nil and not false) then each sampling
  step that involves generation of random digits will have 50% chances of producing
  less digits than required (at least 1 digit remaining). The number of digits is
  chosen randomly. It is advised to enable shrinking when expecting highly improbable
  phone numbers, for instance with the impossible? predicate.

  The last, optional argument chooses whether raw input should be preserved within
  the PhoneNumber objects when generating samples. By default it is not preserved."
  {:added "8.12.4-0" :tag lazy_map.core.LazyMap}
  ([]
   (generate nil nil nil nil nil nil nil false false))
  ([^clojure.lang.Keyword region-code]
   (generate region-code nil nil nil nil nil nil false false))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type]
   (generate region-code number-type nil nil nil nil nil false false))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate]
   (generate region-code number-type predicate nil nil nil nil false false))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Long                 retries]
   (generate region-code number-type predicate retries nil nil nil false false))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Long                 retries
    ^Long                 min-digits]
   (generate region-code number-type predicate retries min-digits nil nil false false))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Long                 retries
    ^Long                 min-digits
    ^clojure.lang.Keyword locale-specification]
   (generate region-code number-type predicate retries min-digits locale-specification nil false false))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Long                 retries
    ^Long                 min-digits
    ^clojure.lang.Keyword locale-specification
    ^Long                 random-seed]
   (generate region-code number-type predicate retries min-digits locale-specification random-seed false false))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Long                 retries
    ^Long                 min-digits
    ^clojure.lang.Keyword locale-specification
    ^Long                 random-seed
    ^Boolean              early-shrinking]
   (generate region-code number-type predicate retries min-digits locale-specification random-seed early-shrinking false))
  ([^clojure.lang.Keyword region-code
    ^clojure.lang.Keyword number-type
    ^clojure.lang.IFn     predicate
    ^Long                 retries
    ^Long                 min-digits
    ^clojure.lang.Keyword locale-specification
    ^Long                 random-seed
    ^Boolean              early-shrinking
    ^Boolean              preserve-raw]
   (type/parse number-type *inferred-namespaces*) ;; assert check
   (let [early-shrinking  (if (nil? early-shrinking) false (or (and early-shrinking true) false))
         random-seed      (long (if (nil? random-seed) (rand Long/MAX_VALUE) random-seed))
         rng              (java.util.Random. random-seed)
         predicate        (if (nil? predicate) any? predicate)
         retries          (if (nil? retries) 1000 (if (false? retries) nil retries))
         lspec            (locale/parse locale-specification *inferred-namespaces*)]
     (loop [tried-combos   #{}
            region-code    (util/ns-infer "phone-number.region" region-code *inferred-namespaces*)
            number-type    (util/ns-infer "phone-number.type"   number-type *inferred-namespaces*)
            template-tries (unchecked-add-int 8 (unchecked-multiply-int
                                                 (if (nil? region-code) (count region-net-code-mix) 1)
                                                 (if (nil? number-type) (count types) 1)))]
       (let [number-type' (if (some? number-type) number-type (type/generate-arg-sample rng))
             region-code' (if (some? region-code) region-code (region-or-code-sample rng))
             non-geo-mode (number? region-code')
             example-fn   (if non-geo-mode example-non-geo example)
             combo        [number-type' region-code']
             tried?       (contains? tried-combos combo)
             template     (when-not tried? (example-fn region-code' number-type'))]
         (if-some [template template]
           ;; template is present for this combo
           (let [number-type'    (if (or (nil? number-type) non-geo-mode) number-type' (type template))
                 region-code'    (if (or (nil? region-code) non-geo-mode) region-code' (region template))
                 valid-type-fn   (if (some? number-type) #(= number-type' (type %)) any?)
                 valid-region-fn (if (nil? region-code) any?
                                     (if non-geo-mode
                                       #(= region-code' (calling-code %))
                                       #(= region-code' (region %))))
                 result (gen-sample template nil
                                    retries
                                    min-digits
                                    (every-pred predicate valid-type-fn valid-region-fn)
                                    rng
                                    (if non-geo-mode true early-shrinking)
                                    preserve-raw)
                 phone-number    (::PN/number result)]
             (if (some? phone-number)
               ;; phone number sample is generated
               (merge
                (lazy-map {::PN/info (info phone-number nil lspec false)})
                (assoc result
                       ::sample/max-samples retries
                       ::sample/random-seed random-seed))
               ;; there is no phone number sample but the global network calling code was given
               ;; and the template probably doesn't have the required number type
               ;; we can try different combo if the required country code is not set
               ;; other cases where template was ok but sampler returned nothing
               ;; are also covered here
               (when (and (not (zero? template-tries))
                          (or (nil? number-type) (nil? region-code)))
                 (recur (conj tried-combos combo)
                        region-code number-type
                        (unchecked-dec-int template-tries)))))
           ;; template is not present for this combo
           (when-not (zero? template-tries)
             ;; some combinations of type and region are not suited to produce a valid template
             ;; in such cases we have to retry if there is a chance to do that
             ;; (at least a region or a number type are random)
             (when (or (nil? region-code) (nil? number-type))
               (if tried?
                 ;; this combo was already tried, do not increase the counter
                 (recur tried-combos
                        region-code number-type
                        template-tries)
                 ;; that was a fresh combo
                 (recur (conj tried-combos combo)
                        region-code number-type
                        (unchecked-dec-int template-tries)))))))))))
