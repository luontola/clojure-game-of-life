(ns game-of-life.parser-test
  (:require [clojure.test :refer :all]
            [game-of-life.parser :as parser]))

(deftest rle-file->pattern-test
  (testing "minimal RLE file"
    (is (= {:hash-lines []
            :header-line "x = 0, y = 0"
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
             x = 0, y = 0"))))

  (testing "multiple lines of pattern are joined into one"
    (is (= {:hash-lines ["#N Gosper glider gun"]
            :header-line "x = 36, y = 9, rule = B3/S23"
            :encoded-pattern "24bo$22bobo$12b2o6b2o12b2o$11bo3bo4b2o12b2o$2o8bo5bo3b2o$2o8bo3bob2o4bobo$10bo5bo7bo$11bo3bo$12b2o!"}
           (parser/rle-file->pattern
            "#N Gosper glider gun
             x = 36, y = 9, rule = B3/S23
             24bo$22bobo$12b2o6b2o12b2o$11bo3bo4b2o12b2o$2o8bo5bo3b2o$2o8bo3bob2o4b
             obo$10bo5bo7bo$11bo3bo$12b2o!"))))

  (testing "ignores empty lines"
    (is (= {:hash-lines []
            :header-line "x = 3, y = 1, rule = B3/S23"
            :encoded-pattern "3o!"}
           (parser/rle-file->pattern
            "
           x = 3, y = 1, rule = B3/S23

           3

           o!

           ")))))

(deftest rle-decode-test
  (testing "empty pattern"
    (is (= "" (parser/rle-decode "")))
    (is (= "!" (parser/rle-decode "!"))))

  (testing "non-repeated tags"
    (is (= "bo$" (parser/rle-decode "bo$")))
    (is (= "bo$" (parser/rle-decode "1b1o1$"))))

  (testing "repeated tags"
    (is (= "bbooo$$$$!" (parser/rle-decode "2b3o4$!")))))

(deftest rle-encode-test
  (testing "empty pattern"
    (is (= "" (parser/rle-encode "")))
    (is (= "!" (parser/rle-encode "!"))))

  (testing "non-repeated tags"
    (is (= "bo$" (parser/rle-encode "bo$"))))

  (testing "repeated tags"
    (is (= "2b3o4$!" (parser/rle-encode "bbooo$$$$!")))))
