(ns read-replace-kmz.core
  (:gen-class)
  (:require
    [clojure.pprint :as pp]
    [clojure.xml :as xml])
  (:import
    (java.util.zip ZipFile)))

(defn -main [& args]
  (let [zip-file (ZipFile. ^String (first args))]

    (doseq [file (enumeration-seq (.entries zip-file))]
      (let [xml (.getInputStream zip-file file) ]

        (pp/pprint [(.getName file)
                    #_(slurp xml)
                    (xml/parse xml)])

        )

      )

    (println "hello there")))


(comment

  (-main "S.kmz")


  )