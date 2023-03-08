(ns emias.client.create
  (:require [ajax.core :refer [POST]]
            [emias.client.patient :refer [check-patient
                                          make-location-json
                                          parse-patient-location
                                          handle-edit-error]]
            [emias.client.data :refer [new-patient-data fetch-patients]]))

(defn handle-patient-added []
  (do
    (reset! new-patient-data {:gender "f" :active true})
    (fetch-patients)))

(defn new-patient []
  [:form {:on-submit #(.preventDefault %)}
   [:div {:class "textwrapper"}
    [:label {:for "name"} "Имя: "]
    [:input {:type "text"
             :id "name"
             :name "name"
             :value (:name @new-patient-data)
             :on-change #(swap! new-patient-data assoc :name (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "patronymic"} "Отчество: "]
    [:input {:type "text"
             :id "patronymic"
             :name "patronymic"
             :value (:patronymic @new-patient-data)
             :on-change #(swap! new-patient-data assoc :patronymic (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "surname"} "Фамилия: "]
    [:input {:type "text"
             :id "surname"
             :name "surname"
             :value (:surname @new-patient-data)
             :on-change #(swap! new-patient-data assoc :surname (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "birthdate"} "Дата рождения: "]
    [:input {:type "text"
             :id "birthdate"
             :name "birthdate"
             :value (:birthdate @new-patient-data)
             :on-change #(swap! new-patient-data assoc :birthdate (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "index"} "Индекс: "]
    [:input {:type "text"
             :value (.-index (parse-patient-location @new-patient-data))
             :on-change #(swap! new-patient-data
                                 assoc
                                 :location
                                 (make-location-json
                                  @new-patient-data "index" (-> % .-target .-value)))}]]
   [:div {:class "textwrapper"}
    [:label {:for "region"} "Регион: "]
    [:input {:type "text"
             :value (.-region (parse-patient-location @new-patient-data))
             :on-change #(swap! new-patient-data
                                 assoc
                                 :location
                                 (make-location-json
                                  @new-patient-data "region" (-> % .-target .-value)))}]]
   [:div {:class "textwrapper"}
    [:label {:for "city"} "Город: "]
    [:input {:type "text"
             :value (.-city (parse-patient-location @new-patient-data))
             :on-change #(swap! new-patient-data
                                 assoc
                                 :location
                                 (make-location-json
                                  @new-patient-data "city" (-> % .-target .-value)))}]]
   [:div {:class "textwrapper"}
    [:label {:for "street"} "Улица: "]
    [:input {:type "text"
             :value (.-street (parse-patient-location @new-patient-data))
             :on-change #(swap! new-patient-data
                                 assoc
                                 :location
                                 (make-location-json
                                  @new-patient-data "street" (-> % .-target .-value)))}]]
   [:div {:class "textwrapper"}
    [:label {:for "house"} "Дом: "]
    [:input {:type "text"
             :value (.-house (parse-patient-location @new-patient-data))
             :on-change #(swap! new-patient-data
                                 assoc
                                 :location
                                 (make-location-json
                                  @new-patient-data "house" (-> % .-target .-value)))}]]
   [:div {:class "textwrapper"}
    [:label {:for "building"} "Корпус/строение: "]
    [:input {:type "text"
             :value (.-building (parse-patient-location @new-patient-data))
             :on-change #(swap! new-patient-data
                                 assoc
                                 :location
                                 (make-location-json
                                  @new-patient-data "building" (-> % .-target .-value)))}]]
   [:div {:class "textwrapper"}
    [:label {:for "flat"} "Квартира: "]
    [:input {:type "text"
             :value (.-flat (parse-patient-location @new-patient-data))
             :on-change #(swap! new-patient-data
                                 assoc
                                 :location
                                 (make-location-json
                                  @new-patient-data "flat" (-> % .-target .-value)))}]]
   [:div {:class "textwrapper"}
    [:label {:for "policy"} "Полис: "]
    [:input {:type "text"
             :id "policy"
             :name "policy"
             :value (:policy @new-patient-data)
             :on-change #(swap! new-patient-data assoc :policy (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "gender"} "Пол: "]
    [:select {:name "gender"
              :id "gender"
              :value (:gender @new-patient-data)
              :on-change #(swap! new-patient-data assoc :gender (-> % .-target .-value) )}
     [:option {:value "f"} "ж"]
     [:option {:value "m"} "м"]]]
   [:div {:class "spacer"}]
   [:div [:input {:type "button"
                  :class "button"
                  :value "Пошёл страус!"
                  :disabled (not (first (check-patient @new-patient-data)))
                  :on-click #(POST "/patients/"
                                   {:format :json
                                    :error-handler handle-edit-error
                                    :handler handle-patient-added
                                    :params @new-patient-data})}]]])


