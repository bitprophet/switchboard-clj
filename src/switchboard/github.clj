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
                      "par" "paramiko/paramiko"
                      "ala" "bitprophet/alabaster"
                      "pat" "fabric/patchwork"
                      "invoc" "pyinvoke/invocations"})

;; User/organization accounts to search within for repo names
(def github-accounts ["bitprophet"])


; Helpers
(def gh (partial build-url "https://github.com"))
(def gh-api (partial build-url "https://api.github.com"))


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
    (nil? rest) (gh proj)
    ; New issue
    (= "new" rest) (gh proj "issues/new")
    ; Milestones
    (contains? #{"m" "ms" "milestones"} rest) (gh proj "milestones")
    ; Specific issue number
    (re-matches #"\d+" rest) (gh proj "issues" rest)
    ; Issue text search
    :else (gh proj (str "search?q=" rest "&ref=cmdform&type=Issues"))))


(defn derive-full-name [proj]
  ; First try shorthand mapping, returning match if found
  (github-projects proj
                   ; Then try expanding repo-name to account/repo-name
                   (let [result (repo-from-accounts proj)]
                     (if-not (nil? result)
                       ; Found one; return its account/repo name form
                       ((json/read-str (:body @result)) "full_name")
                       ; Not there either, so give up & return input
                       proj))))


(defn github [rest]
  (if (nil? rest)
    ; Base case: github.com
    (gh)
    ; Try expanding first input to repo full_name, then apply rules like
    ; go-to-landing, go-to-issue-number, make-new-issue, etc
    (let [[proj rest] (string/split rest #" " 2)]
      (github-project-dispatch (derive-full-name proj) rest))))
