(ns

    ^{:doc    "Public specs of phone-number library."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.spec

  (:require [phone-number]
            [phone-number.core  :as phone]
            [clojure.spec.alpha :as     s]))

;;
;; Phone number specs
;;

(ns phone-number
  (:require [clojure.spec.alpha :as s]))


(s/def ::valid phone/valid?)

(s/def ::possible             phone/possible?)
(s/def ::short                phone/is-short?)
(s/def ::maybe-short          phone/is-maybe-short?)

(s/def ::has-region           phone/has-region?)
(s/def ::has-country-code     phone/has-country-code?)
(s/def ::has-location         phone/has-location?)
(s/def ::has-time-zone        phone/has-time-zone?)
(s/def ::has-known-type       phone/has-known-type?)

(s/def ::mobile               phone/is-mobile?)
(s/def ::fixed-line           phone/is-fixed-line?)
(s/def ::toll-free            phone/is-toll-free?)
(s/def ::premium-rate         phone/is-premium-rate?)
(s/def ::shared-cost          phone/is-shared-cost?)
(s/def ::voip                 phone/is-voip?)
(s/def ::personal             phone/is-personal?)
(s/def ::pager                phone/is-pager?)
(s/def ::uan                  phone/is-uan?)
(s/def ::voicemail            phone/is-voicemail?)
(s/def ::unknown              phone/is-unknown?)
(s/def ::maybe-mobile         phone/is-maybe-mobile?)
(s/def ::maybe-fixed-line     phone/is-maybe-fixed-line?)
(s/def ::fixed-line-or-mobile phone/is-fixed-line-or-mobile?)
(s/def ::uncertain-fixed-line-or-mobile phone/is-uncertain-fixed-line-or-mobile?)

;;
;; Short number specs
;;

(ns phone-number.short
  (:require [clojure.spec.alpha :as s]))

(s/def ::valid                phone-number.core/short-valid?)
(s/def ::possible             phone-number.core/short-possible?)
(s/def ::carrier-specific     phone-number.core/short-carrier-specific?)

;;
;; Phone number region specs
;;

(ns phone-number.region
  (:require [clojure.spec.alpha :as s]))

(s/def ::valid valid?)

;;
;; Phone number type specs
;;

(ns phone-number.type
  (:require [clojure.spec.alpha :as s]))

(s/def ::valid valid?)

;;
;; Phone number format specs
;;

(ns phone-number.format
  (:require [clojure.spec.alpha :as s]))

(s/def ::valid valid?)

;;
;; Phone number time zone format specs
;;

(ns phone-number.tz-format
  (:require [clojure.spec.alpha :as s]))

(s/def ::valid valid?)
