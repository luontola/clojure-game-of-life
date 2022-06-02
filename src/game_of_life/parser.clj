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
          \b (recur output
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

(defn- row-of-cells->pattern [row-of-cells min-x]
  (let [xs (->> row-of-cells
                (map :x)
                (sort))
        max-x (last xs)
        x-alive? (set xs)]
    (when-not (empty? xs)
      (apply str (for [x (range min-x (inc max-x))]
                   (if (x-alive? x)
                     \o
                     \b))))))

(defn cells->pattern [cells]
  (let [xs (->> cells
                (map :x)
                (sort))
        min-x (or (first xs) 0)
        max-x (or (last xs) 0)
        ys (->> cells
                (map :y)
                (sort))
        min-y (or (first ys) 0)
        max-y (or (last ys) 0)
        row->cells (group-by :y cells)]
    {:min-x min-x
     :min-y min-y
     :width (if (empty? cells)
              0
              (- (inc max-x) min-x))
     :height (if (empty? cells)
               0
               (- (inc max-y) min-y))
     :pattern (str
               (str/join "$" (for [y (range min-y (inc max-y))]
                               (row-of-cells->pattern (row->cells y)
                                                      min-x)))
               "!")}))

(defn rle-file->world [data]
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
        (assoc :cells (-> (:encoded-pattern pattern)
                          (rle-decode)
                          (pattern->cells))))))

(defn world->rle-file [world]
  (let [{:keys [min-x min-y width height pattern]} (cells->pattern (:cells world))
        header-line (str "x = " width ", y = " height ", rule = " life-rule)]
    ;; TODO: add "#R" line for the top-left corner
    ;; TODO: remove any existing "#R" line 
    (str/join "\n" (concat (:hash-lines world)
                           [header-line
                            (rle-encode pattern)]))))
