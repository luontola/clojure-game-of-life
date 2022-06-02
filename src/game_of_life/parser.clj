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

(defn rle-decode [encoded]
  (loop [decoded (StringBuilder.)
         encoded encoded]
    (if (empty? encoded)
      (.toString decoded)
      (let [[matched run-count tag] (re-find #"(\d+)?(.)" encoded)
            run-count (if run-count
                        (parse-long run-count)
                        1)
            tag-decoded (apply str (repeat run-count tag))]
        (recur (.append decoded tag-decoded)
               (subs encoded (count matched)))))))

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
    ;; TODO: decode pattern
    pattern))

(defn pattern->rle-file [pattern]
  ;; TODO: unit tests
  (str/join "\n" (concat (:hash-lines pattern)
                         ;; TODO: generate header line based on pattern width/height
                         [(:header-line pattern)
                          ;; TODO: encode pattern
                          (:encoded-pattern pattern)])))
