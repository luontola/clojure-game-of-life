(ns game-of-life.parser
  (:require [clojure.string :as str]))

(defn rle-file->pattern [data]
  (reduce (fn [result line]
            (case (first line)
              \# (update result :hash-lines conj line)
              \x (assoc result :header-line line)
              (update result :encoded-pattern str line)))
          {:hash-lines []
           :header-line nil
           :encoded-pattern ""}
          (str/split-lines data)))

(defn pattern->rle-file [pattern]
  (str/join "\n" (concat (:hash-lines pattern)
                         [(:header-line pattern)
                          (:encoded-pattern pattern)])))
