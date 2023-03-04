(ns emias.client.edit
  (:require [emias.client.patient :refer [check-patient parse-patient-location fetch-patient make-location-json reset-patient]]
            [emias.client.data :refer [patients]]
            [ajax.core :refer [GET PUT]]))

(defn handle-edited [p]
  #(reset! patients (assoc @patients (:id p) (assoc p :processing false))))

(def edit-fields [:name :patronymic :surname :policy :gender :birthdate :location])

(defn edit-patient-form [p]
  [:tr
   [:td (:id p)]
   [:td [:input {:type "text"
                 :value (:name p)
                 :on-change #(swap! patients assoc (:id p) (assoc p :name (-> % .-target .-value)))
                 :id (str (:id p) "-name")}]]
   [:td [:input {:type "text"
                 :value (:patronymic p)
                 :on-change #(swap! patients assoc
                                    (:id p) (assoc p :patronymic (-> % .-target .-value)))
                 :id (str (:id p) "-patronymic")}]]
   [:td [:input {:type "text"
                 :value (:surname p)
                 :on-change #(swap! patients assoc (:id p) (assoc p :surname (-> % .-target .-value)))
                 :id (str (:id p) "-surname")}]]
   [:td [:input {:type "text"
                 :value (:birthdate p)
                 :on-change #(swap! patients assoc (:id p) (assoc p :birthdate (-> % .-target .-value)))
                 :id (str (:id p) "-birthdate")}]]

   [:td
    [:div "Индекс: " [:input {:type "text"
                              :value (.-index (parse-patient-location p))
                              :on-change #(swap!
                                           patients
                                           assoc
                                           (:id p)
                                           (assoc
                                            p :location
                                            (make-location-json
                                             p "index"
                                             (-> % .-target .-value))))}]]
    [:div "Регион: " [:input {:type "text"
                              :value (.-region (parse-patient-location p))
                              :on-change #(swap!
                                           patients
                                           assoc
                                           (:id p)
                                           (assoc
                                            p :location
                                            (make-location-json
                                             p "region"
                                             (-> % .-target .-value))))}]]
    [:div "Город: " [:input {:type "text"
                              :value (.-city (parse-patient-location p))
                              :on-change #(swap!
                                           patients
                                           assoc
                                           (:id p)
                                           (assoc
                                            p :location
                                            (make-location-json
                                             p "city"
                                             (-> % .-target .-value))))}]]
    [:div "Улица: " [:input {:type "text"
                              :value (.-street (parse-patient-location p))
                              :on-change #(swap!
                                           patients
                                           assoc
                                           (:id p)
                                           (assoc
                                            p :location
                                            (make-location-json
                                             p "street"
                                             (-> % .-target .-value))))}]]
    [:div "Дом: " [:input {:type "text"
                              :value (.-house (parse-patient-location p))
                              :on-change #(swap!
                                           patients
                                           assoc
                                           (:id p)
                                           (assoc
                                            p :location
                                            (make-location-json
                                             p "house"
                                             (-> % .-target .-value))))}]]
    [:div "Корпус: " [:input {:type "text"
                              :value (.-building (parse-patient-location p))
                              :on-change #(swap!
                                           patients
                                           assoc
                                           (:id p)
                                           (assoc
                                            p :location
                                            (make-location-json
                                             p "building"
                                             (-> % .-target .-value))))}]]
    [:div "Квартира: " [:input {:type "text"
                              :value (.-flat (parse-patient-location p))
                              :on-change #(swap!
                                           patients
                                           assoc
                                           (:id p)
                                           (assoc
                                            p :location
                                            (make-location-json
                                             p "flat"
                                             (-> % .-target .-value))))}]]
    
    ]
   [:td [:input {:type "text"
                 :value (:policy p)
                 :on-change #(swap! patients assoc (:id p) (assoc p :policy (-> % .-target .-value)))
                 :id (str (:id p) "-policy")}]]
   [:td [:select {:name "gender"
                  :id "gender"
                  :on-change #(swap! patients assoc (:id p) (assoc p :gender (-> % .-target .-value)))
                  :value (:gender p)}
         [:option {:value "f"} "ж"]
         [:option {:value "m"} "м"]]]
   [:td (if (:active p) "Активен" "Неактивен")]
   [:td
    [:input {:type "button"
             :class "button"
             :value "Пошёл страус!"
             :disabled (not (first (check-patient p)))
             :on-click #(PUT (str "/patients/" (:id p) "/")
                             {:format :json
                              :handler (handle-edited p)
                              :params (select-keys (get @patients (:id p)) edit-fields)})}]
    [:input {:type "button"
             :class "button"
             :value "Отмена"
             :on-click #(GET (str "/patients/" (:id p) "/")
                             {:format :json
                              :handler (fetch-patient (:id p))})}]]])

