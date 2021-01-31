(ns

    ^{:doc    "Cross-category databases and generators for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.16-1"}

    phone-number.db

  (:require [clojure.set]
            [phone-number.util         :as         util]
            [phone-number.net-code     :as     net-code]
            [phone-number.country-code :as country-code]
            [phone-number.calling-code :as calling-code]
            [phone-number.region       :as       region]
            [phone-number.type         :as         type]
            [phone-number.format       :as       format]
            [phone-number.tz-format    :as    tz-format]
            [phone-number.match        :as        match]
            [phone-number.locale       :as       locale]
            [phone-number.leniency     :as     leniency]
            [phone-number.cost         :as         cost])

  (:import  [com.google.i18n.phonenumbers
             PhoneNumberUtil
             PhoneNumberUtil$PhoneNumberType]))

;;
;; Aliases
;;

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  regions
  "A set of all possible phone number region codes."
  (set region/all-vec))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  net-codes
  "A set of all possible global network calling codes."
  net-code/all)

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  country-codes
  "A set of all possible country calling codes."
  country-code/all)

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  calling-codes
  "A set of all possible country calling codes."
  calling-code/all)

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  formats
  "A set of all possible phone number formats as a sequence of keywords."
  (set (keys format/all)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  leniencies
  "A set of all possible phone number leniencies as a sequence of keywords."
  (set (keys leniency/all)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  locales
  "A set of all possible phone number locales as a sequence of keywords."
  (set (keys locale/all)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  types
  "A set of all possible phone number types as a sequence of keywords."
  (set type/all-vec))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  match-types
  "A set of all possible phone number match types as a sequence of keywords."
  (set (keys match/all)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  tz-formats
  "A set of all possible time zone formats as a sequence of keywords."
  (set (keys tz-format/all)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  costs
  "A set of all possible phone number cost classes as a sequence of keywords."
  (set cost/all-vec))

;;
;; Aliases (args)
;;

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  regions-args
  "A set of all possible phone number region codes (suitable to be used as arguments)."
  (set region/all-arg-vec))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  net-codes-args
  "A set of all possible global network calling codes (suitable to be used as
  arguments)."
  net-code/all-arg)

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  country-codes-args
  "A set of all possible country calling codes (suitable to be used as arguments)."
  country-code/all-arg)

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  calling-codes-args
  "A set of all possible country calling codes (suitable to be used as arguments)."
  calling-code/all-arg)

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  formats-args
  "A set of all possible phone number formats as a sequence of keywords (suitable to be
  used as arguments)."
  (set (keys format/all-arg)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  leniencies-args
  "A set of all possible phone number leniencies as a sequence of keywords (suitable to
  be used as arguments)."
  (set (keys leniency/all-arg)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  locales-args
  "A set of all possible phone number locales as a sequence of keywords (suitable to be
  used as arguments)."
  (set (keys locale/all-arg)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  types-args
  "A set of all possible phone number types as a sequence of keywords (suitable to be
  used as arguments)."
  (set type/all-arg-vec))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  match-types-args
  "A set of all possible phone number match types as a sequence of keywords (suitable
  to be used as arguments)."
  (set (keys match/all-arg)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  tz-formats-args
  "A set of all possible time zone formats as a sequence of keywords (suitable to be
  used as arguments)."
  (set (keys tz-format/all-arg)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet}
  costs-args
  "A set of all possible phone number cost classes as a sequence of keywords (suitable
  to be used as arguments)."
  (set cost/all-arg-vec))

;;
;; Regions <---> calling codes
;;

(defn regions-for-calling-code
  "Returns a set of region codes (keywords) matching the given country calling code
  numbers (integer numbers)."
  {:added "8.12.16-1" :tag clojure.lang.PersistentHashSet :private true}
  [^Integer calling-code]
  (some->> calling-code
           (.getRegionCodesForCountryCode (util/instance))
           util/lazy-iterator-seq
           (map region/by-val)
           (remove nil?)
           seq
           set))

(defn regions-arg-for-calling-code
  "Returns a set of region codes (keywords) valid as arguments and matching the given
  country calling code numbers (integer numbers)."
  {:added "8.12.16-1" :tag clojure.lang.PersistentHashSet :private true}
  [^Integer calling-code]
  (some->> calling-code
           (.getRegionCodesForCountryCode (util/instance))
           util/lazy-iterator-seq
           (map region/by-val-arg)
           (remove nil?)
           seq
           set))

