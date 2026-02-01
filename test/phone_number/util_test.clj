(ns

    ^{:doc    "phone-number library, util tests."
      :author "Paweł Wilk"
      :added  "3.23-0"
      :no-doc true}

    phone-number.util-test

  (:require [midje.sweet      :refer :all]
            [phone-number.util :as util]))

(facts "about `try-parse` and `try-parse-or-false`"
       (fact "they swallow only phone-number ExceptionInfo (with :phone-number/error)"
             (util/try-parse (throw (ex-info "x" {:phone-number/error ::x}))) => nil
             (util/try-parse-or-false (throw (ex-info "x" {:phone-number/error ::x}))) => false)
       (fact "they rethrow non-phone-number ExceptionInfo"
             (util/try-parse (throw (ex-info "x" {:x 1}))) => (throws clojure.lang.ExceptionInfo)
             (util/try-parse-or-false (throw (ex-info "x" {:x 1}))) => (throws clojure.lang.ExceptionInfo)))

