;; `gh`: GitHub expansions
;;
;; **Basics**
;;
;; * Empty invocation (`gh`): go to `github.com`.
;; * Anything not matching one of the other rules:
;;     * First, the input is interpreted as being a repo name under each of the
;;       accounts listed in `github-accounts` (in order) and tested for
;;       existence. E.g. if `(def github-accounts ["foo" "bar"])`, `gh blah`
;;       will first check for `github.com/foo/blah`, redirecting to it if it
;;       exists, then will check `github.com/bar/blah`, etc.
;;     * Should all of those tests fail, the input is simply slapped onto
;;       `github.com/` directly, e.g. `gh randomuser/randomrepo` becomes
;;       `github.com/randomuser/randomrepo`.
;;
;; **Project shortcuts**
;;
;; * Shorthand project name found in `github-projects` (e.g. `gh inv`): go to
;;   its project page.
;; * Project + issue number (`gh inv 123`): go to that issue's page.
;; * Project + 'new' (`gh inv new`): go to 'new issue' page.
;; * Project + anything else (`gh inv namespace`): issue search on that
;;   project.

(ns switchboard.github
  (:require [clojure.string :as string]
            [clojure.data.json :as json]
            [org.httpkit.client :as http]
            [switchboard.utils :refer :all]))


;; Map of Github projects' shorthand identifiers
(def github-projects {"inv" "pyinvoke/invoke"
                      "fab" "fabric/fabric"
                      "par" "paramiko/paramiko"})

;; User/organization accounts to search within for repo names
(def github-accounts ["bitprophet"])


; Helpers
(def gh (partial build-url "https://github.com"))
(def gh-api (partial build-url "https://api.github.com"))
(defn gh-proj [proj & xs] (apply gh (github-projects proj) xs))


; External data - cached
(def gh-token (System/getenv "SWITCHBOARD_GITHUB_TOKEN"))


(defn repo-from-accounts [proj]
  (first (filter
           #(= 200 (:status @%))
           (map
             #(http/get (gh-api "repos" % proj) {:oauth-token gh-token})
             github-accounts))))


(defn github-project-dispatch [proj rest]
  (cond
    ; Landing page if just-the-shorthand.
    ; Testing for nil rest must come first to avoid NPEs/etc during regex
    ; tests farther down.
    (nil? rest) (gh-proj proj)
    ; New issue
    (= "new" rest) (gh-proj proj "issues/new")
    ; Specific issue number
    (re-matches #"\d+" rest) (gh-proj proj "issues" rest)
    ; Issue text search
    :else (gh-proj proj (str "search?q=" rest "&ref=cmdform&type=Issues"))))


(defn github [rest]
  (if (nil? rest)
    ; Base case: github.com
    (gh)
    (let [[proj rest] (string/split rest #" " 2)]
      (if (contains? github-projects proj)
        ; First word is project shorthand: enter per-project logic
        (github-project-dispatch proj rest)
        (let [result (repo-from-accounts proj)]
          (if-not (nil? result)
            ; API scan found a repo in github-accounts: go there.
            ((json/read-str (:body @result)) "html_url")
            ; Anything else: treat as arbitrary github.com URI path.
            (gh proj rest)))))))
