(ns game-of-life.parser
  (:require [clojure.string :as str]))

(def life-rule "B3/S23")

(defn parse-header [header-line]
  (when-not header-line
    (throw (IllegalArgumentException. "header line is missing")))
  (let [rule (or (second (re-find #"rule = (.+)" header-line))
                 life-rule)]
    (when-not (= life-rule rule)
      (throw (IllegalArgumentException. (str "unsupported rule: " rule))))))

(defn rle-file->pattern [data]
  (let [pattern (->> (str/split-lines data)
                     (map str/trim)
                     (reduce (fn [result line]
                               (case (first line)
                                 \# (update result :hash-lines conj line)
                                 \x (assoc result :header-line line)
                                 (update result :encoded-pattern str line)))
                             {:hash-lines []
                              :header-line nil
                              :encoded-pattern ""}))]
    (parse-header (:header-line pattern))
    pattern))

(defn pattern->rle-file [pattern]
  ;; TODO: unit tests
  (str/join "\n" (concat (:hash-lines pattern)
                         [(:header-line pattern)
                          (:encoded-pattern pattern)])))
