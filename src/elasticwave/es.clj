(ns elasticwave.es
  (:require [elasticwave.config :refer [conf]]
            [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.document :as esd]
            [clojurewerkz.elastisch.native.bulk :as es-b]
            [cheshire.core :as json]
            [mount.core :as mount :refer [defstate]])
  (:import [org.elasticsearch.action.bulk BulkProcessor BulkProcessor$Listener BulkRequest BulkResponse]
           [org.elasticsearch.action ActionRequest]
           [org.elasticsearch.client Client Requests])
  (:gen-class))

(set! *warn-on-reflection* true)

(defstate es-conn :start
  (let [es-hosts (conf :elasticsearch.hosts)
        es-port (conf :elasticsearch.port)
        es-host-port-pairs (if (coll? es-hosts)
                              (map vector es-hosts (repeat es-port))
                              [[es-hosts es-port]])
        es-settings (conf :elasticsearch-config)]
  (es/connect es-host-port-pairs es-settings)))

(defstate es-index :start
  (conf :elasticsearch.index))

(defstate es-type :start
  (conf :elasticsearch.type))

(defstate index-doc-single :start
  (fn [doc]
    (esd/create es-conn es-index es-type doc)))

(defstate ^BulkProcessor bulk-processor
  :start
    (let [builder
            (BulkProcessor/builder es-conn
              (reify BulkProcessor$Listener
                (^void beforeBulk [this ^long execution-id ^BulkRequest request])
                (^void afterBulk [this ^long execution-id ^BulkRequest request ^BulkResponse response]
                  (when (.hasFailures response)
                    (prn "Bulk indexing failed, count = " (count (.getItems response)))))
                (^void afterBulk [this ^long execution-id ^BulkRequest request ^Throwable failure]
                  (prn (.getMessage failure)))))]
      (do
        (.setConcurrentRequests builder 0)

        (when-let [param (conf :elasticsearch.bulk.flush.max.actions)]
          (.setBulkActions builder param))

        (when-let [param (conf :elasticsearch.bulk.flush.max.size.mb)]
          (.setBulkSize builder param))

        (when-let [param (conf :elasticsearch.bulk.flush.interval.ms)]
          (.setFlushInterval builder param))
        
        (.build builder)))
  :stop
    (.close bulk-processor))

(defstate index-doc-bulk :start
  (fn [doc]
    (let [^ActionRequest request
            (doto (Requests/indexRequest)
              (.index es-index)
              (.type es-type)
              (.source (json/encode doc)))]
      (.add bulk-processor request))))

(defstate index-doc :start
  (if (conf :elasticsearch.use-bulk-api false)
    index-doc-bulk
    index-doc-single))

; Elastisch's Bulk API
(defstate bulk-index-docs :start
  (fn [docs]
    (es-b/bulk-with-index-and-type es-conn es-index es-type docs)))
