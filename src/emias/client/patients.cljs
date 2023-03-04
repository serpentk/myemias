(ns emias.client.patients
  (:require [emias.client.data :refer [patients]]
            [emias.client.patient :refer [parse-patient-location]]
            [emias.client.edit :refer [edit-patient-form]]
            [ajax.core :refer [PUT]]))

(defn edit-patient-button [p]
  [:input {:type "button"
           :class "button"
           :value "Поправить"
           :on-click #(swap! patients assoc (:id p) (assoc p :processing true))}])

;; (defn delete-patient-button [p]
;;   [:input {:type "button"
;;            :value "Этот не нужен"
;;            :on-click #(DELETE (str "/patients/" (:id p) "/")
;;                               {:handler fetch-patients})}])

(defn handle-deleted [p]
  #(reset! patients (assoc @patients (:id p) (assoc p :processing false :active false))))

(defn handle-restored [p]
  #(reset! patients (assoc @patients (:id p) (assoc p :processing false :active true))))

(defn delete-patient-button [p]
  [:input {:type "button"
           :class "button"
           :value "Этот не нужен"
           :on-click #(PUT (str "/patients/" (:id p) "/")
                           {:format :json
                            :handler (handle-deleted p)
                            :params {:active false}})}])

(defn restore-patient-button [p]
  [:input {:type "button"
           :class "button"
           :value "Воскресить"
           :on-click #(PUT (str "/patients/" (:id p) "/")
                           {:format :json
                            :handler (handle-restored p)
                            :params {:active true}})}])

(defn patient-row [p]
  (if (:processing p)
    (edit-patient-form p)
    [:tr {:id (str (:id p))}
     [:td (str (:id p))]
     [:td (:name p)]
     [:td (:patronymic p)]
     [:td (:surname p)]
     [:td (:birthdate p)]
     [:td
      [:div "Индекс: " (.-index (parse-patient-location p))]
      [:div "Регион: " (.-region (parse-patient-location p))]
      [:div "Нас. пункт: " (.-city (parse-patient-location p))]
      [:div "Улица: " (.-street (parse-patient-location p))]
      [:div "Дом: " (.-house (parse-patient-location p))]
      [:div "Корпус/строение: " (.-building (parse-patient-location p))]
      [:div "Квартира: " (.-flat (parse-patient-location p))]]
     [:td (:policy p)]
     [:td (if (= (:gender p) "f") "ж" "м")]
     [:td (if (:active p) "Активен" "Неактивен")]
     [:td (edit-patient-button p)
      (if (:active p) (delete-patient-button p) (restore-patient-button p))]]))

(defn data-table []
  [:table
   (into
    [:tbody
     [:tr
      [:th "ID"]
      [:th "Имя"]
      [:th "Отчество"]
      [:th "Фамилия"]
      [:th "Дата рождения"]
      [:th "Адрес"]
      [:th "Номер полиса"]
      [:th "Пол"]
      [:th "Статус"]
      [:th "Сделать что-нибудь"]]]
    (map patient-row (vals @patients)))])
