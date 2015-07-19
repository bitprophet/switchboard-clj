(defproject switchboard "0.1.0-SNAPSHOT"
  ;; Core info
  :description "Browser search backend featuring shortcuts & smart searching."
  :url "http://example.com/FIXME"
  :license {:name "BSD 2-Clause License"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring "1.3.0"]
                 [http-kit "2.1.19"]]

  ;; Build-time options
  :main ^:skip-aot switchboard.core
  :target-path "target/%s"

  ;; Profiles
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[midje "1.6.3"]
                                  [ring/ring-mock "0.2.0"]
                                  [marginalia "0.8.0"]]}}

  ;; Lein plugins & their config
  :plugins [[lein-ring "0.8.7"]
            [lein-midje "3.1.3"]
            [lein-marginalia "0.8.0"]]
  :ring {:handler switchboard.core/app :port 8000})
