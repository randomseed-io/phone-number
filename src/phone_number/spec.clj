(ns

    ^{:doc    "Public specs of phone-number library."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.spec

  (:require [phone-number.core             :as     phone]
            [phone-number.type             :as      type]
            [phone-number.match            :as     match]
            [phone-number.format           :as    format]
            [phone-number.tz-format        :as tz-format]
            [phone-number.region           :as    region]
            [phone-number.cost             :as      cost]
            [phone-number.calling-code     :as    c-code]
            [phone-number.generator        :as      pgen]
            [clojure.spec.alpha            :as         s]
            [clojure.spec.gen.alpha        :as       gen]))

;;
;; Phone number region specs
;;

(s/def :phone-number/region
  (s/with-gen
    region/valid?
    #(gen/elements region/all-vec)))

;;
;; Phone number type specs
;;

(s/def :phone-number/type
  (s/with-gen
    type/valid?
    #(gen/elements type/all-vec)))

;;
;; Phone number format specs
;;

(s/def :phone-number/format
  (s/with-gen
    format/valid?
    #(gen/elements format/all-vec)))

;;
;; Phone number time zone format specs
;;

(s/def :phone-number/tz-format
  (s/with-gen
    tz-format/valid?
    #(gen/elements tz-format/all-vec)))

;;
;; Phone number specs
;;

(s/def :phone-number/valid
  (s/with-gen
    phone/valid?
    #(gen/fmap (fn [random-seed]
                 (phone/generate nil
                                 nil
                                 phone/valid?
                                 150
                                 nil
                                 (.getMostSignificantBits random-seed)
                                 false))
               (gen/uuid))))

(s/def :phone-number/invalid
  (s/with-gen
    phone/invalid?
    #(gen/fmap (fn [random-seed]
                 (phone/generate nil
                                 nil
                                 phone/invalid?
                                 100
                                 nil
                                 (.getMostSignificantBits random-seed)
                                 true))
               (gen/uuid))))

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
                           (list 'phone-number.core/generate
                                 region-code
                                 number-type
                                 f
                                 200
                                 nil
                                 (list '.getMostSignificantBits 'random-seed)
                                 random-shrinking))
                     (list 'clojure.spec.gen.alpha/uuid))))))

(s/def :phone-number/has-region           (phone-gen phone/has-region?))
(s/def :phone-number/has-country-code     (phone-gen phone/has-country-code?))
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
(s/def :phone-number.short/valid?           phone/short-valid?)
(s/def :phone-number.short/invalid          phone/short-invalid?)
(s/def :phone-number.short/possible         phone/short-possible?)
(s/def :phone-number.short/carrier-specific phone/short-carrier-specific?)
