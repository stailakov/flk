(ns flk.data
  (:require
   [honeysql.core :as sql]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]))

(def ds (jdbc/get-datasource  {:dbtype "postgresql"
   :dbname "flk"
   :host "localhost"
   :port 5433
   :user "postgres"
   :password "postgres"
    }
))
  
  (defn execute [querry]
    (jdbc/execute!
     ds
     (sql/format querry)
     {:builder-fn rs/as-unqualified-maps
      :return-keys true}))


(defn add-to-table [table values]
  (let [res {:insert-into table
             :values [values]}]
    (execute res)))


(defn update-table [table values id]
  (let [res {:update table
             :set values
             :where [:= :id (Integer/parseInt id)]}]
    (execute res)))


(defn delete-table [table id]
  (let [res  {:delete-from table 
              :where [:= :id (Integer/parseInt id)]
                }]
      (execute res)
      ))


(defn select-mappings []
  (let [res  {:select [
                       [:m.id "id"]
                       [:m.value "value"]
                       ]
              :from [[:mapping :m]]
             }]
    (execute res)))


(defn get-mapping-by-id [id]
  (let [res  {:select [[:m.value "value"]
                       ]
              :from [[:mapping :m]]
             :where [:= :m.id (Integer/parseInt id)]
             }]
    (execute res)))

(defn get-mapping [id]
  (:value (first (get-mapping-by-id id))))

(defn create-mapping-db [request]
  (let [{:keys [body]} request 
        res (add-to-table :mapping body)]
    res))

(defn delete-mapping-by-id [id]
      (delete-table :mapping id))

(defn update-mapping-db [body id]
  ( let [res (update-table :mapping body id)]
  {:data res}))
