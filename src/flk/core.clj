(ns flk.core
  (:require [compojure.route :as route]
            [clojure.spec.alpha :as s]
            [cheshire.core :refer :all])
  (:use [ring.adapter.jetty :as jetty]
        ring.middleware.content-type
        ring.middleware.session
        [ring.middleware.json :only [wrap-json-response wrap-json-body]]
        [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
        [ring.middleware.keyword-params :refer [wrap-keyword-params]]
        [ring.middleware.resource :refer [wrap-resource]]
        [ring.middleware.cors :refer [wrap-cors]]
        [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
        ring.util.response
        compojure.core
        compojure.handler)
  (:gen-class))

(defn create-doc [request]
  (let [{:keys [body]} request]
      {:status 200
     :body (map-to s-map body)}))

(defroutes compojure-handler
  (POST "/doc" req (create-doc req))
  (route/not-found "<h1>Not found!</h1>"))

(def app (-> compojure-handler
  ;;          (wrap-basic-authentication authenticated?)
             wrap-json-response
             (wrap-defaults
              (assoc-in site-defaults [:security :anti-forgery] false))
            (wrap-json-body {:keywords? true})
             (wrap-cors :access-control-allow-origin [#".*"] :access-control-allow-methods [:get :post :delete :put])))
 
(defn -main
  [] (jetty/run-jetty app {:port 3000, :join? false}))


(def s-map "{\"mapping\" : {\"field1\": \"field2\",\"field3\": \"field4\",\"field5\": \"field6\"}}")
(def j {:field1 "aaa" :field3 "bbb" :field5 "ccc"})
(get j :field1)

(defn map-to [mapping from]
  (zipmap (mapping-fields second mapping)
          (map
           (fn [e] (get from e))
           (mapping-fields first mapping))))


(defn mapping-fields [fun source]
  (map fun (seq (:mapping (parse-string source true)))))


;;(defonce server (jetty/run-jetty #'app {:port 3000 :join? false}))
