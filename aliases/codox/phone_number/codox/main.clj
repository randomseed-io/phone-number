(ns phone-number.codox.main
  (:require [codox.main                      :as       c]
            [clojure.java.io                 :as      io]
            [clojure.edn                     :as     edn]
            [clojure.tools.deps.alpha        :as    deps]
            [clojure.string                  :as     str]))

(defn- read-deps
  []
  (:project-edn (deps/find-edn-maps)))

(defn- get-options [deps & more]
  (let [key     (or (first more) :codox)
        version (or (second more) "0.0.0")
        codox   (key deps)
        userdir (System/getProperty "user.dir")]
    (merge {:source-paths (:paths deps ["src"])
            :root-path    (:root-path deps userdir)
            :output-path  (str (io/file (:target deps userdir) "docs"))}
           codox
           {:name        (str/capitalize (:name codox (:name deps)))
            :license     (:license     codox (:license deps))
            :package     (:package     codox (:license deps))
            :description (:description codox (:description deps))
            :version     (or version (:version codox (:version deps)))})))

(defn codox
  "Generate API documentation from source code."
  [key version]
  (let [version (when version (str version))
        deps    (read-deps)
        options (get-options deps key version)]
    (codox.main/generate-docs options)
    (shutdown-agents)
    (println "Generated HTML docs in" (:output-path options))))

(defn -main
  [& args]
  (codox :codox (first args)))
