(defproject emias "0.1.0-SNAPSHOT"
  :description "Patients CRUD"
  :url "http://example.com/FIXME"
  :license {:name "GNU General Public License"
            :url "http://www.gnu.org/licenses/gpl.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [http-kit "2.3.0"]
                 [com.stuartsierra/component "1.1.0"]
                 [reloaded.repl "0.2.4"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [org.postgresql/postgresql "42.4.0"]
                 [compojure "1.7.0"]
                 [org.clojure/data.json "2.4.0"]
                ]
  :main ^:skip-aot emias.core
  :target-path "target/%s"
  :profiles {:dev {:plugins []
                   :dependencies []
                   :source-paths ["dev"]}})
