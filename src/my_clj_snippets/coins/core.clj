(ns my-clj-snippets.coins.core
  (:require [my-clj-snippets.coins.const :as c]
            [my-clj-snippets.coins.util :as u])
  (:gen-class))

(defn get-coins "Returns optimal coin change from specific amount.
  But! Can return self coin if provided a coin value.
  Example: fn(25) -> [25]. fn(15) -> [10 5]
  "
  ([amount]
   (get-coins amount []))

  ([amount acc]
   (if (zero? amount)
     acc
     (let [highest-coin (u/highest-coin-that-fits amount)]
       (recur (- amount highest-coin) (conj acc highest-coin))))))

(defn get-change
  "Given any $ amount, returns optimal change.
  Always provides change. That is, if given a quarter (25) Q,
  will return [10 10 5]."
  [value]
  (cond
    (u/is-coin? value) (c/mappings value)
    :else (get-coins value)))

;; Universe of all possible purses
(def universe (atom #{}))

(defn rr [] (reset! universe #{}))

(defn coin-correct
  "inefficient coin challenge
   meh... don't have the energy to optimize algorithm or
   convert to tail position or completely immutable
  "
  [purse]
  (when (not (u/all-pennies? purse))
    (doseq [[money idx] (u/vector-with-index purse)]
      (when (not (u/is-penny? money))
        (let [result (sort (u/fit-coll purse idx (get-change money)))]
          (println result)
          (swap! universe conj result)
          (coin-correct result))))))

(defmulti coin-challenge vector?)

(defmethod coin-challenge true
  [purse]
  (coin-correct purse))

(defmethod coin-challenge false
  [value]
  (coin-correct [value]))

(defn main
  "converts any value to all possible coin combinations
   This is slow. Uses set atom: resets atom and populate it, returns output,
   which will print result when running on repl."
  [i]
  (rr)
  (coin-challenge i)
  @universe)

(comment "
   brainstorming...

   Could implement tree, then traverse depth-first, but... I'll cowboy this and:

   maps through vector
   sample input [26] (if 26, surround by []), acc = []
   if all Penny, done, return acc; which is a set.

         [25 25 10 5 1 1 1]
         for each, call fn
         if 1, we're done: return that
         next amount, call conversion:
         generates a vector for example for 25: [10 10 5]
         map call again this vector.

         coins(10) -> [10]
         [10] -> for each, coins
         not all 1? for each, to coins

   else, if first mapped element is higer than a coin, convert to coin equivalent
   the result of first fn is added to acc, then we call the resulting vector recursively
   so [26] -> [25 1], this version is easy
   now the input is [25 1] we map again. for each one that isnt a penny, get the conversion of it so:
   [25 1] -> [10 10 5 1]; add to acc. then recur again with this result.
   ;; ^ this is key, 25 doesnt return a 25 coin, but the next coin.
   ;; ^ we insert into original vector, so that they are merged and not nested vectors.
   [10 10 5 1] -> [5 5 10 5 1] call again, but also rest of mapped elements in vector:
                  [5 5 5 5 5 1]
                  [1 1 1 1 1 5 5 5 5 1]
                  [1 1 1 1 1 1 1 1 1 1 5 5 5 1]
                  [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 5 5 1]
                  [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 5 1]
                  [1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1]
          all intermediary states are called by map and recur for each elem.
          hopefully acc covers everything when we're done. else, with no acc, just print each yielding result..

map-indexed
")
