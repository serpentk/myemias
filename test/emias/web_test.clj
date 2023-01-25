(ns emias.web-test
  (:require [emias.web :as sut]
            [emias.fixtures :refer [db-test-fixture db-table-fixture patient-data]]
            [clojure.data.json :as json]
            [clojure.test :refer :all]))

(use-fixtures :once db-test-fixture db-table-fixture)

(deftest test-main
  (testing "Main page"
    (is (= 200 (:status (sut/app {:uri "/" :request-method :get}))))))

(deftest test-api
  (testing "Test api"
    (let [response (sut/app {:uri "/patients/"
                             :request-method :get})]
      (is (= (:status response) 200))
      (is (= (json/read-json (:body response))
             {:page {:current 1 :has-next false} :result []})))))

