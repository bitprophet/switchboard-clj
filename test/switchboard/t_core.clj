(ns switchboard.t-core
  (:require [switchboard.core :as core]
            [switchboard.utils :refer :all]
            [switchboard.t-utils :refer :all]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))


(defn goog [x] (redirect (str "https://google.com/search?q=" x)))


(facts "general behavior"

       (fact "lack of query param displays error"
             (request {}) => error-response)

       (fact "present but empty query param value displays error"
             (query "") => error-response)

       (fact "if no submodule is matched, default is to Google"
             (query "nope") => (goog "nope")
             (query "nope nohow") => (goog "nope nohow")))
