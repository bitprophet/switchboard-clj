(ns switchboard.t-core
  (:require [switchboard.core :as core]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))


(defn query [x] (core/app (mock/request :get "" {:query x})))
(defn redirect [x] {:body "" :status 302 :headers {"Location" x}})

(defn goog [x] (str "https://google.com/search?q=" x))


(facts "about general behavior"

       (future-fact "lack of query param displays help info")

       (future-fact "present but empty query param value displays help info")

       (fact "if no submodule is matched, default is to Google"
             (query "nope") => (redirect (goog "nope"))
             (query "nope nohow") => (redirect (goog "nope nohow"))))


(facts "concerning github"

       (fact "bare key just hits homepage"
             (query "gh") => (redirect "https://github.com"))

       (fact "bare project id just hits its landing page"
             (query "gh inv") => (redirect "https://github.com/pyinvoke/invoke")))
