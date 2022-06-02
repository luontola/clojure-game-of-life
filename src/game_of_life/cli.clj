(ns game-of-life.cli
  (:gen-class))

(defn -main [& [input-file iterations]]
  (println (slurp input-file)))
