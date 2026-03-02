(ns

    ^{:doc    "Optional DB adapters for phone number types."
      :author "Paweł Wilk"
      :added  "9.0.23-3"}

    io.randomseed.utils.db.types.phone-number

  (:require [phone-number.core :as ph]
            [next.jdbc.prepare :as jp])

  (:import  (com.google.i18n.phonenumbers Phonenumber$PhoneNumber)
            (java.sql                     PreparedStatement)))

(set! *warn-on-reflection* true)

(defonce
  ^{:arglists '([])
    :doc      "Extends `next.jdbc.prepare/SettableParameter` protocol to support phone number
  conversions so `com.google.i18n.phonenumbers/Phonenumber$PhoneNumber` data are converted to
  strings (in E.164 format) and then saved."}
  add-setter-phone-number
  (fn []
    (extend-protocol jp/SettableParameter

      Phonenumber$PhoneNumber

      (set-parameter [^Phonenumber$PhoneNumber v ^PreparedStatement ps ^long i]
        (.setString ^PreparedStatement ps i ^String (ph/format v nil :phone-number.format/e164))))))
