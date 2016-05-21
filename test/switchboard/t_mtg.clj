(ns switchboard.t-mtg
  (:require [midje.sweet :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [switchboard.t-utils :refer :all]))


(facts "about basic behavior"

  (fact "bare 'mtg' key just hits MTG Salvation wiki"
        (query "mtg") => (redirect "http://mtgsalvation.com"))

  (fact "non-empty 'mtg' search term searches magiccards.info"
        (query "mtg black lotus") => (redirect "http://magiccards.info/query?q=black lotus&v=scan&s=cname")))
