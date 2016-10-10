(ns elasticwave.core
  (:require [clojure.tools.cli :as cli]
            [elasticwave.message :refer [run]]
            [mount.core :as mount :refer [defstate]])
  (:gen-class))

(def ^:private cli-options
  [["-c" "--config my-config.yaml" "Set .yaml file for configuration"]])

(defn- exit [status msg]
  (println msg)
  (System/exit status))


(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)
        help (str "Options: \n\n" summary \newline)]

    ; handles help and error conditions
    (cond
      (:help options) (exit 0 help)
      errors (exit 1 (str "Errors occurred: \n\n" errors \newline)))

    (mount/start-with-args options)

    (run)))
