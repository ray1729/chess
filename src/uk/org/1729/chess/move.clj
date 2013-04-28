(ns uk.org.1729.chess.move
  (:require [clojure.string :as str]
            [uk.org.1729.chess.fen :refer [piece-at]]
            [uk.org.1729.chess.util :refer [on-board? colour-of type-of opponent-of cartesian->algebraic add-delta]]))

(def type-of-piece-at (comp type-of piece-at))
(def colour-of-piece-at (comp colour-of piece-at))
(def opponent-of-piece-at (comp opponent-of colour-of piece-at))

(defn not-occupied?
  [fen pos]
  (nil? (piece-at fen pos)))

(defmulti piece-activity (fn [fen pos] (type-of-piece-at fen pos)))

(defmethod piece-activity :pawn
  [fen pos]
  (let [[first-rank opponent direction] (case (colour-of-piece-at fen pos)
                                          :w [6 :b  -1]
                                          :b [1 :w 1])]
    (remove nil?
            [;; Move forward 1
             (let [candidate-pos (add-delta pos [0 direction])]
               (when (and (on-board? candidate-pos)
                          (not-occupied? fen candidate-pos))
                 candidate-pos))
             ;; Move forward 2
             (let [candidate-pos (add-delta pos [0 (* 2 direction)])]
               (when (and (= (second pos) first-rank)
                          (not-occupied? fen (add-delta pos [0 direction]))
                          (not-occupied? fen candidate-pos)))
               candidate-pos)
             ;; Capture on forward/right diagonal
             (let [candidate-pos (add-delta pos [1 direction])]
               (when (and (on-board? candidate-pos)
                          (= (colour-of-piece-at fen candidate-pos) opponent))
                 candidate-pos))
             ;; Capture on forward/left diagonal
             (let [candidate-pos (add-delta pos [-1 direction])]
               (when (and (on-board? candidate-pos)
                          (= (colour-of-piece-at fen candidate-pos) opponent))
                 candidate-pos))
             ;; Capture en passant forward/right
             (let [candidate-pos (add-delta pos [1 (* 2 direction)])]
               (when (and (on-board? candidate-pos)
                          (= (:en-passant fen) (cartesian->algebraic candidate-pos)))
                 candidate-pos))
             ;; Capture en passant forward/left
             (let [candidate-pos (add-delta pos [-1 (* 2 direction)])]
               (when (and (on-board? candidate-pos)
                          (= (:en-passant fen) (cartesian->algebraic candidate-pos)))
                 candidate-pos))])))

(defmethod piece-activity :knight
  [fen pos]
  (let [opponent (opponent-of-piece-at fen pos)]
    (filter #(and (on-board? %)
                  (or (not-occupied? fen %)
                      (= opponent (colour-of-piece-at fen %))))
            (map (partial add-delta pos) [[1 2] [1 -2] [-1 2] [-1 -2] [2 1] [2 -1] [-2 1] [-2 -1]]))))

(defn moves-in-direction
  [fen pos direction]
  (let [opponent (opponent-of-piece-at fen pos)]
    (loop [pos pos accum []]
      (let [next-pos (add-delta pos direction)]
        (cond
         (and (on-board? next-pos) (not-occupied? fen next-pos)) (recur next-pos (conj accum next-pos))
         (and (on-board? next-pos) (= opponent (colour-of-piece-at fen next-pos))) (conj accum next-pos)
         :else accum)))))

(defmethod piece-activity :bishop
  [fen pos]
  (mapcat (partial moves-in-direction fen pos) [[1 1] [1 -1] [-1 1] [-1 -1]]))

(defmethod piece-activity :rook
  [fen pos]
  (mapcat (partial moves-in-direction fen pos) [[0 1] [0 -1] [1 0] [-1 0]]))

(defmethod piece-activity :queen
  [fen pos]
  (mapcat (partial moves-in-direction fen pos) [[0 1] [0 -1] [1 0] [-1 0] [1 1] [1 -1] [-1 1] [-1 -1]]))

(defmethod piece-activity :king
  [fen pos]
  (let [opponent (opponent-of-piece-at fen pos)]
    (filter #(and (on-board? %)
                  (or (not-occupied? fen %)
                      (= opponent (colour-of-piece-at fen %))))
            (map (partial add-delta pos) [[0 1] [0 -1] [1 0] [-1 0] [1 1] [1 -1] [-1 1] [-1 -1]]))))
