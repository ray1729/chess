(ns uk.org.1729.chess.move-test
  (:require [uk.org.1729.chess.fen :as fen]
            [uk.org.1729.chess.move :as move]
            [uk.org.1729.chess.util :as util]
            [midje.sweet :refer :all]))

(def p (fen/parse fen/initial-position))

(defn piece-activity
  [game-position square]
  (set (map util/cartesian->algebraic (move/piece-activity game-position square))))

(fact "The e2 pawn can move to e3 or e4 from the initial position"
      (piece-activity p "e2") => #{"e3" "e4"})

(fact "The b1 knight can move to a3 or c3 from the initial position"
      (piece-activity p "b1") => #{"a3" "c3"})
