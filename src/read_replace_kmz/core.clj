(ns read-replace-kmz.core
  (:gen-class)
  (:require
    [clojure.zip :as zip]
    [clojure.pprint :as pp]
    [clojure.java.io :as io]
    [clojure.data.xml :as xml]
    [clojure.string :as str])
  (:import
    (java.io PipedOutputStream)
    (java.io PipedInputStream)
    (java.io FileOutputStream)
    (java.io File)
    (java.nio.file Paths)
    (java.util.zip ZipFile)
    (java.util.zip ZipOutputStream)
    (java.util.zip ZipEntry)))

(def file-pref "read-replace-kmz-fix")

(defn write-kmz [^String kmz-file-name name data]
  (let [out-file (File. kmz-file-name)
        file-stream (FileOutputStream. out-file)
        output-stream (ZipOutputStream. file-stream)
        zip-entry (ZipEntry. ^String name)]

    (.putNextEntry output-stream ^ZipEntry zip-entry)
    (.write output-stream data 0 (count data))
    (.closeEntry output-stream)
    (.close output-stream)))


#_(loop [z z]
  (if (zip/end? z)
    (-> z zip/root xml/emit-str)
    (let [n (zip/node z)]
      (if (string? n)
        (if (contains-special-chars? n)
          (recur (zip/edit z xml/->CData))
          (recur (zip/next z)))
        (recur (zip/next z))))))


(defn transform [xml]
  (loop [xml xml]
    (if (zip/end? xml)
      (zip/root xml)
      (let [node (zip/node xml)]
        (if (= :LineStyle (:tag node))

          (do (println node)
              (recur (zip/next xml))
              )

          (recur (zip/next xml)))))))


(defn open-kmz [^File kmz-file]
  (let [kmz-file-name (.getName kmz-file)
        parent-path (.getAbsolutePath (.getParentFile kmz-file))
        new-file-name (str (File. parent-path (str file-pref "-" kmz-file-name)))
        zip-file (ZipFile. kmz-file)]

    (doseq [file (enumeration-seq (.entries zip-file))]

      (when (and (str/ends-with? kmz-file-name ".kmz")
                 (not (str/includes? kmz-file-name file-pref)))

        (pp/pprint [:found-kmz-file  (.getName kmz-file)])

        (with-open [input-stream (.getInputStream zip-file file)]
          (let [original-xml (xml/parse input-stream)

                ;; TODO, do some transformation of the data

                _ (pp/pprint [:new (transform (zip/xml-zip original-xml))])
                new-xml (xml/emit-str original-xml)
                new-byte-array (.getBytes new-xml)]

            #_(pp/pprint original-xml)

            #_(pp/pprint [:transforming (.getAbsolutePath kmz-file) :writing-to new-file-name])
            (println)

            #_(write-kmz new-file-name (.getName file) new-byte-array)))

        ))))

(defn -main [& args]
  (let [folder (File. ^String (first args))]
    (when (.isDirectory folder)
      (doseq [file (file-seq folder)]
        (when-not (.isDirectory file)
          (open-kmz file))))))


(comment
  (-main "S.kmz"))