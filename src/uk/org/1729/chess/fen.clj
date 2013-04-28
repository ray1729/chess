(ns uk.org.1729.chess.fen
  (:require [clojure.string :as str]
            [uk.org.1729.chess.util :refer [abs cartesian->algebraic algebraic->cartesian ensure-cartesian type-of opponent-of add-delta]]))

(def initial-position "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")

(defn- digit
  "Given a character `c`, return the integer value of `c` when `c` is
  a digit between 1-8, otherwise nil."
  [c]
  ({\1 1 \2 2 \3 3 \4 4 \5 5 \6 6 \7 7 \8 8} c))

(defn- parse-rank
  [rank]
  {:post [(= 8 (count %))]}
  (reduce (fn [accum c]
            (if-let [n (digit c)]
              (into accum (repeat n nil))
              (conj accum (keyword (str c)))))
          [] (seq rank)))

(defn- parse-piece-placement
  [piece-placement]
  {:post [(= 8 (count %))]}
  (mapv parse-rank (str/split piece-placement #"/")))

(def ^:private fen-rx #"^\s*([rnbkqpRNBKQP12345678/]+)\s+([bw])\s+(-|[KQkq]+)\s+(-|[abcdefgh][36])\s+(\d+)\s+(\d+)\s*$")

(defn parse
  "Parse a FEN record, returning a map of the components with the piece
  placement represented as a vector of vectors."
  [fen-str]
  (when-let [[_ piece-placement active-player castling-availability en-passant halfmove-clock fullmove-number]
             (re-matches fen-rx fen-str)]
    {:piece-placement       (parse-piece-placement piece-placement)
     :active-player         (keyword  active-player)
     :castling-availability castling-availability
     :en-passant            en-passant
     :halfmove-clock        (Integer/parseInt halfmove-clock)
     :fullmove-number       (Integer/parseInt fullmove-number)}))

(defn- unparse-rank
  [rank]
  (str/join (map (fn [part] (if (nil? (first part)) (count part) (str/join (map name part))))
                 (partition-by nil? rank))))

(defn- unparse-piece-placement
  [piece-placement]
  (str/join "/" (map unparse-rank piece-placement)))

(defn unparse
  [fen]
  (format "%s %s %s %s %d %d"
          (unparse-piece-placement (:piece-placement fen))
          (name  (:active-player fen))
          (:castling-availability fen)
          (:en-passant fen)
          (:halfmove-clock fen)
          (:fullmove-number fen)))

(defn piece-at
  [fen pos]
  (let [[x y] (ensure-cartesian pos)]
    (get-in (:piece-placement fen) [y x])))

(defn- make-simple-move
  [fen from to]
  (let [[from-x from-y] (ensure-cartesian from)
        [to-x to-y] (ensure-cartesian to)
        piece (piece-at fen [from-x from-y])]
    (-> fen
        (assoc-in [:piece-placement to-y to-x] piece)
        (assoc-in [:piece-placement from-y from-x] nil))))

(defn- update-castling-availability
  [player]
  (let [replace-rx (if (= player :w) #"[KQ]" #"[kq]")]
    (fn [availability]
      (let [availability (str/replace availability replace-rx "")]
        (if (= availability "") "-" availability)))))

(defn- make-castling-move
  [fen king-from king-to]
  (let [[rook-from rook-to] (case (cartesian->algebraic king-to)
                              "c1" ["a1" "d1"]
                              "g1" ["h1" "f1"]
                              "c8" ["a8" "d8"]
                              "g8" ["h8" "f8"])]
    (-> fen
        (make-simple-move king-from king-to)
        (make-simple-move rook-from rook-to)
        (update-in [:castling-availability] (update-castling-availability (:active-player fen))))))

(defn- delta-x-y
  [from to]
  (let [[from-x from-y] (ensure-cartesian from)
        [to-x to-y]     (ensure-cartesian to)
        delta-x         (abs (- to-x from-x))
        delta-y         (abs (- to-y from-y))]
    [delta-x delta-y]))

(defn- en-passant-square
  [player pos]
  (let [delta-y (if (= :w player) 1 -1)]
    (cartesian->algebraic (add-delta pos [0 delta-y]))))

(defn- update-castling-availability
  [is-castling? piece active-player [to-x to-y]]
  (if (or is-castling? (not (#{:king :rook} (type-of piece))))
    identity
    (fn [availability]
      (let [match        (if (= to-x 6) "k" "q") ; kingside or queenside?
            availability (str/replace availability
                                      (if (= active-player :w) (str/upper-case match) match)
                                      "")]
        (if (= availability "") "-" availability)))))

;; TODO: Handle pawn promotion; update castling availability on
;; king/rook move
(defn make-move
  [fen from to]
  (let [piece                 (piece-at fen from)
        capture               (piece-at fen to)
        [dx dy]               (delta-x-y from to)
        is-castling?          (and (= (type-of piece) :king) (= 2 dx))
        move                  (if is-castling? make-castling-move make-simple-move)]
    (-> (move fen from to)
        (assoc-in [:en-passant] (if (and (= (type-of piece) :pawn) (= 0 dx) (= 2 dy))
                                  (en-passant-square (:active-player fen) to)
                                  "-"))
        (update-in [:castling-availability] (update-castling-availability is-castling? piece (:active-player fen) to))
        (update-in [:active-player] opponent-of)
        (update-in [:fullmove-number] #(if (= (:active-player fen) :b) (inc %) %))
        (update-in [:halfmove-clock] #(if (or capture (= (type-of piece) :pawn)) 0 (inc %))))))
