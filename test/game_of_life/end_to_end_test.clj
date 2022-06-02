(ns game-of-life.end-to-end-test
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [game-of-life.cli :as cli]))

(defn normalize-whitespace [s]
  (-> s
      (str/replace "\r\n" "\n")
      (str/trim)
      (str "\n")))

(deftest end-to-end-test
  (is (= (normalize-whitespace (slurp "testdata/block.rle"))
         (with-out-str
           (cli/-main "testdata/block.rle" "1")))))
