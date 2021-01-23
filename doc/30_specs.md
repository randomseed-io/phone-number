# Specs and checks

## Predicates

The phone-number provides many predicate functions that can be used to check for
certain properties of phone numbers. These include:

| Function Name                                       | Description                                               |
|-----------------------------------------------------|-----------------------------------------------------------|
| [`core/valid-input?`](phone-number.core.html#var-valid-input.3F) | Checks if a value can possibly be used as a phone number |
| [`core/valid?`](phone-number.core.html#var-valid.3F) | Checks if a phone numer is valid |
| [`core/invalid?`](phone-number.core.html#var-invalid.3F) | Checks if a phone numer is invalid |
| [`core/possible?`](phone-number.core.html#var-possible.3F) | Checks if a phone numer is a possible number |
| [`core/impossible?`](phone-number.core.html#var-impossible.3F) | Checks if a phone numer is an impossible number |
| [`core/native?`](phone-number.core.html#var-native.3F) | Checks if a phone numer is in a native format (`PhoneNumber`) |
| [`core/has-raw-input?`](phone-number.core.html#var-has-raw-input.3F) | Checks if a phone numer contains the retained raw input |
| [`core/geographical?`](phone-number.core.html#var-geographical.3F) | Checks if a phone numer identifies a line with some geographical location |
| [`core/short-possible?`](phone-number.core.html#var-short-possible.3F) | Checks if a phone numer is in a possible short number |
| [`core/short-valid?`](phone-number.core.html#var-short-valid.3F) | Checks if a phone numer is in a valid short number |
| [`core/short-emergency?`](phone-number.core.html#var-short-emergency.3F) | Checks if a phone numer is in an emergency number |
| [`core/short-to-emergency?`](phone-number.core.html#var-short-to-emergency.3F) | Checks if a phone numer can be used to connect to an emergency |
| [`core/short-to-emergency?`](phone-number.core.html#var-short-to-emergency.3F) | Checks if a phone numer can be used to connect to an emergency |


## Specs

Clojure Spec is used in phone-number library to validate data and generate
property-based tests. Most of the defined specs have generators attached.

There are two groups of specs: first are specs that are used **internally** to test
the library for corner cases and make sure that new features or other changes won't
break anything, second are **generic** specs, created for easy data validation in
programs that are already using specs.

This page describes the generic specs. To use them just require `phone-number.spec`,
`clojure.spec.alpha` and optionally `clojure.spec.gen.alpha`:

``` clojure
(require [phone-number.spec       :as spec]
         [clojure.spec.alpha      :as    s]
         [clojure.spec.gen.alpha  :as  gen])
```

| Spec Name                         | Data description                                      |
|-----------------------------------|-------------------------------------------------------|
|`:phone-number/region`             | region code                                           |
|`:phone-number/type`               | phone number type                                     |
|`:phone-number/leniency`           | phone number leniency                                 |
|`:phone-number/format`             | phone number format                                   |
|`:phone-number/format-global`      | phone number format<br>(with calling code prefix)     |
|`:phone-number/format-regional`    | phone number format<br>(without calling code prefix)  |
|`:phone-number/tz-format`          | time zone format                                      |
|`:phone-number/net-code`           | global network calling code                           |
|`:phone-number/country-code`       | country calling code                                  |
|`:phone-number/calling-code`       | calling code (either global or country)               |
|`:phone-number/cost`               | cost class of short numbers                           |
|`:phone-number/native`             | phone number in native format<br>(`PhoneNumber` object)|
|`:phone-number/native-valid`       | native phone number which is valid                    |
|`:phone-number/string`             | phone number as a string                              |
|`:phone-number/string-global`      | phone number as a string which includes calling code  |
|`:phone-number/string-regional`    | phone number as a string without calling code         |
|`:phone-number/string-valid`       | valid phone number as a string                        |
|`:phone-number/string-invalid`     | invalid phone number as a string                      |
|`:phone-number/numeric`            | phone number as natural number without calling code   |
|`:phone-number/info`               | phone number as a map                                 |
|`:phone-number/info-valid`         | valid phone number as a map                           |
|`:phone-number/info-invalid`       | invalid phone number as a map                         |
|`:phone-number/possible`           | possible phone number                                 |
|`:phone-number/impossible`         | impossible phone number                               |
|`:phone-number/unknown`            | phone number of unknown type                          |
|`:phone-number/has-region`         | phone number with region code information             |
|`:phone-number/has-calling-code`   | phone number with calling code                        |
|`:phone-number/has-location`       | phone number having geographical location             |
|`:phone-number/has-time-zone`      | phone number having time zone information             |
|`:phone-number/has-known-type`     | phone number of known type                            |
|`:phone-number/mobile`             | mobile phone number                                   |
|`:phone-number/fixed-line`         | fixed-line phone number                               |
|`:phone-number/toll-free`          | toll-free phone number                                |
|`:phone-number/premium-rate`       | premium-rate phone number                             |
|`:phone-number/shared-cost`        | shared-cost phone number                              |
|`:phone-number/voip`               | VoIP phone number                                     |
|`:phone-number/personal `          | personal phone number                                 |
|`:phone-number/pager`              | pager number                                          |
|`:phone-number/uan`                | UAN number                                            |
|`:phone-number/voicemail`          | voicemail number                                      |
|`:phone-number/fixed-line-or-mobile`| fixed-line or mobile number (incl. uncertain)        |
|`:phone-number/uncertain-fixed-line-or-mobile` | uncertain number (fixed line or mobile)   |
|`:phone-number/maybe-mobile`       | uncertain number (fixed line or mobile) or mobile     |
|`:phone-number/maybe-fixed-line`   | uncertain number (fixed line or mobile) or fixed-line |
|`:phone-number/short`              | valid short number                                    |
|`:phone-number/maybe-short`        | possible short number                                 |
|`:phone-number.short/valid`        | valid short number                                    |
|`:phone-number.short/invalid`      | invalid short number                                  |
|`:phone-number.short/possible`     | possible short number                                 |
|`:phone-number.short/carrier-specific`| carrier-specific short number                      |
|`:phone-number.short/info`         | short number properties as a map                      |
|`:phone-number.arg/number`         | phone number (with calling code or without)           |

Phone-number also contains specs for functions, however they are intended for testing
and it is **NOT RECOMMENDED** to enable **instrumentation** in production.

Please note that if you intend to use phone-number's specs in a program (not in
phone-number's REPL) you should add proper dependencies since production profile of
phone-number does not depend on spec nor test library. Example, minimal `deps.edn`:

``` clojure
{:paths ["src" "resources"]

 :deps {io.randomseed/phone-number {:mvn/version "8.12.16-0"}}

 :aliases {
           :dev {
                 :extra-deps {org.clojure/spec.alpha  {:mvn/version "0.2.194"}
                              org.clojure/test.check  {:mvn/version "1.1.0"}}
                 :extra-paths ["dev/src" "test"]
                 :jvm-opts ["-Dclojure.spec.compile-asserts=true"]}

           :test {
                  :extra-paths ["test"]
                  :extra-deps {org.clojure/test.check {:mvn/version "1.1.0"}}}}
```
