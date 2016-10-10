(ns elasticwave.message
  (:require [elasticwave.config :refer [conf]]
            [elasticwave.es :refer [index-doc]]
            [elasticwave.util :refer [get-time-now]]
            [mount.core :as mount :refer [defstate]])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn- run-with [^double throughput action]
  (let [interval (/ 1000.0 throughput)
        tolerance 10000]
    (loop [start-time (double (System/currentTimeMillis))]
      (action)
      (let [now (double (System/currentTimeMillis))
            elapsed (- now start-time)
            remaining (long (- interval elapsed))]
        (if (< remaining (- tolerance))
          (printf "Data generation is lagging behind by more than %d ms. Trying to stop the thread...\n"
              tolerance)
          (do
            (when (> remaining 0)
              (Thread/sleep remaining))
            (recur (+ start-time interval))))))))

(defn make-doc []
  {:timestamp (get-time-now)
   :name "dummy"
   :key (format "%4d" (rand-int 1000))
   :value (rand-int 10000)})

(defstate run :start
  (fn []
    (run-with (conf :throughput) #(index-doc (make-doc)))))
