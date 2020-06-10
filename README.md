# phone-number – validation and inspection of phone numbers

[![Phone-number on Clojars](https://img.shields.io/clojars/v/io.randomseed/phone-number.svg)](https://clojars.org/io.randomseed/phone-number)

Clojure library which uses Google's Libphonenumber to validate, inspect and generate phone numbers.

## Features

* Polymorphic interface (phone numbers can be expressed as numbers, strings,
  `PhoneNumber` objects or maps).

* Supported operations: creation, validation, generation, matching, string-searching,
  formatting, specification testing and properties reporting.

* Promotes keyword-indexed maps with namespace inference
  (e.g. key can be `:phone-number/type` or just `:type`,
  type can be `:phone-number.type/mobile` or just `:mobile`).

* Uses lazily evaluated map values to store phone number information during
  text-searching and samples generation.

* Provides specs with generators.

## Sneak peeks

* It **shows information** about phone numbers:

```clojure
(require '[phone-number.core :as phone])

;; region taken from a phone number
;; using system's default locale

(phone/info "+44 29 2018 3133")

{:phone-number/country-code               44,
 :phone-number/geographical?              true,
 :phone-number/location                   "Cardiff",
 :phone-number/possible?                  true,
 :phone-number/region                     :phone-number.region/gb,
 :phone-number/type                       :phone-number.type/fixed-line,
 :phone-number/valid?                     true,
 :phone-number.format/e164                "+442920183133",
 :phone-number.format/international       "+44 29 2018 3133",
 :phone-number.format/national            "029 2018 3133",
 :phone-number.format/raw-input           "+44 29 2018 3133",
 :phone-number.format/rfc3966             "tel:+44-29-2018-3133",
 :phone-number.tz-format/full-standalone  '("Greenwich Mean Time"),
 :phone-number.tz-format/id               '("Europe/London"),
 :phone-number.tz-format/short-standalone '("GMT"),
 :phone.number.short/possible?            false,
 :phone.number.short/valid?               false}

;; region passed as an argument
;; locale setting passed as an argument

(phone/info "601 100 601" :pl :pl)

{:phone-number/carrier                    "Plus",
 :phone-number/country-code               48,
 :phone-number/geographical?              false,
 :phone-number/location                   "Polska",
 :phone-number/possible?                  true,
 :phone-number/region                     :phone-number.region/pl,
 :phone-number/type                       :phone-number.type/mobile,
 :phone-number/valid?                     true,
 :phone-number.format/e164                "+48601100601",
 :phone-number.format/international       "+48 601 100 601",
 :phone-number.format/national            "601 100 601",
 :phone-number.format/raw-input           "601 100 601",
 :phone-number.format/rfc3966             "tel:+48-601-100-601",
 :phone-number.tz-format/full-standalone  '("Czas środkowoeuropejski"),
 :phone-number.tz-format/id               '("Europe/Warsaw"),
 :phone-number.tz-format/short-standalone '("CET"),
 :phone.number.short/possible?            false,
 :phone.number.short/valid?               false}

(phone/info "8081 570001" :gb :phone-number.region/jp)

{:phone-number/country-code               44,
 :phone-number/geographical?              false,
 :phone-number/possible?                  true,
 :phone-number/region                     :phone-number.region/gb,
 :phone-number/type                       :phone-number.type/toll-free,
 :phone-number/valid?                     true,
 :phone-number.format/e164                "+448081570001",
 :phone-number.format/international       "+44 808 157 0001",
 :phone-number.format/national            "0808 157 0001",
 :phone-number.format/raw-input           "8081 570001",
 :phone-number.format/rfc3966             "tel:+44-808-157-0001",
 :phone-number.tz-format/full-standalone  '("Greenwich Mean Time" "British Time"),
 :phone-number.tz-format/id               '("Europe/Guernsey"
                                            "Europe/Isle_of_Man"
                                            "Europe/Jersey"
                                            "Europe/London"),
 :phone-number.tz-format/short-standalone  '("GMT" "BT"),
 :phone.number.short/possible?             false,
 :phone.number.short/valid?                false}
```

* It **validates** phone numbers:

```clojure
(require '[phone-number.core :as phone])

(phone/valid? 8081570001 :gb)      ; => true
(phone/valid? "+448081570001")     ; => true
(phone/valid? 8081570001 :pl)      ; => false
(phone/valid? "8081570001")        ; => false

(phone/possible? "8081570001")     ; => false
(phone/possible? "8081570001" :gb) ; => true
(phone/possible? "8081570001" :pl) ; => true
```

* It gives known phone number **formats** and **types**:

```clojure
(require '[phone-number.core :as phone])

phone/formats

#{:phone-number.format/e164
  :phone-number.format/international
  :phone-number.format/national
  :phone-number.format/raw-input
  :phone-number.format/rfc3966}

phone/types

#{:phone-number.type/fixed-line
  :phone-number.type/fixed-line-or-mobile
  :phone-number.type/mobile
  :phone-number.type/pager
  :phone-number.type/personal
  :phone-number.type/premium-rate
  :phone-number.type/shared-cost
  :phone-number.type/toll-free
  :phone-number.type/uan
  :phone-number.type/unknown
  :phone-number.type/voicemail
  :phone-number.type/voip}
```

* It **generates** phone numbers:

```clojure
(phone/generate)

{:phone-number/info {:phone-number/country-code         213,
                     :phone-number/geographical?        false,
                     :phone-number/possible?            true,
                     :phone-number/region               :phone-number.region/dz,
                     :phone-number/type                 :phone-number.type/unknown,
                     :phone-number/valid?               false,
                     :phone-number.format/e164          "+213181525997",
                     :phone-number.format/international "+213 181525997",
                     :phone-number.format/national      "181525997",
                     :phone-number.format/rfc3966       "tel:+213-181525997",
                     :phone.number.short/possible?      false,
                     :phone.number.short/valid?         false},
 :phone-number/number #<com.google.i18n.phonenumbers.Phonenumber$PhoneNumber@3edea9e6>,
 :phone-number.sample/digits      ["+213" nil "181525997"],
 :phone-number.sample/hits        10,
 :phone-number.sample/max-samples 1000,
 :phone-number.sample/random-seed 7521527664400716800,
 :phone-number.sample/samples     11}

(require [phone-number.spec       :as spec]
         [clojure.spec.alpha      :as    s]
         [clojure.spec.gen.alpha  :as  gen])

(gen/generate (s/gen :phone-number/valid))

{:phone-number/info   #delay[{:status :pending, :val nil} 0x3810d15d],
 :phone-number/number #<com.google.i18n.phonenumbers.Phonenumber$PhoneNumber@79cc08fb>,
 :phone-number.sample/digits      ["+7" nil "937627908"],
 :phone-number.sample/hits        11,
 :phone-number.sample/max-samples 150,
 :phone-number.sample/random-seed 7581363778716192180,
 :phone-number.sample/samples     27}

(gen/generate (s/gen (s/and :phone-number/possible :phone-number/invalid)))

{:phone-number/info    #delay[{:status :pending, :val nil} 0x41c0e225],
 :phone-number/number #<com.google.i18n.phonenumbers.Phonenumber$PhoneNumber@36a74c18>,
 :phone-number.sample/digits      ["+84" nil "0270454"],
 :phone-number.sample/hits        8,
 :phone-number.sample/max-samples 200,
 :phone-number.sample/random-seed -9105741821593959780,
 :phone-number.sample/samples     12}
```

And more…

## Installation

To use phone-number in your project, add the following to dependencies section of
`project.clj` or `build.boot`:

```clojure
[io.randomseed/phone-number "8.12.4-0"]
```

For `deps.edn` add the following as an element of a map under `:deps` or
`:extra-deps` key:

```clojure
io.randomseed/phone-number {:mvn/version "8.12.4-0"}
```

Additionally you can use (in your development profile):

```clojure
org.clojure/spec.alpha {:mvn/version "0.2.176"}
org.clojure/test.check {:mvn/version "0.10.0-alpha4"}
```

If you want to utilize specs and generators provided by the phone-number.

You can also download JAR from [Clojars](https://clojars.org/io.randomseed/phone-number).

## Documentation

Full documentation including usage examples is available at:

* https://randomseed.io/software/phone-number/

## License

Copyright © 2020 Paweł Wilk

Phone-number is copyrighted software owned by Paweł Wilk (pw@gnu.org). You may
redistribute and/or modify this software as long as you comply with the terms of
the [GNU Lesser General Public License][LICENSE] (version 3).

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

## Development

[![CircleCI](https://circleci.com/gh/randomseed-io/phone-number.svg?style=svg)](https://circleci.com/gh/randomseed-io/phone-number)

### Building docs

```bash
make docs
```

### Building JAR

```bash
make jar
```

### Rebuilding POM

```bash
make pom
```

### Signing POM

```bash
make sig
```

### Deploying to Clojars

```bash
make deploy
```

### Interactive development

```bash
bin/repl
```

Starts REPL and nREPL server (port number is stored in `.nrepl-port`).

[LICENSE]:    https://github.com/randomseed-io/phone-number/blob/master/LICENSE
