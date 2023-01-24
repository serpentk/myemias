(ns emias.web-test
  (:require [emias.web :as sut]
            [emias.fixtures :refer [db-test-fixture db-table-fixture patient-data]]
            [clojure.data.json :as json]
            [clojure.test :refer :all]))

(use-fixtures :once db-test-fixture db-table-fixture)

(deftest test-api
  (testing "Test api"
    (let [response (sut/app {:compojure/route [:get "/patients/"]
                             :uri "/patients/"
                             :request-method :get})]
      (is (= (:status response) 200))
      (is (= (json/read-json (:body response))
             {:page {:current 1 :has-next false} :result []})))))