(defn region-for-calling-code
  "Returns a primary region (keyword) matching the given calling code number (an
  integer number)."
  {:added "8.12.16-1" :tag clojure.lang.Keyword :private true}
  [^Integer calling-code]
  (when (some? calling-code)
    (region/by-val (.getRegionCodeForCountryCode (util/instance) calling-code))))

(defn region-arg-for-calling-code
  "Returns a primary region (keyword) matching the given calling code number (an
  integer number) that is valid as an argument."
  {:added "8.12.16-1" :tag clojure.lang.Keyword :private true}
  [^Integer calling-code]
  (when (some? calling-code)
    (region/by-val-arg (.getRegionCodeForCountryCode (util/instance) calling-code))))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  calling-code-to-regions
  "Mapping of all supported calling codes (integer numbers) to regions (sets of
  keywords) they are assigned to."
  (into {} (map (juxt identity regions-for-calling-code) calling-code/by-val)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  calling-code-to-region
  "Mapping of all supported calling codes (integer numbers) to their primary
  regions (sets of keywords)."
  (into {} (map (juxt identity region-for-calling-code) calling-code/by-val)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  calling-code-to-regions-arg
  "Mapping of all supported calling codes (integer numbers) to regions (sets of
  keywords) they are assigned to and are valid as arguments. Be aware that this map
  does not contain `:phone-number.region/world`."
  (into {} (map (juxt identity regions-arg-for-calling-code) calling-code/by-val)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  calling-code-to-region-arg
  "Mapping of all supported calling codes (integer numbers) to their primary
  regions (sets of keywords) which are valid as arguments. Be aware that this map
  does not contain `:phone-number.region/world`."
  (into {} (map (juxt identity region-arg-for-calling-code) calling-code/by-val)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  region-to-calling-codes
  "Mapping of all supported regions (keywords) to their calling codes (sets of integer
  numbers)."
  (util/map-of-sets-invert calling-code-to-regions))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  region-to-calling-code
  "Mapping of all supported regions (keywords) to their primary calling codes (integer
  numbers)."
  (clojure.set/map-invert calling-code-to-region))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  region-arg-to-calling-codes
  "Mapping of all supported regions (keywords) which are valid as arguments to their
  calling codes (sets of integer numbers). Be aware that this map does not contain
  `:phone-number.region/world`."
  (util/map-of-sets-invert calling-code-to-regions-arg))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  region-arg-to-calling-code
  "Mapping of all supported regions (keywords) which are valid as arguments to their
  primary calling codes (integer numbers). Be aware that this map does not contain
  `:phone-number.region/world`."
  (clojure.set/map-invert calling-code-to-region-arg))

;;
;; Regions <---> types
;;

(defn get-supported-types-for-non-geo-entity
  "Method wrapper"
  {:added "8.12.16-1" :tag clojure.lang.PersistentHashSet :private true}
  [^Integer net-code]
  (some->> net-code net-code/all
           (.getSupportedTypesForNonGeoEntity (util/instance)) seq
           (map type/by-val) (remove nil?) seq set))

(defn get-supported-types-for-region
  "Method wrapper."
  {:added "8.12.16-1" :tag clojure.lang.PersistentHashSet :private true}
  [^clojure.lang.Keyword region-code]
  (some->> region-code region/all
           (.getSupportedTypesForRegion (util/instance)) seq
           (map type/by-val) (remove nil?) seq set))

