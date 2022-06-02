(ns game-of-life.parser-test
  (:require [clojure.test :refer :all]
            [game-of-life.parser :as parser]))

(def round-trip-decode
  (comp parser/pattern->rle-file
        parser/rle-file->pattern))

(deftest rle-parsing-test

  (testing "minimal RLE file"
    (is (= "x = 0, y = 0\n"
           (round-trip-decode "x = 0, y = 0"))))

  (testing "error: empty file"
    (is (thrown-with-msg?
         IllegalArgumentException #"header line is missing"
         (round-trip-decode ""))))

  (testing "supports basic Life rules"
    (is (= "x = 0, y = 0, rule = B3/S23\n"
           (round-trip-decode "x = 0, y = 0, rule = B3/S23"))))

  (testing "doesn't support HighLife rules"
    (is (thrown-with-msg?
         IllegalArgumentException #"unsupported rule: B36/S23"
         (round-trip-decode "x = 0, y = 0, rule = B36/S23"))))

  (testing "supports comments and other # lines"
    (is (= "#N Name of the pattern\n#C This is a comment\nx = 0, y = 0\n"
           (round-trip-decode
            "#N Name of the pattern
             #C This is a comment
             x = 0, y = 0"))))

  (testing "multiple lines of pattern are joined into one"
    (is (= "#N Gosper glider gun\nx = 36, y = 9, rule = B3/S23\n24bo$22bobo$12b2o6b2o12b2o$11bo3bo4b2o12b2o$2o8bo5bo3b2o$2o8bo3bob2o4bobo$10bo5bo7bo$11bo3bo$12b2o!"
           (round-trip-decode
            "#N Gosper glider gun
             x = 36, y = 9, rule = B3/S23
             24bo$22bobo$12b2o6b2o12b2o$11bo3bo4b2o12b2o$2o8bo5bo3b2o$2o8bo3bob2o4b
             obo$10bo5bo7bo$11bo3bo$12b2o!"))))

  (testing "ignores empty lines"
    (is (= "x = 3, y = 1, rule = B3/S23\n3o!"
           (round-trip-decode
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

(deftest pattern->cells-test
  (testing "empty pattern"
    (is (= #{} (parser/pattern->cells "")))
    (is (= #{} (parser/pattern->cells "!")))
    (is (= #{} (parser/pattern->cells "d$d!"))
        "only dead cells"))

  (testing "one live cell"
    (is (= #{{:x 0, :y 0}}
           (parser/pattern->cells "o"))))

  (testing "many cells in the same row"
    (is (= #{{:x 0, :y 0}
             {:x 1, :y 0}
             {:x 2, :y 0}}
           (parser/pattern->cells "ooo"))))

  (testing "many cells in the same column"
    (is (= #{{:x 0, :y 0}
             {:x 0, :y 1}
             {:x 0, :y 2}}
           (parser/pattern->cells "o$o$o"))))

  (testing "mix of dead and live cells (diagonal shape)"
    (is (= #{{:x 0, :y 0}
             {:x 1, :y 1}
             {:x 2, :y 2}}
           (parser/pattern->cells "o$do$ddo"))))

  (testing "the pattern ends with !"
    (is (= #{{:x 0, :y 0}
             {:x 1, :y 1}}
           (parser/pattern->cells "o$do$!ddo")))))
