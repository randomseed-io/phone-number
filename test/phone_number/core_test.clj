(ns

    ^{:doc    "phone-number library, core tests."
      :author "Paweł Wilk"
      :added  "8.12.4-0"
      :no-doc true}

    phone-number.core-test

  (:refer-clojure :exclude [format type])

  (:require [clojure.spec.alpha      :as               s]
            [midje.sweet             :refer         :all]
            [midje.experimental      :refer    [for-all]]
            [clojure.spec.gen.alpha  :as             gen]
            [orchestra.spec.test     :as              st]
            [phone-number            :as              PN]
            [phone-number.core       :refer         :all]
            [phone-number.spec       :as            spec]
            [expound.alpha           :as         expound])

  (:import  [com.google.i18n.phonenumbers
             Phonenumber$PhoneNumber
             NumberParseException]))

(s/check-asserts true)

#_(alter-var-root #'*default-dialing-region* (constantly :us))

(defmacro with-dfl-dialing-reg-us
  [& body]
  `(binding [*default-dialing-region* :us]
     ~@body))

(defmacro with-dfl-dialing-reg-gb
  [& body]
  `(binding [*default-dialing-region* :gb]
     ~@body))

(defmacro without-dfl-dialing-reg
  [& body]
  `(binding [*default-dialing-region* nil]
     ~@body))

(def text            "This is my number +448081570 001 Bye! 1a2b3c OK")
(def found-number    {:phone-number/info (info "+448081570 001")
                      :phone-number.match/raw-string "+448081570 001"
                      :phone-number.match/start      18
                      :phone-number.match/end        32})
(def found-number-pl {:phone-number/info (info "+448081570 001" nil :pl)
                      :phone-number.match/raw-string "+448081570 001"
                      :phone-number.match/start      18
                      :phone-number.match/end        32})
(def found-number-en {:phone-number/info (info "+448081570 001" nil :en)
                      :phone-number.match/raw-string "+448081570 001"
                      :phone-number.match/start      18
                      :phone-number.match/end        32})

(def found-number-dial-de
  {:phone-number/info (info "+448081570 001" nil nil :phone-number.region/de)
   :phone-number.match/raw-string "+448081570 001"
   :phone-number.match/start      18
   :phone-number.match/end        32})

(def found-number-en-dial-de
  {:phone-number/info (info "+448081570 001" nil :en :phone-number.region/de)
   :phone-number.match/raw-string "+448081570 001"
   :phone-number.match/start      18
   :phone-number.match/end        32})

(def found-number-pl-dial-de
  {:phone-number/info (info "+448081570 001" nil :pl :phone-number.region/de)
   :phone-number.match/raw-string "+448081570 001"
   :phone-number.match/start      18
   :phone-number.match/end        32})

(def find-number  (comp #(dissoc % :phone-number/number)
                        (partial into {})
                        first (partial find-numbers)))

(def find-number-opts (comp #(dissoc % :phone-number/number)
                            (partial into {})
                            first (partial find-numbers-opts)))

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
             (number "abc1")                                       => (throws clojure.lang.ExceptionInfo)
             (number "")                                           => (throws clojure.lang.ExceptionInfo)
             (number 1)                                            => (throws clojure.lang.ExceptionInfo)
             (number 12)                                           => (throws clojure.lang.ExceptionInfo)
             (number 998)                                          => (throws clojure.lang.ExceptionInfo)
             (number "998")                                        => (throws NumberParseException)
             (number {:a 1})                                       =future=> (throws clojure.lang.ExceptionInfo)))

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

(def valid-map-e164
  {:phone-number.format/e164 "+448081570001"
   :phone-number/calling-code 44
   :phone-number/geographical? true
   :phone-number/possible? true
   :phone-number/type :phone-number.type/toll-free
   :phone-number/valid? true
   :phone-number.short/possible? true
   :phone-number.short/valid? false})

(facts "about `valid?`"
       (fact "when it validates correct numbers"
             (valid? "+448081570001")                                         => true
             (valid? "+448081570001" nil)                                     => true
             (with-dfl-dialing-reg-us (valid? "+448081570001" nil nil))       => false
             (valid? "+448081570001" nil :gb)                                 => true
             (valid? "+448081570001" nil :pl)                                 => false)
       (fact "when it uses dialing region when source is a map (and up to 1 argument more)"
             (with-dfl-dialing-reg-gb (valid? valid-map-e164))                => true
             (with-dfl-dialing-reg-gb (valid? valid-map-e164 nil))            => true
             (with-dfl-dialing-reg-gb (valid?
                                       (merge
                                        valid-map-e164
                                        {:phone-number/dialing-region :gb
                                         :phone-number.dialing-region/derived? true}))) => true
             (without-dfl-dialing-reg (valid? valid-map-e164))                => true
             (without-dfl-dialing-reg (valid? valid-map-e164 nil))            => true
             (without-dfl-dialing-reg (valid?
                                       (merge
                                        valid-map-e164
                                        {:phone-number/dialing-region :gb
                                         :phone-number.dialing-region/derived? true}))) => true)
       (fact "when it uses dialing region when source is a map (and 2 more arguments)"
             (with-dfl-dialing-reg-gb
               (valid? {:phone-number.format/e164 "+448081570001"} nil nil)) => true
             (valid? {:phone-number.format/e164 "+448081570001"} nil :pl)    => false
             (valid? {:phone-number.format/e164 "+448081570001"} nil :gb)    => true
             (with-dfl-dialing-reg-gb
               (valid? {:phone-number.format/e164 "+448081570001"
                        :phone-number/dialing-region :gb} nil nil))          => true
             (with-dfl-dialing-reg-gb
               (valid? {:phone-number.format/e164 "+448081570001"
                        :phone-number/dialing-region :gb
                        :phone-number.dialing-region/derived? true} nil nil)) => true
             (without-dfl-dialing-reg
              (valid? {:phone-number.format/e164 "+448081570001"} nil nil))   => true
             (valid? {:phone-number.format/e164 "+448081570001"} nil :pl)     => false
             (valid? {:phone-number.format/e164 "+448081570001"} nil :gb)     => true
             (without-dfl-dialing-reg
              (valid? {:phone-number.format/e164 "+448081570001"
                       :phone-number/dialing-region :gb} nil nil))            => true
             (without-dfl-dialing-reg
              (valid? {:phone-number.format/e164 "+448081570001"
                       :phone-number/dialing-region :gb
                       :phone-number.dialing-region/derived? true} nil nil))  => true
             (without-dfl-dialing-reg
              (valid? {:phone-number.format/e164 "+448081570001"
                       :phone-number/dialing-region :us
                       :phone-number.dialing-region/derived? true} nil nil))  => true
             (without-dfl-dialing-reg
              (valid? {:phone-number.format/e164 "+448081570001"
                       :phone-number/dialing-region :us
                       :phone-number.dialing-region/derived? false} nil nil))  => false))

(facts "about `find-numbers`"
       (fact "when it finds single number in text"
             (find-number text)                                 => found-number
             (find-number text :gb)                             => found-number
             (find-number text :pl)                             => found-number
             (find-number text :valid)                          => found-number
             (find-number text :phone-number.leniency/valid)    => found-number
             (find-number text :exact)                          => {}
             (find-number text :strict)                         => {}
             (find-number text :gb :vaild)                      => found-number
             (find-number text :gb :strict)                     => {}
             (find-number text :gb :en)                         => found-number-en
             (find-number text :gb :pl)                         => found-number-pl
             (find-number text :gb :phone-number.locale/pl)     => found-number-pl
             (find-number text :gb 2)                           => found-number
             (find-number text :gb 0)                           => {}
             (find-number text :valid 2)                        => found-number
             (find-number text :valid 0)                        => {}
             (find-number text :strict 2)                       => {}
             (find-number text :strict 0)                       => {}
             (find-number text :valid :pl)                      => found-number-pl
             (find-number text :valid :en)                      => found-number-en
             (find-number text :strict :pl)                     => {}
             (find-number text :strict :en)                     => {}
             (find-number text :gb :valid 2)                    => found-number
             (find-number text :gb :strict 2)                   => {}
             (find-number text :gb :valid 0)                    => {}
             (find-number text :gb :strict 2)                   => {}
             (find-number text :gb :strict 0)                   => {}
             (find-number text :gb :en :de)                     => found-number-en-dial-de
             (find-number text :gb :pl :de)                     => found-number-pl-dial-de
             (find-number text :gb :phone-number.locale/pl :de) => found-number-pl-dial-de
             (find-number text :gb :valid  2)                   => found-number
             (find-number text :gb :strict 2 :pl)               => {}
             (find-number text :gb :valid  0 :en)               => {}
             (find-number text :gb :strict 2 :phone-number.locale/pl) => {}
             (find-number text :gb :strict 0 :pl)               => {}
             (find-number text :gb :valid  2 :pl)               => found-number-pl
             (find-number text :gb :valid  2 :en)               => found-number-en
             (find-number text :gb :strict 2 :phone-number.region/pl)     => {}
             (find-number text :gb :valid  0 :phone-number.region/gb)     => {}
             (find-number text :gb :strict 2 :phone-number.region/gb)     => {}
             (find-number text :gb :strict 0 :phone-number.region/de)     => {}
             (find-number text :gb :valid  2 :phone-number.region/de)     => found-number-dial-de
             (find-number text :gb :valid  2 :phone-number.region/de)     => found-number-dial-de
             (find-number text :gb :strict 2 :pl :phone-number.region/pl) => {}
             (find-number text :gb :valid  0 :en :phone-number.region/gb) => {}
             (find-number text :gb :strict 2 :phone-number.locale/pl :phone-number.region/gb) => {}
             (find-number text :gb :strict 0 :pl :phone-number.region/de) => {}
             (find-number text :gb :valid  2 :pl :phone-number.region/de) => found-number-pl-dial-de
             (find-number text :gb :valid  2 :en :phone-number.region/de) => found-number-en-dial-de
             (find-number text :gb :strict 2 :pl :pl) => {}
             (find-number text :gb :valid  0 :en :gb) => {}
             (find-number text :gb :strict 2 :phone-number.locale/pl :gb) => {}
             (find-number text :gb :strict 0 :pl :de) => {}
             (find-number text :gb :valid  2 :pl :de) => found-number-pl-dial-de
             (find-number text :gb :valid  2 :en :de) => found-number-en-dial-de))

(facts "about `find-numbers-opts`"
       (fact "when it finds a single number in text"
             (find-number-opts text {:region-code :gb
                                    :leniency    :valid
                                    :max-tries   2}) => found-number)
       (fact "when it disables info generation"
             (find-number-opts text {:region-code :gb
                                    :leniency    :valid
                                    :max-tries   2
                                    :locale-specification false})
             => {:phone-number.match/raw-string "+448081570 001"
                 :phone-number.match/start      18
                 :phone-number.match/end        32}))
