(defproject switchboard "0.1.0-SNAPSHOT"
  :description "Browser search backend featuring shortcuts & smart searching."
  :url "http://example.com/FIXME"
  :license {:name "BSD 2-Clause License"
            :url "http://opensource.org/licenses/BSD-2-Clause"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ring/ring-core "1.3.0"]
                 [ring/ring-jetty-adapter "1.3.0"]]
  :plugins [[lein-ring "0.8.7"]]
  :ring {:handler switchboard.core/app :port 8000}
  :main ^:skip-aot switchboard.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
