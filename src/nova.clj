(ns nova
  (:use [clojure.java.shell :only [sh with-sh-env]]
        [clojure.pprint :only [pprint]]))

(defn plus-locations [line]
  (filter identity
          (map (fn [[k v]] (when (= v \+) k))
               (map vector (range) line))))

(defn keywordize [s]
  (-> s
      (.replace \  \-)
      (.toLowerCase)
      keyword))

(defn deliminate [line delim-cols]
  (let [delim-col-pairs (partition 2 1 delim-cols)]
    (map (fn [[s e]]
           (.trim (.substring line (inc s) (dec e))))
         delim-col-pairs)))

(defn parse-table [output]
  (let [lines (.split output "\n")
        delim-cols (plus-locations (first lines))
        headings (map keywordize (deliminate (second lines) delim-cols))
        rows (map #(deliminate % delim-cols) (butlast (drop 3 lines)))]
    (map #(zipmap headings %) rows)))

(defn optify [kw]
  (str "--" (name kw)))

(defn flatten-opt [[arg-kw val]]
  (if (string? val)
    [(optify arg-kw) val]
    (if val
      [(optify arg-kw)] [])))

(defn nova [opts subcommand]
  (let [flatopts (mapcat flatten-opt opts)
        cmd (concat ["nova"] flatopts [(name subcommand)])
        output (with-sh-env {} (apply sh cmd))]
    (merge output
           {:cmd cmd
            :map (parse-table (output :out))})))


(def envs {:rnde {:os-username "gilliard-rnde"
                  :os-password "XXXXXXXX"
                  :os-tenant-name "gilliard-rnde-project"
                  :os-auth-url "https://csnode.rnde.aw1.hpcloud.net:35357/v2.0/"}})


(def nova-rnde (partial nova (merge (envs :rnde) {:insecure true})))
