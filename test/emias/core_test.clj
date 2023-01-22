(ns emias.core-test
  (:require [clojure.test :refer :all]
            [emias.core :refer :all]
            [emias.fixtures :refer [db-test-fixture]]))

(use-fixtures :once db-test-fixture)

(deftest test-start
  (testing "Start system"
    (is (some? (.start (create-system))))))
