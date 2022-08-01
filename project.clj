(defproject emias "0.1.0-SNAPSHOT"
  :description "Patients CRUD"
  :url "http://example.com/FIXME"
  :license {:name "GNU General Public License"
            :url "http://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.11.60"]
                 [http-kit "2.3.0"]
                 [com.stuartsierra/component "1.1.0"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.postgresql/postgresql "42.4.0"]
                 [compojure "1.7.0"]
                 [org.clojure/data.json "2.4.0"]
                 [reagent "1.1.1"]
                 [cljsjs/react "18.0.0-rc.0-0"]
                 [cljsjs/react-dom "18.0.0-rc.0-0"]
                 [cider/piggieback "0.5.3"]
                ]
  :main ^:skip-aot emias.core
  :target-path "target/%s"
  :profiles {:dev {:plugins [[lein-cljsbuild "1.1.8"]
                             [lein-figwheel "0.5.18"]
                             [figwheel-sidecar "0.5.20"]]
                   :dependencies [[reloaded.repl "0.2.4"]]
                   :source-paths ["dev"]
                   :cljsbuild {:builds [{:id "dev"
                                         :source-paths ["src" "dev"]
                                         :figwheel true
                                         :compiler {:main emias.client
                                                    :asset-path "out"
                                                    :output-to "target/default/classes/public/app.js"
                                                    :output-dir "target/default/classes/public/out"
                                                    :optimizations :none
                                                    :recompile-dependents true
                                                    :source-map true}}]}
                   :repl-options {:nrepl-middleware [cider.piggieback/wrap-cljs-repl]}}})
