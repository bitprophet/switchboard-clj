(ns switchboard.t-core
  (:require [switchboard.core :as core]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))


(defn query [x] (core/app (mock/request :get "" {:query x})))
(defn redirect [x] {:body "" :status 302 :headers {"Location" x}})

(defn goog [x] (redirect (str "https://google.com/search?q=" x)))
(defn gh [x] (redirect (str "https://github.com" x)))


(facts "general behavior"

       (future-fact "lack of query param displays help info")

       (future-fact "present but empty query param value displays help info")

       (fact "if no submodule is matched, default is to Google"
             (query "nope") => (goog "nope")
             (query "nope nohow") => (goog "nope nohow")))


(facts "concerning github"

       (fact "bare key just hits homepage"
             (query "gh") => (gh ""))

       (fact "bare project id just hits its landing page"
             (query "gh inv") => (gh "/pyinvoke/invoke"))

       (fact "project id plus issue number goes to that issue"
             (query "gh inv 123") => (gh "/pyinvoke/invoke/issues/123")))
