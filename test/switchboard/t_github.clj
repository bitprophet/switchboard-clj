(ns switchboard.t-core
  (:require [clojure.data.json :as json]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [org.httpkit.fake :refer [with-fake-http]]
            [switchboard.t-utils :refer :all]
            [switchboard.core :as core]))


(defn gh [x] (redirect (str "https://github.com" x)))

(defn gh-issue-search [x] (gh (str "/pyinvoke/invoke/search?q=" x "&ref=cmdform&type=Issues")))

(def gh-api-repo (partial core/gh-api "repos"))

(def fake-gh-repo-data [["bitprophet" "myrepo" :ok]
                        ["urbanairship" "myrepo" 404]
                        ["bitprophet" "otherrepo" 404]
                        ["urbanairship" "otherrepo" :ok]])

(defn fake-gh-repo [[acct repo action]]
  [(gh-api-repo acct repo)
   (if (= :ok action)
     (json/write-str {:html_url (core/gh acct repo)})
     action)])

(def fake-gh-repos (flatten (map fake-gh-repo fake-gh-repo-data)))


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

         (with-fake-http fake-gh-repos

           (fact "when input attached to an account yields a repo, go there"
                 (query "gh myrepo") => (gh "/bitprophet/myrepo"))

           (fact "when first account doesn't match, next is tried"
                 (query "gh otherrepo") => (gh "/urbanairship/otherrepo")))))
