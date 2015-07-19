(ns switchboard.t-core
  (:require [switchboard.core :refer :all]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))


(defn query [x] (app (mock/request :get "" {:query x})))
(defn redirect [x] {:body "" :status 302 :headers {"Location" x}})


(facts "about general behavior"

       (future-fact "lack of query param displays help info")

       (future-fact "present but empty query param value displays help info")

       (fact "if no submodule is matched, default is to Google"
             (let [goog "https://google.com/search?q="]
               (query "nope") => (redirect (str goog "nope"))
               (query "nope nohow") => (redirect (str goog "nope nohow")))))


(facts "concerning github"

       (fact "bare key just hits homepage"
             (query "gh") => (redirect "https://github.com")))
