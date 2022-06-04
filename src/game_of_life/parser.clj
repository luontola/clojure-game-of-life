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

(def dead-cell \b)
(def live-cell \o)
(def end-of-row \$)
(def end-of-pattern \!)

(defn pattern->cells [input]
  (loop [output #{}
         input (seq input)
         x 0
         y 0]
    (if-some [tag (first input)]
      (condp = tag
        dead-cell (recur output
                         (rest input)
                         (inc x)
                         y)
        live-cell (recur (conj output {:x x, :y y})
                         (rest input)
                         (inc x)
                         y)
        end-of-row (recur output
                          (rest input)
                          0
                          (inc y))
        end-of-pattern output)
      output)))

(defn- row-of-cells->pattern [row-of-cells min-x]
  (let [xs (->> row-of-cells
                (map :x)
                (sort))
        max-x (last xs)
        x-alive? (set xs)]
    (when-not (empty? xs)
      (apply str (for [x (range min-x (inc max-x))]
                   (if (x-alive? x)
                     live-cell
                     dead-cell))))))

(defn- min-max [coll]
  (let [ascending (sort coll)]
    [(first ascending) (last ascending)]))

(defn cells->pattern [cells]
  (let [[min-x max-x] (min-max (map :x cells))
        [min-y max-y] (min-max (map :y cells))
        row->cells (group-by :y cells)]
    {:min-x (or min-x 0)
     :min-y (or min-y 0)
     :width (if (empty? cells)
              0
              (- (inc max-x) min-x))
     :height (if (empty? cells)
               0
               (- (inc max-y) min-y))
     :pattern (str
               (when-not (empty? cells)
                 (str/join end-of-row
                           (for [y (range min-y (inc max-y))]
                             (row-of-cells->pattern (row->cells y)
                                                    min-x))))
               end-of-pattern)}))


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
