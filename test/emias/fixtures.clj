(ns emias.fixtures
  (:require  [clojure.test :as t]
             [emias.db :refer :all]
             [clojure.java.jdbc :as jdbc]
             [emias.config :refer [db-config]]))

(defn db-test-fixture [f]
  (let [dbname (:dbname @db-config)]
    (swap! db-config assoc :dbname "test")
    (f)
    (swap! db-config assoc :dbname dbname)))

(defn db-table-fixture [f]
  (create-gender-type)
  (create-patients-table)
  (f)
  (jdbc/db-do-commands @db-config "drop table patients"))

(def patient-data {:name "Кощей"
                   :surname "Бессмертный"
                   :gender "m"
                   :policy "666"
                   :birthdate "1696-02-29"
                   :active true})
