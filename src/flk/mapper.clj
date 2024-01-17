(ns flk.mapper
  (:require
   [cheshire.core :refer :all]
   [clojure.string :as str]
))

(defn assoc-in* [path v]
  (assoc-in {} path v))

(defn mapping-fields-json [fun source]
  (map fun (seq  (parse-string source))))

(defn to-keywords-path [fun source]
   (map (fn [e] (map keyword (str/split e #"\."))) (mapping-fields-json fun source)))

(defn map-to-inner [mapping source]
  (into {}
         (map assoc-in*
              (to-keywords-path second mapping)
              (map
               (fn [e] (get-in source e))
               (to-keywords-path first mapping))
              )))

