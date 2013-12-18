(ns clova.user
  (:refer-clojure :exclude [*1 *2 *3])
  (:use [clojure.repl]
        [nova]))

;; Anything in this namespace is visible to the user
;; It's pretty empty but they can define stuff if they like
;; and vars like *0, *1... get created here to hold the
;; answers to their previous commands
