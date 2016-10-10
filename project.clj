(defproject elasticwave "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clojurewerkz/elastisch "3.0.0-beta1"]
                 [org.clojure/tools.cli "0.3.5"]
                 [clj-yaml "0.4.0"]
                 [org.apache.commons/commons-lang3 "3.4"]
                 [org.elasticsearch/elasticsearch "2.3.5"]
                 [cheshire "5.6.1"]
                 [mount "0.1.10"]]
  :main ^:skip-aot elasticwave.core
  :repl-options {:init-ns user}
  :profiles
    {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
           :source-paths ["dev"]
           :resource-paths ["resources"]
           :main elasticwave.core}
     :uberjar {:aot :all
               :main elasticwave.core}})
