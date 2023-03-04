(ns emias.client.search
  (:require [reagent.core]
            [emias.client.data :refer [search-params fetch-patients]]))

(defn search-form []
  [:form {:id "search" :on-submit #(.preventDefault %)}
   [:div {:class "textwrapper"}
    [:label {:for "name"} "Имя: "]
    [:input {:type "text"
             :id "name"
             :name "name"
             :value (:name @search-params)
             :on-change #(swap! search-params assoc :name (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "patronymic"} "Отчество: "]
    [:input {:type "text"
             :id "patronymic"
             :name "patronymic"
             :value (:patronymic @search-params)
             :on-change #(swap! search-params assoc :patronymic (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "surname"} "Фамилия: "]
    [:input {:type "text"
             :id "surname"
             :name "surname"
             :value (:surname @search-params)
             :on-change #(swap! search-params assoc :surname (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "birthdate"} "Дата рождения: "]
    [:input {:type "text"
             :id "birthdate"
             :name "birthdate"
             :value (:birthdate @search-params)
             :on-change #(swap! search-params assoc :birthdate (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "index"} "Индекс: "]
    [:input {:type "text"
             :id "index"
             :name "index"
             :value (:index @search-params)
             :on-change #(swap! search-params assoc :index (-> % .-target .-value))}]]
   [:div {:class "textwrapper"}
    [:label {:for "region"} "Регион: "]
    [:input {:type "text"
             :id "region"
             :name "region"
             :value (:region @search-params)
             :on-change #(swap! search-params assoc :region (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "city"} "Нас. пункт: "]
    [:input {:type "text"
             :id "city"
             :name "city"
             :value (:city @search-params)
             :on-change #(swap! search-params assoc :city (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "street"} "Улица: "]
    [:input {:type "text"
             :id "street"
             :name "street"
             :value (:street @search-params)
             :on-change #(swap! search-params assoc :street (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "house"} "Дом: "]
    [:input {:type "text"
             :id "house"
             :name "house"
             :value (:house @search-params)
             :on-change #(swap! search-params assoc :house (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "building"} "Корпус/строение: "]
    [:input {:type "text"
             :id "building"
             :name "building"
             :value (:building @search-params)
             :on-change #(swap! search-params assoc :building (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "flat"} "Квартира: "]
    [:input {:type "text"
             :id "flat"
             :name "flat"
             :value (:flat @search-params)
             :on-change #(swap! search-params assoc :flat (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "policy"} "Полис: "]
    [:input {:type "text"
             :id "policy"
             :name "policy"
             :value (:policy @search-params)
             :on-change #(swap! search-params assoc :policy (-> % .-target .-value) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "gender"} "Пол: "]
    [:select {:name "gender"
              :id "gender"
              :value (:gender @search-params)
              :on-change #(swap! search-params assoc :gender (-> % .-target .-value) )}
     [:option {:value ""} "Не важно"]
     [:option {:value "f"} "ж"]
     [:option {:value "m"} "м"]]]
   [:div {:class "spacer"}]
   [:div [:input {:type "button"
                  :class "button"
                  :value "Пошёл страус!"
                  :on-click fetch-patients}]]])
