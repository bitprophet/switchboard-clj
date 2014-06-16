(ns switchboard.core
  (:require [ring.adapter.jetty :refer :all]
            [ring.util.request :refer :all])
  (:gen-class))

; TODO: nuke in prod or make only enabled when testing
(require ['clojure.pprint :as 'pprint] :verbose)
(defn pp [val] (with-out-str (pprint/pprint val)))
; END TODO

(defn handler [request]
  {:status 200
   :body (str
           (pp request)
           "\n-------\n\n"
           (body-string request))
   :headers {"Content-Type" "text/html"}})


(defn -main [& args]
  (run-jetty handler {:port 8000}))
