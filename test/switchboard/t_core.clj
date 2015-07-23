(ns switchboard.t-core
  (:require [switchboard.core :as core]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]))


(defn query [x] (core/app (mock/request :get "" {:query x})))
(defn redirect [x] {:body "" :status 302 :headers {"Location" x}})

(defn goog [x] (redirect (str "https://google.com/search?q=" x)))
(defn gh [x] (redirect (str "https://github.com" x)))
(defn gh-issue-search [x] (gh (str "/pyinvoke/invoke/search?q=" x "&ref=cmdform&type=Issues")))

(def error-response {:body "What?", :status 400})


(facts "general behavior"

       (fact "lack of query param displays error"
             (core/app (mock/request :get "" {})) => error-response)

       (fact "present but empty query param value displays error"
             (core/app (mock/request :get "" {:query ""})) => error-response)

       (fact "if no submodule is matched, default is to Google"
             (query "nope") => (goog "nope")
             (query "nope nohow") => (goog "nope nohow")))


(facts "concerning github"

       (facts "about basic behavior"

         (fact "bare key just hits homepage"
               (query "gh") => (gh ""))

         (fact "anything not a project id, etc, is appended to github.com"
               (query "gh somebody/project") => (gh "/somebody/project")))

       (facts "about specific projects"

         (fact "bare project id just hits its landing page"
               (query "gh inv") => (gh "/pyinvoke/invoke"))

         (fact "project id plus issue number goes to that issue"
               (query "gh inv 123") => (gh "/pyinvoke/invoke/issues/123"))

         (fact "project id plus 'new' goes to issue creation page"
               (query "gh inv new") => (gh "/pyinvoke/invoke/issues/new"))

         (fact "anything else becomes an issue search for that project id"
               (query "gh inv lolcats")
                  => (gh-issue-search "lolcats")
               (query "gh inv a query with spaces")
                  => (gh-issue-search "a query with spaces")))

       (facts "about account searching"

         (future-fact "when input attached to an account yields a repo, go there")

         (future-fact "when first account doesn't match, next is tried")))
