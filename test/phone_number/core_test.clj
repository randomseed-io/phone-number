(ns

    ^{:doc    "phone-number library, core tests."
      :author "Paweł Wilk"
      :added  "8.12.4-0"
      :no-doc true}

    phone-number.core-test

  (:require [clojure.spec.alpha      :as            s]
            [midje.sweet             :refer      :all]
            [midje.experimental      :refer [for-all]]
            [clojure.spec.gen.alpha  :as          gen]
            [clojure.spec.test.alpha :as           st]
            ;;[orchestra.spec.test     :as           ot]
            [phone-number.core       :refer      :all]
            [phone-number.spec       :as         spec]))

(s/check-asserts true)

;; (facts "about `trim-both-onc`"
;;        (fact "when it returns nil for nil"
;;              (trim-both-once nil)          => nil
;;              (trim-both-once #{} nil)      => nil
;;              (trim-both-once \a \b nil)    => nil))
