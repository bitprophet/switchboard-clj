(ns switchboard.utils
  (:require [clojure.string :as string]))


(defn build-url [base & xs]
  (string/join "/" (conj (remove nil? xs) base)))

(def error-response
  {:body "What?", :status 400})
