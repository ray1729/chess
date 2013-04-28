(ns uk.org.1729.chess.util
  (:require [clojure.string :as str]))

(defn on-board?
  "Returns true if the position `[x y]` is on a standard 8x8 chess board, otherwise false."
  [[x y]]
  (and x y (<= 0 x 7) (<= 0 y 7)))

(defn colour-of
  "Returns the colour of `piece` (:w or :b)."
  [piece]
  (when piece 
    (let [piece (name piece)]
      (if (= piece (str/upper-case piece)) :w :b))))

(defn opponent-of
  [player]
  (case player :w :b :b :w))

(defn type-of
 "Returns the type of the piece `p`."
 [p]
 (case p
   :p :pawn
   :P :pawn
   :r :rook
   :R :rook
   :n :knight
   :N :knight
   :b :bishop
   :B :bishop
   :k :king
   :K :king
   :q :queen
   :Q :queen
   nil))

(def columns (vec (seq "abcdefgh")))
(def ranks   (vec (seq "87654321")))

(defn algebraic->cartesian
  "Translate an algebraic position such as `c3` to a Cartesian coordinate vector."
  [algebraic]
  {:post [(on-board? %)]}
  (let [[col rank] (seq algebraic)
        col  (.indexOf columns col)
        rank (.indexOf ranks rank)]
    [col rank]))

(defn cartesian->algebraic
  "Translate a Cartesian coordinate vector to algebraic notation."
  [[x y]]
  {:pre [(on-board? [x y])]}
  (let [col (get columns x)
        rank (get ranks y)]
    (str col rank)))

(defn ensure-cartesian
  [pos]
  (if (sequential? pos) pos (algebraic->cartesian pos)))

(defn abs
  [x]
  (if (>= x 0) x (- x)))

(defn add-delta
  [pos delta]
  (map + (ensure-cartesian pos) delta))
