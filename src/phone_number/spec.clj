(ns

    ^{:doc    "Public specs of phone-number library."
      :author "Paweł Wilk"
      :added  "8.12.4-0"}

    phone-number.spec

  (:require [clojure.spec.alpha           :as         s]
            [phone-number.util            :as      util]
            [phone-number.core            :as     phone]))

(s/def ::possible-number phone/possible?)
(s/def ::valid-number    phone/valid?)

(s/def ::mobile-number   (s/and ::valid-number #(= :mobile       (phone/type %))))
(s/def ::landline-number (s/and ::valid-number #(= :fixed-line   (phone/type %))))
(s/def ::voip-number     (s/and ::valid-number #(= :voip         (phone/type %))))
(s/def ::premium-number  (s/and ::valid-number #(= :premium-rate (phone/type %))))
