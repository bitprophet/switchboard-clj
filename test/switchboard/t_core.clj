(ns switchboard.t-core
  (:require [switchboard.core :refer :all]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))


(defn query [x] (app (mock/request :get "" {:query x})))
(defn redirect [x] {:body "" :status 302 :headers {"Location" x}})


(facts "about general behavior"
  (fact "lack of query param displays help info")
  (fact "empty query param value displays help info")
  (facts "if no submodule is matched, default is to Google"
    (query "nope") => (redirect "https://google.com/search?q=nope")
    (query "nope nohow") => (redirect "https://google.com/search?q=nope nohow")))

(facts "concerning github"
  (query "gh") => (redirect "https://github.com"))
