(ns io.randomseed.phone-number.rebel.main
  (:require
   rebel-readline.clojure.main
   rebel-readline.core
   io.aviso.ansi
   puget.printer))

(defn -main
  [& args]
  (rebel-readline.core/ensure-terminal
   (rebel-readline.clojure.main/repl
    :init (fn []
            (try
              (println "[phone-number] Loading Clojure code, please wait...")
              (when (System/getProperty "nrepl.load")
                (try
                  (require 'io.randomseed.phone-number.nrepl)
                  (catch Exception e
                    (.printStackTrace e)
                    (println "[phone-number] Failed to start nREPL (see exception above)."))))
              (require 'user)
              (in-ns 'user)
              (catch Exception e
                (.printStackTrace e)
                (println "[phone-number] Failed to require user, this usually means there was a syntax error. See exception above.")))))))
