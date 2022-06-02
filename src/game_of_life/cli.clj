(ns game-of-life.cli
  (:gen-class)
  (:require [clojure.string :as str]))

(defn parse-rle-pattern [data]
  (reduce (fn [result line]
            (case (first line)
              \# (update result :hash-lines conj line)
              \x (assoc result :header-line line)
              (update result :encoded-pattern str line)))
          {:hash-lines []
           :header-line nil
           :encoded-pattern ""}
          (str/split-lines data)))

(defn -main [& [input-file iterations]]
  (let [data (slurp input-file)
        parsed (parse-rle-pattern data)]
    (println (str/join "\n" (:hash-lines parsed)))
    (println (:header-line parsed))
    (println (:encoded-pattern parsed))))
