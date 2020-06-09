(ns user
  (:require
   [clojure.spec.alpha           :as                s]
   [clojure.spec.gen.alpha       :as              gen]
   [clojure.repl                 :refer          :all]
   [clojure.tools.namespace.repl :refer [refresh
                                         refresh-all]]
   [phone-number.core            :as            phone]
   [phone-number.util            :as             util]
   [phone-number.spec            :as               sp]
   [puget.printer                :refer      [cprint]]
   [infra])

  (:import [com.google.i18n.phonenumbers
            Phonenumber$PhoneNumber
            ShortNumberInfo
            geocoding.PhoneNumberOfflineGeocoder
            PhoneNumberToCarrierMapper
            PhoneNumberToTimeZonesMapper
            NumberParseException]))

(set! *warn-on-reflection* true)

(when (System/getProperty "nrepl.load")
(require 'nrepl))

(defn test-all []
  (refresh))

(comment 
(refresh-all)
(test-all)

)
