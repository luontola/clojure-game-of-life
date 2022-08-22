(ns game-of-life.cli
  (:gen-class)
  (:require [game-of-life.game :as game]
            [game-of-life.parser :as parser]))

(defn -main [& [input-file iterations]]
  (println (binding [*out* *err*]
             (-> input-file
                 (slurp)
                 (parser/rle-file->world)
                 (game/simulate (parse-long iterations))
                 (parser/world->rle-file)))))
