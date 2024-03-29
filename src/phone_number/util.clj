(ns

    ^{:doc    "Singleton wrappers and helpers for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.util

  (:refer-clojure :exclude [short])

  (:require [trptr.java-wrapper.locale :as l])

  (:import [com.google.i18n.phonenumbers
            PhoneNumberUtil
            ShortNumberInfo
            geocoding.PhoneNumberOfflineGeocoder
            PhoneNumberToCarrierMapper
            PhoneNumberToTimeZonesMapper
            NumberParseException]))

;; Singletons

(defn instance
  {:tag PhoneNumberUtil, :added "8.12.4-0"}
  ^PhoneNumberUtil []
  (PhoneNumberUtil/getInstance))

(defn short
  {:tag ShortNumberInfo, :added "8.12.4-0"}
  ^ShortNumberInfo []
  (ShortNumberInfo/getInstance))

(defn geo-coder
  {:tag PhoneNumberOfflineGeocoder, :added "8.12.4-0"}
  ^PhoneNumberOfflineGeocoder []
  (PhoneNumberOfflineGeocoder/getInstance))

(defn carrier-mapper
  {:tag PhoneNumberToCarrierMapper, :added "8.12.4-0"}
  ^PhoneNumberToCarrierMapper []
  (PhoneNumberToCarrierMapper/getInstance))

(defn time-zones-mapper
  {:tag PhoneNumberToTimeZonesMapper, :added "8.12.4-0"}
  ^PhoneNumberToTimeZonesMapper []
  (PhoneNumberToTimeZonesMapper/getInstance))

;; Helpers

(defmacro try-parse
  "Evaluates body and if NumberParseException or NumberFormatException exception is
  caught it returns nil."
  {:added "8.12.4-0"}
  [& body]
  `(try ~@body
        (catch AssertionError        e# nil)
        (catch NumberParseException  e# nil)
        (catch NumberFormatException e# nil)))

(defmacro try-parse-or-false
  "Evaluates body and if NumberParseException or NumberFormatException exception is
  caught it returns false."
  {:added "8.12.4-0"}
  [& body]
  `(try (or (do ~@body) false)
        (catch AssertionError        e# false)
        (catch NumberParseException  e# false)
        (catch NumberFormatException e# false)))

(defmacro try-null
  "Evaluates body and if NullPointerException exception is caught it returns
  nil. Otherwise it returns the value of last expression in the body."
  {:added "8.12.4-0"}
  [& body]
  `(try ~@body
        (catch NullPointerException  e# nil)))

(defmacro when-not-empty
  "Evaluates body when the given value is a non-empty collection."
  {:added "8.12.16-1"}
  [val & body]
  `(when (seq ~val)
     ~@body))

(defn with-not-empty
  "Returns the collection if it's not empty. Otherwise returns `nil`."
  {:added "8.12.16-1"}
  [obj]
  (if (seq obj) obj))

(defn count-digits
  {:added "8.12.4-1" :tag 'long}
  [^long n]
  (if (zero? n) 1
      (unchecked-inc
       (long (Math/floor (Math/log10 n))))))

(defn ns-infer
  "Takes a string of namespace name and a keyword. If the given keyword is not
  namespace-qualified it returns a new keyword with the given namespace added. If the
  given keyword is already equipped with a namespace it returns it."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^String ns-name
    ^clojure.lang.Keyword k]
   (if (simple-keyword? k)
     (keyword ns-name (name k))
     k))
  ([^String ns-name
    ^clojure.lang.Keyword k
    ^Boolean use-infer]
   (if use-infer (ns-infer ns-name k) k)))

(defn inferred-contains?
  "Just like the contains? but if the keyword is namespace-qualified it also checks if
  the collection contains the same keyword as its key but without a namespace."
  {:added "8.12.4-0" :tag Boolean}
  [^clojure.lang.IPersistentMap coll
   ^clojure.lang.Keyword k]
  (or (contains? coll k)
      (if (simple-keyword? k)
        false
        (contains? coll (keyword (name k))))))

(defn inferred-get
  "Just like the get function but if the keyword is namespace-qualified it first
  attempts to look for the value associated with it. If that fails it uses the
  variant of the keyword without any namespace."
  {:added "8.12.4-0"}
  ([^clojure.lang.IPersistentMap coll
    ^clojure.lang.Keyword k]
   (inferred-get coll k nil))
  ([^clojure.lang.IPersistentMap coll
    ^clojure.lang.Keyword k
    default]
   (if (simple-keyword? k)
     (k coll default)
     ((if (contains? coll k) k (keyword (name k))) coll default))))

(defn fmap-k
  "For each key and value of the given map m calls a function passed as the first
  argument (passing successive keys during calls to it) and generates a map with
  values updated by the results returned by the function. When the third argument is
  given it should be a map on which operations are performed instead of using the
  original map. This may be helpful when we want to avoid merging the results with
  another map."
  {:added "8.12.4-0" :tag clojure.lang.IPersistentMap}
  ([^clojure.lang.IFn f
    ^clojure.lang.IPersistentMap m]
   (fmap-k f m m))
  ([^clojure.lang.IFn f
    ^clojure.lang.IPersistentMap m
    ^clojure.lang.IPersistentMap dst]
   (reduce-kv
    (fn [^clojure.lang.IPersistentMap mp k v] (assoc mp k (f k)))
    dst m)))

(defn fmap-v
  "For each key and value of the given map m calls a function passed as the first
  argument (passing successive values during calls to it) and generates a map with
  values updated by the results returned by the function. When the third argument is
  given it should be a map on which operations are performed instead of using the
  original map. This may be helpful when we want to avoid merging the results with
  another map."
  {:added "8.12.16-1" :tag clojure.lang.IPersistentMap}
  ([^clojure.lang.IFn f
    ^clojure.lang.IPersistentMap m]
   (fmap-v f m m))
  ([^clojure.lang.IFn f
    ^clojure.lang.IPersistentMap m
    ^clojure.lang.IPersistentMap dst]
   (reduce-kv
    (fn [^clojure.lang.IPersistentMap mp k v] (assoc mp k (f v)))
    dst m)))

(defn map-of-sets-invert
  "Like `clojure.set/map-invert` but for map of sets (as values) to preserve all
  possible values (as keys of newly created map)."
  {:added "8.12.16-1" :tag clojure.lang.IPersistentMap}
  [^clojure.lang.IPersistentMap m]
  (reduce (fn [^clojure.lang.IPersistentMap am [k v]]
            (assoc am k (conj (am k (hash-set)) v)))
          (hash-map)
          (for [[k st] m v st] [v k])))

(defn remove-empty-vals
  "Removes empty values from a map."
  {:added "8.12.4-0" :tag clojure.lang.IPersistentMap}
  [^clojure.lang.IPersistentMap m]
  (reduce-kv
   (fn [^clojure.lang.IPersistentMap mp k v]
     (if (nil? v) (dissoc mp k) mp))
   m m))

(defn- gen-is-sexp
  "For the given keyword k and function name f it generates predicate function
  definition code that compares the result of calling the function on a phone number
  with the keyword. A helper that is used in macros."
  {:added "8.12.4-0"}
  [^clojure.lang.Keyword k
   ^clojure.lang.Symbol  f]
  (let [fn-name (symbol (str "is-" (name k) "?"))]
    (list 'defn fn-name
          {:added (:added (meta (var gen-is-sexp))), :tag 'Boolean
           :doc (str "Returns true when " (name f) " is " k ", false otherwise.\n")}
          (list '[phone-number]
                (list fn-name 'phone-number nil))
          (list '[phone-number region-specification]
                (list 'util/try-parse-or-false
                      (list '= k (list f 'phone-number 'region-specification)))))))

(defmacro gen-is
  "For the given keyword k and function name f uses gen-is-sexp to generate predicate
  function definitions."
  {:added "8.12.4-0"}
  [k f]
  (gen-is-sexp k f))

(defmacro gen-ises
  "Takes a collection of keywords (evaluated) and a function expressed as a symbol (not
  evaluated) and generates bunch of function definitions using gen-is-sexp."
  {:added "8.12.4-0"}
  [coll f]
  (cons 'do (map #(gen-is-sexp % f) (eval coll))))

(defn get-rand-int
  "Like rand-int but optionally uses random number generator."
  {:added "8.12.4-0"} ; was: :tag 'int
  ([^long n]
   (if (some? n)
     (rand-int n)))
  ([^long n
    ^java.util.Random rng]
   (if (some? n)
     (if (nil? rng)
       (get-rand-int n)
       (if (zero? n) (int n) (.nextInt rng n))))))

(defn random-digits-len
  "For 0 or 1 it returns its argument. For other positive numbers it returns a random
  natural number from 1 to this number (inclusive) in 50% cases. In other 50% cases
  it returns its argument."
  {:added "8.12.4-0"} ; was: :tag 'long
  ([^long x
    ^long iteration
    ^Boolean shrink-now]
   (if (some? x)
     (if (zero? x) x
         (if-not shrink-now x
                 (if (zero? iteration) 1
                     (if (or (< iteration 6) (zero? (rand-int 2)))
                       (unchecked-inc (rand-int x)) x))))))
  ([^long x
    ^long iteration
    ^Boolean shrink-now
    ^java.util.Random rng]
   (if (some? x)
     (if (nil? rng)
       (random-digits-len x iteration shrink-now)
       (if (zero? x) x
           (if-not shrink-now x
                   (if (zero? iteration) 1
                       (if (or (< iteration 6) (zero? (get-rand-int 2 rng)))
                         (unchecked-inc (get-rand-int x rng)) x))))))))

(defn gen-digits
  "Generates the given number of random digits and converts all into a single string.
  When the second argument is present it should be an instance of random number
  generator used to get the digits."
  {:added "8.12.4-0" :tag String}
  ([^long num]
   (apply str (repeatedly num #(rand-int 10))))
  ([^long num
    ^java.util.Random rng]
   (if (some? num)
     (if (nil? rng)
       (gen-digits num)
       (apply str (repeatedly num #(.nextInt rng 10)))))))

(defn get-rand-nth
  "Returns a random element of the given vector. When the second argument is present it
  should be an instance of random number generator used to get the random position."
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  ([^clojure.lang.IPersistentVector v]
   (when-not-empty v (rand-nth v)))
  ([^clojure.lang.IPersistentVector v
    ^java.util.Random rng]
   (when-not-empty v
     (if (nil? rng)
       (rand-nth v)
       (nth v (.nextInt rng (count v)))))))

(defn lazy-iterator-seq
  "Returns a lazy sequence as an interface to the given iterable Java object."
  {:added "8.12.4-0" :tag clojure.lang.LazySeq}
  ([^Iterable coll]
   (lazy-iterator-seq coll (.iterator coll)))
  ([^Iterable coll ^java.util.Iterator iter]
   (lazy-seq
    (if (.hasNext iter)
      (cons (.next iter) (lazy-iterator-seq coll iter))))))

(defn char-ranges->set
  "Returns a set of characters defined as a collection of collections with start and
  stop character, e.g.: [\\A \\Z][\\0 \\9]"
  {:added "8.12.4-1" :tag clojure.lang.PersistentHashSet}
  [& ranges]
  (set (mapcat #(map char (range (byte (first %)) (inc (byte (second %))))) ranges)))

(def ^{:added "8.12.16-1" :tag clojure.lang.PersistentHashSet :private true}
  all-locales
  l/available-locales)

(def ^{:added "8.12.4-1" :deprecated "8.12.4-3" :tag clojure.lang.PersistentHashSet}
  available-locales
  "DEPRECATED: Please use `phone-number.locale/by-val-vec`."
  all-locales)

(def ^{:added "8.12.4-1" :deprecated "8.12.4-3" :tag clojure.lang.PersistentVector}
  available-locales-vec
  "DEPRECATED: Please use `phone-number.locale/by-val-vec`."
  (vec all-locales))

(defn valid-locale?
  "DEPRECATED: Please use `phone-number.locale/valid?`."
  {:added "8.12.4-1" :deprecated "8.12.4-3" :tag Boolean}
  [^java.util.Locale locale-specification]
  (if (nil? locale-specification) true
      (try
        (contains? all-locales (l/locale locale-specification))
        (catch Throwable e false))))
