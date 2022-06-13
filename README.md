# clj unglue

Clojure unglue, inspired by [https://github.com/moodymudskipper/unglue](https://github.com/moodymudskipper/unglue). Diverges on some key features like applying a function directly to the match.

## Installation

Download from https://github.com/pyons/clj-unglue

## Usage

Examples can be found in the test folder


```clojure

(def puzzle
  "forward 5
down 5
forward 8
up 3
down 8
forward 2")

(defn read-lines [input]
  (let [lines (str/split-lines input)
        pattern "{direction=keyword} {units=parse-long}"]
    (unglue pattern lines)))

(read-lines puzzle)
;;=>
    [{:direction :forward, :units 5}
     {:direction :down, :units 5}
     {:direction :forward, :units 8}
     {:direction :up, :units 3}
     {:direction :down, :units 8}
     {:direction :forward, :units 2}]

```


Run the project's tests:

    $ clojure -X:test:runner

