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

Clojure Spec is used in the phone-number library to validate data and generate
property-based tests. Most of the defined specs have generators attached.

There are 3 groups of specs:

* **Input specs**, created for easy data validation.  
  (ns-prefix: `phone-number.input`)

* **Generic specs** used to describe the output data.  
  (ns-prefix: `phone-number`)

* Specs that are used **internally** to test the library for corner cases
  and make sure that new features or other changes won't break anything.  
  (ns-prefixes: `phone-number.arg`, `phone-number.args`, `phone-number.props`)

This page lists the input and some of the generic specs. To use them just require
`phone-number.spec`, `clojure.spec.alpha` and optionally `clojure.spec.gen.alpha`:

``` clojure
(require [phone-number.spec       :as spec]
         [clojure.spec.alpha      :as    s]
         [clojure.spec.gen.alpha  :as  gen])
```

| Spec Name                         | Data description                                      |
|-----------------------------------|-------------------------------------------------------|
|`:phone-number.input/region`             | region code                                           |
|`:phone-number.input/type`               | phone number type                                     |
|`:phone-number.input/leniency`           | phone number leniency                                 |
|`:phone-number.input/format`             | phone number format                                   |
|`:phone-number.input/format-global`      | phone number format<br>(with calling code prefix)     |
|`:phone-number.input/format-regional`    | phone number format<br>(without calling code prefix)  |
|`:phone-number.input/tz-format`          | time zone format                                      |
|`:phone-number.input/net-code`           | global network calling code                           |
|`:phone-number.input/country-code`       | country calling code                                  |
|`:phone-number.input/calling-code`       | calling code (either global or country)               |
|`:phone-number.input/cost`               | cost class of short numbers                           |
|`:phone-number.input/native`             | phone number in native format<br>(`PhoneNumber` object)|
|`:phone-number.input/native-valid`       | native phone number which is valid                    |
|`:phone-number.input/string`             | phone number as a string                              |
|`:phone-number.input/string-global`      | phone number as a string with a calling code          |
|`:phone-number.input/string-regional`    | phone number as a string without a calling code       |
|`:phone-number.input/string-valid`       | valid phone number as a string                        |
|`:phone-number.input/string-invalid`     | invalid phone number as a string                      |
|`:phone-number.input/numeric`            | phone number as natural number without calling code   |
|`:phone-number/info`                     | phone number as a map                                 |
|`:phone-number/info-valid`               | valid phone number as a map                           |
|`:phone-number/info-invalid`             | invalid phone number as a map                         |
|`:phone-number.input/possible`           | possible phone number                                 |
|`:phone-number.input/impossible`         | impossible phone number                               |
|`:phone-number.input/unknown`            | phone number of unknown type                          |
|`:phone-number.input/has-region`         | phone number with region code information             |
|`:phone-number.input/has-calling-code`   | phone number with calling code                        |
|`:phone-number.input/has-location`       | phone number having geographical location             |
|`:phone-number.input/has-time-zone`      | phone number having time zone information             |
|`:phone-number.input/has-known-type`     | phone number of known type                            |
|`:phone-number.input/mobile`             | mobile phone number                                   |
|`:phone-number.input/fixed-line`         | fixed-line phone number                               |
|`:phone-number.input/toll-free`          | toll-free phone number                                |
|`:phone-number.input/premium-rate`       | premium-rate phone number                             |
|`:phone-number.input/shared-cost`        | shared-cost phone number                              |
|`:phone-number.input/voip`               | VoIP phone number                                     |
|`:phone-number.input/personal `          | personal phone number                                 |
|`:phone-number.input/pager`              | pager number                                          |
|`:phone-number.input/uan`                | UAN number                                            |
|`:phone-number.input/voicemail`          | voicemail number                                      |
|`:phone-number.input/fixed-line-or-mobile`| fixed-line or mobile number (incl. uncertain)        |
|`:phone-number.input/uncertain-fixed-line-or-mobile` | uncertain number (fixed line or mobile)   |
|`:phone-number.input/maybe-mobile`       | uncertain number (fixed line or mobile) or mobile     |
|`:phone-number.input/maybe-fixed-line`   | uncertain number (fixed line or mobile) or fixed-line |
|`:phone-number.input/short`              | valid short number                                    |
|`:phone-number.input/maybe-short`        | possible short number                                 |
|`:phone-number.input.short/valid`        | valid short number                                    |
|`:phone-number.input.short/invalid`      | invalid short number                                  |
|`:phone-number.input.short/possible`     | possible short number                                 |
|`:phone-number.input.short/carrier-specific`| carrier-specific short number                      |
|`:phone-number.input.short/info`         | short number properties as a map                      |
|`:phone-number.input/number`             | phone number (with calling code or without)           |

Phone-number also contains specs for functions, however they are intended for testing
and it is **NOT RECOMMENDED** to enable the **instrumentation** in production.

Please note that if you intend to use phone-number's specs in a program (not in
phone-number's REPL) you should add proper dependencies since production profile of
phone-number does not depend on spec nor test library. Example, minimal `deps.edn`:

``` clojure
{:paths ["src" "resources"]

 :deps {io.randomseed/phone-number {:mvn/version "8.12.16-1"}}

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
