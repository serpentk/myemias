(ns emias.validation-test
  (:require [clojure.test :refer :all]
            [emias.validation :refer :all]))

(deftest test-validation
  (testing "Check gender"
    (is (= (check-gender "f") [true nil]))
    (is (= (check-gender "m") [true nil]))
    (is (= (check-gender "I don't know") [false "Gender must be 'f' or 'm'"]))
    (is (= (check-gender [1 2 3]) [false "Gender must be 'f' or 'm'"]))
    (is (= (check-gender nil) [false "Gender must be 'f' or 'm'"]))))
