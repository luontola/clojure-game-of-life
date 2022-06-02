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


(defn- dead-cell-with-neighbours [n]
  (set (take n (shuffle test-neighbours))))

(defn- live-cell-with-neighbours [n]
  (-> (dead-cell-with-neighbours n)
      (conj test-cell)))

(defn- will-live? [cells]
  (contains? (game/step cells) test-cell))

(def ^:private will-die? (complement will-live?))

(deftest game-rules-test
  (testing "live cell with <2 neighbours dies (underpopulation)"
    (doseq [n [0 1]]
      (is (will-die? (live-cell-with-neighbours n))
          (str n " neighbours"))))

  (testing "live cell with 2-3 neighbours stays alive (healthy population)"
    (doseq [n [2 3]]
      (is (will-live? (live-cell-with-neighbours n))
          (str n " neighbours"))))

  (testing "live cell with >3 neighbours dies (overpopulation)"
    (doseq [n [4 5 6 7 8]]
      (is (will-die? (live-cell-with-neighbours n))
          (str n " neighbours"))))

  (testing "dead cell with exactly 3 live neighbours becomes alive (reproduction)"
    (doseq [n [3]]
      (is (will-live? (dead-cell-with-neighbours n))
          (str n " neighbours"))))

  (testing "dead cell with less or more than 3 live neighbours stays dead"
    (doseq [n [0 1 2
               4 5 6 7 8]]
      (is (will-die? (dead-cell-with-neighbours n))
          (str n " neighbours"))))

  (testing "no nil cells"
    (is (= #{} (game/step #{}))
        "empty world")
    (is (= #{} (game/step #{test-cell}))
        "cell dies alone")))

(deftest simulate-world-test
  (let [glider-t0 {:hash-lines ["#N Glider"]
                   :cells #{{:x 0, :y 2}
                            {:x 1, :y 0}
                            {:x 1, :y 2}
                            {:x 2, :y 1}
                            {:x 2, :y 2}}}
        glider-t3 {:hash-lines ["#N Glider"]
                   :cells #{{:x 1, :y 1}
                            {:x 1, :y 3}
                            {:x 2, :y 2}
                            {:x 2, :y 3}
                            {:x 3, :y 2}}}]
    (is (= glider-t3 (game/simulate glider-t0 3)))))
