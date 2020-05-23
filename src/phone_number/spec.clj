(ns

    ^{:doc    "Specs for phone-number library."
      :author "Paweł Wilk"
      :added  "8.12.4-0"
      :no-doc true}

    phone-number.spec

  (:require [phone-number.util            :as      util]
            [phone-number.proto           :as     proto]
            [phone-number.core            :as     phone]
            [trptr.java-wrapper.locale    :as         l]
            [clojure.algo.generic.functor :refer [fmap]])

  (:import [phone_number.proto Phoneable]
           [com.google.i18n.phonenumbers
            Phonenumber$PhoneNumber
            ShortNumberInfo
            geocoding.PhoneNumberOfflineGeocoder
            PhoneNumberToCarrierMapper
            PhoneNumberToTimeZonesMapper
            NumberParseException]))
