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

;; Core "take params, spit back URL for redirect" logic
(defn dispatch [[key rest]]
  (case key
    ;; 'gh': Github functionality
    "gh" (case rest
           ;; With no parameters, simply heads to github homepage
           nil "https://github.com")
    ;; Default behavior: just google the input (thus acting like a regular
    ;; browser search bar that isn't hooked up to us)
    (str "https://google.com/search?q=" key (if rest (str " " rest)))))


;; Basic HTTP handler logic
(defn handler [request]
  (let [query (-> request :params :query)]
    (if-not (nil? query)
      (redirect (dispatch (split query #" " 2)))
      (not-found "What are you even doing?"))))

(def app (-> handler
           wrap-keyword-params
           wrap-params
           wrap-stacktrace-web))
