(ns emias.web-test
  (:require [emias.web :as sut]
            [emias.fixtures :refer [db-test-fixture db-table-fixture patient-data]]
            [clojure.data.json :as json]
            [clojure.test :refer :all]))

(use-fixtures :once db-test-fixture db-table-fixture)

(deftest test-main
  (testing "Main page"
    (is (= 200 (:status (sut/app {:uri "/" :request-method :get}))))))

(deftest test-get
  (testing "Get empty list"
    (let [response (sut/app {:uri "/patients/"
                             :request-method :get})]
      (is (= (:status response) 200))
      (is (= (json/read-json (:body response))
             {:page {:current 1 :has-next false} :result []})))))

(deftest test-create
  (testing "Add patient"
    (let [response (sut/app {:uri "/patients/"
                             :request-method :post
                             :headers {"Content-Type" "application/json"}
                             :body (.getBytes (json/write-str patient-data))})]
      (is (= 201 (:status response))))))

