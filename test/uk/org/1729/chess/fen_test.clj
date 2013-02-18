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
                    (fact (:active-player fen) => "w")
                    (fact (:castling-availability fen) => "KQkq")
                    (fact (:en-passant fen) => "-")
                    (fact (:halfmove-clock fen) => 0)
                    (fact (:fullmove-number fen) => 1))
