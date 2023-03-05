(ns flk.core
  (:require [compojure.route :as route]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            [json-path :as jp]
            [cheshire.core :refer :all]
            [flk.data :as data]
            [flk.checker :as ch]
            [flk.applyer :as a]
            [flk.mapper :as mapper]
            )
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

(defn eval-chain [req]
  (let [{:keys [body]} req
        {:keys [value functions]} body]
    {:statys 200
     :body (a/eval-chain-funcs value functions)}
    ))

(defn eval-chain-predicates [req]
  (let [{:keys [body]} req
        {:keys [value functions]} body]
    {:statys 200
     :body (ch/eval-checks value functions)}
    ))

(defn test [req]
  (let [{:keys [body]} req
        {:keys [value functions]} body]
    (println {:value (str value)})))

(defn map-with [request]
  (let [{:keys [body route-params]} request
        {:keys [id]} route-params]
      {:status 200
     :body (mapper/map-to-inner (data/get-mapping id) body)}))


(defn get-mapping-contr [id]
  {:status 200
   :body {:data
          (data/get-mapping id)}})

(defn create-mapping-contr [req]
  {:status 200
   :body {:data
          (data/create-mapping-db req)}})


(defroutes compojure-handler
  (GET "/mapping/:id" [id] (get-mapping-contr id))
  (POST "/mapping/" req (create-mapping-contr req))
  (POST "/map-with/:id" req (map-with req))
  (POST "/eval-chain" req (eval-chain req))
  (POST "/check" req (eval-chain-predicates req))
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

