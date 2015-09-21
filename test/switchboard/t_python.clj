(ns switchboard.t-python
  (:require [midje.sweet :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [switchboard.t-utils :refer :all]))


(defn py [x] (redirect (str "https://docs.python.org" x)))
(defn pp [x] (redirect (str "https://pypi.python.org" x)))


(facts "about basic behavior"

  (fact "bare 'py' key just hits 2.6 stdlib landing page"
        (query "py") => (py "/2.6/library"))

  (with-fake-http ["https://docs.python.org/2.6/library/operator.html" "ok"]
    (fact "direct module name hits become straight redirects"
          (query "py operator") => (py "/2.6/library/operator.html")))

  (fact "non module name hits become generic searches"
        (query "py lol wut") => (py "/2.6/search.html?q=lol wut")))


(facts "about different Python versions"

  (fact "py27 uses Python 2.7 docs"
        (query "py27 os") => (py "/2.7/library/os.html"))

  (fact "py3 uses Python 3.3 docs")
       (query "py3 re") => (py "/3.3/library/re.html"))


(facts "regarding pypi search"

  (fact "bare 'pp' key just goes to pypi landing page"
        (query "pp") => (pp ""))

  (with-fake-http ["https://pypi.python.org/pypi/paramiko" "ok"
                   "https://pypi.python.org/pypi/lol" 404]
    (fact "direct package name hit goes straight there"
          (query "pp paramiko") => (pp "/pypi/paramiko"))

    (fact "'pp' with a non-exact-matching argument searches pypi"
          (query "pp lol") => (pp "/pypi?:action=search&submit=search&term=lol"))))
