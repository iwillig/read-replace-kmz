(ns read-replace-kmz.core
  (:gen-class)
  (:require
    [clojure.pprint :as pp]
    [clojure.java.io :as io]
    [clojure.data.xml :as xml])
  (:import
    (java.io PipedOutputStream)
    (java.io PipedInputStream)
    (java.io FileOutputStream)
    (java.io File)
    (java.nio.file Paths)
    (java.util.zip ZipFile)
    (java.util.zip ZipOutputStream)
    (java.util.zip ZipEntry)))

(defn write-kmz [^String kmz-file-name name data]
  (let [out-file (File. kmz-file-name)
        file-stream (FileOutputStream. out-file)
        output-stream (ZipOutputStream. file-stream)
        zip-entry (ZipEntry. ^String name)]

    (.putNextEntry output-stream ^ZipEntry zip-entry)
    (.write output-stream data 0 (count data))
    (.closeEntry output-stream)
    (.close output-stream)))

(defn open-kmz [^File kmz-file]
  (let [kmz-file-name (.getName kmz-file)
        parent-path (.getAbsolutePath (.getParentFile kmz-file))
        new-file-name (str (File. parent-path (str "fix-" kmz-file-name)))
        zip-file (ZipFile. kmz-file)]

    (doseq [file (enumeration-seq (.entries zip-file))]
      (with-open [input-stream (.getInputStream zip-file file)]
        (let [original-xml (xml/parse input-stream)
              ;; TODO, do some transformation of the data
              new-xml (xml/emit-str original-xml)
              new-byte-array (.getBytes new-xml)]

          (pp/pprint [:transforming (.getAbsolutePath kmz-file) :writing-to new-file-name])
          (write-kmz new-file-name (.getName file) new-byte-array))))))

(defn -main [& args]
  (let [folder (File. ^String (first args))]

    (when (.isDirectory folder)
      (doseq [file (file-seq folder)]
        (when-not (.isDirectory file)
          #_(pp/pprint [:reading (.getName file) :in (.getAbsolutePath file)])
          (open-kmz file)))))

  )


(comment
  (-main "S.kmz"))