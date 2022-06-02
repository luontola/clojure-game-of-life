(ns game-of-life.cli
  (:gen-class)
  (:require [game-of-life.game :as game]
            [game-of-life.parser :as parser]))

(defn -main [& [input-file iterations]]
  (-> input-file
      (slurp)
      (parser/rle-file->pattern)
      (game/simulate (parse-long iterations))
      (parser/pattern->rle-file)
      (println)))
