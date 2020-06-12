(ns

    ^{:doc    "Public specs of phone-number library."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.spec

  (:require [phone-number.core          :as        phone]
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
            [clojure.spec.gen.alpha     :as          gen])

  (:import  [com.google.i18n.phonenumbers
             Phonenumber$PhoneNumber]))

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
    #(gen/elements region/all-vec)))

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

(s/def :phone-number.arg/format :phone-number/format)

(s/def :phone-number/format-global
  (s/with-gen
    #(format/valid? % phone/*inferred-namespaces*)
    #(gen/elements format/all-calling-coded-vec)))

(s/def :phone-number.arg/format-global :phone-number/format-global)

(s/def :phone-number/format-regional
  (s/with-gen
    #(format/valid? % phone/*inferred-namespaces*)
    #(gen/elements format/all-not-calling-coded-vec)))

(s/def :phone-number.arg./format-regional :phone-number/format-regional)

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

(s/def :phone-number/native
  (s/with-gen
    phone/native?
    #(gen/fmap (fn [random-seed]
                 (:phone-number/number
                  (phone/generate nil
                                  nil
                                  nil
                                  150
                                  nil
                                  (.getMostSignificantBits random-seed)
                                  true)))
               (gen/uuid))))

(s/def :phone-number/native-valid
  (s/with-gen
    phone/valid?
    #(gen/fmap (fn [random-seed]
                 (:phone-number/number
                  (phone/generate nil
                                  nil
                                  phone/valid?
                                  150
                                  nil
                                  (.getMostSignificantBits random-seed)
                                  false)))
               (gen/uuid))))

(s/def :phone-number/native-invalid
  (s/with-gen
    phone/invalid?
    #(gen/fmap (fn [random-seed]
                 (:phone-number/number
                  (phone/generate nil
                                  nil
                                  phone/invalid?
                                  100
                                  nil
                                  (.getMostSignificantBits random-seed)
                                  true)))
               (gen/uuid))))


(s/def :phone-number/string
  (s/with-gen
    (s/and string? phone/valid-input?)
    #(gen/fmap
      (fn [n] (phone/format n nil (gen/generate (s/gen :phone-number/format))))
      (s/gen :phone-number/native))))

(s/def :phone-number/string-global
  (s/with-gen
    (s/and string? phone/valid-input?)
    #(gen/fmap
      (fn [n] (phone/format n nil (gen/generate (s/gen :phone-number/format-global))))
      (s/gen :phone-number/native))))

(s/def :phone-number/string-regional
  (s/with-gen
    (s/and string? phone/valid-input?)
    #(gen/fmap
      (fn [n] (phone/format n nil (gen/generate (s/gen :phone-number/format-regional))))
      (s/gen :phone-number/native))))

(s/def :phone-number/string-valid
  (s/with-gen
    (s/and string? phone/valid?)
    #(gen/fmap
      (fn [n] (phone/format n nil (gen/generate (s/gen :phone-number/format))))
      (s/gen :phone-number/native-valid))))

(s/def :phone-number/string-invalid
  (s/with-gen
    (s/and string? phone/invalid?)
    #(gen/fmap
      (fn [n] (phone/format n nil (gen/generate (s/gen :phone-number/format))))
      (s/gen :phone-number/native-invalid))))

