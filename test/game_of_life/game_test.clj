(ns game-of-life.game-test
  (:require [clojure.test :refer :all]
            [game-of-life.game :as game]))

(def test-cell {:x 3, :y 7})
(def test-neighbours
  #{{:x 2, :y 6}
    {:x 2, :y 7}
    {:x 2, :y 8}
    {:x 3, :y 6}
    {:x 3, :y 8}
    {:x 4, :y 6}
    {:x 4, :y 7}
    {:x 4, :y 8}})

(deftest neighbours-test
  (is (= test-neighbours (game/neighbours test-cell))))


(defn random-neighbours [n]
  (set (take n (shuffle test-neighbours))))

(deftest game-rules-test
  (testing "live cell with <2 neighbours dies (underpopulation)"
    (doseq [n [0 1]]
      (let [cells (conj (random-neighbours n) test-cell)]
        (is (not (contains? (game/step cells) test-cell))
            (str n " neighbours")))))

  (testing "live cell with 2-3 neighbours stays alive (healthy population)"
    (doseq [n [2 3]]
      (let [cells (conj (random-neighbours n) test-cell)]
        (is (contains? (game/step cells) test-cell)
            (str n " neighbours")))))

  (testing "live cell with >3 neighbours dies (overpopulation)"
    (doseq [n [4 5 6 7 8]]
      (let [cells (conj (random-neighbours n) test-cell)]
        (is (not (contains? (game/step cells) test-cell))
            (str n " neighbours")))))

  (testing "dead cell with exactly 3 live neighbours becomes alive (reproduction)"
    (doseq [n [3]]
      (let [cells (random-neighbours n)]
        (is (contains? (game/step cells) test-cell)
            (str n " neighbours")))))

  (testing "dead cell with less or more than 3 live neighbours stays dead"
    (doseq [n [0 1 2
               4 5 6 7 8]]
      (let [cells (random-neighbours n)]
        (is (not (contains? (game/step cells) test-cell))
            (str n " neighbours")))))

  (testing "no nil cells"
    (is (= #{} (game/step #{}))
        "empty world")
    (is (= #{} (game/step #{test-cell}))
        "cell dies alone")))
