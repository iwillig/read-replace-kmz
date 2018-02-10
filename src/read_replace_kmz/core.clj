(ns read-replace-kmz.core
  (:gen-class)
  (:require
    [clojure.pprint :as pp]
    [clojure.java.io :as io]
    [clojure.data.xml :as xml])
  (:import
    (java.io PipedOutputStream)
    (java.io PipedInputStream)
    (java.util.zip ZipFile)
    (java.util.zip ZipOutputStream)
    (java.util.zip ZipEntry)))

#_(set! *warn-on-reflection* true)

(defn write-kmz [name ]
  (let []))

(defn -main [& args]
  (let [zip-file (ZipFile. ^String (first args))]
    (doseq [file (enumeration-seq (.entries zip-file))]
      (with-open [input-stream (.getInputStream zip-file file)]

        (let [original-xml (xml/parse input-stream)
              new-xml (xml/emit-str original-xml)
              new-stream (io/input-stream (.getBytes new-xml))]

          (pp/pprint [(.getName file)
                      #_original-xml
                      new-xml
                      new-stream
                      ])

          )

        ))))


(comment

  (-main "S.kmz")


  )