(ns emias.validation-test
  (:require [clojure.test :refer :all]
            [emias.fixtures :refer [patient-data]]
            [emias.validation :refer :all]))

(deftest test-validation
  (testing "Check gender"
    (is (= (check-gender "f") [true nil]))
    (is (= (check-gender "m") [true nil]))
    (is (= (check-gender "I don't know") [false "Gender must be 'f' or 'm'"]))
    (is (= (check-gender [1 2 3]) [false "Gender must be 'f' or 'm'"]))
    (is (= (check-gender nil) [false "Gender must be 'f' or 'm'"])))
  (testing "Check birthdate"
    (is (= (check-birthdate "1799-06-06") [true nil]))
    (is (= (check-birthdate "1984-02-29") [true nil]))
    (is (= (check-birthdate "1984-02-30") [false "Wrong day of month"]))
    (is (= (check-birthdate "Давным-давно") [false "Wrong birthdate format"])))
  (testing "Create patient"
    (is (= (validate-patient patient-data) [true nil]))
    (is (= (validate-patient (assoc patient-data :birthdate "Ololo"))
           [false '("Wrong birthdate format")]))
    (is (= (validate-patient (assoc patient-data :gender "nothing"))
           [false '("Gender must be 'f' or 'm'")]))
    (is (= (validate-patient (dissoc patient-data :policy)) [false '()]))
    (is (= (validate-patient (dissoc patient-data :active)) [false '()]))
    (is (= (validate-patient (dissoc patient-data :name)) [false '()]))
    (is (= (validate-patient (dissoc patient-data :surname)) [false '()])))
  (testing "Edit patient"
    (is (= (validate-patient-edit {:name "Alice"}) [true nil]))))
