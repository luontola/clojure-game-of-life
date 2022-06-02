(ns game-of-life.end-to-end-test
  (:require [clojure.test :refer [deftest is]]
            [game-of-life.cli :as cli]))

(deftest end-to-end-test
  (is (= (str (slurp "testdata/block.rle") "\n")
         (with-out-str
           (cli/-main "testdata/block.rle" "1")))))
