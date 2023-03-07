(ns emias.client.patient
  (:require [emias.client.data :refer [patients info handle-fetch-error prepare-patient]]
            [ajax.core :refer [GET]]))

(defn check-patient [patient]
  (let [check-result {:birthdate (if (clojure.string/blank? (patient :birthdate))
                                   [false "Дата рождения обязательна"]
                                   (if (js/isNaN (.parse js/Date (patient :birthdate)))
                                     [false "Неверный формат даты"]
                                     [true nil]))
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
  (reset! info "Данные загружены")
  )

(defn fetch-patient [id]
  #(GET (str "/patients/" id "/")
             {:response-format :json
              :handler reset-patient
              :error-handler handle-fetch-error
              :keywords? :true
              }))

(defn parse-patient-location [p]
  (.parse js.JSON (or (:location p) "{}")))

(defn handle-edit-error [r]
  (case (:status r)
    409 (reset! info "Пациент с таким номером полиса уже существует")
    400 (reset! info "Неправильный формат данных") ; TODO use (:response r)
    (reset! info "Что-то пошло не так")))

(defn make-location-json [patient field value]
  (.stringify js/JSON (clj->js (assoc (js->clj (parse-patient-location patient)) field value))))
