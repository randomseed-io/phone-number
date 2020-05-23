(ns user
  (:require
   [clojure.spec.alpha           :as                s]
   [clojure.repl                 :refer          :all]
   [clojure.tools.namespace.repl :refer [refresh
                                         refresh-all]]
   [phone-number.core            :as            phone]
   [phone-number.spec            :as               sp]
   [puget.printer                :refer      [cprint]]
   [infra]))

(set! *warn-on-reflection* true)

(when (System/getProperty "nrepl.load")
  (require 'nrepl))

(defn test-all []
  (refresh))

(comment 
(refresh-all)
(test-all)

)
