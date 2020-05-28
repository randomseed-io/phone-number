(ns

    ^{:doc    "Public specs of phone-number library."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.spec

  (:require [phone-number.core]
            [clojure.spec.alpha :as s]))

(s/def :phone-number/valid-number    phone-number.core/valid?)
(s/def :phone-number/possible-number phone-number.core/possible?)

(ns phone-number.type
  (:require [clojure.spec.alpha  :as      s]
            [phone-number.core   :as  phone]
            [phone-number.util   :as   util]))

(util/gen-ises (remove #{::fixed-line-or-mobile} (keys all)) phone/type)

(defn is-fixed-line-or-mobile?
  [num]
  (util/try-parse-or-false
   (contains?
    #{::fixed-line-or-mobile ::fixed-line ::mobile}
    (phone/type num))))

(defn is-uncertain-fixed-line-or-mobile?
  [num]
  (util/try-parse-or-false
   (= ::fixed-line-or-mobile (phone/type num))))

(defn is-maybe-mobile?
  [num]
  (util/try-parse-or-false
   (contains?
    #{::fixed-line-or-mobile ::mobile}
    (phone/type num))))

(defn is-maybe-fixed-line?
  [num]
  (util/try-parse-or-false
   (contains?
    #{::fixed-line-or-mobile ::fixed-line}
    (phone/type num))))

(s/def ::mobile               is-mobile?)
(s/def ::fixed-line           is-fixed-line?)
(s/def ::toll-free            is-toll-free?)
(s/def ::premium-rate         is-premium-rate?)
(s/def ::shared-cost          is-shared-cost?)
(s/def ::voip                 is-voip?)
(s/def ::personal             is-personal?)
(s/def ::pager                is-pager?)
(s/def ::uan                  is-uan?)
(s/def ::voicemail            is-voicemail?)
(s/def ::unknown              is-unknown?)
(s/def ::maybe-mobile         is-maybe-mobile?)
(s/def ::maybe-fixed-line     is-maybe-fixed-line?)
(s/def ::fixed-line-or-mobile is-fixed-line-or-mobile?)
(s/def ::uncertain-fixed-line-or-mobile is-uncertain-fixed-line-or-mobile?)
