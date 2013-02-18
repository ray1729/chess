(ns uk.org.1729.chess.fen
  (:require [clojure.string :as str]))

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
              (into accum (repeat n :free))
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
     :active-player         active-player
     :castling-availability castling-availability
     :en-passant            en-passant
     :halfmove-clock        (Integer/parseInt halfmove-clock)
     :fullmove-number       (Integer/parseInt fullmove-number)}))

(defn- unparse-rank
  [rank]
  (str/join (map (fn [part] (if (= :free (first part)) (count part) (str/join (map name part))))
                 (partition-by #{:free} rank))))

(defn- unparse-piece-placement
  [piece-placement]
  (str/join "/" (map unparse-rank piece-placement)))

(defn unparse
  [fen]
  (format "%s %s %s %s %d %d"
          (unparse-piece-placement (:piece-placement fen))
          (:active-player fen)
          (:castling-availability fen)
          (:en-passant fen)
          (:halfmove-clock fen)
          (:fullmove-number fen)))
