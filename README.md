# phone-number – Validation and Inspection of Phone Numbers

[![Phone-number on Clojars](https://img.shields.io/clojars/v/io.randomseed/phone-number.svg)](https://clojars.org/io.randomseed/phone-number)

Clojure library that uses Google's Libphonenumber to help validate and inspect phone numbers.

* It **shows information** about phone numbers:

```clojure
(require '[phone-number.core :as phone])

;; region taken from a phone number
;; system's default locale

(phone/info "+44 29 2018 3133")

    '{:carrier  nil,
      :country-code         44,
      :location             "Cardiff",
      :possible?            true,
      :region-code          "GB",
      :type                 :fixed-line,
      :valid?               true,
      :format/e164          "+442920183133",
      :format/international "+44 29 2018 3133",
      :format/national      "029 2018 3133",
      :format/rfc3966       "tel:+44-29-2018-3133",
      :time-zones/full      ("Greenwich Mean Time"),
      :time-zones/ids       ("Europe/London"),
      :time-zones/short     ("GMT")}

;; region passed as an argument
;; locale setting passed as an argument

(phone/info "601 100 601" :pl :pl)

    '{:carrier              "Plus",
      :country-code         48,
      :location             "Polska",
      :possible?            true,
      :region-code          "PL",
      :type                 :mobile,
      :valid?               true,
      :format/e164          "+48601100601",
      :format/international "+48 601 100 601",
      :format/national      "601 100 601",
      :format/rfc3966       "tel:+48-601-100-601",
      :time-zones/full      ("Czas środkowoeuropejski"),
      :time-zones/ids       ("Europe/Warsaw"),
      :time-zones/short     ("CET")}

(phone/info "8081 570001" :GB :JP)

    '{:carrier              nil,
      :country-code         44,
      :location             nil,
      :possible?            true,
      :region-code          "GB",
      :type                 :toll-free,
      :valid?               true,
      :format/e164          "+448081570001",
      :format/international "+44 808 157 0001",
      :format/national      "0808 157 0001",
      :format/rfc3966       "tel:+44-808-157-0001",
      :time-zones/full      ("Greenwich Mean Time" "British Time"),
      :time-zones/ids       ("Europe/Guernsey"
                             "Europe/Isle_of_Man"
                             "Europe/Jersey"
                             "Europe/London"),
      :time-zones/short     ("GMT" "BT")}
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

(:format/e164
 :format/international
 :format/national
 :format/rfc3966)

(phone/types)

(:personal-number
 :uan
 :unknown
 :voicemail
 :premium-rate
 :fixed-line
 :shared-cost
 :pager
 :mobile
 :voip
 :toll-free
 :fixed-line-or-mobile)
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
