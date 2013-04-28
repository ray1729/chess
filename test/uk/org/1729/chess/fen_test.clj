(ns uk.org.1729.chess.fen-test
  (:require [uk.org.1729.chess.fen :as fen]
            [midje.sweet :refer :all]))

(def starting-position "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")

(def move-1-e4 "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")

(def move-1-e4-c5 "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2")

(def move-1-e4-c5-2-Nf3 "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2")

(tabular
 (fact "We can round-trip FEN strings"
       (fen/unparse (fen/parse ?position)) => ?position)
 ?position
 starting-position
 move-1-e4
 move-1-e4-c5
 move-1-e4-c5-2-Nf3)

(against-background [(around :facts (let [fen (fen/parse starting-position)] ?form))]
                    (fact (first (:piece-placement fen)) => [:r :n :b :q :k :b :n :r])
                    (fact (:active-player fen) => :w)
                    (fact (:castling-availability fen) => "KQkq")
                    (fact (:en-passant fen) => "-")
                    (fact (:halfmove-clock fen) => 0)
                    (fact (:fullmove-number fen) => 1))

(fact "1. e4 gives expected FEN"
      (fen/unparse (fen/make-move (fen/parse starting-position) "e2" "e4")) => move-1-e4)

(fact "1. ...c5 gives expected FEN"
      (fen/unparse (-> (fen/parse starting-position)
                       (fen/make-move "e2" "e4")
                       (fen/make-move "c7" "c5")))
      => move-1-e4-c5)

(fact "2. Nf3 gives expected FEN"
      (fen/unparse (-> (fen/parse starting-position)
                       (fen/make-move "e2" "e4")
                       (fen/make-move "c7" "c5")
                       (fen/make-move "g1" "f3")))
      => move-1-e4-c5-2-Nf3)

(tabular
 (fact "We find the expected piece on each square in the starting position"
       (fen/piece-at (fen/parse starting-position) ?square) => ?expected)
 ?square ?expected
 "a1"    :R
 "b1"    :N
 "c1"    :B
 "d1"    :Q
 "e1"    :K
 "f1"    :B
 "g1"    :N
 "h1"    :R
 "a2"    :P
 "b2"    :P
 "c2"    :P
 "d2"    :P
 "e2"    :P
 "f2"    :P
 "g2"    :P
 "h2"    :P
 "a8"    :r
 "b8"    :n
 "c8"    :b
 "d8"    :q
 "e8"    :k
 "f8"    :b
 "g8"    :n
 "h8"    :r
 "a7"    :p
 "b7"    :p
 "c7"    :p
 "d7"    :p
 "e7"    :p
 "f7"    :p
 "g7"    :p
 "h7"    :p)
