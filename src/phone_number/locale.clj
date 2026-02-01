(ns

    ^{:doc    "Locale support for phone-number"
      :author "Paweł Wilk"
      :added  "8.12.4-3"}

    phone-number.locale

  (:require [clojure.set]
            [phone-number.util         :as util]
            [trptr.java-wrapper.locale :as    l])

  (:import (com.google.i18n.phonenumbers PhoneNumberUtil)))

(def ^{:added "8.12.4-3"
       :const true
       :tag   clojure.lang.Keyword}
  default ::default)

(def ^{:added "8.12.4-3"
       :tag java.util.Locale}
  default-val (l/locale nil))

(def ^{:added "8.12.4-3"
       :tag clojure.lang.PersistentArrayMap}
  all
  "Map of locales (keywords) to Locale values."
  (assoc
   (into #::{}
         (map
          (juxt #(keyword "phone-number.locale" (str %)) identity)
          (filter #(seq (str %))
                  l/available-locales)))
   ::default
   default-val))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentArrayMap}
  all-arg
  "Map of locales (keywords) to Locale values suitable to be passed as arguments."
  all)

(def ^{:added "8.12.4-3"
       :tag clojure.lang.PersistentArrayMap}
  by-val
  "Map of Locale values to locales (keywords)."
  (clojure.set/map-invert all))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentArrayMap}
  by-val-arg
  "Map of Locale values to locales (keywords) suitable to be passed as method
  arguments."
  by-val)


(def ^{:added "8.12.4-3"
       :tag clojure.lang.PersistentHashSet}
  available
  "Set of available locale (keywords)."
  (set (keys all)))

(def ^{:added "8.12.4-3"
       :tag clojure.lang.PersistentVector}
  all-vec
  "Vector of locales (keywords)."
  (vec (keys all)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  all-arg-vec
  "Vector of locales (keywords) suitable to be used as arguments."
  all-vec)

(def ^{:added "8.12.4-3"
       :tag clojure.lang.PersistentVector}
  by-val-vec
  "Vector of locales (Locale values)."
  (vec (keys by-val)))

(def ^{:added "8.12.16-1"
       :tag clojure.lang.PersistentVector}
  by-val-arg-vec
  "Vector of locales (Locale values) suitable to be used as method arguments."
  by-val-vec)

(defn valid-arg?
  "Returns `true` if the given locale specification is valid and supported, `false`
  otherwise. For `nil` it returns `true` assuming it will be a default, system
  locale. If `strict` flag is set then for nil value it returns `false` and for
  keywords it only checks if they are in the locale
  map (`phone-number.locale/all`)."
  {:added "8.12.4-3" :tag Boolean}
  ([^java.util.Locale locale-specification]
   (valid-arg? locale-specification true false))
  ([^java.util.Locale locale-specification
    use-infer]
   (if (nil? locale-specification) true
       (or
        (and (keyword? locale-specification)
             (contains? all (util/ns-infer "phone-number.locale" locale-specification use-infer)))
        (try (contains? by-val (l/locale locale-specification)) (catch Throwable _e false)))))
  ([^java.util.Locale locale-specification
    use-infer
    strict]
   (if-not strict
     (valid-arg? locale-specification use-infer)
     (if (nil? locale-specification) false
         (if (keyword? locale-specification)
           (contains? all (util/ns-infer "phone-number.locale" locale-specification use-infer))
           (try (contains? by-val (l/locale locale-specification)) (catch Throwable _e false)))))))

(defn strictly-valid-arg?
  "Returns `true` if the given locale specification is valid and supported, `false`
  otherwise. For `nil` it returns `false` and for keywords it only checks if they are
  in the locale map (`phone-number.locale/all`). If the key is not there, it returns
  `false`. Namespace inference is supported using the second argument (the default is
  not to infer)."
  {:added "8.12.4-3" :tag Boolean}
  ([^java.util.Locale locale-specification]
   (valid-arg? locale-specification false true))
  ([^java.util.Locale locale-specification
    ^Boolean          use-infer]
   (valid-arg? locale-specification use-infer true)))

(def ^{:added "8.12.16-1" :tag Boolean
       :arglists '([^java.util.Locale locale-specification]
                   [^java.util.Locale locale-specification
                    ^Boolean          use-infer]
                   [^java.util.Locale locale-specification
                    ^Boolean          use-infer
                    ^Boolean          strict])}
  valid?
  "Returns `true` if the given locale specification is valid and supported, `false`
  otherwise. For `nil` it returns `true` assuming it will be a default, system
  locale. If `strict` flag is set then for nil value it returns `false` and for
  keywords it only checks if they are in the locale map (`phone-number.locale/all`)."
  valid-arg?)

(def ^{:added "8.12.16-1" :tag Boolean
       :arglists '([^java.util.Locale locale-specification]
                   [^java.util.Locale locale-specification
                    ^Boolean          use-infer])}
  strictly-valid?
  "Returns `true` if the given locale specification is valid and supported, `false`
  otherwise. For `nil` it returns `false` and for keywords it only checks if they are
  in the locale map (`phone-number.locale/all`). If the key is not there, it returns
  `false`. Namespace inference is supported using the second argument (the default is
  not to infer)."
  strictly-valid-arg?)

(defn parse
  "Parses locale. If it is a `java.util.Locale` object it returns it. If it's not
  keyword then it parses it by calling `trptr.java-wrapper.locale/locale`. If it is a
  keyword it first tries to infer a namespace (if not present and the `use-infer` is
  set to some truthy value) and then looks it up in locales map. If the value is
  found, returns the associated `Locale` object. Otherwise it falls back to getting
  locale using the function from `trptr.java-wrapper.locale/locale` (before doing so
  it strips any namespace from a keyword, if found)."
  {:added "8.12.4-3" :tag java.util.Locale}
  ([^java.util.Locale locale-specification]
   (parse locale-specification true))
  ([^java.util.Locale locale-specification
    ^Boolean          use-infer]
   (if (instance? java.util.Locale locale-specification) locale-specification
       (if-not (keyword? locale-specification) (l/locale locale-specification)
               (if-some [lready (all-arg (util/ns-infer "phone-number.locale" locale-specification use-infer))]
                 lready
                 (l/locale locale-specification))))))

(defn generate-sample
  "Generates random locale."
  {:added "8.12.4-3" :tag clojure.lang.Keyword}
  ([] (rand-nth all-vec))
  ([^java.util.Random rng] (util/get-rand-nth all-vec rng)))

(defn generate-sample-val
  "Generates random locale (string value)."
  {:added "8.12.4-3" :tag String}
  ([] (rand-nth by-val-vec))
  ([^java.util.Random rng] (util/get-rand-nth by-val-vec rng)))
