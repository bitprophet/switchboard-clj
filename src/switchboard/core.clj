;; # Overview
;;
;; Switchboard is designed to run as a browser backend search engine which
;; takes a single string query, parses it, and interprets it according to a set
;; of rules.

(ns switchboard.core
  (:require [clojure.string :as string]
            [clojure.data.json :as json]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace-web]]
            [ring.util.response :refer [redirect]]
            [org.httpkit.client :as http]
            [switchboard.utils :refer :all]
            [switchboard.github :refer [github]]))


;; TODO: move to its own module, meh
;; TODO: try factoring out the common pattern between this and pypi
(def py (partial build-url "https://docs.python.org"))
(defn python [version rest]
  (let [py (partial py version)]
    (if (nil? rest)
      (py "library")
      (let [direct (py "library" (str rest ".html"))]
        (if (exists? direct)
          direct
          (py (str "search.html?q=" rest)))))))

(def py26 (partial python "2.6"))
(def py27 (partial python "2.7"))
;; TODO: I might care about 3.4-specific shit someday?
(def py3 (partial python "3.3"))

(def -pypi (partial build-url "https://pypi.python.org"))
(defn pypi [rest]
  (if (nil? rest)
    (-pypi)
    (let [direct (-pypi "pypi" rest)]
      (if (exists? direct)
        direct
        (-pypi (str "pypi?:action=search&submit=search&term=" rest))))))


;; TODO: own module? maybe just a single "simple searches" module...
(def ud (partial build-url "http://urbandictionary.com"))
(defn urbandictionary [rest]
  (if (nil? rest)
    (ud)
    (ud (str "define.php?term=" rest))))

;; TODO: also good candidate for simple search refactor/module? but is only one
;; to have different site for base vs rest cases...
(defn mtg [rest]
  (if (nil? rest)
    "http://mtgsalvation.com"
    ;; NOTE: v=scan means default to just-the-cards view, which is a nice list
    ;; for multiple-hit results; the site automagically displays full view if
    ;; only one hit, too. Less work for me!
    (str "http://magiccards.info/query?v=scan&q=" rest)))


;; TODO: split into own module
(def pb (partial build-url "https://pinboard.in"))
(def pb-user "u:bitprophet")
(defn pinboard [rest]
  (if (nil? rest)
    (pb pb-user)
    (let [rest (http/url-encode rest)
          tag-url (pb pb-user (str "t:" rest))]
      ; Sadly, an 'empty' page of bookmarks isn' a 404 or similar, so...we do
      ; this instead. Easier than using auth + API for now.
      (if-not (.contains (@(http/get tag-url) :body) "<span class=\"bookmark_count\">0</span>")
        tag-url
        (pb "search" pb-user (str "?query=" rest))))))


(defn gis [rest]
  (if (nil? rest)
    ;; Empty search, while unlikely, can just take us to images.google.com -
    ;; good for e.g. reverse image lookup. ¯\_(ツ)_/¯
    "https://images.google.com/"
    ;; Regular ol' GIS otherwise
    (str "https://www.google.com/search?tbm=isch&q=" rest)))

(def -ann (partial build-url "http://www.animenewsnetwork.com"))
(defn ann [rest]
  (if (nil? rest)
    (-ann)
    (-ann "encyclopedia" "search" (str "name?q=" rest))))

;; Dispatch requests to given modules based on first word ("key").
;;
;; When no matching key is found, all text is used as-is in a Google search.
(defn dispatch [[key rest]]
  (case key
    "gh" (github rest)
    "py" (py26 rest)
    "py27" (py27 rest)
    "py3" (py3 rest)
    "pp" (pypi rest)
    "ud" (urbandictionary rest)
    "pb" (pinboard rest)
    "mtg" (mtg rest)
    "gis" (gis rest)
    "ann" (ann rest)
    (str "https://google.com/search?q=" key (if rest (str " " rest)))))


; Basic HTTP handler logic
(defn handler [request]
  (let [query (-> request :params :query)]
    (if-not (empty? query)
      ; Use HTTP 307 so browsers don't cache when manually testing/poking
      (redirect (dispatch (string/split query #" " 2)) :temporary-redirect)
      error-response)))


; App wrapping requests w/ easy access to params via map+keyword
(def app (-> handler
           wrap-keyword-params
           wrap-params))

; Human-facing app adding stacktrace display to the mix
(def human-app (-> app wrap-stacktrace-web))