(defn types-for-region
  "Gets types for a region or for a world."
  {:added "8.12.16-1" :tag clojure.lang.PersistentHashMap :private true}
  [^clojure.lang.Keyword region-code]
  (if (region/valid-arg? region-code)
    (get-supported-types-for-region region-code)
    (some->> region-code region-to-calling-codes seq
             (map (comp seq get-supported-types-for-non-geo-entity))
             flatten (remove nil?) seq set)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  region-to-types
  "Mapping of all regions (keywords) to supported phone number types (sets of
  keywords). Please be aware that the `:phone-number.region/world` pseudo-region
  groups many calling codes which can vary when it comes to the supported types, hence
  making this database less precise. To get more granular data for global numbers
  which are not geographical, please use `phone-number.db/calling-code-to-types`. For
  geographical regions this database will be precise."
  (util/fmap-k types-for-region region/all))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  region-arg-to-types
  "Mapping of all regions (keywords) which are valid as arguments to supported phone
  number types (sets of keywords). Please be aware that this map does not contain
  `:phone-number.region/world` pseudo-region."
  (util/fmap-k types-for-region region/all-arg))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  type-to-regions
  "Mapping of all supported phone number types (keywords) to regions (sets of
  keywords). Please be aware that the `:phone-number.region/world` pseudo-region
  groups many calling codes which can vary when it comes to the supported types,
  hence making this database less precise. To get more granular data for global
  numbers which are not geographical, please use
  `phone-number.db/type-to-calling-codes`. For geographical regions this database
  will be precise."
  (util/map-of-sets-invert region-to-types))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  type-to-regions-arg
  "Mapping of all supported phone number types (keywords) to regions (sets of keywords)
  which are valid as arguments. Please be aware that this map does not contain
  `:phone-number.region/world` pseudo-region within its values."
  (util/map-of-sets-invert region-arg-to-types))

;;
;; Calling codes <---> types
;;

