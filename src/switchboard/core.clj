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
            [clojure.string :refer [split, join]]
            [clojure.pprint :refer [pprint]]
            [puget.printer :refer [cprint]]
            [org.httpkit.client :as http])
  (:gen-class))


;; Map of Github projects' shorthand identifiers; used with `gh` below.
(def github-projects {"inv" "pyinvoke/invoke"})

;; Same but for organizations
(def github-orgs {"ua" "urbanairship"})

(defn gh [& xs] (join "/" (conj (remove nil? xs) "https://github.com")))
(defn gh-proj [proj & xs] (apply gh (github-projects proj) xs))

;; `gh`: GitHub expansions
;;
;; **Basics**
;;
;; * Empty invocation (`gh`): go to `github.com`.
;; * Anything not matching one of the other rules: simply slapped onto
;;   github.com, e.g. `gh owner/repo` expands to `github.com/owner/repo`.
;;
;; **Project shortcuts**
;;
;; * Shorthand project name found in `github-projects` (e.g. `gh inv`): go to
;;   its project page.
;; * Project + issue number (`gh inv 123`): go to that issue's page.
;; * Project + 'new' (`gh inv new`): go to 'new issue' page.
;; * Project + anything else (`gh inv namespace`): issue search on that
;;   project.
;;
;; **Organization shortcuts**
;;
;; * Shorthand organization name found in `github-orgs`, by itself (e.g. `gh
;;   ua`): go to organization landing page.
;; * Organization name plus a slash plus anything else (e.g. `gh ua/tessera`):
;; expand organization URL out of the name (leading to e.g.
;; `github.com/urbanairship/tessera`).
(defn github [rest]
  (if (nil? rest)
    (gh)
    (let [[proj rest] (split rest #" " 2)]
      (if (contains? github-projects proj)
        (cond
          ; Testing for nil rest must come first to avoid NPEs/etc during regex
          ; tests farther down.
          (nil? rest) (gh-proj proj)
          (= "new" rest) (gh-proj proj "issues/new")
          (re-matches #"\d+" rest) (gh-proj proj "issues" rest)
          :else (gh-proj proj (str
                                "search?q="
                                rest
                                "&ref=cmdform&type=Issues")))
        (let [[org repo] (split proj #"/" 2)]
          (if (contains? github-orgs org)
            (gh (github-orgs org) repo)
            ; Fall-through: just slap whatever strings were given onto github.
            ; If 'rest' is empty or nil, it won't mattress.
            (gh proj rest)))))))


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
    (if-not (empty? query)
      (redirect (dispatch (split query #" " 2)))
      {:body "What?", :status 400})))

; App wrapping requests w/ easy access to params via map+keyword
(def app (-> handler
           wrap-keyword-params
           wrap-params))

; Human-facing app adding stacktrace display to the mix
(def human-app (-> app wrap-stacktrace-web))
