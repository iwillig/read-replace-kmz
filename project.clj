(defproject read-replace-kmz "0.0.1"

  :description ""
  :url ""

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main read-replace-kmz.core

  :profiles {:dev {:source-paths ["dev"] :repl-options {:init-ns user}}
             :uberjar {:aot :all :uberjar-name "read-replace.jar"}}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.2"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.namespace "0.2.11"]])