(defn types-for-calling-code
  "Gets types for a calling code."
  {:added "8.12.16-1" :tag clojure.lang.PersistentHashMap :private true}
  [^clojure.lang.Keyword calling-code]
  (if (net-code/valid-arg? calling-code)
    (get-supported-types-for-non-geo-entity calling-code)
    (some->> calling-code calling-code-to-regions seq
             (map (comp seq get-supported-types-for-region))
             flatten (remove nil?) seq set)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  calling-code-to-types
  "Mapping of all calling codes (integer numbers) to sets of the supported phone number
  types (keywords). Please be aware that single country calling code may be used by
  more than one geographical region (e.g. 1 maps to US, Canada and more), making this
  database less precise for geographical location. In case of regional numbers
  consider using `phone-number.db/region-to-types`. This database will work precisely
  with non-geographical, global network calling codes."
  (into {} (map (juxt identity types-for-calling-code)
                calling-code/all)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentHashMap}
  type-to-calling-codes
  "Mapping of all the supported phone number types (keywords) to sets of calling
  codes (integer numbers). Please be aware that single country calling code may be
  used by more than one geographical region (e.g. 1 maps to US, Canada and more),
  making this database less precise for geographical location. In case of regional
  numbers consider using `phone-number.db/type-to-regions`. This database will work
  precisely with non-geographical, global network calling codes."
  (util/map-of-sets-invert calling-code-to-types))

;;
;; Additional generators
;;

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector
       :arglists '([^clojure.lang.Keyword region-code])}
  region-to-types-vec
  "Returns a vector of phone number types for the given region and caches the result."
  (memoize
   (comp util/with-not-empty vec region-to-types)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector
       :arglists '([^clojure.lang.Keyword region-code])}
  region-arg-to-types-vec
  "Returns a vector of phone number types for the given region (which must be suitable
  to be used as an argument) and caches the result."
  (memoize
   (comp util/with-not-empty vec region-arg-to-types)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector
       :arglists '([^clojure.lang.Keyword region-code])}
  calling-code-to-regions-vec
  "Returns a vector of phone number types for the given calling code and caches the
  result."
  (memoize
   (comp util/with-not-empty vec calling-code-to-regions)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector
       :arglists '([^clojure.lang.Keyword region-code])}
  calling-code-to-regions-arg-vec
  "Returns a vector of phone number regions (which can be used as arguments) for the
  given calling code and caches the result."
  (memoize
   (comp util/with-not-empty vec calling-code-to-regions-arg)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector
       :arglists '([^clojure.lang.Keyword region-code])}
  calling-code-to-types-vec
  "Returns a vector of phone number types for the given calling code and caches the
  result."
  (memoize
   (comp util/with-not-empty vec calling-code-to-types)))

(defn generate-type-for-region
  "Generates a random number type which is valid for the given region. Works for
  geographical numbers, doesn't work for `:phone-number.region/world`."
  {:added "8.12.16-1" :tag clojure.lang.Keyword}
  ([]
   (rand-nth type/all-vec))
  ([region-code-or-random]
   (if (keyword? region-code-or-random)
     (rand-nth (region-to-types-vec region-code-or-random))
     (util/get-rand-nth type/all-vec region-code-or-random)))
  ([^clojure.lang.Keyword region-code
    ^java.util.Random rng]
   (util/get-rand-nth
    (region-to-types-vec region-code)
    rng)))

(defn generate-type-arg-for-region
  "Generates a random number type which is valid for the given region and can be used
  as an argument. Works for geographical numbers, doesn't work for
  `:phone-number.region/world`."
  {:added "8.12.16-1" :tag clojure.lang.Keyword}
  ([]
   (rand-nth type/all-arg-vec))
  ([region-code-or-random]
   (if (keyword? region-code-or-random)
     (rand-nth (region-to-types-vec region-code-or-random))
     (util/get-rand-nth type/all-arg-vec region-code-or-random)))
  ([^clojure.lang.Keyword region-code
    ^java.util.Random rng]
   (util/get-rand-nth
    (region-to-types-vec region-code)
    rng)))

(defn generate-type-for-region-arg
  "Generates a random number type which is valid for the given region (taken from the
  region codes which are suitable to be used as arguments). Works for geographical
  numbers, doesn't work for `:phone-number.region/world`."
  {:added "8.12.16-1" :tag clojure.lang.Keyword}
  ([]
   (rand-nth type/all-arg-vec))
  ([region-code-or-random]
   (if (keyword? region-code-or-random)
     (rand-nth (region-arg-to-types-vec region-code-or-random))
     (util/get-rand-nth type/all-arg-vec region-code-or-random)))
  ([^clojure.lang.Keyword region-code
    ^java.util.Random rng]
   (util/get-rand-nth
    (region-arg-to-types-vec region-code)
    rng)))

(defn generate-type-for-calling-code
  "Generates a random number type which is valid for the given region. Works for
  non-geographical numbers (global network calling codes), doesn't work well for
  country calling codes."
  {:added "8.12.16-1" :tag Integer}
  ([]
   (rand-nth type/all-vec))
  ([calling-code-or-random]
   (if (number? calling-code-or-random)
     (rand-nth (calling-code-to-types-vec calling-code-or-random))
     (util/get-rand-nth type/all-arg-vec calling-code-or-random)))
  ([^Integer calling-code
    ^java.util.Random rng]
   (util/get-rand-nth
    (calling-code-to-types-vec calling-code)
    rng)))

(defn generate-type-arg-for-calling-code
  "Generates a random number type which is valid for the given region and suitable to
  be used as an argument. Works for non-geographical numbers (global network calling
  codes), doesn't work well for country calling codes."
  {:added "8.12.16-1" :tag Integer}
  ([]
   (rand-nth type/all-arg-vec))
  ([calling-code-or-random]
   (if (number? calling-code-or-random)
     (rand-nth (calling-code-to-types-vec calling-code-or-random))
     (util/get-rand-nth type/all-arg-vec calling-code-or-random)))
  ([^Integer calling-code
    ^java.util.Random rng]
   (util/get-rand-nth
    (calling-code-to-types-vec calling-code)
    rng)))

(defn generate-region-arg-for-calling-code
  "Generates a random region code (keyword) which is suitable as an argument for the
  given calling code."
  {:added "8.12.16-1" :tag String}
  ([]
   (rand-nth region/all-arg-vec))
  ([^Integer calling-code]
   (rand-nth (calling-code-to-regions-arg-vec calling-code)))
  ([^Integer calling-code
    ^java.util.Random rng]
   (util/get-rand-nth
    (calling-code-to-regions-arg-vec calling-code)
    rng)))

(defn generate-region-for-calling-code
  "Generates random region code (keyword) for the given calling code."
  {:added "8.12.16-1" :tag String}
  ([]
   (rand-nth region/all-vec))
  ([^Integer calling-code]
   (rand-nth (calling-code-to-regions-vec calling-code)))
  ([^Integer calling-code
    ^java.util.Random rng]
   (util/get-rand-nth
    (calling-code-to-regions-vec calling-code)
    rng)))
