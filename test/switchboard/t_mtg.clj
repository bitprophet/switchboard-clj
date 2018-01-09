(ns switchboard.t-mtg
  (:require [midje.sweet :refer :all]
            [org.httpkit.fake :refer [with-fake-http]]
            [switchboard.t-utils :refer :all]))


(facts "about basic behavior"

  (fact "bare 'mtg' key just hits MTG Salvation wiki"
        (query "mtg") => (redirect "https://mtg.gamepedia.com/Main_Page"))

  (fact "non-empty 'mtg' search term searches scryfall.com"
        (query "mtg black lotus") => (redirect "https://scryfall.com/search?q=black+lotus"))

  (fact "searches get URL-encoded"
        (query "mtg mana={B}{B} cmc=5 black") => (redirect "https://scryfall.com/search?q=mana%3D%7BB%7D%7BB%7D+cmc%3D5+black")))
