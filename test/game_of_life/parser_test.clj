(ns game-of-life.parser-test
  (:require [clojure.test :refer :all]
            [game-of-life.parser :as parser]))

(deftest rle-file->pattern-test
  (testing "minimal RLE file"
    (is (= {:hash-lines []
            :header-line "x = 0, y = 0" ; TODO: the header content is not actually used, so remove it; only validate the rule
            :encoded-pattern ""}
           (parser/rle-file->pattern "x = 0, y = 0"))))

  (testing "error: empty file"
    (is (thrown-with-msg?
         IllegalArgumentException #"header line is missing"
         (parser/rle-file->pattern ""))))

  (testing "supports basic Life rules"
    (is (= {:hash-lines []
            :header-line "x = 0, y = 0, rule = B3/S23"
            :encoded-pattern ""}
           (parser/rle-file->pattern "x = 0, y = 0, rule = B3/S23"))))

  (testing "doesn't support HighLife rules"
    (is (thrown-with-msg?
         IllegalArgumentException #"unsupported rule: B36/S23"
         (parser/rle-file->pattern "x = 0, y = 0, rule = B36/S23"))))

  (testing "supports comments and other # lines"
    (is (= {:hash-lines ["#N Name of the pattern"
                         "#C This is a comment"]
            :header-line "x = 0, y = 0"
            :encoded-pattern ""}
           (parser/rle-file->pattern
            "#N Name of the pattern
             #C This is a comment
             x = 0, y = 0")))))
