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
  ;; FUNKY NEW FEATURE: reddit support, with no prefix because it looks neat
  (if (string/starts-with? key "r/")
    (reddit key rest)
    (case key
      "gh" (github rest)
      "py" (py3 rest)
      "py2" (py2 rest)
      "py36" (py36 rest)
      "pp" (pypi rest)
      "ud" (urbandictionary rest)
      "pb" (pinboard rest)
      "mtg" (mtg rest)
      "gis" (gis rest)
      "ann" (ann rest)
      "wh" (wowhead rest)
      "nms" (nms rest)
      "askme" (askme rest)
      (str "https://google.com/search?q=" key (if rest (str " " rest))))))


; Basic HTTP handler logic
(defn handler [request]
  (let [query (-> request :params :query)]
    (if-not (empty? query)
      (let [result (dispatch (string/split query #" " 2))]
        (pprint result)
        (if (string? result)
          ; Just-strings are expected to be URIs to redirect to; use HTTP 307
          ; so browsers don't cache when manually testing/poking.
          (redirect result :temporary-redirect)
          ; Some dispatch results are NOT just URIs but full responses; return
          ; as-is.
          result))
      error-response)))


; App wrapping requests w/ easy access to params via map+keyword
(def app (-> handler
           wrap-keyword-params
           wrap-params))

; Human-facing app adding stacktrace display to the mix
(def human-app (-> app wrap-stacktrace-web))
