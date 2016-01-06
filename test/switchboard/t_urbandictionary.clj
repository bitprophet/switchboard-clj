(ns switchboard.t-urbandictionary
  (:require [midje.sweet :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [switchboard.t-utils :refer :all]))


(facts "about basic behavior"

  (fact "bare 'ud' key just hits urbandictionary homepage"
        (query "ud") => (redirect "http://urbandictionary.com"))

  (fact "non-empty 'ud' search term searches urbandictionary"
        (query "ud bae") => (redirect "http://urbandictionary.com/define.php?term=bae"))

  (fact "spaces work too"
        (query "ud netflix and chill") => (redirect "http://urbandictionary.com/define.php?term=netflix and chill")))
