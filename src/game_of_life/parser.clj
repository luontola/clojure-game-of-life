(ns game-of-life.parser
  (:require [clojure.string :as str])
  (:import (java.util.regex Pattern)))

;;;; Run Length Encoding

(defn run-length-decode [input]
  (str/replace input #"(\d+)(\D)"
               (fn [[_ run-count tag]]
                 (apply str (repeat (parse-long run-count)
                                    tag)))))

(defn run-length-encode [input]
  (str/replace input #"(\D)\1+"
               (fn [[repeated-tag tag]]
                 (str (count repeated-tag) tag))))

(defn rle-line-wrap [input line-length]
  ;; the line must not end in a number, because we must avoid wrapping in the middle of a <run_count><tag> item
  (let [line-pattern (Pattern/compile (format ".{0,%d}\\D" (dec line-length)))]
    (re-seq line-pattern input)))


;;;; Pattern <-> Cells

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


;;;; RLE file <-> World

(def ^:private life-rule "B3/S23")

(defn- parse-header [header-line]
  (when-not header-line
    (throw (IllegalArgumentException. "header line is missing")))
  (let [rule (or (second (re-find #"rule = (.+)" header-line))
                 life-rule)]
    (when-not (= life-rule rule)
      (throw (IllegalArgumentException. (str "unsupported rule: " rule))))))

(defn- parse-file [data]
  (->> (str/split-lines data)
       (map str/trim)
       (reduce (fn [result line]
                 (case (first line)
                   \# (update result :hash-lines conj line)
                   \x (assoc result :header-line line)
                   (update result :encoded-pattern str line)))
               {:hash-lines []
                :header-line nil
                :encoded-pattern ""})))

(defn rle-file->world [data]
  (let [{:keys [hash-lines header-line encoded-pattern]} (parse-file data)
        _ (parse-header header-line) ; only used for validation
        cells (-> encoded-pattern
                  (run-length-decode)
                  (pattern->cells))]
    {:hash-lines hash-lines
     :cells cells}))

(defn world->rle-file [world]
  (let [{:keys [min-x min-y width height pattern]} (cells->pattern (:cells world))
        header-line (str "x = " width ", y = " height ", rule = " life-rule)
        pattern-lines (-> (run-length-encode pattern)
                          (rle-line-wrap 70))]
    ;; TODO: add "#R" line for the top-left corner
    ;; TODO: remove any existing "#R" line 
    (str/join "\n" (concat (:hash-lines world)
                           [header-line]
                           pattern-lines))))
