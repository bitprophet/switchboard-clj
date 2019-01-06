(ns switchboard.t-core
  (:require [switchboard.utils :refer :all]
            [switchboard.t-utils :refer :all]
            [midje.sweet :refer :all]))


(defn goog [x] (redirect (str "https://google.com/search?q=" x)))


(fact "lack of query param displays error"
      (request {}) => error-response)

(fact "present but empty query param value displays error"
      (query "") => error-response)

(fact "if no submodule is matched, default is to Google"
      (query "nope") => (goog "nope")
      (query "nope nohow") => (goog "nope nohow"))

(fact "gis searches google image search"
      (query "gis") => (redirect "https://images.google.com/")
      (query "gis apple") => (redirect "https://www.google.com/search?tbm=isch&q=apple")
      (query "gis apple pie") => (redirect "https://www.google.com/search?tbm=isch&q=apple pie"))

(fact "ann searches Anime News Network"
      (query "ann") => (redirect "http://www.animenewsnetwork.com")
      (query "ann sword art online") => (redirect "http://www.animenewsnetwork.com/encyclopedia/search/name?q=sword art online"))

(fact "nms goes to Gamepedia or searches Google directly"
      (query "nms") => (redirect "https://nomanssky.gamepedia.com")
      (query "nms mark on map") => (redirect "https://www.google.com/search?q=\"No Man's Sky\" mark on map"))

(fact "r/stuff goes to Reddit"
      (query "r/spacex") => (redirect "https://www.reddit.com/r/spacex"))

(fact "askme searches Ask Metafilter"
      (query "askme snow tires") => (goog "site:ask.metafilter.com snow tires"))
