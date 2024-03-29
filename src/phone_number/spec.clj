(ns

    ^{:doc    "Public specs of phone-number library."
      :author "Paweł Wilk"
      :added  "8.12.4-0"
      :no-doc true}

    phone-number.spec

  (:require [phone-number                 :as             pn]
            [phone-number.core            :as          phone]
            [phone-number.util            :as           util]
            [phone-number.type            :as           type]
            [phone-number.match           :as          match]
            [phone-number.format          :as         format]
            [phone-number.locale          :as         locale]
            [phone-number.leniency        :as       leniency]
            [phone-number.tz-format       :as      tz-format]
            [phone-number.region          :as         region]
            [phone-number.cost            :as           cost]
            [phone-number.net-code        :as       net-code]
            [phone-number.country-code    :as   country-code]
            [phone-number.calling-code    :as   calling-code]
            [phone-number.short           :as          short]
            [phone-number.sample          :as         sample]
            [phone-number.dialing-region  :as dialing-region]
            [clojure.spec.alpha           :as              s]
            [clojure.spec.gen.alpha       :as            gen])

  (:import  [com.google.i18n.phonenumbers
             Phonenumber$PhoneNumber
             NumberParseException]
            [java.util UUID Random Locale]))

;;
;; Namespaces for easy use of keywords
;;

(alias 'arg   (create-ns 'phone-number.arg))   ;; for internal arg specs
(alias 'args  (create-ns 'phone-number.args))  ;; for internal args specs
(alias 'prop  (create-ns 'phone-number.prop))  ;; for additional properties

(alias 'input                (create-ns 'phone-number.input))           ;; in for public input specs
(alias 'input-short          (create-ns 'phone-number.input.short))     ;; in for public input specs
(alias 'input-tz-format      (create-ns 'phone-number.input.tz-format)) ;; in for public input specs
(alias 'input-format         (create-ns 'phone-number.input.format))
(alias 'input-dialing-region (create-ns 'phone-number.input.dialing-region))
(alias 'input-region         (create-ns 'phone-number.input.region))
(alias 'input-country-code   (create-ns 'phone-number.input.country-code))
(alias 'input-calling-code   (create-ns 'phone-number.input.calling-code))

;;
;; Phone number region specs
;;

