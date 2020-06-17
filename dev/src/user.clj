(ns user
  (:require
   [clojure.spec.alpha           :as                s]
   [orchestra.spec.test          :as               st]
   [clojure.spec.gen.alpha       :as              gen]
   [clojure.repl                 :refer          :all]
   [clojure.tools.namespace.repl :refer [refresh
                                         refresh-all]]
   [expound.alpha                :as          expound]
   [phone-number.core            :as            phone]
   [phone-number.util            :as             util]
   [phone-number.spec            :as             spec]
   [puget.printer                :refer      [cprint]]
   [midje.repl                   :refer          :all]
   [kaocha.repl                  :refer          :all]
   [infra])

  (:import [com.google.i18n.phonenumbers
            Phonenumber$PhoneNumber
            ShortNumberInfo
            geocoding.PhoneNumberOfflineGeocoder
            PhoneNumberToCarrierMapper
            PhoneNumberToTimeZonesMapper
            NumberParseException]))

(set! *warn-on-reflection* true)

(alter-var-root
 #'s/*explain-out*
 (constantly
  (expound/custom-printer {:show-valid-values? false
                           :print-specs?        true
                           :theme    :figwheel-theme})))

(when (System/getProperty "nrepl.load")
  (require 'nrepl))

(st/instrument)

(defn test-all []
  (refresh)
  (load-facts :print-facts))

(comment 
  (refresh-all)
  (test-all)

  )
