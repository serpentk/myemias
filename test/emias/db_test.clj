(ns emias.db-test
  (:require [clojure.test :refer :all]
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

(use-fixtures :once db-test-fixture db-table-fixture)

(deftest test-db
  (testing "Happy life cycle"
    (let [id (:id (create-patient patient-data))
          patient-from-db (get-patient id)
          updated (update-patient (assoc patient-from-db :location "{\"index\": \"123456\"}"))]
      (is (= patient-from-db (assoc patient-data :id id :location "{}" :patronymic nil)))
      (is (= (count (filter-patients {:policy (:policy patient-data)})) 1))
      (is (= (count (filter-patients {"location->>'index'" "123456"})) 1))
      (is (= (count (filter-patients {"location->>'index'" "654321"})) 0))
      (is (= (updated (assoc patient-from-db :location "{\"index\": \"123456\"}"))))
      (is (= (delete-patient id) '(1))))))
