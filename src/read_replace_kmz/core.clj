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
    (java.text SimpleDateFormat)
    (java.io File)
    (java.util.zip ZipFile)
    (java.util.zip ZipOutputStream)
    (java.util.zip ZipEntry)))

(def file-pref "fix")

(defn write-kmz [^String kmz-file-name name data]
  (let [out-file (File. kmz-file-name)
        file-stream (FileOutputStream. out-file)
        output-stream (ZipOutputStream. file-stream)
        zip-entry (ZipEntry. ^String name)]

    (.putNextEntry output-stream ^ZipEntry zip-entry)
    (.write output-stream data 0 (count data))
    (.closeEntry output-stream)
    (.close output-stream)))


(defn transform [xml]

  (loop [loc xml]

    (let [node (zip/node loc)]

      (cond

        (zip/end? loc)
        (zip/root loc)

        (and (= :name (:tag node))
             (= '("S") (:content node)))

        (recur
          (zip/next
            (zip/edit loc (fn [node]
                            (assoc node :content '("T"))))))

        (= :LineStyle (:tag node))
        (recur
          (zip/next (zip/edit loc (fn [node] (assoc node :content
                                                         [(xml/element :color {} "ff00ff00")
                                                          (xml/element :width {} "2")])))))

        (= :PolyStyle (:tag node))
        (recur
          (zip/next
            (zip/edit loc
                      (fn [node] (assoc node :content [(xml/element :color {} "8000ff00")])))))

        (= :coordinates (:tag node))
        (recur (zip/next (zip/edit loc (fn [node] (update node :content #(map str/trim %))))))

        :else (recur (zip/next loc))))))


(defn find-type [xml])

(defn transform-kmz [^File kmz-file]
  (let [kmz-file-name (.getName kmz-file)]

    (when (and (str/ends-with? kmz-file-name ".kmz")
               (not (str/includes? kmz-file-name file-pref)))

      (let [parent-path (.getAbsolutePath (.getParentFile kmz-file))
            new-file-name (str (File. parent-path (str file-pref "-" kmz-file-name)))
            zip-file (ZipFile. kmz-file)]

        (doseq [file (enumeration-seq (.entries zip-file))]

          (pp/pprint [:found-kmz-file  (.getName kmz-file)])

          (with-open [input-stream (.getInputStream zip-file file)]

            (let [original-xml (xml/parse input-stream)
                  _ (pp/pprint original-xml)
                  transformed-xml (transform (zip/xml-zip original-xml))

                  new-xml (xml/emit-str transformed-xml)
                  new-byte-array (.getBytes new-xml)]

              (write-kmz new-file-name (.getName file) new-byte-array))))))))

(defn -main [& args]
  (let [folder (File. ^String (first args))]
    (if (.isDirectory folder)
      (let [file-seq (file-seq folder)]
        (doseq [file file-seq]
          (when-not (.isDirectory file)
            (transform-kmz file))))
      (transform-kmz folder))))


(comment
  (-main "S.kmz"))