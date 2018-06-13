(ns switchboard.t-python
  (:require [midje.sweet :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [switchboard.t-utils :refer :all]))


(defn py [x] (redirect (str "https://docs.python.org" x)))
(defn pp [x] (redirect (str "https://pypi.org" x)))


(facts "about basic behavior"

  (fact "bare 'py' key just hits 2.7 stdlib landing page"
        (query "py") => (py "/2.7/library"))

  (with-fake-http ["https://docs.python.org/2.7/library/operator.html" "ok"]
    (fact "direct module name hits become straight redirects"
          (query "py operator") => (py "/2.7/library/operator.html")))

  (fact "non module name hits become generic searches"
        (query "py lol wut") => (py "/2.7/search.html?q=lol wut")))


(facts "about different Python versions"

  (fact "py26 uses Python 2.6 docs"
        (query "py26 os") => (py "/2.6/library/os.html"))

  (fact "py3 uses Python 3.6 docs")
       (query "py3 re") => (py "/3.6/library/re.html"))


(facts "regarding pypi search"

  (fact "bare 'pp' key just goes to pypi landing page"
        (query "pp") => (pp ""))

  (with-fake-http ["https://pypi.org/project/paramiko" "ok"
                   "https://pypi.org/project/lol" 404]
    (fact "direct package name hit goes straight there"
          (query "pp paramiko") => (pp "/project/paramiko"))

    (fact "'pp' with a non-exact-matching argument searches pypi"
          (query "pp lol") => (pp "/search/?q=lol"))))
