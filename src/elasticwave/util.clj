(ns elasticwave.util
  (:import [org.apache.commons.lang3.time FastDateFormat])
  (:gen-class))

(def ^:private ^org.apache.commons.lang3.time.FastDateFormat
  time-formatter
  "Formats time according to Common Log Format"
  (let [time-zone (java.util.TimeZone/getTimeZone "GMT+8")]
    (FastDateFormat/getInstance "dd/MMM/yyyy:HH:mm:ss Z" time-zone)))

(defn format-time [^long t]
  (.format time-formatter (java.util.Date. t)))

(defn get-time-now []
  (.format time-formatter (java.util.Date.)))
