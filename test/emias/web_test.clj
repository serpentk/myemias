(ns emias.web-test
  (:require [emias.web :as sut]
            [emias.db :as db]
            [emias.fixtures :refer [db-test-fixture db-table-fixture patient-data]]
            [clojure.data.json :as json]
            [clojure.test :refer :all]))

(use-fixtures :once db-test-fixture db-table-fixture)

(deftest test-main
  (testing "Main page"
    (is (= 200 (:status (sut/app {:uri "/" :request-method :get}))))))

(deftest test-get-empty
  (testing "Get empty list"
    (let [response (sut/app {:uri "/patients/"
                             :request-method :get})]
      (is (= (:status response) 200))
      (is (= (json/read-json (:body response))
             {:page {:current 1 :has-next false} :result []}))))
  (testing "Get non-existing patient info"
    (let [response (sut/app {:uri "/patients/100500/"
                             :request-method :get})]
      (is (= 404 (:status response)))
      (is (= (json/read-json (:body response))
             {:error "Patient not found"}))))
  (testing "Get bad id"
    (is (= 404 (:status (sut/app {:uri "/patients/100500/"
                                  :request-method :get}))))))

(deftest test-create-delete
  (testing "Life cycle"
    (let [create-response (sut/app {:uri "/patients/"
                                    :request-method :post
                                    :headers {"Content-Type" "application/json"}
                                    :body (.getBytes (json/write-str patient-data))})
          created-data (json/read-json (:body create-response))
          id (:id created-data)
          delete-response (sut/app {:uri (str "/patients/" id "/")
                                    :request-method :delete})]
      (is (= 201 (:status create-response)))
      (is (= 204 (:status delete-response)))
      (is (= 404 (:status (sut/app {:uri (str "/patients/" id "/")
                                    :request-method :get}))))
      (is (nil? (db/get-patient id))))))
