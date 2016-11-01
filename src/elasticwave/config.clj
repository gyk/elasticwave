(ns elasticwave.config
  (:require [clojure.java.io :as io]
            [clj-yaml.core :as yaml]
            [clojure.walk :refer [stringify-keys]]
            [mount.core :as mount :refer [defstate]])
  (:gen-class))

(defn- load-config
  "Retrieves configuration from .yaml file"
  [conf-path]
  (let [conf-file (or conf-path (.getFile (io/resource "local-config.yaml")))
        conf (yaml/parse-string (slurp conf-file))

        es-config (some-> (conf :elasticsearch-config) stringify-keys)

        conf (assoc conf :elasticsearch-config es-config)]
    conf))

(defn get-resource-path [f]
  (.getPath (io/resource f)))

(defstate conf
  :start (load-config ((mount/args) :config)))
