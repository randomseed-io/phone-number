(ns

    ^{:doc    "Public specs of phone-number library."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.spec

  (:require [phone-number.core          :as        phone]
            [phone-number.util          :as         util]
            [phone-number.type          :as         type]
            [phone-number.match         :as        match]
            [phone-number.format        :as       format]
            [phone-number.tz-format     :as    tz-format]
            [phone-number.region        :as       region]
            [phone-number.cost          :as         cost]
            [phone-number.net-code      :as     net-code]
            [phone-number.country-code  :as country-code]
            [phone-number.calling-code  :as calling-code]
            [clojure.spec.alpha         :as            s]
            [orchestra.spec.test        :as           st]
            [clojure.spec.gen.alpha     :as          gen])

  (:import  [com.google.i18n.phonenumbers
             Phonenumber$PhoneNumber
             NumberParseException]))

;;
;; Phone number region specs
;;

(s/def :phone-number/region
  (s/with-gen
    #(region/valid? % phone/*inferred-namespaces*)
    #(gen/elements region/all-vec)))

(s/def :phone-number.arg/region
  (s/with-gen
    #(region/valid? % phone/*inferred-namespaces*)
    #(gen/elements region/all-arg-vec)))

(defn random-region-code
  []
  (gen/generate (s/gen :phone-number/region)))

(defn random-region-code-arg
  []
  (gen/generate (s/gen :phone-number.arg/region)))

;;
;; Phone number type specs
;;

(s/def :phone-number/type
  (s/with-gen
    #(type/valid? % phone/*inferred-namespaces*)
    #(gen/elements type/all-vec)))

(s/def :phone-number.arg/type
  (s/with-gen
    #(type/valid-arg? % phone/*inferred-namespaces*)
    #(gen/elements type/all-arg-vec)))

;;
;; Phone number format specs
;;

(s/def :phone-number/format
  (s/with-gen
    #(format/valid? % phone/*inferred-namespaces*)
    #(gen/elements format/all-vec)))

(s/def :phone-number/format-global
  (s/with-gen
    #(format/valid? % phone/*inferred-namespaces*)
    #(gen/elements format/global-vec)))

(s/def :phone-number/format-regional
  (s/with-gen
    #(format/valid? % phone/*inferred-namespaces*)
    #(gen/elements format/regional-vec)))

(s/def :phone-number.arg/format           :phone-number/format)
(s/def :phone-number.arg/format-global    :phone-number/format-global)
(s/def :phone-number.arg/format-regional  :phone-number/format-regional)

(defn random-format
  [n]
  (phone/format n nil (gen/generate (s/gen :phone-number/format))))

(defn random-format-global
  [n]
  (phone/format n nil (gen/generate (s/gen :phone-number/format-global))))

(defn random-format-regional
  [n]
  (phone/format n nil (gen/generate (s/gen :phone-number/format-regional))))

;;
;; Phone number time zone format specs
;;

(s/def :phone-number/tz-format
  (s/with-gen
    #(tz-format/valid? % phone/*inferred-namespaces*)
    #(gen/elements tz-format/all-vec)))

(s/def :phone-number.arg/tz-format :phone-number/tz-format)

;;
;; Network calling code specs
;;

(s/def :phone-number/net-code
  (s/with-gen
    net-code/valid?
    #(gen/elements net-code/all-vec)))

(s/def :phone-number.arg/net-code :phone-number/net-code)

;;
;; Country calling code specs
;;

(s/def :phone-number/country-code
  (s/with-gen
    country-code/valid?
    #(gen/elements country-code/all-vec)))

(s/def :phone-number.arg/country-code :phone-number/country-code)

;;
;; Calling code specs
;;

(s/def :phone-number/calling-code
  (s/with-gen
    calling-code/valid?
    #(gen/elements calling-code/all-vec)))

(s/def :phone-number.arg/calling-code :phone-number/calling-code)

;;
;; Short phone number cost specs
;;

(s/def :phone-number/cost
  (s/with-gen
    #(cost/valid? % phone/*inferred-namespaces*)
    #(gen/elements cost/all-vec)))

(s/def :phone-number.arg/cost
  (s/with-gen
    #(cost/valid-arg? % phone/*inferred-namespaces*)
    #(gen/elements cost/all-arg-vec)))

;;
;; Phone number specs
;;

(defn phone-gen
  "Phone number generator for specs.
  Options map:
  :region, :type, :predicate, :retries, :min-digits, :locale, :random-seed, :early-shrinking :preserve-raw"
  {:added "8.12.4-1" :tag clojure.lang.Fn}
  [^clojure.lang.IPersistentMap options]
  (fn []
    (gen/fmap
     (fn [random-uuid]
       (:phone-number/number
        (phone/generate (:region          options)
                        (:type            options)
                        (:predicate       options)
                        (:retries         options 150)
                        (:min-digits      options 3)
                        (:locale          options)
                        (:random-seed     options (.getMostSignificantBits random-uuid))
                        (:early-shrinking options false)
                        (:preserve-raw    options true))))
     (gen/uuid))))

(s/def :phone-number/noraw-has-region
  (s/with-gen
    (s/and :phone-number/native-noraw
           :phone-number/has-region)
    (phone-gen {:preserve-raw false})))

(s/def :phone-number/noraw-has-calling-code
  (s/with-gen
    (s/and :phone-number/native-noraw
           :phone-number/has-calling-code)
    (phone-gen {:predicate phone/has-calling-code? :preserve-raw false})))

;;
;; Native form of a phone number
;;

(s/def :phone-number/native
  (s/with-gen
    (s/and phone/native?)
    (phone-gen {:early-shrinking true})))

(s/def :phone-number/native-with-raw
  (s/with-gen
    (s/and phone/native? phone/has-raw-input?)
    (phone-gen {:early-shrinking true :preserve-raw true})))

(s/def :phone-number/native-noraw
  (s/with-gen
    (s/and phone/native?
           (complement phone/has-raw-input?))
    (phone-gen {:early-shrinking true :preserve-raw false})))

(s/def :phone-number/native-valid
  (s/with-gen
    phone/valid?
    (phone-gen {:predicate phone/valid?})))

(s/def :phone-number/native-invalid
  (s/with-gen
    phone/invalid?
    (phone-gen {:predicate       phone/invalid?
                :retries         100
                :early-shrinking true})))

;;
;; String form of a phone number
;;

(defn begins-with-plus?
  "Helper that returns true if a string has the + character before any digit."
  {:added "8.12.4-1" :tag Boolean}
  [^String s]
  (and (some? s)
       (or (= \+ (first s)) ;; just to speed things up
           (->> s
                (take-while (complement phone/required-first-input-characters))
                (some #(= \+ %))))
       true))

(def ^{:added "8.12.4-1" :tag Boolean :arglists '[[^String s]]}
  not-begins-with-plus?
  "Complementary to begins-with-plus?"
  (complement begins-with-plus?))

(s/def :phone-number/string
  (s/with-gen
    (s/and string? phone/valid-input?)
    #(gen/fmap
      random-format
      (s/gen :phone-number/native))))

(s/def :phone-number/string-global
  (s/with-gen
    (s/and string?
           phone/valid-input?
           phone/has-calling-code?)
    #(gen/fmap
      random-format-global
      (s/gen :phone-number/has-calling-code))))

(s/def :phone-number/string-regional
  (s/with-gen
    (s/and string?
           phone/valid-input?
           not-begins-with-plus?)
    #(gen/fmap
      random-format-regional
      (s/gen :phone-number/native))))

(s/def :phone-number/string-valid
  (s/with-gen
    (s/and string? phone/valid?)
    #(gen/fmap
      random-format
      (s/gen :phone-number/native-valid))))

(s/def :phone-number/string-invalid
  (s/with-gen
    (s/and string? phone/invalid?)
    #(gen/fmap
      random-format
      (s/gen :phone-number/native-invalid))))

(s/def :phone-number/numeric
  (s/with-gen
    (s/and pos-int? phone/valid-input?)
    #(gen/fmap
      phone/numeric
      (s/gen :phone-number/native))))

(defmacro phone-spec
  "Template macro for constructing specs with generators for the given predicate. It
  uses the generate function from phone-number.core to support generation of samples
  that are meeting the criteria."
  ([f] (list 'phone-number.spec/phone-spec f {}))
  ([f options] `(let [f# ~f] (s/with-gen f# (phone-gen (merge {:predicate f#} ~options))))))

(s/def :phone-number/has-region       (phone-spec phone/has-region?))
(s/def :phone-number/has-calling-code (phone-spec phone/has-calling-code?))
(s/def :phone-number/has-location     (phone-spec phone/has-location?))
(s/def :phone-number/has-time-zone    (phone-spec phone/has-time-zone?))
(s/def :phone-number/has-known-type   (phone-spec phone/has-known-type?))

(s/def :phone-number/mobile           (phone-spec phone/is-mobile?       {:type :phone-number.type/mobile      }))
(s/def :phone-number/fixed-line       (phone-spec phone/is-fixed-line?   {:type :phone-number.type/fixed-line  }))
(s/def :phone-number/toll-free        (phone-spec phone/is-toll-free?    {:type :phone-number.type/toll-free   }))
(s/def :phone-number/premium-rate     (phone-spec phone/is-premium-rate? {:type :phone-number.type/premium-rate}))
(s/def :phone-number/shared-cost      (phone-spec phone/is-shared-cost?  {:type :phone-number.type/shared-cost }))
(s/def :phone-number/voip             (phone-spec phone/is-voip?         {:type :phone-number.type/voip        }))
(s/def :phone-number/personal         (phone-spec phone/is-personal?     {:type :phone-number.type/personal    }))
(s/def :phone-number/pager            (phone-spec phone/is-pager?        {:type :phone-number.type/pager       }))
(s/def :phone-number/uan              (phone-spec phone/is-uan?          {:type :phone-number.type/uan         }))
(s/def :phone-number/voicemail        (phone-spec phone/is-voicemail?    {:type :phone-number.type/voicemail   }))

(s/def :phone-number/fixed-line-or-mobile (phone-spec phone/is-fixed-line-or-mobile?))

(s/def :phone-number/uncertain-fixed-line-or-mobile (phone-spec phone/is-uncertain-fixed-line-or-mobile?
                                                                {:type :phone-number.type/fixed-line-or-mobile
                                                                 :early-shrinking true}))

(s/def :phone-number/possible         (phone-spec phone/possible?            {:early-shrinking true}))
(s/def :phone-number/impossible       (phone-spec phone/impossible?          {:early-shrinking true}))
(s/def :phone-number/unknown          (phone-spec phone/is-unknown?          {:early-shrinking true}))
(s/def :phone-number/maybe-mobile     (phone-spec phone/is-maybe-mobile?     {:early-shrinking true}))
(s/def :phone-number/maybe-fixed-line (phone-spec phone/is-maybe-fixed-line? {:early-shrinking true}))

;;
;; Short number specs
;;

(s/def :phone-number/short                  phone/short-valid?)
(s/def :phone-number/maybe-short            phone/short-possible?)
(s/def :phone-number.short/valid            phone/short-valid?)
(s/def :phone-number.short/invalid          phone/short-invalid?)
(s/def :phone-number.short/possible         phone/short-possible?)
(s/def :phone-number.short/carrier-specific phone/short-carrier-specific?)

;;
;; Info map spec and props
;;

(s/def :phone-number.prop/format string?)

(s/def :phone-number.tz-format/timezones (s/coll-of string? :distinct true :min-count 1))

(s/def :phone-number/valid?                        boolean?)
(s/def :phone-number/possible?                     boolean?)
(s/def :phone-number.short/valid?                  boolean?)
(s/def :phone-number.short/possible?               boolean?)
(s/def :phone-number.short/emergency?              (s/nilable boolean?))
(s/def :phone-number.short/to-emergency?           (s/nilable boolean?))
(s/def :phone-number.short/sms-service?            boolean?)
(s/def :phone-number.short/carrier-specific?       boolean?)
(s/def :phone-number.short/dialing-region-derived? boolean?)
(s/def :phone-number.short/dialing-region          (s/nilable :phone-number/region))
(s/def :phone-number/valid-for-region?             boolean?)
(s/def :phone-number/geographical?                 boolean?)
(s/def :phone-number/dialing-region-derived?       boolean?)
(s/def :phone-number/location                      (s/nilable string?))
(s/def :phone-number/carrier                       (s/nilable string?))
(s/def :phone-number/dialing-region                (s/nilable :phone-number/region))
(s/def :phone-number.format/e164                   string?)
(s/def :phone-number.format/international          string?)
(s/def :phone-number.format/national               string?)
(s/def :phone-number.format/rfc3966                string?)
(s/def :phone-number.format/raw-input              string?)
(s/def :phone-number.tz-format/full-global         :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/short-global        :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/narrow-global       :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/full                :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/short               :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/narrow              :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/id                  :phone-number.tz-format/timezones)

(def short-info-keys
  (s/keys :req [:phone-number.short/valid?
                :phone-number.short/possible?]
          :opt [:phone-number.short/dialing-region
                :phone-number.short/dialing-region-derived?
                :phone-number.short/carrier-specific?
                :phone-number.short/cost
                :phone-number.short/emergency?
                :phone-number.short/sms-service?
                :phone-number.short/to-emergency?]))

(def info-keys
  (s/keys :req [:phone-number/calling-code
                :phone-number/geographical?
                :phone-number/possible?
                :phone-number/valid?
                :phone-number/type
                :phone-number.short/valid?
                :phone-number.short/possible?]
          :opt [:phone-number/region
                :phone-number/location
                :phone-number/carrier
                :phone-number/dialing-region
                :phone-number/dialing-region-derived?
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
                :phone-number.short/dialing-region
                :phone-number.short/dialing-region-derived?
                :phone-number.short/carrier-specific?
                :phone-number.short/cost
                :phone-number.short/emergency?
                :phone-number.short/sms-service?
                :phone-number.short/to-emergency?]))

(s/def :phone-number/info
  (s/with-gen
    info-keys
    #(gen/fmap
      phone/info
      (s/gen :phone-number/native))))

(s/def :phone-number/info-valid
  (s/with-gen
    info-keys
    #(gen/fmap
      phone/info
      (s/gen :phone-number/native-valid))))

(s/def :phone-number/info-invalid
  (s/with-gen
    info-keys
    #(gen/fmap
      phone/info
      (s/gen :phone-number/native-invalid))))

(s/def :phone-number.short/info
  (s/with-gen
    short-info-keys
    #(gen/fmap
      phone/short-info
      (s/gen :phone-number/native))))

(s/def :phone-number.arg/map                :phone-number/info) ;; TODO
(s/def :phone-number.arg/map-regional       :phone-number/info)
(s/def :phone-number.arg/map-global         :phone-number/info)

(s/def :phone-number.arg/number-global
  (s/with-gen
    (s/alt :native :phone-number/native
           :map    :phone-number.arg/map-global
           :string :phone-number/string-global
           :nil    nil?)
    #(gen/fmap
      (juxt (gen/generate (gen/elements [random-format-global
                                         phone/info
                                         identity
                                         (constantly nil)])))
      (s/gen :phone-number/has-calling-code))))

(s/def :phone-number.arg/number-regional
  (s/with-gen
    (s/alt :numeric :phone-number/numeric
           :map     :phone-number.arg/map-regional
           :string  :phone-number/string-regional)
    #(gen/fmap
      (juxt (gen/generate (gen/elements [random-format-regional
                                         phone/info
                                         phone/numeric])))
      (s/gen :phone-number/has-region))))

(s/def :phone-number.arg/locale
  (s/with-gen
    (s/nilable util/valid-locale?)
    #(gen/fmap
      (gen/generate (gen/elements [identity
                                   (fn [l] (.getLanguage l))
                                   (comp keyword (fn [l] (.getLanguage l)))
                                   (constantly nil)]))
      (gen/elements util/available-locales-vec))))

;; Please note that a standalone regional number will be invalid
;; This is just for abstract testing.

(s/def :phone-number.arg/number
  (s/or :regional :phone-number.arg/number-regional
        :global   :phone-number.arg/number-global))

;;
;; Argument tuples which validity can be checked only when tested together
;; (mainly used in fdefs).
;;

(s/def :phone-number.args/native.region
  (s/with-gen
    (s/cat :phone-number :phone-number/native
           :region-code  (s/nilable :phone-number.arg/region))
    #(gen/fmap
      (juxt identity phone/region)
      (s/gen :phone-number/has-region))))

(s/def :phone-number.args/string-regional.region
  (s/with-gen
    (s/cat :phone-number :phone-number/string-regional
           :region-code  :phone-number.arg/region)
    #(gen/fmap
      (juxt random-format-regional phone/region)
      (s/gen :phone-number/has-region))))

(s/def :phone-number.args/string-global.region
  (s/with-gen
    (s/cat :phone-number :phone-number/string-global
           :region-code  (s/nilable :phone-number.arg/region))
    #(gen/fmap
      (juxt random-format-global phone/region)
      (s/gen :phone-number/has-calling-code))))

(s/def :phone-number.args/numeric.region
  (s/with-gen
    (s/cat :phone-number :phone-number/numeric
           :region-code  :phone-number.arg/region)
    #(gen/fmap
      (juxt phone/numeric phone/region)
      (s/gen :phone-number/has-region))))

(s/def :phone-number.args/map.region ;; TODO: regional-map
  (s/with-gen
    (s/cat :phone-number :phone-number.arg/map
           :region-code  :phone-number.arg/region)
    #(gen/fmap
      (juxt phone/info phone/region)
      (s/gen :phone-number/has-region))))

(s/def :phone-number.args/map-regional.region
  (s/with-gen
    (s/cat :phone-number :phone-number.arg/map-regional
           :region-code  :phone-number.arg/region)
    #(gen/fmap
      (juxt phone/info phone/region)
      (s/gen :phone-number/has-region))))

(s/def :phone-number.args/map-global.region
  (s/with-gen
    (s/cat :phone-number :phone-number.arg/map-global
           :region-code  (s/nilable :phone-number.arg/region))
    #(gen/fmap
      (juxt phone/info phone/region)
      (s/gen :phone-number/has-calling-code))))

(s/def :phone-number.args/nil.region
  (s/with-gen
    (s/cat :phone-number nil?
           :region-code (s/nilable :phone-number.arg/region))
    #(gen/fmap
      (juxt (constantly nil) phone/region)
      (s/gen :phone-number/has-region))))

(s/def :phone-number.args/number-regional.region
  (s/alt :numeric :phone-number.args/numeric.region
         :string  :phone-number.args/string-regional.region
         :map     :phone-number.args/map-regional.region))

(s/def :phone-number.args/number-global.region
  (s/alt :native :phone-number.args/native.region
         :string :phone-number.args/string-global.region
         :map    :phone-number.args/map-global.region
         :nil    :phone-number.args/nil.region))

(s/def :phone-number.args/number.region
  (s/or :nil      :phone-number.args/nil.region
        :regional :phone-number.args/number-regional.region
        :global   :phone-number.args/number-global.region))

(s/def :phone-number.args/number
  (s/or :nil        (s/cat :phone-number nil? :region-code (s/? (s/nilable :phone-number.arg/region)))
        :arity-2    :phone-number.args/number.region
        :arity-1    :phone-number.arg/number-global))

(s/def :phone-number.args/format
  (s/or :nil        (s/cat :phone-number nil? :region-code (s/? (s/nilable :phone-number.arg/region)))
        :arity-3-nr (s/cat :number-regional.region :phone-number.args/number-regional.region :format (s/nilable :phone-number.arg/format))
        :arity-2-nr :phone-number.args/number-regional.region
        :arity-3-ng (s/cat :number-global.region :phone-number.args/number-global.region :format (s/nilable :phone-number.arg/format))
        :arity-2-ng :phone-number.args/number-global.region
        :arity-1    :phone-number.arg/number-global))

(s/def :phone-number.args/number+dialing-region
  (s/or :nil        (s/cat :phone-number           nil?
                           :region-code            (s/nilable :phone-number.arg/region)
                           :dialing-region          :phone-number.arg/region)
        :arity-3-nr (s/cat :number-regional.region :phone-number.args/number-regional.region
                           :dialing-region          :phone-number.arg/region)
        :arity-3-ng (s/cat :number-global.region   :phone-number.args/number-global.region
                           :dialing-region          :phone-number.arg/region)))

(s/def :phone-number.args/number+opt-dialing-region
  (s/or :nil        (s/cat :phone-number           nil?
                           :region-code            (s/nilable :phone-number.arg/region)
                           :dialing-region          :phone-number.arg/region)
        :arity-3-nr (s/cat :number-regional.region :phone-number.args/number-regional.region
                           :dialing-region          (s/nilable :phone-number.arg/region))
        :arity-2-nr :phone-number.args/number-regional.region
        :arity-3-ng (s/cat :number-global.region   :phone-number.args/number-global.region
                           :dialing-region          (s/nilable :phone-number.arg/region))
        :arity-2-ng :phone-number.args/number-global.region
        :arity-1-ng :phone-number.arg/number-global))

(s/def :phone-number.args/number+locale
  (s/or :nil        (s/cat :phone-number           nil?
                           :region-code            (s/? (s/nilable :phone-number.arg/region)))
        :arity-3-nr (s/cat :number-regional.region :phone-number.args/number-regional.region
                           :locale                 (s/nilable :phone-number.arg/locale))
        :arity-2-nr :phone-number.args/number-regional.region
        :arity-3-ng (s/cat :number-global.region   :phone-number.args/number-global.region
                           :locale                 (s/nilable :phone-number.arg/locale))
        :arity-2-ng :phone-number.args/number-global.region
        :arity-1    :phone-number.arg/number-global))

(s/def :phone-number.args/number+tz+locale
  (s/or :nil        (s/cat :phone-number nil? :region-code (s/? (s/nilable :phone-number.arg/region)))
        :arity-4-nr (s/cat :number-regional.region :phone-number.args/number-regional.region
                           :locale                 (s/nilable :phone-number.arg/locale)
                           :format                 (s/nilable :phone-number.arg/tz-format))
        :arity-3-nr (s/cat :number-regional.region :phone-number.args/number-regional.region
                           :format                 (s/nilable :phone-number.arg/tz-format))
        :arity-2-nr :phone-number.args/number-regional.region
        :arity-4-ng (s/cat :number-global.region   :phone-number.args/number-global.region
                           :locale                 (s/nilable :phone-number.arg/locale)
                           :format                 (s/nilable :phone-number.arg/tz-format))
        :arity-3-ng (s/cat :number-global.region   :phone-number.args/number-global.region
                           :format                 (s/nilable :phone-number.arg/tz-format))
        :arity-2-ng :phone-number.args/number-global.region
        :arity-1    :phone-number.arg/number-global))

(s/def :phone-number.args/number+locale+dialing-region
  (s/or :nil        (s/cat :phone-number nil? :region-code (s/? (s/nilable :phone-number.arg/region)))
        :arity-4-nr (s/cat :number-regional.region :phone-number.args/number-regional.region
                           :locale                 (s/nilable :phone-number.arg/locale)
                           :dialing-region          (s/nilable :phone-number.arg/region))
        :arity-3-nr (s/cat :number-regional.region :phone-number.args/number-regional.region
                           :dialing-region          (s/nilable :phone-number.arg/dialing-region))
        :arity-2-nr :phone-number.args/number-regional.region
        :arity-4-ng (s/cat :number-global.region   :phone-number.args/number-global.region
                           :locale                 (s/nilable :phone-number.arg/locale)
                           :dialing-region          (s/nilable :phone-number.arg/region))
        :arity-3-ng (s/cat :number-global.region   :phone-number.args/number-global.region
                           :dialing-region          (s/nilable :phone-number.arg/region))
        :arity-2-ng :phone-number.args/number-global.region
        :arity-1    :phone-number.arg/number-global))

;;
;; Function specs (in progress)
;;

;; Phoneable protocol

(s/fdef phone/native?
  :args (s/cat :object any?)
  :ret  boolean?
  :fn   (s/or :exact (s/and #(:ret %) #(= Phonenumber$PhoneNumber    (class (-> % :args :object))))
              :other                  #(not= Phonenumber$PhoneNumber (class (-> % :args :object)))))

(s/fdef phone/valid-input?
  :args (s/cat :number any?)
  :ret   boolean?)

(s/fdef phone/valid?
  :args :phone-number.args/number+opt-dialing-region
  :ret  boolean?)

(s/fdef phone/number
  :args :phone-number.args/number
  :ret  (s/nilable :phone-number/native-with-raw))

(s/fdef phone/number-noraw
  :args :phone-number.args/number
  :ret  (s/nilable :phone-number/native))

(s/fdef phone/raw-input
  :args :phone-number.args/number
  :ret  (s/nilable :phone-number/string))

;;
;; Core functions
;;

(s/fdef phone/numeric
  :args :phone-number.args/number
  :ret  (s/nilable :phone-number/numeric))

(s/fdef phone/calling-code
  :args :phone-number.args/number
  :ret  (s/nilable :phone-number/calling-code))

(s/fdef phone/location
  :args :phone-number.args/number+locale
  :ret  :phone-number/location)

(s/fdef phone/carrier
  :args :phone-number.args/number+locale
  :ret  :phone-number/carrier)

(s/fdef phone/format
  :args :phone-number.args/format
  :ret  (s/nilable :phone-number.prop/format))

(s/fdef phone/time-zones
  :args :phone-number.args/number+tz+locale
  :ret  (s/nilable (s/coll-of string?
                              :distrinct true
                              :min-count 1)))

(s/fdef phone/time-zones-all-formats
  :args :phone-number.args/number+locale
  :ret  (s/nilable (s/map-of :phone-number/tz-format
                             (s/coll-of string? :distinct true :min-count 1)
                             :conform-keys true
                             :count (count tz-format/all))))

(s/fdef phone/all-formats
  :args :phone-number.args/number
  :ret  (s/nilable (s/map-of :phone-number/format
                             :phone-number/string
                             :conform-keys true
                             :count (count format/all))))

(s/fdef phone/region
  :args :phone-number.args/number
  :ret  (s/nilable :phone-number/region))

(s/fdef phone/type
  :args :phone-number.args/number
  :ret  (s/nilable :phone-number/type))

(s/fdef phone/short-cost
  :args :phone-number.args/number+opt-dialing-region
  :ret  (s/nilable :phone-number/cost))

(s/fdef phone/has-raw-input?           :args :phone-number.args/number :ret boolean?)
(s/fdef phone/has-calling-code?        :args :phone-number.args/number :ret boolean?)
(s/fdef phone/has-location?            :args :phone-number.args/number :ret boolean?)
(s/fdef phone/has-time-zone?           :args :phone-number.args/number :ret boolean?)
(s/fdef phone/has-known-type?          :args :phone-number.args/number :ret boolean?)
(s/fdef phone/is-maybe-fixed-line?     :args :phone-number.args/number :ret boolean?)
(s/fdef phone/is-maybe-mobile?         :args :phone-number.args/number :ret boolean?)
(s/fdef phone/is-fixed-line-or-mobile? :args :phone-number.args/number :ret boolean?)
(s/fdef phone/possible?                :args :phone-number.args/number :ret :phone-number/possible?)
(s/fdef phone/impossible?              :args :phone-number.args/number :ret :phone-number/possible?)
(s/fdef phone/invalid?                 :args :phone-number.args/number :ret :phone-number/valid?)
(s/fdef phone/geographical?            :args :phone-number.args/number :ret :phone-number/geographical?)

(s/fdef phone/is-uncertain-fixed-line-or-mobile?
  :args :phone-number.args/number
  :ret  boolean?)

(s/fdef phone/valid-for-region?
  :args :phone-number.args/number+dialing-region
  :ret :phone-number/valid-for-region?)

(s/fdef phone/short-to-emergency?
  :args (s/or :regional :phone-number.args/string-regional.region
              :global   :phone-number.args/string-global.region)
  :ret  :phone-number.short/to-emergency?)

(s/fdef phone/short-emergency?
  :args (s/or :regional :phone-number.args/string-regional.region
              :global   :phone-number.args/string-global.region)
  :ret  :phone-number.short/emergency?)

(s/fdef phone/short-possible?
  :args :phone-number.args/number+opt-dialing-region
  :ret  :phone-number.short/possible?)

(s/fdef phone/short-valid?
  :args :phone-number.args/number+opt-dialing-region
  :ret  :phone-number.short/valid?)

(s/fdef phone/short-carrier-specific?
  :args :phone-number.args/number+opt-dialing-region
  :ret  :phone-number.short/carrier-specific?)

(s/fdef phone/short-sms-service?
  :args :phone-number.args/number+opt-dialing-region
  :ret  :phone-number.short/sms-service?)

(s/fdef phone/short-info
  :args :phone-number.args/number+opt-dialing-region
  :ret  (s/nilable :phone-number.short/info))
