(ns drake-honeyql.core
  (:require [clojure.string :as str]
            [sosueme.conf :as conf]
            [cheshire.core :as json])
  (:import [com.factual.honey HoneyStatement]
           [com.factual.driver Factual]))

(defn factual-handle []
  (let [{:keys [key secret]} (conf/dot-factual "factual-auth.yaml")]
    (Factual. key secret)))

(defn run-honey
  "Returns an ArrayList of records from Factual, resulting from
   running the query described by sql."
  [sql]
  (-> (HoneyStatement. sql)
      (.execute (factual-handle))
      .getData))

(defn as-json-recs [facts]
  (map json/generate-string facts))

(defn run-step [step]
  (let [sql       (str/join " " (:cmds step))
        json-recs (-> sql
                      run-honey
                      as-json-recs)
        output    (first (:outputs step))]
    (spit output (str/join "\n" json-recs))))

(def STEP
  {:cmds ["SELECT name, owner FROM restaurants WHERE owner IS NOT NULL"]})
