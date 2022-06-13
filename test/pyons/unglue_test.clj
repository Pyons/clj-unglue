(ns pyons.unglue-test
  (:require [clojure.test :refer [deftest is]]
            [pyons.unglue :refer [unglue]]))

(deftest parse-uuid-test
  (is (= '({:sensor #uuid "ac960434-c2bc-4cd5-8681-8afa43355c65"}
           {:sensor #uuid "bc960434-c2bc-4cd5-8681-8afa43355c65"})
         (unglue ["sensor: {sensor = parse-uuid} " 
                  "{sensor=parse-uuid},sensor"]
                 ["sensor: ac960434-c2bc-4cd5-8681-8afa43355c65"
                  "bc960434-c2bc-4cd5-8681-8afa43355c65,sensor"]))))

(defn- parse-int [s]
  (int (parse-long s)))

(deftest custom-parser
  (is  (= '({:sensor 4} {:sensor 10})
          (unglue ["sensor: {sensor = parse-long} "
                   "{sensor=pyons.unglue-test/parse-int},sensor"]
                  ["sensor: 4" "10,sensor"])))
  (is  (= '({:name "HANS PETER"})
          (unglue "name: {name = clojure.string/upper-case} "
                  ["name: Hans Peter"]))))

(deftest custom-separator
  (is (= '({:sensor 4})
         (unglue "sensor: [sensor = parse-long] "
                 ["sensor: 4" "10,sensor"] :separator [\[ \]]))))

(deftest single-separator
  (is (=
       '({:sensor 4})
       (unglue "sensor: ~sensor = parse-long~,device "
               ["sensor: 4,device" "10,sensor"] :separator \~))))

(deftest nil-and-empty
  (is (= '()
         (unglue nil nil)))
  (is (= '()
         (unglue '() '())))
  (is (= '()
         (unglue "sensor: {id}" '())))
  (is (= '()
         (unglue ["sensor: {id}"] nil))))

(deftest no-match
  (is (= '()
         (unglue nil ["sensor: {id}"])))
  (is (= '()
         (unglue '() ["sensor: {id}"])))
  (is (= '()
         (unglue ["sensor: {id}"]
                 ["device: 10"]))))

(deftest additional-regex
  (is (= '({:x 1 :y 2})
         (unglue "{x \\d=parse-long}{y \\d=parse-long}" ["12"]))))

(deftest facts-test
  (let [facts '("Antarctica is the largest desert in the world!",
                "The largest country in Europe is Russia!",
                "The smallest country in Europe is Vatican!",
                "Disneyland is the most visited place in Europe! Disneyland is in Paris!",
                "The largest island in the world is Green Land!")
        patterns '("The {adjective} {place-type} in {bigger-place} is {place}!",
                   "{place} is the {adjective=str/} {place-type=} in {bigger-place}!")
        result-map '({"place" "Antarctica",
                      "adjective" "largest",
                      "place-type" "desert",
                      "bigger-place" "the world"}
                     {"adjective" "largest",
                      "place-type" "country",
                      "bigger-place" "Europe",
                      "place" "Russia"}
                     {"adjective" "smallest",
                      "place-type" "country",
                      "bigger-place" "Europe",
                      "place" "Vatican"}
                     {"place" "Disneyland",
                      "adjective" "most",
                      "place-type" "visited place",
                      "bigger-place" "Europe! Disneyland is in Paris"}
                     {"adjective" "largest",
                      "place-type" "island",
                      "bigger-place" "the world",
                      "place" "Green Land"})]
    (is (= result-map (unglue patterns facts :keyword-fn identity)))))

(deftest tranducer-arity
  (let [facts '("Antarctica is the largest desert in the world!",
                "The largest country in Europe is Russia!",
                "The smallest country in Europe is Vatican!",
                "Disneyland is the most visited place in Europe! Disneyland is in Paris!",
                "The largest island in the world is Green Land!")
        patterns '("The {adjective} {place-type} in {bigger-place} is {place}!",
                   "{place} is the {adjective} {place-type=} in {bigger-place}!")
        result-map  [{:place "Antarctica",
                      :adjective "largest",
                      :place-type "desert",
                      :bigger-place "the world"}
                     {:adjective "largest",
                      :place-type "country",
                      :bigger-place "Europe",
                      :place "Russia"}
                     {:adjective "smallest",
                      :place-type "country",
                      :bigger-place "Europe",
                      :place "Vatican"}
                     {:place "Disneyland",
                      :adjective "most",
                      :place-type "visited place",
                      :bigger-place "Europe! Disneyland is in Paris"}
                     {:adjective "largest",
                      :place-type "island",
                      :bigger-place "the world",
                      :place "Green Land"}]]
    (is (= result-map (into [] (unglue patterns) facts)))
    (is (= '({:place "Disneyland",
              :adjective "most",
              :place-type "visited place",
              :bigger-place "Europe! Disneyland is in Paris"})))
    (transduce (comp (unglue patterns) (filter #(= "most" (:adjective %)))) conj facts)))
