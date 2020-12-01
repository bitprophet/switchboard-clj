(defproject switchboard "0.1.0-SNAPSHOT"
  ;; Core info
  :description "Browser search backend featuring shortcuts & smart searching."
  :url "https://github.com/bitprophet/switchboard"
  :license {:name "BSD 2-Clause License"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ring "1.7.1"]
                 [http-kit "2.3.0"]
                 [org.clojure/data.json "0.2.6"]]

  ;; Build-time options
  :main ^:skip-aot switchboard.core

  ;; Profiles
  :profiles {:dev {:dependencies [[midje "1.9.4"]
                                  [ring/ring-mock "0.3.2"]
                                  [http-kit.fake "0.2.2"]
                                  [marginalia "0.9.1"]]}}

  ;; Lein plugins & their config
  :plugins [[lein-midje "3.2.1"]
            [lein-ring "0.12.5"]
            [lein-marginalia "0.9.0"]]

  ;; "Prod" lein server invokable via 'lein ring server-headless'
  :ring {:handler switchboard.core/human-app :port 8787}

  ;; Drop max heap size about as far as it'll go before Java just refuses to
  ;; start up, lmao. For what this service does it doesn't need to eat a half
  ;; gig of RAM or more. (With this at 32m it seems to "only" eat around
  ;; 300M...)
  :jvm-opts ["-Xmx32m"]

  ;; Personal REPL development setup
  :repl-options {:init (do
    ;; Load & autorun test suite
    (use 'midje.repl)
    (autotest)

    ;; Easy reinvocation of marginalia inline instead of suffering another 20s
    ;; 'lein marg' run in a shell.
    ;; TODO: either run on CLI using drip, or reverse engineer (autotest)?
    (require '[marginalia.core :refer [run-marginalia]])
    (def marg #(binding [marginalia.html/*resources* ""]
                 (marginalia.core/run-marginalia '())))

    ;; Spin up a dev jetty server
    (require '[ring.adapter.jetty :refer [run-jetty]])
    (defonce server (run-jetty #'human-app {:port 8788 :join? false}))
    (.start server))})
