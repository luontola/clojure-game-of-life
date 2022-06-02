(ns game-of-life.end-to-end-test
  (:require [clojure.test :refer :all]
            [game-of-life.cli :as cli]))

(deftest end-to-end-test
  (testing "block - a still life"
    (is (= (slurp "testdata/block.rle")
           (with-out-str
             (cli/-main "testdata/block.rle" "1")))))

  (testing "blinker - an oscillator"
    (is (= (slurp "testdata/blinker-t1.rle")
           (with-out-str
             (cli/-main "testdata/blinker.rle" "3"))))))
