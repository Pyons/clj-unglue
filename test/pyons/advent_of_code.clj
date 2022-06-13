(ns pyons.advent-of-code
  (:require [clojure.test :refer [deftest is]]
            [clojure.string :as str]
            [pyons.unglue :refer [unglue]]))

(def puzzle-day2-2020
  "forward 5
down 5
forward 8
up 3
down 8
forward 2")

(defn- read-day2-2020 [input]
  (let [lines (str/split-lines input)
        pattern "{direction=keyword} {units=parse-long}"]
    (into [] (unglue pattern) lines)))

(deftest day2
  (is (= [{:direction :forward, :units 5}
          {:direction :down, :units 5}
          {:direction :forward, :units 8}
          {:direction :up, :units 3}
          {:direction :down, :units 8}
          {:direction :forward, :units 2}]
         (read-day2-2020 puzzle-day2-2020))))

(def puzzle-day5-2021
  "0,9 -> 5,9
8,0 -> 0,8
9,4 -> 3,4
2,2 -> 2,1
7,0 -> 7,4
6,4 -> 2,0
0,9 -> 2,9
3,4 -> 1,4
0,0 -> 8,8
5,5 -> 8,2")

(defn- read-day5-2021 [input]
  (let [lines (str/split-lines input)
        pattern "{x1=parse-long},{y1=parse-long} -> {x2=parse-long},{y2=parse-long}"]
    (into [] (unglue pattern) lines)))

(deftest day5
  (is (= [{:x1 0, :y1 9, :x2 5, :y2 9}
          {:x1 8, :y1 0, :x2 0, :y2 8}
          {:x1 9, :y1 4, :x2 3, :y2 4}
          {:x1 2, :y1 2, :x2 2, :y2 1}
          {:x1 7, :y1 0, :x2 7, :y2 4}
          {:x1 6, :y1 4, :x2 2, :y2 0}
          {:x1 0, :y1 9, :x2 2, :y2 9}
          {:x1 3, :y1 4, :x2 1, :y2 4}
          {:x1 0, :y1 0, :x2 8, :y2 8}
          {:x1 5, :y1 5, :x2 8, :y2 2}]
          (read-day5-2021 puzzle-day5-2021))))
