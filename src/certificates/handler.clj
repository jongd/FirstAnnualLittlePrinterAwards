(ns certificates.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.etag.core :refer [with-etag create-hashed-etag-fn md5]]
            [ring.util.response :refer [response]]
            [ring.adapter.jetty :refer [run-jetty]]
						[clojure.string :refer [split-lines]]
						[clojure.java.io :refer [file]]
            [net.cgrand.enlive-html :as html]))

(html/deftemplate certificate (file "static/sample.html")
  [award]
  [:#awardTitle] (html/content (:title award)))

(defn static-file
  [filename]
  (slurp (str "static/" filename)))

(defn awards
  []
  (split-lines (static-file "awardTitles.txt")))

(defroutes app-routes
  (GET "/meta.json" []
       (static-file "meta.json"))
  (GET "/sample/" []
       (apply str (certificate {:title "Most Fabulous Printer User"})))
  (GET "/edition/" []
       (apply str (certificate {:title (rand-nth (awards))})))
  (route/files "/" {:root "static"})
  (route/not-found "Not Found"))

(def app
  (-> (handler/site app-routes)
  		(with-etag {:etag-generator (create-hashed-etag-fn md5)})))

(defn -main []
  (let [port-str (System/getenv "PORT")
        port (if port-str (Integer/parseInt port-str) 3000)]
    (run-jetty app {:port port})))
