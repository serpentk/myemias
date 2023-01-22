(ns emias.db-test
  (:require [clojure.test :refer :all]
            [emias.db :refer :all]
            [emias.fixtures :refer [db-test-fixture db-table-fixture patient-data]]))

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
