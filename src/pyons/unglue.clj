(ns pyons.unglue
  (:require [clojure.string :as str]))

(defn- resolve-string
  ([s ns]
   (some->> s str/trim symbol (ns-resolve ns) deref))
  ([s]
   (resolve-string s *ns*)))

(defn unglue-regex
  [pattern & {:keys [separator]
              :or {separator [\{ \}]}}]
  (let [[start-sym end-sym]
        (if (char? separator)
          [separator separator]
          separator)]
    (loop [pattern (str/trim pattern) labels [] regex []]
      (if (empty? pattern)
        [labels (re-pattern (format "^%s$" (str/join regex)))]
        (let [[pre after] (split-with #(not= start-sym %) pattern)
              [raw-label after] (split-with #(not= end-sym %) (rest after))
              [label fun] (some-> raw-label seq str/join (str/split #"="))
              [keyname re] (when label (-> label str/trim (str/split #" " 2)))
              labels (if keyname
                       (conj labels
                             [keyname
                              (or
                               (resolve-string fun)
                               identity)])
                       labels)
              token (when keyname
                      (if re
                        (format "(%s)" re)
                        "(.*?)"))]
          (recur (rest after) labels (into regex (concat pre token))))))))

(defn- unglue1 [pattern string
                & {:keys [keyword-fn]
                   :or {keyword-fn keyword}}]
  (let [[labels pattern] pattern
        matches (rest (re-matches pattern string))]
    (into {} (map
              (fn [[k f] match]
                [(keyword-fn k) (f (str/trim match))])
              labels matches))))

(defn unglue-transducer
  ([patterns]
   (unglue-transducer patterns {}))
  ([patterns {:keys [separator keyword-fn]
              :or {separator [\{ \}]
                   keyword-fn keyword}}]
   (fn [rf]
     (let [create-pattern (fn [pattern]
                            (unglue-regex pattern :separator separator))
           patterns (into [] (map create-pattern) patterns)]
       (fn
         ([] (rf))
         ([result] (rf result))
         ([result input]
          (let [match-fn (fn [pattern]
                           (unglue1 pattern input :keyword-fn keyword-fn))]
            (if-let [match (some not-empty (map match-fn patterns))]
              (rf result match)
              result))))))))

(defn unglue
  "Returns a lazy sequence of the first matching pattern for each string 
  in coll or nothing.
  Returns a transducer when no collection is provided. 
  To pass options to the transducer, use unglue-transducer directly"
  ([pattern-or-patterns coll & opts]
   (let [patterns (if (string? pattern-or-patterns) [pattern-or-patterns] pattern-or-patterns)]
     (sequence (unglue-transducer patterns opts) coll)))
  ([patterns]
   (let [patterns (if (string? patterns) [patterns] patterns)]
     (unglue-transducer patterns))))
