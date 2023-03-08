(ns emias.web-test
  (:require [emias.web :as sut]
            [emias.db :as db]
            [emias.fixtures :refer [db-test-fixture db-table-fixture patient-data]]
            [clojure.data.json :as json]
            [clojure.test :refer :all]))

(use-fixtures :each db-test-fixture db-table-fixture)

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

(deftest test-filter
  (testing "Filter by birthdate"
    (let [patient (db/create-patient patient-data)
          filter-date (sut/app {:uri "/patients/"
                                :params {"birthdate" "1696-02-29"}
                                :request-method :get})
          filtered-date-exact (:result (json/read-json (:body filter-date)))
          filter-wrong-date (sut/app {:uri "/patients/"
                                      :params {"birthdate" "1696-02-28"}
                                      :request-method :get})
          filtered-wrong-date (:result (json/read-json (:body filter-wrong-date)))
          filter-from-success (sut/app {:uri "/patients/"
                                        :params {"from" "1696-02-01"}
                                        :request-method :get})
          filtered-from-success (:result (json/read-json (:body filter-from-success)))
          filter-from-empty (sut/app {:uri "/patients/"
                                      :params {"from" "1696-03-01"}
                                      :request-method :get})
          filtered-from-empty (:result (json/read-json (:body filter-from-empty)))
          filter-to-success (sut/app {:uri "/patients/"
                                      :params {"to" "1696-03-01"}
                                      :request-method :get})
          filtered-to-success (:result (json/read-json (:body filter-to-success)))
          filter-to-empty (sut/app {:uri "/patients/"
                                    :params {"to" "1696-02-01"}
                                    :request-method :get})
          filtered-to-empty (:result (json/read-json (:body filter-to-empty)))]
      (is (= 1 (count filtered-date-exact)))
      (is (= 0 (count filtered-wrong-date)))
      (is (= 1 (count filtered-from-success)))
      (is (= 0 (count filtered-from-empty)))
      (is (= 1 (count filtered-to-success)))
      (is (= 0 (count filtered-to-empty))))))

(deftest test-create-edit-delete
  (testing "Life cycle"
    (let [create-response (sut/app {:uri "/patients/"
                                    :request-method :post
                                    :headers {"Content-Type" "application/json"}
                                    :body (.getBytes (json/write-str patient-data))})
          created-data (json/read-json (:body create-response))
          id (:id created-data)
          edit-response (sut/app {:uri (str "/patients/" id "/")
                                  :request-method :put
                                  :headers {"Content-Type" "application/json"}
                                  :body (.getBytes
                                         (json/write-str
                                          (assoc
                                           patient-data
                                           :location (json/write-str {:index "123456"}))))})
          delete-response (sut/app {:uri (str "/patients/" id "/")
                                    :request-method :delete})]
      (is (= 200 (:status edit-response)))
      (is (= 201 (:status create-response)))
      (is (= 204 (:status delete-response)))
      (is (= 404 (:status (sut/app {:uri (str "/patients/" id "/")
                                    :request-method :get}))))
      (is (nil? (db/get-patient id))))))

(deftest test-duplicate-policy
  (testing "Test policy uniqueness"
    (let [created (sut/app {:uri "/patients/"
                            :request-method :post
                            :headers {"Content-Type" "application/json"}
                            :body (.getBytes (json/write-str patient-data))})
          created-data (json/read-json (:body created))
          id (:id created-data)
          created-another (sut/app {:uri "/patients/"
                                    :request-method :post
                                    :headers {"Content-Type" "application/json"}
                                    :body (.getBytes
                                           (json/write-str
                                            (assoc patient-data :policy "007")))})
          not-created-again (sut/app {:uri "/patients/"
                                      :request-method :post
                                      :headers {"Content-Type" "application/json"}
                                      :body (.getBytes (json/write-str patient-data))})
          conflict-editing (sut/app {:uri (str "/patients/" id "/")
                                     :request-method :put
                                     :headers {"Content-Type" "application/json"}
                                     :body (.getBytes
                                            (json/write-str
                                             {:policy "007"}))})]
      (is (= 201 (:status created)))
      (is (= 201 (:status created-another)))
      (is (= 409 (:status not-created-again)))
      (is (= 409 (:status conflict-editing)))
      (is (= 2 (count (db/filter-patients {})))))))
