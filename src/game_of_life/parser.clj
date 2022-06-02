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

(defn rle-decode [input]
  (loop [output (StringBuilder.)
         input input]
    (if (empty? input)
      (.toString output)
      (let [[matched run-count tag] (re-find #"(\d+)?(.)" input)
            run-count (if run-count
                        (parse-long run-count)
                        1)
            tag-decoded (apply str (repeat run-count tag))]
        (recur (.append output tag-decoded)
               (subs input (count matched)))))))

(defn- repeated-prefix [s]
  (let [ch (first s)]
    [(count (take-while #(= ch %) s))
     ch]))

(defn rle-encode [input]
  (loop [output (StringBuilder.)
         input input]
    (if (empty? input)
      (.toString output)
      (let [[run-count tag] (repeated-prefix input)
            tag-encoded (if (= 1 run-count)
                          tag
                          (str run-count tag))]
        (recur (.append output tag-encoded)
               (subs input run-count))))))

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
    ;; TODO: the header content is not actually used, so remove it after rule validation
    (-> pattern
        (dissoc :encoded-pattern)
        (assoc :pattern (rle-decode (:encoded-pattern pattern))))))

(defn pattern->rle-file [pattern]
  (str/join "\n" (concat (:hash-lines pattern)
                         ;; TODO: generate header line based on pattern width/height
                         [(:header-line pattern)
                          (rle-encode (:pattern pattern))])))
