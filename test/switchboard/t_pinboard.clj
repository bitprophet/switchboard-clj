(ns switchboard.t-pinboard
  (:require [midje.sweet :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [switchboard.t-utils :refer :all]))


(facts "about basic behavior"

  (fact "bare 'pb' key hits my bookmarks"
        (query "pb") => (redirect "https://pinboard.in/u:bitprophet"))

  (with-fake-http ["https://pinboard.in/u:bitprophet/t:sysadmin" {:body "fuck"}]
    (fact "terms matching a tag go to that tag"
          (query "pb sysadmin") => (redirect "https://pinboard.in/u:bitprophet/t:sysadmin")))

    (fact "terms not matching a tag become a search"
          (query "pb wat") => (redirect "https://pinboard.in/search/u:bitprophet/?query=wat"))

    (fact "multi-word terms also become a search instead of exploding, sheesh"
          (query "pb wat now") => (redirect "https://pinboard.in/search/u:bitprophet/?query=wat+now")))
