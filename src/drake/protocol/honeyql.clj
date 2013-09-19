(ns drake.protocol.honeyql
  (:use [drake-interface.core :only [Protocol]])
  (:require [clojure.string :as str]
            [sosueme.conf :as conf]
            [cheshire.core :as json])
  (:import [com.factual.honey HoneyStatement]
           [com.factual.driver Factual]))

(defn read-auth-dot-factual
  "Returns a [key secret] tuple from reading ~/.factual/factual-auth.yaml.
   Returns nil if none found."
  []
  (when-let [{:keys [key secret]} (conf/dot-factual "factual-auth.yaml")]
    [key secret]))

(defn get-auth
  "Returns a [key secret] tuple for auth. First looks at step's :opts,
   expecting :key and :secret. If not both are defined, tries reading
   ~/.factual/factual-auth.yaml."
  [{:keys [key secret]}]
  (if (and key secret)
    [key secret]
    (if-let [auth (read-auth-dot-factual)]
      auth
      (throw (Exception. "Could not find authentication in step's :opts or in ~/.factual/factual-auth.yaml")))))

(defn run-honey
  "Returns an ArrayList of records from Factual, resulting from
   running the query described by sql."
  [sql [key secret]]
  (-> (HoneyStatement. sql)
      (.execute (Factual. key secret))
      .getData))

(defn as-json-recs [facts]
  (map json/generate-string facts))

(defn run-honeyql-step
  "Runs the HoneyQl statement in the step's body. Will use the auth
   specified in the step's :opts via :key and :secret if both are present,
   or will read the ~/.factual/factual-auth.yaml file."
  [step]
  (let [sql       (str/join " " (:cmds step))
        auth-opts (select-keys (:opts step) [:key :secret])
        auth      (get-auth auth-opts)
        json-recs (-> sql
                      (run-honey auth)
                      as-json-recs)
        outfile   (first (:outputs step))]
    (spit outfile (str/join "\n" json-recs))))

(defn honeyql []
  (reify Protocol
    (cmds-required? [_] true)
    (run [_ step]
      (run-honeyql-step step))))