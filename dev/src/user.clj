(ns user
  (:require
   [clojure.spec.alpha           :as                s]
   [orchestra.spec.test          :as               st]
   [clojure.spec.test.alpha      :as              cst]
   [clojure.spec.gen.alpha       :as              gen]
   [clojure.repl                 :refer          :all]
   [clojure.tools.namespace.repl :refer [refresh
                                         refresh-all]]
   [expound.alpha                :as          expound]
   [phone-number.core            :as            phone]
   [phone-number.util            :as             util]
   [phone-number.spec            :as             spec]
   [phone-number.db              :as               db]
   [phone-number.region          :as           region]
   [phone-number.type            :as             type]

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
  (cst/with-instrument-disabled
    (binding [phone/*default-dialing-region* :phone-number.region/us]
      (load-facts :print-facts))))

(alter-var-root #'*warn-on-reflection*
                (constantly true)
                (when (thread-bound? #'*warn-on-reflection*)
                  (set! *warn-on-reflection* true)))

(comment 
  (refresh-all)
  (cst/with-instrument-disabled (test-all))
  (cst/with-instrument-disabled (run-all))
  )
