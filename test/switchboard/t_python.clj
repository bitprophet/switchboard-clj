(ns switchboard.t-python
  (:require [midje.sweet :refer :all]
            [switchboard.t-utils :refer :all]))


(defn py [x] (redirect (str "http://docs.python.org" x)))


(facts "about basic behavior"

  (fact "bare key  just hits 2.6 stdlib landing page"
        (query "py") => (py "/2.6/library/"))

  (fact "direct module name hits become straight redirects")

  (fact "non module name hits become generic searches"))


(facts "about different Python versions"

  (fact "py27 uses Python 2.7 docs")

  (fact "py3 uses Python 3.3 docs"))