(defmacro phone-gen
  "Template macro for constructing specs with generators for the given predicate. It
  uses the generate function from phone-number.core to support generation of samples
  that are meeting the criteria."
  ([f]
   (list 'phone-gen f nil nil false))
  ([f region-code]
   (list 'phone-gen f region-code nil false))
  ([f region-code number-type]
   (list 'phone-gen f region-code number-type false))
  ([f region-code number-type random-shrinking]
   (list 'clojure.spec.alpha/with-gen
         f
         (list 'fn []
               (list 'clojure.spec.gen.alpha/fmap
                     (list 'fn '[random-seed]
                           (list ':phone.number/number
                                 (list 'phone-number.core/generate
                                       region-code
                                       number-type
                                       f
                                       200
                                       nil
                                       (list '.getMostSignificantBits 'random-seed)
                                       random-shrinking)))
                     (list 'clojure.spec.gen.alpha/uuid))))))

(s/def :phone-number/has-region           (phone-gen phone/has-region?))
(s/def :phone-number/has-calling-code     (phone-gen phone/has-calling-code?))
(s/def :phone-number/has-location         (phone-gen phone/has-location?))
(s/def :phone-number/has-time-zone        (phone-gen phone/has-time-zone?))
(s/def :phone-number/has-known-type       (phone-gen phone/has-known-type?))

(s/def :phone-number/mobile               (phone-gen phone/is-mobile?       nil :phone-number.type/mobile))
(s/def :phone-number/fixed-line           (phone-gen phone/is-fixed-line?   nil :phone-number.type/fixed-line))
(s/def :phone-number/toll-free            (phone-gen phone/is-toll-free?    nil :phone-number.type/toll-free))
(s/def :phone-number/premium-rate         (phone-gen phone/is-premium-rate? nil :phone-number.type/premium-rate))
(s/def :phone-number/shared-cost          (phone-gen phone/is-shared-cost?  nil :phone-number.type/shared-cost))
(s/def :phone-number/voip                 (phone-gen phone/is-voip?         nil :phone-number.type/voip))
(s/def :phone-number/personal             (phone-gen phone/is-personal?     nil :phone-number.type/personal))
(s/def :phone-number/pager                (phone-gen phone/is-pager?        nil :phone-number.type/pager))
(s/def :phone-number/uan                  (phone-gen phone/is-uan?          nil :phone-number.type/uan))
(s/def :phone-number/voicemail            (phone-gen phone/is-voicemail?    nil :phone-number.type/voicemail))

(s/def :phone-number/fixed-line-or-mobile           (phone-gen phone/is-fixed-line-or-mobile?))
(s/def :phone-number/uncertain-fixed-line-or-mobile (phone-gen phone/is-uncertain-fixed-line-or-mobile?
                                                               nil :phone-number.type/fixed-line-or-mobile true))

(s/def :phone-number/possible             (phone-gen phone/possible?                nil nil true))
(s/def :phone-number/impossible           (phone-gen phone/impossible?              nil nil true))
(s/def :phone-number/unknown              (phone-gen phone/is-unknown?              nil nil true))
(s/def :phone-number/maybe-mobile         (phone-gen phone/is-maybe-mobile?         nil nil true))
(s/def :phone-number/maybe-fixed-line     (phone-gen phone/is-maybe-fixed-line?     nil nil true))

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
;; Info map spec
;;

(s/def :phone-number.tz-format/timezones (s/coll-of string? :distinct true :min-count 1))

(s/def :phone-number/valid?                      boolean?)
(s/def :phone-number/possible?                   boolean?)
(s/def :phone-number.short/valid?                boolean?)
(s/def :phone-number.short/possible?             boolean?)
(s/def :phone-number/geographical?               boolean?)
(s/def :phone-number/location                    string?) ;; TODO: specify formats more accurately
(s/def :phone-number.format/e164                 string?)
(s/def :phone-number.format/international        string?)
(s/def :phone-number.format/national             string?)
(s/def :phone-number.format/rfc3966              string?)
(s/def :phone-number.format/raw-input            string?)
(s/def :phone-number.tz-format/full-global   :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/short-global  :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/narrow-global :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/full              :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/short             :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/narrow            :phone-number.tz-format/timezones)
(s/def :phone-number.tz-format/id                :phone-number.tz-format/timezones)

(def info-keys
  (s/keys :opt [:phone-number/location
                :phone-number/carrier
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
                :phone-number.tz-format/id]
          :req [:phone-number/calling-code
                :phone-number/geographical?
                :phone-number/possible?
                :phone-number/valid?
                :phone-number/type
                :phone-number/region
                :phone-number.short/valid?
                :phone-number.short/possible?]))

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

(s/def :phone-number.arg/any
  (s/or :native :phone-number/native
        :map    :phone-number/info     ;; TODO: create another spec with map as an argument
        :string :phone-number/string))

(s/def :phone-number.arg/global
  (s/or :native :phone-number/native
        :map    :phone-number/info     ;; TODO: create another spec with map as an argument
        :string :phone-number/string-global))

(s/def :phone-number.arg/regional
  (s/or :native :phone-number/native
        :map    :phone-number/info     ;; TODO: create another spec with map as an argument
        :string :phone-number/string-regional))


;;
;; Function specs (TODO)
;;

(s/fdef phone/native?
  :args (s/cat :object any?)
  :ret  boolean?
  :fn   (s/or :exact (s/and #(:ret %) #(= Phonenumber$PhoneNumber (class (-> % :args :object))))
              :other #(not= Phonenumber$PhoneNumber (class (-> % :args :object)))))

(s/fdef phone/number
  :args (s/or :nil      (s/cat :phone-number nil?
                               :region-code  any?)
              :global   (s/cat :phone-number :phone-number.arg/global
                               :region-code  (s/nilable :phone-number.arg/region))
              :regional (s/cat :phone-number :phone-number.arg/regional
                               :region-code  :phone-number.arg/region))
  :ret  (s/nilable :phone-number/native))
