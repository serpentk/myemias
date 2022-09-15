(ns emias.client
  (:require [reagent.core]
            [reagent.dom]
            [ajax.core :refer [GET POST]]))

(def patients (reagent.core/atom []))

(def new-patient-name (reagent.core/atom ""))
(def new-patient-patronymic (reagent.core/atom ""))
(def new-patient-surname (reagent.core/atom ""))
(def new-patient-birthdate (reagent.core/atom ""))
(def new-patient-policy (reagent.core/atom ""))
(def new-patient-address (reagent.core/atom ""))
(def new-patient-gender (reagent.core/atom "f"))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))


(defn patient-row [p]
  [:tr [:td (str (:id p))] [:td (:name p)] [:td (:surname p)]]
  )

(defn data-table []
  (into
   [:table [:tr [:th "ID"] [:th "Имя"] [:th "Фамилия"]]]
   (map patient-row @patients)))

(defn page-content []
  [:div
   [new-patient]
   [data-table]])



(defn new-patient []
  [:form {:on-submit #(.preventDefault %)}
   [:label {:for "name"} "Имя: "]
   [:input {:type "text"
            :id "name"
            :name "name"
            :value @new-patient-name
            :on-change #(reset! new-patient-name (-> % .-target .-value) )}]
   [:label {:for "patronymic"} "Отчество: "]
   [:input {:type "text"
            :id "patronymic"
            :name "patronymic"
            :value @new-patient-patronymic
            :on-change #(reset! new-patient-patronymic (-> % .-target .-value) )}]
   [:label {:for "surname"} "Фамилия: "]
   [:input {:type "text"
            :id "surname"
            :name "surname"
            :value @new-patient-surname
            :on-change #(reset! new-patient-surname (-> % .-target .-value) )}]
   [:label {:for "birthdate"} "Дата рождения: "]
   [:input {:type "text"
            :id "birthdate"
            :name "birthdate"
            :value @new-patient-birthdate
            :on-change #(reset! new-patient-birthdate (-> % .-target .-value) )}]
   [:label {:for "address"} "Адрес: "]
   [:input {:type "text"
            :id "address"
            :name "address"
            :value @new-patient-address
            :on-change #(reset! new-patient-address (-> % .-target .-value) )}]
   [:label {:for "policy"} "Полис: "]
   [:input {:type "text"
            :id "policy"
            :name "policy"
            :value @new-patient-policy
            :on-change #(reset! new-patient-policy (-> % .-target .-value) )}]
   [:label {:for "gender"} "Пол: "]
   [:select {:name "gender"
             :id "gender"
             :on-change #(reset! new-patient-gender (-> % .-target .-value) )}
    [:option {:value "f"} "ж"]
    [:option {:value "m"} "м"]] 
   [:input {:type "button"
            :value "Пошёл страус!"
            :on-click #(POST "/patients/"
                            {:format :json
                             :params {:name @new-patient-name
                                      :gender @new-patient-gender
                                      :patronymic @new-patient-patronymic,
                                      :surname @new-patient-surname
                                      :birthdate @new-patient-birthdate
                                      :address @new-patient-address
                                      :policy @new-patient-policy}})}]])

(reagent.dom/render
 [page-content]
 (js/document.getElementById "app"))
(GET "/patients/"
     {:handler #(reset! patients %)
      :error-handler error-handler
      :response-format :json
      :keywords? true})
