# Introduction

The phone-number library is an interface to the
[Libphonenumber](https://github.com/google/libphonenumber) with some extra sugar. It
allows to validate, inspect and generate phone numbers.

## Installation

To use phone-number in your project, add the following to dependencies section of
`project.clj` or `build.boot`:

```clojure
[io.randomseed/phone-number "8.13.6-2"]
```

For `deps.edn` add the following as an element of a map under `:deps` or
`:extra-deps` key:

```clojure
io.randomseed/phone-number {:mvn/version "8.13.6-2"}
```

You can also download JAR from
[Clojars](https://clojars.org/io.randomseed/phone-number).

Additionally you can use (in your development profile) if you want to utilize specs
and spec-integrated generators provided by the phone-number:

```clojure
org.clojure/spec.alpha {:mvn/version "0.2.194"}
org.clojure/test.check {:mvn/version "1.1.0"}
```

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

### Caveats

If your program processes a lot of phone numbers and your strategy is to keep them in
a native format (a result of calling `phone-number.core/number`) then be aware that
by default all `PhoneNumber` objects created will have raw input stored internally.
This will affect **comparison** in a way that the object representing a number
`+442920183133` will **not be equal** to the object representing the same number but
with spaces (`+44 2920 183 133`). This is due to equality test based on, among
others, raw input values which are used to generate the hash code of each object.

To work around that you have 2 choices:

* Use `phone-number.core/number-noraw` on input data to parse numbers without
  preserving raw inputs.
* Use `phone-number.core/number-noraw` on existing objects to create raw-input-free
  copies.

Optionally, `phone-number.core/number-optraw` may be used too, specifically in
processing pipelines, in order to preserve raw input only when a created object is
initialized with the existing one (an instance of `PhoneNumber`). In case of other
argument types the protocol method behaves like `number-noraw`.

### Namespace inference

In the examples above some regions were set using namespace-qualified keywords and
some were not. This is due to namespace inference when it comes to specifying phone
number properties that are expressed as keywords.

When the dynamic variable
[`phone-number.core/*inferred-namespaces*`](phone-number.core.html#var-*inferred-namespaces*)
is set to `true` (which is the default setting) all functions that require keyword
arguments will automatically enrich simple keywords to be namespace-qualified. That
affects:

* region codes (e.g. `:pl` becomes `:phone-number.region/pl`),
* number types (e.g. `:mobile` becomes `:phone-number.type/mobile`),
* number formats (e.g. `:international` becomes `:phone-number.format/international`),
* time zone formats (e.g. `:short` becomes `:phone-number.tz-format/short`).

## Phone number properties

To get some specific property of a phone number use one of the functions grouped in
[`phone-number.core`](phone-number.core.html) namespace.

There is also the [`info`](phone-number.core.html#var-info) function that produces
a map with most of the properties:

```clojure
(require '[phone-number.core :as phone])

(phone/info "+44 29 2018 3133")

{:phone-number/country-code               44,
 :phone-number/geographical?              true,
 :phone-number/location                   "Cardiff",
 :phone-number/possible?                  true,
 :phone-number/region                     :phone-number.region/gb,
 :phone-number/dialing-region             :phone-number.region/gb,
 :phone-number.dialing-region/derived?    true,
 :phone-number.dialing-region/defaulted?  false,
 :phone-number.dialing-region/valid-for?  true,
 :phone-number/type                       :phone-number.type/fixed-line,
 :phone-number/valid?                     true,
 :phone-number.format/e164                "+442920183133",
 :phone-number.format/international       "+44 29 2018 3133",
 :phone-number.format/national            "029 2018 3133",
 :phone-number.format/raw-input           "+44 29 2018 3133",
 :phone-number.format/rfc3966             "tel:+44-29-2018-3133",
 :phone-number.tz-format/full-standalone ("Greenwich Mean Time"),
 :phone-number.tz-format/id              ("Europe/London"),
 :phone-number.tz-format/short-standalone("GMT"),
 :phone.number.short/possible?            false,
 :phone.number.short/valid?               false}
```

The `info` function (like most of the functions operating on phone numbers) takes an
additional, second argument which should be a **region code**. It is used only when
the given phone number was expressed in a format that cannot provide such information
(a string without calling code, a natural number, a map without any entry containing
region or calling code).

```clojure
(require '[phone-number.core :as phone])

(phone/info "0491 570 006" :phone-number.region/au)

{:phone-number/calling-code               61,
 :phone-number/carrier                    "Telstra",
 :phone-number/geographical?              false,
 :phone-number/possible?                  true,
 :phone-number/region                     :phone-number.region/au,
 :phone-number/dialing-region             :phone-number.region/au,
 :phone-number.dialing-region/derived?    true,
 :phone-number.dialing-region/defaulted?  false,
 :phone-number.dialing-region/valid-for?  true,
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

It is worth to note that the time zone information was presented in English (being
the default locale for the system). That behavior can be changed with a third,
optional argument, which should be a `java.util.Locale` instance or a value that can
be converted to it (a string, a keyword, a symbol).

```clojure
(require '[phone-number.core :as phone])

(phone/info "0491 570 006" :au :pl)

{:phone-number/calling-code               61,
 :phone-number/carrier                    "Telstra",
 :phone-number/geographical?              false,
 :phone-number/possible?                  true,
 :phone-number/region                     :phone-number.region/au,
 :phone-number/dialing-region             :phone-number.region/au,
 :phone-number.dialing-region/derived?    true,
 :phone-number.dialing-region/defaulted?  false,
 :phone-number.dialing-region/valid-for?  true,
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

### Short phone numbers

There is a special class of phone numbers called **short numbers**. These include all
of the special, short phone numbers used mostly for emergency purposes but also
for local services (like taxis). In the examples above we could see the
keys belonging to the `:phone-number.short/` namespace which are describing short
number properties. If the number is not a short number the information map will only
contain `:phone-number.short/valid?` and `:phone-number.short/possible?` keys.

Short number example:

```clojure
(require '[phone-number.core :as phone])

(phone/info "997" :pl)

{:phone-number/calling-code             48,
 :phone-number/geographical?            false,
 :phone-number/possible?                false,
 :phone-number/region                   :phone-number.region/pl,
 :phone-number/dialing-region           :phone-number.region/pl,
 :phone-number.dialing-region/derived?   true,
 :phone-number.dialing-region/defaulted? false,
 :phone-number.dialing-region/valid-for? true,
 :phone-number/type                      :phone-number.type/unknown,
 :phone-number/valid?                    false,
 :phone-number.format/e164               "+48997",
 :phone-number.format/international      "+48 997",
 :phone-number.format/national           "997",
 :phone-number.format/raw-input          "997",
 :phone-number.format/rfc3966            "tel:+48-997",
 :phone-number.short/carrier-specific?   false,
 :phone-number.short/cost                :phone-number.cost/toll-free,
 :phone-number.short/emergency?           true,
 :phone-number.short/possible?            true,
 :phone-number.short/sms-service?         false,
 :phone-number.short/to-emergency?        true,
 :phone-number.short/valid?               true}
```

### Phone number types

Some functions will require phone number type to be given. In is easy to get all the
supported types via global variable:

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

Note that `:phone-number.type/unknown` is valid as a property but not valid as an
argument.

### Region codes

**Region code** is alphabetic, two-letter keyword that allows phone-number functions
to create, generate and match phone numbers. It is also used for reporting.

Region codes can be translated to **country calling codes** which can be observed in
international phone number notations (with the plus sign in front of the number). For
example the region code `:pl` (or `phone-number.region/pl`) will be represented as
`+48` country code prefix.

It is possible to get all of the supported region codes with the
[`phone-number.core/regions`](phone-number.core.html#var-regions) global variable.

### Dialing region codes

**Dialing region code** (a.k.a calling region code) is a region code that some of the
functions use as a supplementary information to establish certain properties of
a number. Semantically it describes **where** the call is made from (what is its
origin).

It is common to use dialing region when testing or validating short phone numbers
since their properties depend on the originating region. For example the number `112`
is a valid short number (see the `:phone-number.short/valid?` key in an info map)
when dialed from Poland but not from USA.

Dialing region codes may be passed directly to functions that make use of it (and
that is the first and prioritized way to obtain this information) but they can also
be taken from the global dynamic variable
`phone-number.core/*default-dialing-region*`. If the phone number is in a form of
a map with `:phone-number/dialing-region` key present then a value associated with it
will be tried before (but only if this property is not derived nor defaulted).

In case of `phone-number.core/info` and `phone-number.core/short-info` there is
another source of getting a dialing region code which is by derivation from a region
code of the number. It is tried when there is no dialing region passed as an
argument, no default dialing region set and no dialing region read from a map (if
a phone number is a map). The derivation is enabled by default but can be controlled
using the `phone-number.core/*info-dialing-region-derived*` dynamic variable.

The value of dialing region will affect all of the calculated properties of the
checked number that depend on it but not the one returned by the
`phone-number.core/valid?` function. In its case one have to use ternary variant for
the dialing region to be taken into consideration (even passing `nil` to make the
function use the default). The reason behind it is that it is very rare need to test
a regular number for validity with a different dialing region. In the majority of
cases the returned value will be `false` and it could be a potential point of failure
(or misunderstanding), especially when some default dialing region is present.

### Calling codes

**Calling code** is a prefix number used to create regional or global phone number
assignation. There are two types of calling codes supported:

* **country calling codes** (that can be derived from region codes),
* **global network calling codes** that are independent of regions.

It is possible for a phone number to have a calling code (which is a global one) but
not a region code.

There are just a few functions that calling codes can be used with directly. In such
cases they are expressed as positive, natural numbers.

* To get all country calling codes use
[`phone-number.core/country-codes`](phone-number.core.html#var-country-codes).
* To get all global network calling codes use
[`phone-number.core/net-codes`](phone-number.core.html#var-net-codes).
* To get all calling codes (both global and country) use
[`phone-number.core/calling-codes`](phone-number.core.html#var-calling-codes).

To get the calling code of a phone number use the
[`phone-number.core/calling-code`](phone-number.core.html#var-calling-code) function.

### Formats

Phone numbers can be presented in different formats. To get all possible formats use
the [`phone-number.core/formats`](phone-number.core.html#var-formats) global
variable:

```clojure
(require '[phone-number.core :as phone])

phone/formats

#{:phone-number.format/e164
  :phone-number.format/international
  :phone-number.format/national
  :phone-number.format/raw-input
  :phone-number.format/rfc3966
  :phone-number.format/raw-input}
```

To get the phone number representation in a specific format use the
[`phone-number.core/format`](phone-number.core.html#var-format) function:

```clojure
(require '[phone-number.core :as phone])

(phone/format "+442920183133" :rfc3966)

"tel:+44-29-2018-3133"
```

### Time zone formats

Some phone numbers can be indirectly associated with time zones (via geographical
locations of the lines or carrier's local offices locations). To get all possible
formats of time zones use the
[`phone-number.core/tz-formats`](phone-number.core.html#var-tz-formats)
global variable:

```clojure
(require '[phone-number.core :as phone])

phone/tz-formats

#{:phone-number.tz-format/full
  :phone-number.tz-format/full-standalone
  :phone-number.tz-format/id
  :phone-number.tz-format/narrow
  :phone-number.tz-format/narrow-standalone
  :phone-number.tz-format/short
  :phone-number.tz-format/short-standalone}
```

To get time zone information for a phone number in a specified format use the
[`phone-number.core/time-zones`](phone-number.core.html#var-time-zones) function:

```clojure
(require '[phone-number.core :as phone])

(phone/time-zones "+442920183133" nil :full)

("Greenwich Mean Time")
```

We can observe that the result is a collection of strings. That's because some phone
numbers can be assigned to more than one time zone.

## Validation

To validate a phone number use the following functions:

* [`phone-number.core/valid?`](phone-number.core.html#var-valid.3F),
* [`phone-number.core/invalid?`](phone-number.core.html#var-invalid.3F),
* [`phone-number.core/possible?`](phone-number.core.html#var-possible.3F),
* [`phone-number.core/impossible?`](phone-number.core.html#var-impossible.3F).

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

Additionally there are variants for short numbers:

* [`phone-number.core/short-valid?`](phone-number.core.html#var-short-valid.3F),
* [`phone-number.core/short-invalid?`](phone-number.core.html#var-short-invalid.3F),
* [`phone-number.core/short-possible?`](phone-number.core.html#var-short-possible.3F).

## Samples generation

To generate samples of phone numbers with optional predicates controlling the
properties of data use the
[`phone-number.core/generate`](phone-number.core.html#var-generate) function. It
tries to get the most probable samples (having the most digits randomized yet meeting
the given criteria) with optional deterministic factor (random seed).

There are also phone number generators associated with many of the existing specs
that make use of this function.

```clojure
(require '[phone-number.core :as phone])

(phone/generate)

{:phone-number/info {:phone-number/country-code              213,
                     :phone-number/geographical?             false,
                     :phone-number/possible?                 true,
                     :phone-number/region                    :phone-number.region/dz,
                     :phone-number/dialing-region            :phone-number.region/dz,
                     :phone-number.dialing-region/derived?   true,
                     :phone-number.dialing-region/defaulted? false,
                     :phone-number.dialing-region/valid-for? false,
                     :phone-number/type                      :phone-number.type/unknown,
                     :phone-number/valid?                    false,
                     :phone-number.format/e164               "+213181525997",
                     :phone-number.format/international      "+213 181525997",
                     :phone-number.format/national           "181525997",
                     :phone-number.format/rfc3966            "tel:+213-181525997",
                     :phone.number.short/possible?           false,
                     :phone.number.short/valid?              false},
 :phone-number/number             #<Phonenumber$PhoneNumber@3edea9e6>,
 :phone-number.sample/digits      ["+213" nil "181525997"],
 :phone-number.sample/hits        10,
 :phone-number.sample/max-samples 1000,
 :phone-number.sample/random-seed 7521527664400716800,
 :phone-number.sample/samples     11}

(require [phone-number.spec       :as spec]
         [clojure.spec.alpha      :as    s]
         [clojure.spec.gen.alpha  :as  gen])

(gen/generate (s/gen :phone-number/valid))

{:phone-number/info               #delay[{:status :pending, :val nil} 0x3810d15d],
 :phone-number/number             #<Phonenumber$PhoneNumber@79cc08fb>,
 :phone-number.sample/digits      ["+7" nil "937627908"],
 :phone-number.sample/hits        11,
 :phone-number.sample/max-samples 150,
 :phone-number.sample/random-seed 7581363778716192180,
 :phone-number.sample/samples     27}

(gen/generate (s/gen (s/and :phone-number/possible :phone-number/invalid)))

{:phone-number/info               #delay[{:status :pending, :val nil} 0x41c0e225],
 :phone-number/number             #<Phonenumber$PhoneNumber@36a74c18>,
 :phone-number.sample/digits      ["+84" nil "0270454"],
 :phone-number.sample/hits        8,
 :phone-number.sample/max-samples 200,
 :phone-number.sample/random-seed -9105741821593959780,
 :phone-number.sample/samples     12}
```

## License

Copyright © 2020–2023 Paweł Wilk

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
