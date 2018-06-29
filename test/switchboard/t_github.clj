(ns switchboard.t-github
  (:require [clojure.data.json :as json]
            [midje.sweet :refer :all]
            [ring.mock.request :as mock]
            [org.httpkit.fake :refer [with-fake-http]]
            [switchboard.t-utils :refer :all]
            [switchboard.github :as github]))


(defn gh [x] (redirect (str "https://github.com" x)))

(defn gh-issue-search [repo term]
  (gh (str "/" repo "/search?q=" term "&ref=cmdform&type=Issues")))


(def gh-api-repo (partial github/gh-api "repos"))

(def fake-gh-accounts ["bitprophet" "employer" "employer2"])
; TODO: surely there's a less shite way to describe this in a functional lang
(def fake-gh-repo-data [["bitprophet" "myrepo" :ok]
                        ["employer" "myrepo" 404]
                        ["employer2" "myrepo" 404]
                        ["bitprophet" "otherrepo" 404]
                        ["employer" "otherrepo" :ok]
                        ["employer2" "otherrepo" 404]
                        ["bitprophet" "yetanotherrepo" 404]
                        ["employer" "yetanotherrepo" 404]
                        ["employer2" "yetanotherrepo" :ok]])

(defn fake-gh-repo [[acct repo action]]
  [(gh-api-repo acct repo)
   (if (= :ok action)
     (json/write-str {:html_url (github/gh acct repo)
                      :full_name (str acct "/" repo)})
     action)])

(def fake-gh-repos (flatten (map fake-gh-repo fake-gh-repo-data)))


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

  (fact "project id plus 'm' goes to milestones page"
        (query "gh inv m") => (gh "/pyinvoke/invoke/milestones"))

  (fact "project id plus 'ms' goes to milestones page"
        (query "gh inv ms") => (gh "/pyinvoke/invoke/milestones"))

  (fact "project id plus 'milestones' goes to milestones page"
        (query "gh inv milestones") => (gh "/pyinvoke/invoke/milestones"))

  ;; (fact "project id plus milestone name goes to that milestone page"
  ;;     (query "gh inv 2.3.0") => (gh "/pyinvoke/invoke/milestones/2.3.0")
        ;; TODO: this actually requires the API since the URLs are numeric IDs
        ;; not the milestone name/label :(

  (fact "anything else becomes an issue search for that project id"
        (query "gh inv lolcats")
           => (gh-issue-search "pyinvoke/invoke" "lolcats")
        (query "gh inv a query with spaces")
           => (gh-issue-search "pyinvoke/invoke" "a query with spaces"))

  (fact "this shorthand applies to all input, not just shorthands"
        (query "gh someaccount/somerepo lolcats")
          => (gh-issue-search "someaccount/somerepo" "lolcats")))


(facts "about account searching"

  ; TODO: probably better way to handle this but it works for now :(
  ; Rebinds the real module's accounts list to a known-at-test-time value.
  ; Keeps us from having to modify the test to reflect the real values all the
  ; time.
  (with-redefs [github/github-accounts fake-gh-accounts]

    (with-fake-http fake-gh-repos

      (fact "when input attached to an account yields a repo, go there"
            (query "gh myrepo") => (gh "/bitprophet/myrepo"))

      (fact "shorthand syntax applies to private repos"
            (query "gh myrepo 25") => (gh "/bitprophet/myrepo/issues/25"))

      (fact "when first account doesn't match, next is tried"
            (query "gh otherrepo") => (gh "/employer/otherrepo"))

      (fact "cascading continues"
            (query "gh yetanotherrepo") => (gh "/employer2/yetanotherrepo")))))
