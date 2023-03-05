(ns flk.checker
  (:require
   [clojure.spec.alpha :as s]))


(defn get-explain [predicate]
  (partial s/explain predicate))

(def check-map {:int? (get-explain integer?) })


(defn map-check-chain [fns value]
 (with-out-str ((apply comp fns) value)))

(defn map-map-check-chain [fns values]
  (map (fn [e] (map-check-chain fns e)) values))


(defn get-fun [f-name f-map]
  (or ((keyword f-name) f-map) identity))

(defn k->fun [keys f-map]
  (map (fn [e] (get-fun e f-map)) keys))

(defn eval-checks [value functions]
    (map-map-check-chain (k->fun functions check-map) value))



