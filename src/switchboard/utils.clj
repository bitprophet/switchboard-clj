(ns switchboard.utils
  (:require [clojure.string :as string]
            [org.httpkit.client :as http]))


(defn build-url [base & xs]
  (string/join "/" (conj (remove nil? xs) base)))

(def error-response
  {:body "What?", :status 400})

(defn exists? [url] (= (@(http/head url) :status) 200))
