(ns switchboard.t-core
  (:require [switchboard.core :refer :all]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))


(defn query [x] (app (mock/request :get "" {:query x})))
(defn redirect [x] {:status 302 :headers {"Location" x}})


(facts "about general behavior"
  (fact "lack of query param displays help info"))

(facts "concerning github"
  (query "gh") => (redirect "https://github.com"))
