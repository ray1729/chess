(ns uk.org.1729.chess.util-test
  (:require [uk.org.1729.chess.util :as util]
            [midje.sweet :refer :all]))

(fact "on-board? returns true for [x y] where 0 <= x < 8, 0 <= y < 8"
      (every? util/on-board? (for [x (range 8) y (range 8)] [x y])) => truthy)

(fact "on-board? returns false for [x y] where x<0 or y < 0 or x>7 or y>7"
      (util/on-board? [-1,0]) => falsey
      (util/on-board? [0,-1]) => falsey
      (util/on-board? [0,8])  => falsey
      (util/on-board? [8,0])  => falsey)

(fact "colour-of white pieces is :w"
      (every? #(= (util/colour-of %) :w) [:R :N :B :K :Q :P]) => truthy)

(fact "colour-of black pieces is :b"
      (every? #(= (util/colour-of %) :b) [:r :n :b :k :q :p]) => truthy)

(fact "opponent-of :w is :b"
      (util/opponent-of :w) => :b)

(fact "opponent-of :b is :w"
      (util/opponent-of :b) => :w)

(tabular
 (fact "type-of various pieces"
       (util/type-of ?piece) => ?expected)
 ?piece ?expected
 :p     :pawn
 :P     :pawn
 :r     :rook
 :R     :rook
 :n     :knight
 :N     :knight
 :b     :bishop
 :B     :bishop
 :k     :king
 :K     :king
 :q     :queen
 :Q     :queen)

(fact "add-delta returns expected value"
      (util/add-delta [0 0] [1 1])  => [1 1]
      (util/add-delta [0 0] [0 1])  => [0 1]
      (util/add-delta [1 1] [-1 1]) => [0 2])

(fact "algebraic->cartesian and cartesian->algebraic are inverses of each other"
      (for [r (seq "abcdefgh") c (seq "12345678") :let [algebraic (str r c)]]
        (util/cartesian->algebraic (util/algebraic->cartesian algebraic) => algebraic)))
