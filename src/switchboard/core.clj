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
            [switchboard.searches :refer :all]
            [switchboard.github :refer [github]]
            [switchboard.python :refer :all]
            [switchboard.utils :refer [error-response]]))


;; Dispatch requests to given modules based on first word ("key").
;;
;; When no matching key is found, all text is used as-is in a Google search.
(defn dispatch [[key rest]]
  (case key
    "gh" (github rest)
    "py" (py27 rest)
    "py26" (py26 rest)
    "py3" (py3 rest)
    "pp" (pypi rest)
    "ud" (urbandictionary rest)
    "pb" (pinboard rest)
    "mtg" (mtg rest)
    "gis" (gis rest)
    "ann" (ann rest)
    "wh" (wowhead rest)
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
