(ns

    ^{:doc    "Public specs of phone-number library."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.spec

  (:require [phone-number.core  :as phone]
            [clojure.spec.alpha :as     s]))

;;
;; Phone number specs
;;

(ns phone-number
  (:require [clojure.spec.alpha :as s]))

(s/def ::valid                phone-number.core/valid?)
(s/def ::possible             phone-number.core/possible?)
(s/def ::has-region-code      phone-number.core/has-region-code?)
(s/def ::has-country-code     phone-number.core/has-country-code?)
(s/def ::has-location         phone-number.core/has-location?)
(s/def ::has-time-zone        phone-number.core/has-time-zone?)
(s/def ::has-known-type       phone-number.core/has-known-type?)

(s/def ::mobile               phone-number.core/is-mobile?)
(s/def ::fixed-line           phone-number.core/is-fixed-line?)
(s/def ::toll-free            phone-number.core/is-toll-free?)
(s/def ::premium-rate         phone-number.core/is-premium-rate?)
(s/def ::shared-cost          phone-number.core/is-shared-cost?)
(s/def ::voip                 phone-number.core/is-voip?)
(s/def ::personal             phone-number.core/is-personal?)
(s/def ::pager                phone-number.core/is-pager?)
(s/def ::uan                  phone-number.core/is-uan?)
(s/def ::voicemail            phone-number.core/is-voicemail?)
(s/def ::unknown              phone-number.core/is-unknown?)
(s/def ::maybe-mobile         phone-number.core/is-maybe-mobile?)
(s/def ::maybe-fixed-line     phone-number.core/is-maybe-fixed-line?)
(s/def ::fixed-line-or-mobile phone-number.core/is-fixed-line-or-mobile?)
(s/def ::uncertain-fixed-line-or-mobile phone-number.core/is-uncertain-fixed-line-or-mobile?)

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
