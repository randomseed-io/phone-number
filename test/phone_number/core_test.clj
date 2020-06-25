(ns

    ^{:doc    "phone-number library, core tests."
      :author "Paweł Wilk"
      :added  "8.12.4-0"
      :no-doc true}

    phone-number.core-test

  (:refer-clojure :exclude [format type])

  (:require [clojure.spec.alpha      :as             s]
            [midje.sweet             :refer       :all]
            [midje.experimental      :refer  [for-all]]
            [clojure.spec.gen.alpha  :as           gen]
            [orchestra.spec.test     :as            st]
            [phone-number.core       :refer       :all]
            [phone-number.spec       :as          spec]
            [expound.alpha           :as       expound])

  (:import  [com.google.i18n.phonenumbers
             Phonenumber$PhoneNumber
             NumberParseException]))

(s/check-asserts true)

#_(alter-var-root #'*default-dialing-region* (constantly :us))

(facts "about `number`"
       (fact "when it returns nil for nil or empty"
             (number nil) => nil
             (number {})  => nil)
       (fact "when it returns PhoneNumber object for a string"
             (class (number "+48 998"))                            => Phonenumber$PhoneNumber
             (class (number "+448081570001"))                      => Phonenumber$PhoneNumber
             (class (number "8081570001" :gb))                     => Phonenumber$PhoneNumber
             (class (number "8081570001" :phone-number.region/gb)) => Phonenumber$PhoneNumber
             (number "+448081570001")                              => native?)
       (fact "when it fails on bad input"
             (number "abc1")                                       => (throws AssertionError)
             (number "")                                           => (throws AssertionError)
             (number 1)                                            => (throws AssertionError)
             (number 12)                                           => (throws AssertionError)
             (number 998)                                          => (throws AssertionError)
             (number "998")                                        => (throws NumberParseException)
             (number {:a 1})                                       =future=> (throws AssertionError)))

(facts "about `info`"
       (fact "when it returns nil for nil or empty"
             (info nil) => nil
             (info {})  => nil)
       (fact "when it retains dialing region when source is a map"
             (:phone-number/dialing-region (info "112" :pl :pl :pl))        => :phone-number.region/pl
             (:phone-number/dialing-region (info (info "112" :pl :pl :pl))) => :phone-number.region/pl)
       (fact "when it is identical for long numbers when called twice"
             (info (info "+448081570001"))                                  => (info "+448081570001")
             (info (info "+448081570001" :gb))                              => (info "+448081570001" :gb)
             (info (info "8081570001" :gb))                                 => (info "8081570001" :gb)
             (info (info "8081570001" :gb :en) nil :en)                     => (info "8081570001" :gb :en)
             (info (info "8081570001" :gb :en :gb) nil :en)                 => (info "8081570001" :gb :en :gb)
             (info (info "8081570001" :gb :en) :gb :en :gb)                 => (info "8081570001" :gb :en :gb))
       (fact "when it is identical for short numbers when called twice"
             (info (info "112" :pl))                                        => (info "112" :pl)
             (info (info "+48112" :pl))                                     => (info "+48112" :pl)
             (info (info "112" :pl :en :pl))                                => (info "112" :pl :en :pl)))

(facts "about `valid?`"
       (fact "when it validates correct numbers"
             (valid? "+448081570001")                                       => true
             (valid? "+448081570001" nil)                                   => true
             (valid? "+448081570001" nil nil)                               => false
             (valid? "+448081570001" nil :gb)                               => true
             (valid? "+448081570001" nil :pl)                               => false)
       (fact "when it uses dialing region when source is a map (and up to 1 argument more)"
             (valid? {:phone-number.format/e164 "+448081570001"})           => true
             (valid? {:phone-number.format/e164 "+448081570001"} nil)       => true
             (valid? {:phone-number.format/e164 "+448081570001"
                      :phone-number/dialing-region :gb
                      :phone-number.dialing-region/derived? true})          => true)
       (fact "when it uses dialing region when source is a map (and 2 more arguments)"
             (valid? {:phone-number.format/e164 "+448081570001"} nil nil)   => false
             (valid? {:phone-number.format/e164 "+448081570001"} nil :pl)   => false
             (valid? {:phone-number.format/e164 "+448081570001"} nil :gb)   => true
             (valid? {:phone-number.format/e164 "+448081570001"
                      :phone-number/dialing-region :gb} nil nil)            => true
             (valid? {:phone-number.format/e164 "+448081570001"
                      :phone-number/dialing-region :gb
                      :phone-number.dialing-region/derived? true} nil nil)  => false))
