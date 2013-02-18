(defproject uk.org.1729/chess "0.1.0-SNAPSHOT"
  :description "Utilities for modelling Chess games in Clojure"
  :url "http://github.com/ray1729/chess"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :profiles {:dev {:dependencies [[midje "1.4.0"]]
                   :plugins      [[lein-midje "2.0.1"]]}})
