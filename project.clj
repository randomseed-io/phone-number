(defproject io.randomseed/phone-number "8.12.4-0"
  :description "Clojure interface for Google's libphonenumber library"
  :url          "https://randomseed.io/software/phone-number"
  :scm          {:name "git", :url "https://github.com/randomseed-io/phone-number"}
  :license      {:name "GNU Lesser General Public License 3.0"
                 :url "https://www.gnu.org/licenses/lgpl-3.0.html"}
  :global-vars {*warn-on-reflection* true}
  :dependencies [[org.clojure/clojure                          "1.10.1"]
                 [org.clojure/spec.alpha                      "0.2.176"]
                 [org.clojure/algo.generic                      "0.1.3"]
                 [trptr/java-wrapper                            "0.2.3"]
                 [com.googlecode.libphonenumber/libphonenumber "8.12.4"]
                 [com.googlecode.libphonenumber/geocoder        "2.140"]
                 [com.googlecode.libphonenumber/carrier         "1.130"]])
