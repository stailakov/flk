(ns flk.core
  (:require [compojure.route :as route]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [json-path :as jp]
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

(def s-map "{\"field1.field3\": \"field2.field4\",\"field5.field7\": \"field6.field8\"}" )

(def json-source {:field1
                  {:field3
                   {:field10 "ccc"}}
                  :field5 {:field7 "bbb"
                           }})

(get-in json-source (map keyword(str/split "field1.field3" #"\.")))
(assoc-in json-source (map keyword(str/split "field1.field3" #"\.")) "ss")
(assoc-in {} [:field1 :field2] "sss")

(defn assoc-in* [path v]
  (assoc-in {} path v))

;;(apply hash-map (map assoc-in* [[:k4 :k5] [:k1]] [s "cc"]))

(defn mapping-fields [fun source]
  (map fun (seq (:mapping (parse-string source true)))))

(defn mapping-fields-json [fun source]
  (map fun (seq  (parse-string s-map))))

(defn to-keywords-path [fun source]
   (map (fn [e] (map keyword (str/split e #"\."))) (mapping-fields-json fun source)))

(defn map-to-flat [mapping from]
  (zipmap (mapping-fields second mapping)
          (map
           (fn [e] (get from e))
           (mapping-fields first mapping))))

(defn map-to-inner [mapping source]
  (into {}
         (map assoc-in*
              (to-keywords-path second mapping)
              (map (fn [e] (get-in source e)) (to-keywords-path first mapping)))))

(defn map-function-on-map-keys [m f]
    (zipmap (map f (keys m)) (vals m)))

(defn create-doc [request]
  (let [{:keys [body]} request]
      {:status 200
     :body (map-to-inner s-map body)}))

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



;(defonce server (jetty/run-jetty #'app {:port 3000 :join? false}))