(s/def ::pn/region                    ; generic spec (includes defaults and unknowns)
  (s/with-gen
    #(region/valid? % phone/*inferred-namespaces*)
    #(gen/elements region/all-vec)))

(s/def ::arg/region ; argument spec (only data valid as argument)
  (s/with-gen
    #(region/valid-arg? % phone/*inferred-namespaces*)
    #(gen/elements region/all-arg-vec)))

(s/def ::input/region ; public spec (validates input)
  ::arg/region)

(defn random-region-code
  []
  (gen/generate (s/gen ::pn/region)))

(defn random-region-code-arg
  []
  (gen/generate (s/gen ::arg/region)))

;;
;; Phone number type specs
;;

(s/def ::pn/type
  (s/with-gen
    #(type/valid? % phone/*inferred-namespaces*)
    #(gen/elements type/all-vec)))

(s/def ::arg/type
  (s/with-gen
    #(type/valid-arg? % phone/*inferred-namespaces*)
    #(gen/elements type/all-arg-vec)))

(s/def ::input/type ::arg/type)

;;
;; Phone number format specs
;;

(s/def ::pn/format
  (s/with-gen
    #(format/valid? % phone/*inferred-namespaces*)
    #(gen/elements format/all-vec)))

(s/def ::pn/format-global
  (s/with-gen
    #(format/valid? % phone/*inferred-namespaces*)
    #(gen/elements format/global-vec)))

(s/def ::pn/format-regional
  (s/with-gen
    #(format/valid? % phone/*inferred-namespaces*)
    #(gen/elements format/regional-vec)))

(s/def ::arg/format           ::pn/format)
(s/def ::arg/format-global    ::pn/format-global)
(s/def ::arg/format-regional  ::pn/format-regional)

(s/def ::input/format          ::arg/format)
(s/def ::input/format-global   ::arg/format-global)
(s/def ::input/format-regional ::arg/format-regional)

(defn random-format
  [n]
  (phone/format n nil (gen/generate (s/gen ::pn/format))))

(defn random-format-global
  [n]
  (phone/format n nil (gen/generate (s/gen ::pn/format-global))))

(defn random-format-regional
  [n]
  (phone/format n nil (gen/generate (s/gen ::pn/format-regional))))

;;
;; Phone number time zone format specs
;;

(s/def ::pn/tz-format
  (s/with-gen
    #(tz-format/valid? % phone/*inferred-namespaces*)
    #(gen/elements tz-format/all-vec)))

(s/def ::arg/tz-format
  (s/with-gen
    #(tz-format/valid-arg? % phone/*inferred-namespaces*)
    #(gen/elements tz-format/all-arg-vec)))

(s/def ::input/tz-format ::arg/tz-format)

;;
;; Phone number leniency specs
;;

(s/def ::pn/leniency
  (s/with-gen
    #(leniency/valid? % phone/*inferred-namespaces*)
    #(gen/elements leniency/all-vec)))

(s/def ::arg/leniency
  (s/with-gen
    #(leniency/valid-arg? % phone/*inferred-namespaces*)
    #(gen/elements leniency/all-arg-vec)))

(s/def ::input/leniency ::arg/leniency)

;;
;; Network calling code specs
;;

(s/def ::pn/net-code
  (s/with-gen
    net-code/valid?
    #(gen/elements net-code/all-vec)))

(s/def ::arg/net-code
  (s/with-gen
    net-code/valid-arg?
    #(gen/elements net-code/all-arg-vec)))

(s/def ::input/net-code ::arg/net-code)

;;
;; Country calling code specs
;;

(s/def ::pn/country-code
  (s/with-gen
    country-code/valid?
    #(gen/elements country-code/all-vec)))

(s/def ::arg/country-code
  (s/with-gen
    country-code/valid-arg?
    #(gen/elements country-code/all-arg-vec)))

(s/def ::input/country-code ::arg/country-code)

;;
;; Calling code specs
;;

(s/def ::pn/calling-code
  (s/with-gen
    calling-code/valid?
    #(gen/elements calling-code/all-vec)))

(s/def ::arg/calling-code
  (s/with-gen
    calling-code/valid-arg?
    #(gen/elements calling-code/all-arg-vec)))

(s/def ::input/calling-code ::arg/calling-code)

;;
;; Short phone number cost specs
;;

(s/def ::pn/cost
  (s/with-gen
    #(cost/valid? % phone/*inferred-namespaces*)
    #(gen/elements cost/all-vec)))

(s/def ::arg/cost
  (s/with-gen
    #(cost/valid-arg? % phone/*inferred-namespaces*)
    #(gen/elements cost/all-arg-vec)))

(s/def ::input/cost ::arg/cost)

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
       (::pn/number
        (phone/generate (:region          options)
                        (:type            options)
                        (:predicate       options)
                        (:retries         options 150)
                        (:min-digits      options 3)
                        (:locale          options)
                        (:random-seed     options (.getMostSignificantBits ^UUID random-uuid))
                        (:early-shrinking options false)
                        (:preserve-raw    options true))))
     (gen/uuid))))

(s/def ::pn/noraw-has-region
  (s/with-gen
    (s/and ::pn/native-noraw
           ::pn/has-region)
    (phone-gen {:preserve-raw false})))

(s/def ::pn/noraw-has-calling-code
  (s/with-gen
    (s/and ::pn/native-noraw
           ::pn/has-calling-code)
    (phone-gen {:predicate phone/has-calling-code? :preserve-raw false})))

(s/def ::input/noraw-has-region       ::pn/noraw-has-region)
(s/def ::input/noraw-has-calling-code ::pn/noraw-has-calling-code)

;;
;; Native form of a phone number
;;

(s/def ::pn/native
  (s/with-gen
    (s/and phone/native?)
    (phone-gen {:early-shrinking true})))

(s/def ::pn/native-with-raw
  (s/with-gen
    (s/and phone/native? phone/has-raw-input?)
    (phone-gen {:early-shrinking true :preserve-raw true})))

(s/def ::pn/native-noraw
  (s/with-gen
    (s/and phone/native?
           (complement phone/has-raw-input?))
    (phone-gen {:early-shrinking true :preserve-raw false})))

(s/def ::pn/native-valid
  (s/with-gen
    phone/valid?
    (phone-gen {:predicate phone/valid?})))

(s/def ::pn/native-invalid
  (s/with-gen
    phone/invalid?
    (phone-gen {:predicate       phone/invalid?
                :retries         100
                :early-shrinking true})))

(s/def ::arg/native            ::pn/native)
(s/def ::arg/native-noraw      ::pn/native-noraw)
(s/def ::arg/native-with-raw   ::pn/native-with-raw)
(s/def ::arg/native-valid      ::pn/native-with-raw)
(s/def ::arg/native-invalid    ::pn/native-with-raw)

(s/def ::input/native          ::arg/native)
(s/def ::input/native-noraw    ::arg/native-noraw)
(s/def ::input/native-with-raw ::arg/native-with-raw)
(s/def ::input/native-valid    ::arg/native-with-raw)
(s/def ::input/native-invalid  ::arg/native-with-raw)

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

(s/def ::pn/string
  (s/with-gen
    (s/and string? phone/valid-input?)
    #(gen/fmap
      random-format
      (s/gen ::pn/native))))

(s/def ::pn/string-global
  (s/with-gen
    (s/and string?
           phone/valid-input?
           phone/has-calling-code?)
    #(gen/fmap
      random-format-global
      (s/gen ::pn/has-calling-code))))

(s/def ::pn/string-regional
  (s/with-gen
    (s/and string?
           phone/valid-input?
           not-begins-with-plus?)
    #(gen/fmap
      random-format-regional
      (s/gen ::pn/native))))

(s/def ::pn/string-valid
  (s/with-gen
    (s/and string? phone/valid?)
    #(gen/fmap
      random-format
      (s/gen ::pn/native-valid))))

(s/def ::pn/string-invalid
  (s/with-gen
    (s/and string? phone/invalid?)
    #(gen/fmap
      random-format
      (s/gen ::pn/native-invalid))))

(s/def ::arg/string            ::pn/string)
(s/def ::arg/string-global     ::pn/string-global)
(s/def ::arg/string-regional   ::pn/string-regional)
(s/def ::arg/string-valid      ::pn/string-valid)
(s/def ::arg/string-invalid    ::pn/string-invalid)

(s/def ::input/string          ::arg/string)
(s/def ::input/string-global   ::arg/string-global)
(s/def ::input/string-regional ::arg/string-regional)
(s/def ::input/string-valid    ::arg/string-valid)
(s/def ::input/string-invalid  ::arg/string-invalid)

(s/def ::pn/numeric
  (s/with-gen
    (s/and pos-int? phone/valid-input?)
    #(gen/fmap
      phone/numeric
      (s/gen ::pn/native))))

(s/def ::input/numeric ::pn/numeric)

(defmacro phone-spec
  "Template macro for constructing specs with generators for the given predicate. It
  uses the generate function from phone-number.core to support generation of samples
  that are meeting the criteria."
  ([f] (list 'phone-number.spec/phone-spec f {}))
  ([f options] `(let [f# ~f] (s/with-gen f# (phone-gen (merge {:predicate f#} ~options))))))

(s/def ::pn/has-region       (phone-spec phone/has-region?))
(s/def ::pn/has-calling-code (phone-spec phone/has-calling-code?))
(s/def ::pn/has-location     (phone-spec phone/has-location?))
(s/def ::pn/has-time-zone    (phone-spec phone/has-time-zone?))
(s/def ::pn/has-known-type   (phone-spec phone/has-known-type?))

(s/def ::input/has-region       ::pn/has-region)
(s/def ::input/has-calling-code ::pn/has-calling-code)
(s/def ::input/has-location     ::pn/has-location)
(s/def ::input/has-time-zone    ::pn/has-time-zone)
(s/def ::input/has-known-type   ::pn/has-known-type)

(s/def ::pn/mobile           (phone-spec phone/is-mobile?       {:type ::type/mobile      }))
(s/def ::pn/fixed-line       (phone-spec phone/is-fixed-line?   {:type ::type/fixed-line  }))
(s/def ::pn/toll-free        (phone-spec phone/is-toll-free?    {:type ::type/toll-free   }))
(s/def ::pn/premium-rate     (phone-spec phone/is-premium-rate? {:type ::type/premium-rate}))
(s/def ::pn/shared-cost      (phone-spec phone/is-shared-cost?  {:type ::type/shared-cost }))
(s/def ::pn/voip             (phone-spec phone/is-voip?         {:type ::type/voip        }))
(s/def ::pn/personal         (phone-spec phone/is-personal?     {:type ::type/personal    }))
(s/def ::pn/pager            (phone-spec phone/is-pager?        {:type ::type/pager       }))
(s/def ::pn/uan              (phone-spec phone/is-uan?          {:type ::type/uan         }))
(s/def ::pn/voicemail        (phone-spec phone/is-voicemail?    {:type ::type/voicemail   }))

(s/def ::input/mobile       ::pn/mobile)
(s/def ::input/fixed-line   ::pn/fixed-line)
(s/def ::input/toll-free    ::pn/toll-free)
(s/def ::input/premium-rate ::pn/premium-rate)
(s/def ::input/shared-cost  ::pn/shared-cost)
(s/def ::input/voip         ::pn/voip)
(s/def ::input/personal     ::pn/personal)
(s/def ::input/pager        ::pn/pager)
(s/def ::input/uan          ::pn/uan)
(s/def ::input/voicemail    ::pn/voicemail)

(s/def ::pn/fixed-line-or-mobile (phone-spec phone/is-fixed-line-or-mobile?))

(s/def ::pn/uncertain-fixed-line-or-mobile (phone-spec phone/is-uncertain-fixed-line-or-mobile?
                                                       {:type ::type/fixed-line-or-mobile
                                                        :early-shrinking true}))

(s/def ::input/fixed-line-or-mobile           ::pn/fixed-line-or-mobile)
(s/def ::input/uncertain-fixed-line-or-mobile ::pn/uncertain-fixed-line-or-mobile)

(s/def ::pn/possible         (phone-spec phone/possible?            {:early-shrinking true}))
(s/def ::pn/impossible       (phone-spec phone/impossible?          {:early-shrinking true}))
(s/def ::pn/unknown          (phone-spec phone/is-unknown?          {:early-shrinking true}))
(s/def ::pn/maybe-mobile     (phone-spec phone/is-maybe-mobile?     {:early-shrinking true}))
(s/def ::pn/maybe-fixed-line (phone-spec phone/is-maybe-fixed-line? {:early-shrinking true}))

(s/def ::input/possible         ::pn/possible)
(s/def ::input/impossible       ::pn/impossible)
(s/def ::input/unknown          ::pn/unknown)
(s/def ::input/maybe-mobile     ::pn/maybe-mobile)
(s/def ::input/maybe-fixed-line ::pn/maybe-fixed-line)

;;
;; Short number specs
;;

(s/def ::pn/short               phone/short-valid?)
(s/def ::pn/maybe-short         phone/short-possible?)
(s/def ::short/valid            phone/short-valid?)
(s/def ::short/invalid          phone/short-invalid?)
(s/def ::short/possible         phone/short-possible?)
(s/def ::short/carrier-specific phone/short-carrier-specific?)

(s/def ::input/short                  ::pn/short)
(s/def ::input/maybe-short            ::pn/maybe-short)
(s/def ::input-short/valid            ::short/valid)
(s/def ::input-short/invalid          ::short/invalid)
(s/def ::input-short/possible         ::short/possible)
(s/def ::input-short/carrier-specific ::short/carrier-specific)

;;
;; Info map spec and props
;;

(s/def ::prop/format string?)

(s/def ::tz-format/timezones (s/coll-of string? :distinct true :min-count 1))
(s/def ::input-tz-format/timezones ::tz-format/timezones)

(s/def ::pn/valid?                     boolean?)
(s/def ::pn/possible?                  boolean?)
(s/def ::short/valid?                  boolean?)
(s/def ::short/possible?               boolean?)
(s/def ::short/emergency?              (s/nilable boolean?))
(s/def ::short/to-emergency?           (s/nilable boolean?))
(s/def ::short/sms-service?            boolean?)
(s/def ::short/carrier-specific?       boolean?)
(s/def ::pn/valid-for-region?          boolean?)
(s/def ::pn/geographical?              boolean?)
(s/def ::dialing-region/derived?       boolean?)
(s/def ::dialing-region/defaulted?     boolean?)
(s/def ::dialing-region/valid-for?     boolean?)
(s/def ::pn/dialing-region-default     (s/nilable ::arg/region))
(s/def ::pn/location                   (s/nilable string?))
(s/def ::pn/carrier                    (s/nilable string?))
(s/def ::pn/dialing-region             (s/nilable ::pn/region))
(s/def ::format/e164                   string?)
(s/def ::format/international          string?)
(s/def ::format/national               string?)
(s/def ::format/rfc3966                string?)
(s/def ::format/raw-input              string?)
(s/def ::tz-format/full-global         ::tz-format/timezones)
(s/def ::tz-format/short-global        ::tz-format/timezones)
(s/def ::tz-format/narrow-global       ::tz-format/timezones)
(s/def ::tz-format/full                ::tz-format/timezones)
(s/def ::tz-format/short               ::tz-format/timezones)
(s/def ::tz-format/narrow              ::tz-format/timezones)
(s/def ::tz-format/id                  ::tz-format/timezones)

(s/def ::input/valid?                        boolean?)
(s/def ::input/possible?                     boolean?)
(s/def ::input-short/valid?                  boolean?)
(s/def ::input-short/possible?               boolean?)
(s/def ::input-short/emergency?              (s/nilable boolean?))
(s/def ::input-short/to-emergency?           (s/nilable boolean?))
(s/def ::input-short/sms-service?            boolean?)
(s/def ::input-short/carrier-specific?       boolean?)
(s/def ::input/valid-for-region?             boolean?)
(s/def ::input/geographical?                 boolean?)
(s/def ::input-dialing-region/derived?       boolean?)
(s/def ::input-dialing-region/defaulted?     boolean?)
(s/def ::input-dialing-region/valid-for?     boolean?)
(s/def ::input/dialing-region-default        (s/nilable ::arg/region))
(s/def ::input/location                      (s/nilable string?))
(s/def ::input/carrier                       (s/nilable string?))
(s/def ::input/dialing-region                (s/nilable ::input/region))
(s/def ::input-format/e164                   string?)
(s/def ::input-format/international          string?)
(s/def ::input-format/national               string?)
(s/def ::input-format/rfc3966                string?)
(s/def ::input-format/raw-input              string?)
(s/def ::input-tz-format/full-global         ::tz-format/timezones)
(s/def ::input-tz-format/input-short-global  ::tz-format/timezones)
(s/def ::input-tz-format/narrow-global       ::tz-format/timezones)
(s/def ::input-tz-format/full                ::tz-format/timezones)
(s/def ::input-tz-format/input-short         ::tz-format/timezones)
(s/def ::input-tz-format/narrow              ::tz-format/timezones)
(s/def ::input-tz-format/id                  ::tz-format/timezones)

(def short-info-keys
  (s/keys :req [::short/valid?
                ::short/possible?]
          :opt [::short/carrier-specific?
                ::short/cost
                ::short/emergency?
                ::short/sms-service?
                ::short/to-emergency?
                ::pn/dialing-region
                ::dialing-region/derived?
                ::dialing-region/defaulted?]))

(def info-keys
  (s/keys :req [::pn/calling-code
                ::pn/geographical?
                ::pn/possible?
                ::pn/valid?
                ::pn/type
                ::short/valid?
                ::short/possible?]
          :opt [::pn/region
                ::pn/location
                ::pn/carrier
                ::pn/dialing-region
                ::dialing-region/valid-for?
                ::dialing-region/derived?
                ::dialing-region/defaulted?
                ::format/e164
                ::format/international
                ::format/national
                ::format/rfc3966
                ::format/raw-input
                ::tz-format/narrow-global
                ::tz-format/full
                ::tz-format/short
                ::tz-format/narrow
                ::tz-format/full-global
                ::tz-format/short-global
                ::tz-format/id
                ::short/carrier-specific?
                ::short/cost
                ::short/emergency?
                ::short/sms-service?
                ::short/to-emergency?]))

(s/def ::pn/info
  (s/with-gen
    info-keys
    #(gen/fmap
      phone/info
      (s/gen ::pn/native))))

(s/def ::pn/info-valid
  (s/with-gen
    info-keys
    #(gen/fmap
      phone/info
      (s/gen ::pn/native-valid))))

(s/def ::pn/info-invalid
  (s/with-gen
    info-keys
    #(gen/fmap
      phone/info
      (s/gen ::pn/native-invalid))))

(s/def ::short/info
  (s/with-gen
    short-info-keys
    #(gen/fmap
      phone/short-info
      (s/gen ::pn/native))))

(s/def ::arg/map                ::pn/info)
(s/def ::arg/map-regional       ::pn/info)
(s/def ::arg/map-global         ::pn/info)

(s/def ::input/map              ::arg/map)
(s/def ::input/map-regional     ::arg/map-regional)
(s/def ::input/map-global       ::arg/map-global)

(s/def ::arg/number-global
  (s/with-gen
    (s/alt :native ::arg/native
           :map    ::arg/map-global
           :string ::arg/string-global
           :nil    nil?)
    #(gen/fmap
      (juxt (gen/generate (gen/elements [random-format-global
                                         phone/info
                                         identity
                                         (constantly nil)])))
      (s/gen ::pn/has-calling-code))))

(s/def ::arg/number-regional
  (s/with-gen
    (s/alt :numeric ::pn/numeric
           :map     ::arg/map-regional
           :string  ::pn/string-regional)
    #(gen/fmap
      (juxt (gen/generate (gen/elements [random-format-regional
                                         phone/info
                                         phone/numeric])))
      (s/gen ::pn/has-region))))

(s/def ::input/number-global   ::arg/number-global)
(s/def ::input/number-regional ::arg/number-regional)

(s/def ::arg/locale
  (s/with-gen
    #(locale/valid? % phone/*inferred-namespaces*)
    #(gen/fmap
      (gen/generate (gen/elements [identity
                                   (fn [^Locale l] (.getLanguage ^Locale l))
                                   (comp keyword (fn [^Locale l] (.getLanguage ^Locale l)))
                                   (constantly nil)]))
      (gen/elements locale/by-val-vec))))

(s/def ::input/locale ::arg/locale)

;; Please note that a standalone regional number will be invalid
;; This is just for abstract testing or raw input specific.

(s/def ::arg/number
  (s/or :regional ::arg/number-regional
        :global   ::arg/number-global))

(s/def ::input/number ::arg/number)

;;
;; Argument tuples which validity can be checked only when tested together
;; (mainly used in fdefs).
;;

(s/def ::args/native.region
  (s/with-gen
    (s/cat :phone-number ::pn/native
           :region-code  (s/nilable ::arg/region))
    #(gen/fmap
      (juxt identity phone/region)
      (s/gen ::pn/has-region))))

(s/def ::args/string-regional.region
  (s/with-gen
    (s/cat :phone-number ::pn/string-regional
           :region-code  ::arg/region)
    #(gen/fmap
      (juxt random-format-regional phone/region)
      (s/gen ::pn/has-region))))

(s/def ::args/string-global.region
  (s/with-gen
    (s/cat :phone-number ::pn/string-global
           :region-code  (s/nilable ::arg/region))
    #(gen/fmap
      (juxt random-format-global phone/region)
      (s/gen ::pn/has-calling-code))))

(s/def ::args/numeric.region
  (s/with-gen
    (s/cat :phone-number ::pn/numeric
           :region-code  ::arg/region)
    #(gen/fmap
      (juxt phone/numeric phone/region)
      (s/gen ::pn/has-region))))

(s/def ::args/map.region ;; TODO: regional-map
  (s/with-gen
    (s/cat :phone-number ::arg/map
           :region-code  ::arg/region)
    #(gen/fmap
      (juxt phone/info phone/region)
      (s/gen ::pn/has-region))))

(s/def ::args/map-regional.region
  (s/with-gen
    (s/cat :phone-number ::arg/map-regional
           :region-code  ::arg/region)
    #(gen/fmap
      (juxt phone/info phone/region)
      (s/gen ::pn/has-region))))

(s/def ::args/map-global.region
  (s/with-gen
    (s/cat :phone-number ::arg/map-global
           :region-code (s/nilable ::arg/region))
    #(gen/fmap
      (juxt phone/info phone/region)
      (s/gen ::pn/has-calling-code))))

(s/def ::args/nil.region
  (s/with-gen
    (s/cat :phone-number nil?
           :region-code (s/nilable ::arg/region))
    #(gen/fmap
      (juxt (constantly nil) phone/region)
      (s/gen ::pn/has-region))))

(s/def ::args/number-regional.region
  (s/alt :numeric ::args/numeric.region
         :string  ::args/string-regional.region
         :map     ::args/map-regional.region))

(s/def ::args/number-global.region
  (s/alt :native ::args/native.region
         :string ::args/string-global.region
         :map    ::args/map-global.region
         :nil    ::args/nil.region))

(s/def ::args/number.region
  (s/or :nil      ::args/nil.region
        :regional ::args/number-regional.region
        :global   ::args/number-global.region))

(s/def ::args/number
  (s/or :nil     (s/cat :phone-number nil? :region-code (s/? (s/nilable ::arg/region)))
        :arity-2 ::args/number.region
        :arity-1 ::arg/number-global))

(s/def ::args/format
  (s/or :nil         (s/cat :phone-number nil? :region-code (s/? (s/nilable ::arg/region)))
        :arity-3-nr  (s/cat :number-regional.region ::args/number-regional.region :format (s/nilable ::arg/format))
        :arity-2-nr  ::args/number-regional.region
        :arity-3-ng  (s/cat :number-global.region ::args/number-global.region :format (s/nilable ::arg/format))
        :arity-2-ng  ::args/number-global.region
        :arity-2-ngf (s/cat :number-global ::arg/number-global :format (s/? (s/nilable ::arg/format)))
        :arity-1     ::arg/number-global))

(s/def ::args/number+dialing-region
  (s/or :nil        (s/cat :phone-number           nil?
                           :region-code            (s/nilable ::arg/region)
                           :dialing-region         ::arg/region)
        :arity-3-nr (s/cat :number-regional.region ::args/number-regional.region
                           :dialing-region         ::arg/region)
        :arity-3-ng (s/cat :number-global.region   ::args/number-global.region
                           :dialing-region         ::arg/region)))

(s/def ::args/number+opt-dialing-region
  (s/or :nil        (s/cat :phone-number           nil?
                           :region-code            (s/nilable ::arg/region)
                           :dialing-region         ::arg/region)
        :arity-3-nr (s/cat :number-regional.region ::args/number-regional.region
                           :dialing-region         (s/nilable ::arg/region))
        :arity-2-nr ::args/number-regional.region
        :arity-3-ng (s/cat :number-global.region   ::args/number-global.region
                           :dialing-region         (s/nilable ::arg/region))
        :arity-2-ng ::args/number-global.region
        :arity-1-ng ::arg/number-global))

(s/def ::args/number+locale
  (s/or :nil         (s/cat :phone-number           nil?
                            :region-code            (s/? (s/nilable ::arg/region)))
        :arity-3-nr  (s/cat :number-regional.region ::args/number-regional.region
                            :locale                 (s/nilable ::arg/locale))
        :arity-2-nr  ::args/number-regional.region
        :arity-3-ng  (s/cat :number-global.region   ::args/number-global.region
                            :locale                 (s/nilable ::arg/locale))
        :arity-2-ng  ::args/number-global.region
        :arity-1     ::arg/number-global))

(s/def ::args/number+locale-or-region
  (s/or :nil         (s/cat :phone-number           nil?
                            :region-code            (s/? (s/nilable ::arg/region)))
        :arity-3-nr  (s/cat :number-regional.region ::args/number-regional.region
                            :locale                 (s/nilable ::arg/locale))
        :arity-2-nr  ::args/number-regional.region
        :arity-3-ng  (s/cat :number-global.region   ::args/number-global.region
                            :locale                 (s/nilable ::arg/locale))
        :arity-2-ng  ::args/number-global.region
        :arity-2-ngl (s/cat :number-global ::arg/number-global :locale (s/? (s/nilable ::arg/locale)))
        :arity-1     ::arg/number-global))

(s/def ::args/number+tz+locale
  (s/or :nil        (s/cat :phone-number nil? :region-code (s/? (s/nilable ::arg/region)))
        :arity-4-nr (s/cat :number-regional.region ::args/number-regional.region
                           :locale                 (s/nilable ::arg/locale)
                           :format                 (s/nilable ::arg/tz-format))
        :arity-3-nr (s/cat :number-regional.region ::args/number-regional.region
                           :format                 (s/nilable ::arg/tz-format))
        :arity-2-nr ::args/number-regional.region
        :arity-4-ng (s/cat :number-global.region   ::args/number-global.region
                           :locale                 (s/nilable ::arg/locale)
                           :format                 (s/nilable ::arg/tz-format))
        :arity-3-ng (s/cat :number-global.region   ::args/number-global.region
                           :format                 (s/nilable ::arg/tz-format))
        :arity-2-ng ::args/number-global.region
        :arity-1    ::arg/number-global))

(s/def ::args/number+tz+locale-or-region
  (s/or :nil        (s/cat :phone-number nil? :other-args (s/? (s/nilable ::arg/region)))
        :arity-4-nr (s/cat :number-regional.region ::args/number-regional.region
                           :locale                   (s/nilable ::arg/locale)
                           :format                   (s/nilable ::arg/tz-format))
        :arity-3-nr  (s/cat :number-regional.region  ::args/number-regional.region
                            :format                  (s/nilable ::arg/tz-format))
        :arity-3-nrl (s/cat :number-regional.region  ::args/number-regional.region
                            :locale                  (s/nilable ::arg/locale))
        :arity-2-nr  ::args/number-regional.region
        :arity-4-ng  (s/cat :number-global.region    ::args/number-global.region
                            :locale                  (s/nilable ::arg/locale)
                            :format                  (s/nilable ::arg/tz-format))
        :arity-3-ngf (s/cat :number-global.region    ::args/number-global.region
                            :format                  (s/? (s/nilable ::arg/tz-format)))
        :arity-3-ngl (s/cat :number-global.region    ::args/number-global.region
                            :locale                  (s/nilable ::arg/locale))
        :arity-2-ng  ::args/number-global.region
        :arity-2-ngf (s/cat :number-global          ::arg/number-global
                            :format                 (s/? (s/nilable ::arg/tz-format)))
        :arity-1    ::arg/number-global))

(s/def ::args/number+locale+dialing-region
  (s/or :nil        (s/cat :phone-number nil? :region-code (s/? (s/nilable ::arg/region)))
        :arity-4-nr (s/cat :number-regional.region ::args/number-regional.region
                           :locale                 (s/nilable ::arg/locale)
                           :dialing-region          (s/nilable ::arg/region))
        :arity-3-nr (s/cat :number-regional.region ::args/number-regional.region
                           :dialing-region          (s/nilable ::arg/dialing-region))
        :arity-2-nr ::args/number-regional.region
        :arity-4-ng (s/cat :number-global.region   ::args/number-global.region
                           :locale                 (s/nilable ::arg/locale)
                           :dialing-region          (s/nilable ::arg/region))
        :arity-3-ng (s/cat :number-global.region   ::args/number-global.region
                           :dialing-region          (s/nilable ::arg/region))
        :arity-2-ng ::args/number-global.region
        :arity-1    ::arg/number-global))

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
  :args ::args/number+opt-dialing-region
  :ret  boolean?)

(s/fdef phone/number
  :args ::args/number
  :ret  (s/nilable ::pn/native-with-raw))

(s/fdef phone/number-noraw
  :args ::args/number
  :ret  (s/nilable ::pn/native))

(s/fdef phone/number-optraw
  :args ::args/number
  :ret  (s/nilable ::pn/native))

(s/fdef phone/raw-input
  :args (s/or :nil         (s/cat :phone-number nil? :region-code (s/? (s/nilable ::arg/region)))
              :arity-2-nr  (s/cat :number ::arg/number-regional :region-code (s/? (s/nilable ::arg/region)))
              :arity-2-ng  (s/cat :number ::arg/number-global   :region-code (s/? (s/nilable ::arg/region)))
              :arity-1     ::arg/number)
  :ret  (s/nilable ::pn/string))

;;
;; Core functions
;;

(s/fdef phone/numeric
  :args ::args/number
  :ret  (s/nilable ::pn/numeric))

(s/fdef phone/calling-code
  :args ::args/number
  :ret  (s/nilable ::pn/calling-code))

(s/fdef phone/location
  :args ::args/number+locale-or-region
  :ret  ::pn/location)

(s/fdef phone/carrier
  :args ::args/number+locale-or-region
  :ret  ::pn/carrier)

(s/fdef phone/format
  :args ::args/format
  :ret  (s/nilable ::prop/format))

(s/fdef phone/time-zones
  :args ::args/number+tz+locale-or-region
  :ret  (s/nilable (s/coll-of string?
                              :distinct true
                              :min-count 1)))

(s/fdef phone/time-zones-all-formats
  :args ::args/number+locale-or-region
  :ret  (s/nilable (s/map-of ::pn/tz-format
                             (s/coll-of string? :distinct true :min-count 1)
                             :conform-keys true
                             :count (count tz-format/all))))

(s/fdef phone/all-formats
  :args ::args/number
  :ret  (s/nilable (s/map-of ::pn/format
                             ::pn/string
                             :conform-keys true
                             :count (count format/all))))

(s/fdef phone/region
  :args ::args/number
  :ret  (s/nilable ::pn/region))

(s/fdef phone/type
  :args ::args/number
  :ret  (s/nilable ::pn/type))

(s/fdef phone/short-cost
  :args ::args/number+opt-dialing-region
  :ret  (s/nilable ::pn/cost))

(s/fdef phone/has-raw-input?           :args ::args/number :ret boolean?)
(s/fdef phone/has-calling-code?        :args ::args/number :ret boolean?)
(s/fdef phone/has-location?            :args ::args/number :ret boolean?)
(s/fdef phone/has-time-zone?           :args ::args/number :ret boolean?)
(s/fdef phone/has-known-type?          :args ::args/number :ret boolean?)
(s/fdef phone/is-maybe-fixed-line?     :args ::args/number :ret boolean?)
(s/fdef phone/is-maybe-mobile?         :args ::args/number :ret boolean?)
(s/fdef phone/is-fixed-line-or-mobile? :args ::args/number :ret boolean?)
(s/fdef phone/possible?                :args ::args/number :ret ::pn/possible?)
(s/fdef phone/impossible?              :args ::args/number :ret ::pn/possible?)
(s/fdef phone/invalid?                 :args ::args/number :ret ::pn/valid?)
(s/fdef phone/geographical?            :args ::args/number :ret ::pn/geographical?)

(s/fdef phone/is-uncertain-fixed-line-or-mobile?
  :args ::args/number
  :ret  boolean?)

(s/fdef phone/valid-for-region?
  :args ::args/number+dialing-region
  :ret ::pn/valid-for-region?)

(s/fdef phone/short-to-emergency?
  :args ::args/number
  :ret  ::short/to-emergency?)

(s/fdef phone/short-emergency?
  :args ::args/number
  :ret  ::short/emergency?)

(s/fdef phone/short-possible?
  :args ::args/number+opt-dialing-region
  :ret  ::short/possible?)

(s/fdef phone/short-valid?
  :args ::args/number+opt-dialing-region
  :ret  ::short/valid?)

(s/fdef phone/short-carrier-specific?
  :args ::args/number+opt-dialing-region
  :ret  ::short/carrier-specific?)

(s/fdef phone/short-sms-service?
  :args ::args/number+opt-dialing-region
  :ret  ::short/sms-service?)

(s/fdef phone/short-info
  :args ::args/number+opt-dialing-region
  :ret  (s/nilable ::short/info))
