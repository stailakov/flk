(ns flk.applyer
  (:require
   [clojure.string :as str]
))


(def func-map {:upper str/upper-case
               :trim str/trim
        })


(defn map-chain [fns value]
 ((apply comp fns) value))

(defn map-map-chain [fns values]
  (map (fn [e] (map-chain fns e)) values))

(defn get-fun [f-name]
  (or ((keyword f-name) func-map) identity))

(defn k->fun [keys]
  (map get-fun keys))

(defn eval-chain-funcs [value functions]
  (map-map-chain (k->fun functions) value))
