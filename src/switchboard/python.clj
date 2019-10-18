(ns switchboard.python
  (:require [switchboard.utils :refer :all]
            [org.httpkit.client :as http]))


;; TODO: try factoring out the common pattern between this and pypi
(def py (partial build-url "https://docs.python.org"))
(defn python [version rest]
  (let [py (partial py version)]
    (if (nil? rest)
      (py "library")
      (let [direct (py "library" (str rest ".html"))]
        ; If the slug was the name of an actual stdlib module, go right to its
        ; page, nothing else required
        (if (exists? direct)
          direct
          ; Otherwise, we use the search page, but strip out the extremely
          ; vexing '&highlight=<term>' that it crams into the hyperlinks. (Wish
          ; we could just turn this off with a GET param or something!)
          (let [response @(http/get (py "search.html") {:query-params {:q rest}})]
            ; TODO: argh. we can't actually do this w/o local JS!!!
            ; TODO: try grabbing the 'full index' page and parsing that HTML
            ; instead? (bonus: lets us go back to just ending up w/ a URL
            ; redirect?)
            response))))))

(def py2 (partial python "2.7"))
(def py3 (partial python "3.4"))
(def py36 (partial python "3.6"))


(def -pypi (partial build-url "https://pypi.org"))
(defn pypi [rest]
  (if (nil? rest)
    (-pypi)
    (let [direct (-pypi "project" rest)]
      (if (exists? direct)
        direct
        (-pypi (str "search/?q=" rest))))))
