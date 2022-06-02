(ns game-of-life.parser-test
  (:require [clojure.test :refer :all]
            [game-of-life.parser :as parser]))

(def rle-round-trip
  (comp parser/world->rle-file
        parser/rle-file->world))

(deftest rle-parsing-test
  (testing "minimal RLE file"
    (is (= "x = 0, y = 0, rule = B3/S23\n!"
           (rle-round-trip "x = 0, y = 0"))))

  (testing "error: empty file"
    (is (thrown-with-msg?
         IllegalArgumentException #"header line is missing"
         (rle-round-trip ""))))

  (testing "supports basic Life rules"
    (is (= "x = 0, y = 0, rule = B3/S23\n!"
           (rle-round-trip "x = 0, y = 0, rule = B3/S23"))))

  (testing "doesn't support HighLife rules"
    (is (thrown-with-msg?
         IllegalArgumentException #"unsupported rule: B36/S23"
         (rle-round-trip "x = 0, y = 0, rule = B36/S23"))))

  (testing "supports comments and other # lines"
    (is (= "#N Name of the pattern\n#C This is a comment\nx = 0, y = 0, rule = B3/S23\n!"
           (rle-round-trip
            "#N Name of the pattern
             #C This is a comment
             x = 0, y = 0"))))

  (testing "multiple lines of pattern are joined into one"
    (is (= "#N Gosper glider gun\nx = 36, y = 9, rule = B3/S23\n24bo$22bobo$12b2o6b2o12b2o$11bo3bo4b2o12b2o$2o8bo5bo3b2o$2o8bo3bob2o4bobo$10bo5bo7bo$11bo3bo$12b2o!"
           (rle-round-trip
            "#N Gosper glider gun
             x = 36, y = 9, rule = B3/S23
             24bo$22bobo$12b2o6b2o12b2o$11bo3bo4b2o12b2o$2o8bo5bo3b2o$2o8bo3bob2o4b
             obo$10bo5bo7bo$11bo3bo$12b2o!"))))

  (testing "ignores empty lines"
    (is (= "x = 3, y = 1, rule = B3/S23\n3o!"
           (rle-round-trip
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
    (is (= #{} (parser/pattern->cells "b$b!"))
        "only dead cells"))

  (testing "one live cell"
    (is (= #{{:x 0, :y 0}}
           (parser/pattern->cells "o!"))))

  (testing "many cells in the same row"
    (is (= #{{:x 0, :y 0}
             {:x 1, :y 0}
             {:x 2, :y 0}}
           (parser/pattern->cells "ooo!"))))

  (testing "many cells in the same column"
    (is (= #{{:x 0, :y 0}
             {:x 0, :y 1}
             {:x 0, :y 2}}
           (parser/pattern->cells "o$o$o!"))))

  (testing "mix of dead and live cells (diagonal shape)"
    (is (= #{{:x 0, :y 0}
             {:x 1, :y 1}
             {:x 2, :y 2}}
           (parser/pattern->cells "o$bo$bbo!"))))

  (testing "the pattern ends with !, anything after it will be discarded"
    (is (= #{{:x 0, :y 0}
             {:x 1, :y 1}}
           (parser/pattern->cells "o$bo$!bbo")))))

(deftest cells->pattern-test
  (testing "no cells"
    (is (= {:min-x 0
            :min-y 0
            :width 0
            :height 0
            :pattern "!"}
           (parser/cells->pattern #{}))))

  (testing "one cell"
    (is (= {:min-x 5
            :min-y 7
            :width 1
            :height 1
            :pattern "o!"}
           (parser/cells->pattern #{{:x 5, :y 7}}))))

  (testing "live cells in the same row"
    (is (= {:min-x 0
            :min-y 0
            :width 3
            :height 1
            :pattern "ooo!"}
           (parser/cells->pattern #{{:x 0, :y 0}
                                    {:x 1, :y 0}
                                    {:x 2, :y 0}}))))

  (testing "live cells in the same column"
    (is (= {:min-x 0
            :min-y 0
            :width 1
            :height 3
            :pattern "o$o$o!"}
           (parser/cells->pattern #{{:x 0, :y 0}
                                    {:x 0, :y 1}
                                    {:x 0, :y 2}}))))

  (testing "dead and alive cells in the same row"
    (is (= {:min-x 0
            :min-y 0
            :width 3
            :height 1
            :pattern "obo!"}
           (parser/cells->pattern #{{:x 0, :y 0}
                                    {:x 2, :y 0}})))
    (is (= {:min-x 1
            :min-y 0
            :width 3
            :height 1
            :pattern "obo!"}
           (parser/cells->pattern #{{:x 1, :y 0}
                                    {:x 3, :y 0}}))
        "row starts with dead cells")
    (is (= {:min-x -3
            :min-y 0
            :width 3
            :height 1
            :pattern "obo!"}
           (parser/cells->pattern #{{:x -1, :y 0}
                                    {:x -3, :y 0}}))
        "negative indexes"))

  (testing "dead and alive cells in the same column"
    (is (= {:min-x 0
            :min-y 0
            :width 1
            :height 3
            :pattern "o$$o!"}
           (parser/cells->pattern #{{:x 0, :y 0}
                                    {:x 0, :y 2}})))
    (is (= {:min-x 0
            :min-y 1
            :width 1
            :height 3
            :pattern "o$$o!"}
           (parser/cells->pattern #{{:x 0, :y 1}
                                    {:x 0, :y 3}}))
        "column starts with dead cells")
    (is (= {:min-x 0
            :min-y -3
            :width 1
            :height 3
            :pattern "o$$o!"}
           (parser/cells->pattern #{{:x 0, :y -1}
                                    {:x 0, :y -3}}))
        "negative indexes"))

  (testing "rows which start with dead cells (diagonal shape)"
    (is (= {:min-x 0
            :min-y 0
            :width 3
            :height 3
            :pattern "o$bo$bbo!"}
           (parser/cells->pattern #{{:x 0, :y 0}
                                    {:x 1, :y 1}
                                    {:x 2, :y 2}})))))
