;; # Overview
;;
;; Switchboard is designed to run as a browser backend search engine which
;; takes a single string query, parses it, and interprets it according to a set
;; of rules. These rules are arbitrary functions but most of them follow common
;; patterns and use a provided set of helper subroutines.
;;
;; For example, one builtin module performs Github shortcuts: `gh <term>` tries
;; finding a repository named `term` belonging to a default user (or a series
;; of users or organizations in a prioritized list), then tries treating it as
;; a literal URL part attached to ``https://github.com/``, then if that fails,
;; as a search term handed to Github's search endpoint. And this is only a
;; portion of the Github module's functionality.
;;
;; Read on for details on how to inform Switchboard of your desired
;; configuration.

(ns switchboard.core
  (:require [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [not-found, redirect]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clojure.string :refer [split]]
            [clojure.pprint :refer [pprint]]
            [puget.printer :refer [cprint]]
            [org.httpkit.client :as http])
  (:gen-class))

(defn dispatch [query]
  (let [[key rest] (split query #" " 2)]
    (cprint key)
    (cprint rest)
    {:body "hi"}))

(defn handler [request]
  (println) (println) (cprint request)
  (if (contains? (:params request) :query)
    (dispatch (:query (:params request)))
    (not-found "What are you even doing?")))

(def app (-> handler
           wrap-keyword-params
           wrap-params))

; REPL dev server
(defonce server (run-jetty #'app {:port 8080 :join? false}))
