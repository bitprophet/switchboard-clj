;; # Overview
;;
;; Switchboard is designed to run as a browser backend search engine which
;; takes a single string query, parses it, and interprets it according to a set
;; of rules.

(ns switchboard.core
  (:require [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace-web]]
            [ring.util.response :refer [not-found, redirect]]
            [clojure.string :refer [split]]
            [clojure.pprint :refer [pprint]]
            [puget.printer :refer [cprint]]
            [org.httpkit.client :as http])
  (:gen-class))


;; Map of Github projects' shorthand identifiers; used with `gh` below.
(def github-projects {"inv" "pyinvoke/invoke"})

(defn gh [x] (str "https://github.com" (if x (str "/" x))))

;; Github module, key: `gh`
;;
;; * Empty invocation (`gh`): go to `github.com`.
;; * Shorthand project name found in `github-projects` (e.g. `gh inv`): go to
;;   its project page.
;; * Project + issue number (`gh inv 123`): go to that issue's page.
;; * Project + 'new' (`gh inv new`): go to 'new issue' page.
;; * Project + anything else (`gh inv namespace`): issue search on that
;; project.
(defn github [rest]
  (if (nil? rest)
    (gh nil)
    (let [[proj rest] (split rest #" " 2)]
      (if (contains? github-projects proj)
        (cond
          ; Testing for nil rest must come first to avoid NPEs/etc during regex
          ; tests farther down.
          (nil? rest) (gh (github-projects proj))
          (= "new" rest) (gh (str (github-projects proj) "/issues/new"))
          (re-matches #"\d+" rest) (gh (str
                                         (github-projects proj)
                                         "/issues/"
                                         rest))
          :else (gh (str
                      (github-projects proj)
                      "/search?q="
                      rest
                      "&ref=cmdform&type=Issues")))))))


;; Dispatch requests to given modules based on first word ("key").
;;
;; When no matching key is found, all text is used as-is in a Google search.
(defn dispatch [[key rest]]
  (case key
    "gh" (github rest)
    (str "https://google.com/search?q=" key (if rest (str " " rest)))))


; Basic HTTP handler logic
(defn handler [request]
  (let [query (-> request :params :query)]
    (if-not (nil? query)
      (redirect (dispatch (split query #" " 2)))
      (not-found "What are you even doing?"))))

; App wrapping requests w/ easy access to params via map+keyword
(def app (-> handler
           wrap-keyword-params
           wrap-params))

; Human-facing app adding stacktrace display to the mix
(def human-app (-> app wrap-stacktrace-web))
