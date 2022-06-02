(ns game-of-life.game)

(defn neighbours [{:keys [x y]}]
  (set (for [dx [-1 0 1]
             dy [-1 0 1]
             :when (not= 0 dx dy)]
         {:x (+ x dx)
          :y (+ y dy)})))

(defn step [live-cells]
  (let [alive? #(contains? live-cells %)]
    (set (for [[cell neighbour-count] (->> live-cells
                                           (mapcat neighbours)
                                           (frequencies))
               :when (if (alive? cell)
                       (<= 2 neighbour-count 3)
                       (= 3 neighbour-count))]
           cell))))

(defn simulate [world iterations]
  ;; TODO
  world)
