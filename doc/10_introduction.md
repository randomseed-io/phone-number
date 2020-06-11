# Introduction

The phone-number library is an interface to the Libphonenumber with some extra
sugar. It allows to validate, inspect and generate phone numbers.

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

## Phone number

A phone number is an abstract that can be a string, an integer number (with the
accompanying region specification), a specially crafted map or a `PhoneNumber` object
(which is the internal representation).

For regular operations (like validating a single number or searching for phone
numbers in strings) it is enough to use data we already have (like a string or
a map).

However, when the performance is important and there is a number of different
operations to be executed on a single phone number it is advised to explicitly
convert the number to its internal representation using the
[`phone-number.core/number`](phone-number.core.html#var-number) function:

```clojure
(require '[phone-number.core :as phone])

(let [n (phone/number "+44 29 2018 3133")]                         …)
(let [n (phone/number     "29 2018 3133" :phone-number.region/gb)] …)
(let [n (phone/number        2920183133  :phone-number.region/gb)] …)
```

### Phone number as an argument

Other forms of a phone number that are acceptable as arguments by most of the
functions this library provides include:

* strings (`"+44 29 2018 3133"`),
* natural numbers (`2920183133`),
* maps:
    * with `PhoneNumber` object:  
```clojure
{:phone-number/number #<Phonenumber$PhoneNumber@3edea9e6>}
```

    * with region code and national number format:  
```clojure
{:phone-number/region :gb
 :phone-number.format/national "2920183133"}
```

    * with calling code and national number format:  
```clojure
{:phone-number/calling-code 44
 :phone-number.format/national "2920183133"}
```

    * with international number format:  
```clojure
{:phone-number.format/international "+44 2920183133"}
```

Note that in case of natural numbers the additional region information is required
(most of the functions will accept it as their second calling argument).

### Namespace inference

In the examples above some regions were set using namespace-qualified keywords and
some were not. This is due to namespace inference when it comes to specifying phone
number properties that are expressed as keywords.

When the dynamic variable
[`phone-number.core/*inferred-namespaces*`](phone-number.core.html#var-*inferred-namespaces*)
is set to `true` (which is the default setting) all functions that require keyword
arguments will automatically enrich simple keywords to be namespace-qualified. That
affects:

* region codes (e.g. `:pl` becomes `phone-number.region/pl`),
* number types (e.g. `:mobile` becomes `phone-number.type/mobile`),
* number formats (e.g. `:international` becomes `phone-number.format/international`),
* time zone formats (e.g. `:short` becomes `:phone-number.tz-format/short`).

## Phone number properties

To get the specific property of a phone number use one of the functions grouped in
[`phone-number.core`](phone-number.core.html) namespace.

Additionally there is the [`info`](phone-number.core.html#var-info) function that
produces a map with most of the properties that can be obtained.

```clojure
(require '[phone-number.core :as phone])

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
```

The `info` function (like most of the functions operating on phone numbers) takes an
additional, second argument which should be a **region code**. It is used only when
the given phone number was expressed in format that cannot provide such information
(a string without calling code, a natural number, a map without any entry containing
region or calling code).

```clojure
(phone/info "0491 570 006" :au)

{:phone-number/calling-code               61,
 :phone-number/carrier                    "Telstra",
 :phone-number/geographical?              false,
 :phone-number/possible?                  true,
 :phone-number/region                     :phone-number.region/au,
 :phone-number/type                       :phone-number.type/mobile,
 :phone-number/valid?                     true,
 :phone-number.format/e164                "+61491570006",
 :phone-number.format/international       "+61 491 570 006",
 :phone-number.format/national            "0491 570 006",
 :phone-number.format/raw-input           "0491 570 006",
 :phone-number.format/rfc3966             "tel:+61-491-570-006",
 :phone-number.tz-format/full-standalone ("Central Australia Time"
                                          "Australian Central Western Time"
                                          "Lord Howe Time"
                                          "Western Australia Time"
                                          "Eastern Australia Time"
                                          "Christmas Island Time"
                                          "Cocos Islands Time"),
:phone-number.tz-format/id               ("Australia/Adelaide"
                                           "Australia/Eucla"
                                           "Australia/Lord_Howe"
                                           "Australia/Perth"
                                           "Australia/Sydney"
                                           "Indian/Christmas"
                                           "Indian/Cocos"),
 :phone-number.tz-format/short-standalone ("ACT"
                                           "ACWT"
                                           "LHT"
                                           "AWT"
                                           "AET"
                                           "CIT"
                                           "CCT"),
 :phone.number.short/possible?            false,
 :phone.number.short/valid?               false}
```

It is worth to note that time zone information is given in English (or the default
locale for the system). That can be changed with a third, optional argument, which
should be a `java.util.Locale` instance or a value that can be converted to it
(string, keyword, symbol).

```clojure
(phone/info "0491 570 006" :au :pl)

{:phone-number/calling-code               61,
 :phone-number/carrier                    "Telstra",
 :phone-number/geographical?              false,
 :phone-number/possible?                  true,
 :phone-number/region                     :phone-number.region/au,
 :phone-number/type                       :phone-number.type/mobile,
 :phone-number/valid?                     true,
 :phone-number.format/e164                "+61491570006",
 :phone-number.format/international       "+61 491 570 006",
 :phone-number.format/national            "0491 570 006",
 :phone-number.format/raw-input           "0491 570 006",
 :phone-number.format/rfc3966             "tel:+61-491-570-006",
 :phone-number.tz-format/full-standalone  ("Czas środkowoaustralijski"
                                           "Czas środkowo-zachodnioaustralijski"
                                           "Lord Howe"
                                           "Czas zachodnioaustralijski"
                                           "Czas wschodnioaustraliski"
                                           "Christmas Island Time"
                                           "Cocos Islands Time"),
 :phone-number.tz-format/id               ("Australia/Adelaide"
                                           "Australia/Eucla"
                                           "Australia/Lord_Howe"
                                           "Australia/Perth"
                                           "Australia/Sydney"
                                           "Indian/Christmas"
                                           "Indian/Cocos"),
 :phone-number.tz-format/short-standalone ("ACT"
                                           "ACWT"
                                           "LHT"
                                           "AWT"
                                           "AET"
                                           "CIT"
                                           "CCT"),
 :phone.number.short/possible?            false,
 :phone.number.short/valid?               false}
```

### Phone number types

```clojure
(require '[phone-number.core :as phone])

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

### Region codes

### Calling codes

### Formats

```clojure
(require '[phone-number.core :as phone])

phone/formats

#{:phone-number.format/e164
  :phone-number.format/international
  :phone-number.format/national
  :phone-number.format/raw-input
  :phone-number.format/rfc3966}
```

### Time zone formats

...

## Validation 


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

## Samples generation

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


[LICENSE]:    https://github.com/randomseed-io/phone-number/blob/master/LICENSE
