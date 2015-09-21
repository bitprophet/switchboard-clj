;; # Overview
;;
;; Switchboard is designed to run as a browser backend search engine which
;; takes a single string query, parses it, and interprets it according to a set
;; of rules.

(ns switchboard.core
  (:require [clojure.string :as string]
            [clojure.data.json :as json]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace-web]]
            [ring.util.response :refer [redirect]]
            [org.httpkit.client :as http]
            [switchboard.utils :refer :all]
            [switchboard.github :refer [github]]))


;; TODO: move to its own module, meh
;; TODO: try factoring out the common pattern between this and pypi
(def py (partial build-url "https://docs.python.org"))
(defn python [version rest]
  (let [py (partial py version)]
    (if (nil? rest)
      (py "library")
      (let [direct (py "library" (str rest ".html"))]
        (if (= (@(http/head direct) :status) 200)
          direct
          (py (str "search.html?q=" rest)))))))

(def py26 (partial python "2.6"))
(def py27 (partial python "2.7"))
;; TODO: I might care about 3.4-specific shit someday?
(def py3 (partial python "3.3"))

(def -pypi (partial build-url "https://pypi.python.org"))
(defn pypi [rest]
  (if (nil? rest)
    (-pypi)
    (let [direct (-pypi "pypi" rest)]
      (if (= (@(http/head direct) :status) 200)
        direct
        (-pypi (str "pypi?:action=search&submit=search&term=" rest))))))


;; Dispatch requests to given modules based on first word ("key").
;;
;; When no matching key is found, all text is used as-is in a Google search.
(defn dispatch [[key rest]]
  (case key
    "gh" (github rest)
    "py" (py26 rest)
    "py27" (py27 rest)
    "py3" (py3 rest)
    "pp" (pypi rest)
    (str "https://google.com/search?q=" key (if rest (str " " rest)))))


; Basic HTTP handler logic
(defn handler [request]
  (let [query (-> request :params :query)]
    (if-not (empty? query)
      ; Use HTTP 307 so browsers don't cache when manually testing/poking
      (redirect (dispatch (string/split query #" " 2)) :temporary-redirect)
      error-response)))


; App wrapping requests w/ easy access to params via map+keyword
(def app (-> handler
           wrap-keyword-params
           wrap-params))

; Human-facing app adding stacktrace display to the mix
(def human-app (-> app wrap-stacktrace-web))
