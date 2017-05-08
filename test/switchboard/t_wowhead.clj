(ns switchboard.t-wowhead
  (:require [midje.sweet :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [switchboard.t-utils :refer :all]))


(facts "about basic behavior"

  (fact "bare 'wh' key just hits Wowhead frontpage"
        (query "wh") => (redirect "http://wowhead.com"))

  (fact "non-empty 'wh' search term searches Wowhead"
        (query "wh spicing things up") => (redirect "http://wowhead.com/search?q=spicing things up")))
