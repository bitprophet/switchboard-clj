; Utils for the tests, no actual tests here.

(ns switchboard.t-utils
  (:require [ring.mock.request :as mock]
            [switchboard.core :as core]))


(defn request [options]
  (core/app (mock/request :get "" options)))

(defn query [x]
  (request {:query x}))

(defn redirect [x]
  {:body ""
   :status 307
   :headers {"Location" x}})
