# phone-number – Validation and Inspection of Phone Numbers

[![Phone-number on Clojars](https://img.shields.io/clojars/v/io.randomseed/phone-number.svg)](https://clojars.org/io.randomseed/phone-number)

Clojure library which uses Google's Libphonenumber to validate, inspect and generate phone numbers.

* It **shows information** about phone numbers:

```clojure
(require '[phone-number.core :as phone])

;; region taken from a phone number
;; system's default locale

(phone/info "+44 29 2018 3133")

    '{:phone-number/carrier                    nil,
      :phone-number/country-code               44,
      :phone-number/location                   "Cardiff",
      :phone-number/possible?                  true,
      :phone-number/region-code                "GB",
      :phone-number/type                       :fixed-line,
      :phone-number/valid?                     true,
      :phone-number.format/e164                "+442920183133",
      :phone-number.format/international       "+44 29 2018 3133",
      :phone-number.format/national            "029 2018 3133",
      :phone-number.format/rfc3966             "tel:+44-29-2018-3133",
      :phone-number.tz-format/id               ("Europe/London"),
      :phone-number.tz-format/full-standalone  ("Greenwich Mean Time"),
      :phone-number.tz-format/short-standalone ("GMT")}

;; region passed as an argument
;; locale setting passed as an argument

(phone/info "601 100 601" :pl :pl)

    '{:phone-number/carrier                    "Plus",
      :phone-number/country-code               48,
      :phone-number/location                   "Polska",
      :phone-number/possible?                  true,
      :phone-number/region-code                "PL",
      :phone-number/type                       :phone-number.type/mobile,
      :phone-number/valid?                     true,
      :phone-number.format/e164                "+48601100601",
      :phone-number.format/international       "+48 601 100 601",
      :phone-number.format/national            "601 100 601",
      :phone-number.format/rfc3966             "tel:+48-601-100-601",
      :phone-number.tz-format/id               ("Europe/Warsaw"),
      :phone-number.tz-format/full-standalone  ("Czas środkowoeuropejski"),
      :phone-number.tz-format/short-standalone ("CET")}

(phone/info "8081 570001" :GB :JP)

    '{:phone-number/carrier                    nil,
      :phone-number/country-code               44,
      :phone-number/location                   nil,
      :phone-number/possible?                  true,
      :phone-number/region-code                nil,
      :phone-number/type                       :phone-number.type/unknown,
      :phone-number/valid?                     false,
      :phone-number.format/e164                "+44808157000",
      :phone-number.format/international       "+44 808157000",
      :phone-number.format/national            "808157000",
      :phone-number.format/rfc3966             "tel:+44-808157000",
      :phone-number.tz-format/full-standalone  nil,
      :phone-number.tz-format/id               nil,
      :phone-number.tz-format/short-standalone nil}

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

(phone/formats)

(:phone-number.format/e164
 :phone-number.format/international
 :phone-number.format/national
 :phone-number.format/rfc3966)

(phone/types)

(:phone-number.type/personal-number
 :phone-number.type/uan
 :phone-number.type/unknown
 :phone-number.type/voicemail
 :phone-number.type/premium-rate
 :phone-number.type/fixed-line
 :phone-number.type/shared-cost
 :phone-number.type/pager
 :phone-number.type/mobile
 :phone-number.type/voip
 :phone-number.type/toll-free
 :phone-number.type/fixed-line-or-mobile)
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
