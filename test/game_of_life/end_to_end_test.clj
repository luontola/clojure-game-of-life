(ns game-of-life.end-to-end-test
  (:require [clojure.string :as str]
            [clojure.test :refer :all]
            [game-of-life.cli :as cli]))

(defn normalize-whitespace [s]
  (-> s
      (str/replace "\r\n" "\n")
      (str/trim)
      (str "\n")))

(deftest end-to-end-test
  (testing "block - a still life"
    (is (= (normalize-whitespace (slurp "testdata/block.rle"))
           (with-out-str
             (cli/-main "testdata/block.rle" "1")))))

  (testing "blinker - an oscillator"
    (is (= (normalize-whitespace (slurp "testdata/blinker-t1.rle"))
           (with-out-str
             (cli/-main "testdata/blinker.rle" "3"))))))
