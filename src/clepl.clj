(ns clepl
  (require [clepl.user])
  (import org.jboss.aesh.util.ANSI
          [org.jboss.aesh.console Prompt Console ConsoleCallback]
          [org.jboss.aesh.console.settings SettingsBuilder]))

(def answers (atom 0))  ;; lol

(defn store-answer [ans]
  (let [n (str "*" @answers)]
    (swap! answers inc)
    (intern (the-ns 'clepl.user) (symbol n) ans)  ;; woah
    n))



(defmacro ansi [c & s]
  `(str (. ANSI ~c) ~@s (ANSI/reset)))  ;; wat

(defn doinput [in]
  (let [[ans num success] (binding [*ns* (the-ns 'clepl.user)]
                            (try (let [ans (eval (read-string in))
                                       num (store-answer ans)]
                                   [ans num true])
                                 (catch Exception e
                                   [(.getMessage e) "  " false])))]
    {:ans (if (nil? ans) "nil" ans)  ;; such hack
     :success success
     :num num}))

(defn main []

  (println (ansi yellowText "ready let's go"))

  (let [settings-builder (SettingsBuilder.)
        console (Console. (.create settings-builder))
        prompt (ansi blueText "clova => ")
        callback (reify ConsoleCallback
                   (readConsoleOutput [_ output]

                     (let [{:keys [ans num success]}
                           (doinput (.getBuffer output))]

                       (.println (.out (.getShell console))
                                 (str (ansi cyanText num)
                                      " ~> "
                                      (if success  ;; very hack
                                        (ansi yellowText ans)
                                        (ansi redText    ans)))))

                     (when (= "quit" (.getBuffer output))
                       (.stop console))
                     0))]

    (.setPrompt console (Prompt. prompt))
    (.setConsoleCallback console callback)
    (.start console)))
