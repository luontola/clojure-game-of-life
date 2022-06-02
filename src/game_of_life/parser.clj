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

(defn pattern->cells [input]
  (loop [output #{}
         input input
         x 0
         y 0]
    (if (empty? input)
      output
      (let [tag (first input)]
        (case tag
          ;; dead cell
          \d (recur output
                    (subs input 1)
                    (inc x)
                    y)
          ;; alive cell
          \o (recur (conj output {:x x, :y y})
                    (subs input 1)
                    (inc x)
                    y)
          ;; end of line
          \$ (recur output
                    (subs input 1)
                    0
                    (inc y))
          ;; end of pattern
          \! output)))))

(defn- format-row-of-cells [cells y] ; FIXME: pass in the global min-x/y
  (let [xs (->> cells
                (filter #(= y (:y %)))
                (map :x)
                (sort))
        min-x (first xs)
        max-x (last xs)]
    (when-not (empty? xs)
      (for [x (range min-x (inc max-x))]
        (if (contains? cells {:x x, :y y})
          \o
          \d)))))

(defn cells->pattern [cells]
  (let [xs (->> cells
                (map :x)
                (sort))
        min-x (first xs)
        ys (->> cells
                (map :y)
                (sort))
        min-y (first ys)
        cells-by-row (group-by :y cells)]
    ;; TODO: calculate dimensions
    ;; TODO: print each row separately, until the last cell in the row
    ;  FIXME: some rows/columns can be empty
    {:min-x (or min-x 0)
     :min-y (or min-y 0)
     :pattern (apply str (concat
                          (format-row-of-cells cells 0)
                          #_(for [x ys]
                              (if (contains? cells {:x x, :y 0})
                                \o
                                \d))
                          "!"))}))

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
