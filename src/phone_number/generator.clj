(ns

    ^{:doc    "Sample data generator interface for phone-number library."
      :author "Paweł Wilk"
      :added  "8.12.4-0"
      :no-doc true}

    phone-number.generator

  (:require [phone-number.core             :as phone]
            [clojure.test.check.rose-tree  :as  rose]
            [clojure.test.check.generators :as  gens])

  (:import [java.util Random]))

(def phone-number
  (gens/no-shrink
   (clojure.test.check.generators/->Generator
    (fn [^Random rng _]
      (rose/make-rose
       (phone/generate nil nil phone/valid? nil nil (.nextLong ^Random rng))
       [])))))
