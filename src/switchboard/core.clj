(ns switchboard.core
  (:require [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [not-found, redirect]]
            [clojure.string :refer [split]]
            [clojure.pprint :refer [pprint]])
  (:gen-class))

(defn dispatch [query]
  (let [[key rest] (split query #" " 2)]
    (pprint key)
    (pprint rest)
    {:body "hi"}))

(defn handler [request]
  (println) (println) (pprint request)
  (if (contains? (:params request) :query)
    (dispatch (:query (:params request)))
    (not-found "What are you even doing?")))

(def app (-> handler
           wrap-keyword-params
           wrap-params))
