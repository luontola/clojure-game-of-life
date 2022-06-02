(defproject new-clojure-project "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [medley "1.4.0"]]
  :managed-dependencies [[org.clojure/spec.alpha "0.3.218"]]
  :pedantic? :abort
  :global-vars {*warn-on-reflection* true}

  :main ^:skip-aot kata
  :target-path "target/%s"
  :javac-options ["--release" "11"]
  :jvm-opts ["--illegal-access=deny"
             "-XX:-OmitStackTraceInFastThrow"]

  :aliases {"kaocha" ["with-profile" "+kaocha" "run" "-m" "kaocha.runner"]}
  :plugins [[lein-ancient "0.7.0"]]

  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[lambdaisland/kaocha "1.66.1034"]
                                  [org.clojure/test.check "1.1.1"]]}
             :kaocha {}})
