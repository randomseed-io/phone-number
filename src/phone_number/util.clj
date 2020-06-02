(ns

    ^{:doc    "Singleton wrappers and helpers for phone-number."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.util

  (:refer-clojure :exclude [short])

  (:import [com.google.i18n.phonenumbers
            PhoneNumberUtil
            ShortNumberInfo
            geocoding.PhoneNumberOfflineGeocoder
            PhoneNumberToCarrierMapper
            PhoneNumberToTimeZonesMapper
            NumberParseException]))

;; Singletons

(defn instance          {:tag PhoneNumberUtil,              :added  "8.12.4-0"} [] (PhoneNumberUtil/getInstance))
(defn short             {:tag ShortNumberInfo,              :added  "8.12.4-0"} [] (ShortNumberInfo/getInstance))
(defn geo-coder         {:tag PhoneNumberOfflineGeocoder,   :added  "8.12.4-0"} [] (PhoneNumberOfflineGeocoder/getInstance))
(defn carrier-mapper    {:tag PhoneNumberToCarrierMapper,   :added  "8.12.4-0"} [] (PhoneNumberToCarrierMapper/getInstance))
(defn time-zones-mapper {:tag PhoneNumberToTimeZonesMapper, :added  "8.12.4-0"} [] (PhoneNumberToTimeZonesMapper/getInstance))

;; Helpers

(defmacro try-parse
  "Evaluates body and if NumberParseException or NumberFormatException exception is
  caught it returns nil."
  {:added "8.12.4-0"}
  [& body]
  `(try ~@body
        (catch NumberParseException  e# nil)
        (catch NumberFormatException e# nil)))

(defmacro try-parse-or-false
  "Evaluates body and if NumberParseException or NumberFormatException exception is
  caught it returns false."
  {:added "8.12.4-0"}
  [& body]
  `(try (or (do ~@body) false)
        (catch NumberParseException  e# false)
        (catch NumberFormatException e# false)))

(defmacro try-null
  "Evaluates body and if NullPointerException exception is caught it returns nil."
  {:added "8.12.4-0"}
  [& body]
  `(try ~@body
        (catch NullPointerException  e# nil)))

(defn ns-infer
  {:added "8.12.4-0" :tag clojure.lang.Keyword}
  [^String ns-name
   ^clojure.lang.Keyword k]
  (if (simple-keyword? k) (keyword ns-name (name k)) k))

(defn fmap-k
  "For each key and value of the given map m calls a function passed as the second
  argument (passing successive keys during calls to it) and generates a map with
  values updated by the results returned by the function."
  {:added "8.12.4-0" :tag clojure.lang.IPersistentMap}
  [^clojure.lang.IFn f
   ^clojure.lang.IPersistentMap m]
  (into (empty m) (for [[k v] m] [k (f k)])))

(defn fmap-k
  "For each key and value of the given map m calls a function passed as the second
  argument (passing successive keys during calls to it) and generates a map with
  values updated by the results returned by the function."
  {:added "8.12.4-0" :tag clojure.lang.IPersistentMap}
  [^clojure.lang.IFn f
   ^clojure.lang.IPersistentMap m]
  (reduce-kv
   (fn [^clojure.lang.IPersistentMap mp k v]
     (assoc mp k (f k)))
   m m))

(defn remove-empty-vals
  "Removes empty values from a map."
  {:added "8.12.4-0" :tag clojure.lang.IPersistentMap}
  [^clojure.lang.IPersistentMap m]
  (reduce-kv
   (fn [^clojure.lang.IPersistentMap mp k v]
     (if (nil? v) (dissoc mp k) mp))
   m m))

(defn gen-is-sexp
  "For the given keyword k and function name f it generates predicate function
  definition code that compares the result of calling the function on a phone number
  with the keyword. A helper that is used in macros."
  {:added "8.12.4-0"}
  [^clojure.lang.Keyword k
   ^clojure.lang.Symbol  f]
  (let [fn-name (symbol (str "is-" (name k) "?"))]
    (list 'defn fn-name
          {:added (:added (meta (var gen-is-sexp))), :tag 'Boolean
           :doc (str "Returns true when " (name f) " is " k ", false otherwise.\n"
                     "  (Auto-generated by gen-is macro.)")}
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

(defn gen-digits
  "Generates the given number of random digits and converts all into a single string."
  {:added "8.12.4-0" :tag String}
  [^long num]
  (reduce str (repeatedly num #(unchecked-int (rand 10)))))

(defn lazy-iterator-seq
  {:added "8.12.4-0"
   :tag clojure.lang.LazySeq}
  ([^Iterable coll]
   (lazy-iterator-seq coll (.iterator coll)))
  ([^Iterable coll ^java.util.Iterator iter]
   (lazy-seq
    (when (.hasNext iter)
      (cons (.next iter) (lazy-iterator-seq coll iter))))))
