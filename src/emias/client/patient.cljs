(ns emias.client.patient
  (:require [emias.client.data :refer [patients]]
            [ajax.core :refer [GET]]))

(defn check-patient [patient]
  (let [check-result {:birthdate (if (clojure.string/blank? (patient :birthdate)) ;; TODO validate format
                              [false "Дата рождения обязательна"]
                              [true nil])
                      :name (if (clojure.string/blank? (patient :name))
                              [false "Имя обязательно"]
                              [true nil])
                      :surname (if (clojure.string/blank? (patient :surname))
                                 [false "Фамилия обязательна"]
                                 [true nil])
                      :policy (if (clojure.string/blank? (patient :policy))
                                [false "Номер полиса обязателен"]
                                [true nil])
                      }
        errors (filter
                #(not (first (second %)))
                check-result)
        ] 
    (if (empty? errors)
      [true nil]
      [false (into {}
                   (map #(vector (first %) (second (second %))) errors))])))

(defn reset-patient [patient-data]
  (reset! patients (assoc @patients (:id patient-data) (prepare-patient patient-data)))
  )

(defn fetch-patient [id]
  #(GET (str "/patients/" id "/")
             {:response-format :json
              :handler reset-patient
              :keywords? :true
              }))

(defn parse-patient-location [p]
  (.parse js.JSON (or (:location p) "{}")))

(defn make-location-json [patient field value]
  (.stringify js/JSON (clj->js (assoc (js->clj (parse-patient-location patient)) field value))))
