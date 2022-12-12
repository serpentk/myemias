(ns emias.client
  (:require [reagent.core]
            [reagent.dom]
            [ajax.core :refer [GET POST PUT DELETE]]))

(def patients (reagent.core/atom []))

(def new-patient-data (reagent.core/atom {:gender "f"}))

(def search-params (reagent.core/atom {:gender ""}))

(def page (reagent.core/atom {:current 1 :has-next false}))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn prepare-patients-dict [datalist]
  (reduce #(assoc %1 (:id %2) (assoc (assoc %2 :processing false) :birthdate (subs (:birthdate %2) 0 10))) {} datalist))

(defn reset-patients [data]
  (do
    (reset! patients (prepare-patients-dict (:result data)))
    (reset! page (:page data))))

(defn fetch-patients []
  (GET "/patients/"
             {:response-format :json
              :handler reset-patients
              :keywords? :true
              :params (into {:page (:current @page)}
                            (filter (fn [p] (not (= (val p) "")))
                                    @search-params))
              }))

(defn handle-edited [p]
  #(reset! patients (assoc @patients (:id p) (assoc p :processing false))))

(defn handle-deleted [p]
  #(reset! patients (assoc @patients (:id p) (assoc p :processing false :active false))))

(defn handle-restored [p]
  #(reset! patients (assoc @patients (:id p) (assoc p :processing false :active true))))

(defn parse-patient-location [p]
  (.parse js.JSON (or (:location p) "{}")))

(def edit-fields [:name :patronymic :surname :policy :gender :birthdate :location])

(defn make-location-json [patient field value]
  (.stringify js/JSON (clj->js (assoc (js->clj (parse-patient-location patient)) field value))))

(defn edit-patient-form [p]
  [:tr
   [:td (:id p)]
   [:td [:input {:type "text"
                 :value (:name p)
                 :on-change #(reset! patients (assoc @patients (:id p) (assoc p :name (-> % .-target .-value))) )
                 :id (str (:id p) "-name")}]]
   [:td [:input {:type "text"
                 :value (:patronymic p)
                 :on-change #(reset! patients (assoc @patients (:id p) (assoc p :patronymic (-> % .-target .-value))) )
                 :id (str (:id p) "-patronymic")}]]
   [:td [:input {:type "text"
                 :value (:surname p)
                 :on-change #(reset! patients (assoc @patients (:id p) (assoc p :surname (-> % .-target .-value))) )
                 :id (str (:id p) "-surname")}]]
   [:td [:input {:type "text"
                 :value (:birthdate p)
                 :on-change #(reset! patients (assoc @patients (:id p) (assoc p :birthdate (-> % .-target .-value))) )
                 :id (str (:id p) "-birthdate")}]]

   [:td
    [:div "Индекс: " [:input {:type "text"
                              :value (.-index (parse-patient-location p))
                              :on-change #(reset!
                                           patients
                                           (assoc @patients
                                                  (:id p)
                                                  (assoc
                                                   p :location
                                                   (make-location-json
                                                    p "index"
                                                    (-> % .-target .-value)))))}]]
    [:div "Регион: " [:input {:type "text"
                              :value (.-region (parse-patient-location p))
                              :on-change #(reset!
                                           patients
                                           (assoc @patients
                                                  (:id p)
                                                  (assoc
                                                   p :location
                                                   (make-location-json
                                                    p "region"
                                                    (-> % .-target .-value)))))}]]
    [:div "Город: " [:input {:type "text"
                              :value (.-city (parse-patient-location p))
                              :on-change #(reset!
                                           patients
                                           (assoc @patients
                                                  (:id p)
                                                  (assoc
                                                   p :location
                                                   (make-location-json
                                                    p "city"
                                                    (-> % .-target .-value)))))}]]
    [:div "Улица: " [:input {:type "text"
                              :value (.-street (parse-patient-location p))
                              :on-change #(reset!
                                           patients
                                           (assoc @patients
                                                  (:id p)
                                                  (assoc
                                                   p :location
                                                   (make-location-json
                                                    p "street"
                                                    (-> % .-target .-value)))))}]]
    [:div "Дом: " [:input {:type "text"
                              :value (.-house (parse-patient-location p))
                              :on-change #(reset!
                                           patients
                                           (assoc @patients
                                                  (:id p)
                                                  (assoc
                                                   p :location
                                                   (make-location-json
                                                    p "house"
                                                    (-> % .-target .-value)))))}]]
    [:div "Корпус: " [:input {:type "text"
                              :value (.-building (parse-patient-location p))
                              :on-change #(reset!
                                           patients
                                           (assoc @patients
                                                  (:id p)
                                                  (assoc
                                                   p :location
                                                   (make-location-json
                                                    p "building"
                                                    (-> % .-target .-value)))))}]]
    [:div "Квартира: " [:input {:type "text"
                              :value (.-flat (parse-patient-location p))
                              :on-change #(reset!
                                           patients
                                           (assoc @patients
                                                  (:id p)
                                                  (assoc
                                                   p :location
                                                   (make-location-json
                                                    p "flat"
                                                    (-> % .-target .-value)))))}]]
    
    ]
   [:td [:input {:type "text"
                 :value (:policy p)
                 :on-change #(reset! patients (assoc @patients (:id p) (assoc p :policy (-> % .-target .-value))) )
                 :id (str (:id p) "-policy")}]]
   [:td [:select {:name "gender"
                  :id "gender"
                  :on-change #(reset! patients (assoc @patients (:id p) (assoc p :gender (-> % .-target .-value))) )
                  :value (:gender p)}
         [:option {:value "f"} "ж"]
         [:option {:value "m"} "м"]]]
   [:td [:input {:type "button"
                 :class "button"
                 :value "Пошёл страус!"
                 :on-click #(PUT (str "/patients/" (:id p) "/")
                                 {:format :json
                                  :handler (handle-edited p)
                                  :params (select-keys (get @patients (:id p)) edit-fields) })}]]])

(defn edit-patient-button [p]
  [:input {:type "button"
           :class "button"
           :value "Поправить"
           :on-click #(reset! patients (assoc @patients (:id p) (assoc p :processing true)))}])

;; (defn delete-patient-button [p]
;;   [:input {:type "button"
;;            :value "Этот не нужен"
;;            :on-click #(DELETE (str "/patients/" (:id p) "/")
;;                               {:handler fetch-patients})}])

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
    [:tr {:id (str (:id p)) :class (if (:odd p) "odd" "even")}
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
    (map patient-row (map-indexed #(assoc %2 :odd (odd? %1)) (vals @patients))))])

(defn change-page [page-num]
  #(GET "/patients/"
        {:response-format :json
         :handler reset-patients
         :keywords? :true
         :params (into {:page page-num}
                       (filter (fn [p] (not (= (val p) "")))
                               @search-params))}))

(defn next-page []
  [:span {:on-click (change-page (+ 1 (:current @page)))}        
          " >"])

(defn prev-page []
  [:span {:on-click (change-page (- (:current @page) 1))}        
          "< "])

(defn pager []
  [:div {:class "pager"}
   [:span "Страница: "]
   (if (> (:current @page) 1) [prev-page])
   [:span (:current @page)]
   (if (:has-next @page) [next-page])])

(defn handle-patient-added []
  (do
    (reset! new-patient-data {:gender "f"})
    (fetch-patients)))

(defn search-form []
  [:form {:id "search" :on-submit #(.preventDefault %)}
   [:div [:label {:for "name"} "Имя: "]
    [:input {:type "text"
             :id "name"
             :name "name"
             :value (:name @search-params)
             :on-change #(reset! search-params (assoc @search-params :name (-> % .-target .-value)) )}]]
   [:div [:label {:for "patronymic"} "Отчество: "]
    [:input {:type "text"
             :id "patronymic"
             :name "patronymic"
             :value (:patronymic @search-params)
             :on-change #(reset! search-params (assoc @search-params :patronymic (-> % .-target .-value)) )}]]
   [:div [:label {:for "surname"} "Фамилия: "]
    [:input {:type "text"
             :id "surname"
             :name "surname"
             :value (:surname @search-params)
             :on-change #(reset! search-params (assoc @search-params :surname (-> % .-target .-value)) )}]]
   [:div [:label {:for "birthdate"} "Дата рождения: "]
    [:input {:type "text"
             :id "birthdate"
             :name "birthdate"
             :value (:birthdate @search-params)
             :on-change #(reset! search-params (assoc @search-params :birthdate (-> % .-target .-value)) )}]]
   [:div [:label {:for "index"} "Индекс: "]
    [:input {:type "text"
             :id "index"
             :name "index"
             :value (:index @search-params)
             :on-change #(reset! search-params (assoc @search-params :index (-> % .-target .-value)))}]]
   [:div [:label {:for "region"} "Регион: "]
    [:input {:type "text"
             :id "region"
             :name "region"
             :value (:region @search-params)
             :on-change #(reset! search-params (assoc @search-params :region (-> % .-target .-value)) )}]]
   [:div [:label {:for "city"} "Нас. пункт: "]
    [:input {:type "text"
             :id "city"
             :name "city"
             :value (:city @search-params)
             :on-change #(reset! search-params (assoc @search-params :city (-> % .-target .-value)) )}]]
   [:div [:label {:for "street"} "Улица: "]
    [:input {:type "text"
             :id "street"
             :name "street"
             :value (:street @search-params)
             :on-change #(reset! search-params (assoc @search-params :street (-> % .-target .-value)) )}]]
   [:div [:label {:for "house"} "Дом: "]
    [:input {:type "text"
             :id "house"
             :name "house"
             :value (:house @search-params)
             :on-change #(reset! search-params (assoc @search-params :house (-> % .-target .-value)) )}]]
   [:div [:label {:for "building"} "Корпус/строение: "]
    [:input {:type "text"
             :id "building"
             :name "building"
             :value (:building @search-params)
             :on-change #(reset! search-params (assoc @search-params :building (-> % .-target .-value)) )}]]
   [:div [:label {:for "flat"} "Квартира: "]
    [:input {:type "text"
             :id "flat"
             :name "flat"
             :value (:flat @search-params)
             :on-change #(reset! search-params (assoc @search-params :flat (-> % .-target .-value)) )}]]
   [:div [:label {:for "policy"} "Полис: "]
    [:input {:type "text"
             :id "policy"
             :name "policy"
             :value (:policy @search-params)
             :on-change #(reset! search-params (assoc @search-params :policy (-> % .-target .-value)) )}]]
   [:div [:label {:for "gender"} "Пол: "]
    [:select {:name "gender"
              :id "gender"
              :value (:gender @search-params)
              :on-change #(reset! search-params (assoc @search-params :gender (-> % .-target .-value)) )}
     [:option {:value ""} "Не важно"]
     [:option {:value "f"} "ж"]
     [:option {:value "m"} "м"]]]
   [:div {:class "spacer"}]
   [:div [:input {:type "button"
                  :class "button"
                  :value "Пошёл страус!"
                  :on-click fetch-patients}]]])

(defn new-patient []
  [:form {:on-submit #(.preventDefault %)}
   [:div [:label {:for "name"} "Имя: "]
    [:input {:type "text"
             :id "name"
             :name "name"
             :value (:name @new-patient-data)
             :on-change #(reset! new-patient-data (assoc @new-patient-data :name (-> % .-target .-value)) )}]]
   [:div [:label {:for "patronymic"} "Отчество: "]
    [:input {:type "text"
             :id "patronymic"
             :name "patronymic"
             :value (:patronymic @new-patient-data)
             :on-change #(reset! new-patient-data (assoc @new-patient-data :patronymic (-> % .-target .-value)) )}]]
   [:div [:label {:for "surname"} "Фамилия: "]
    [:input {:type "text"
             :id "surname"
             :name "surname"
             :value (:surname @new-patient-data)
             :on-change #(reset! new-patient-data (assoc @new-patient-data :surname (-> % .-target .-value)) )}]]
   [:div [:label {:for "birthdate"} "Дата рождения: "]
    [:input {:type "text"
             :id "birthdate"
             :name "birthdate"
             :value (:birthdate @new-patient-data)
             :on-change #(reset! new-patient-data (assoc @new-patient-data :birthdate (-> % .-target .-value)) )}]]
   [:div [:label {:for "index"} "Индекс: "]
    [:input {:type "text"
             :value (.-index (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                        ;:index (-> % .-target .-value)
                                  :location
                                  (make-location-json @new-patient-data "index" (-> % .-target .-value))))}]]
   [:div [:label {:for "region"} "Регион: "]
    [:input {:type "text"
             :value (.-region (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "region" (-> % .-target .-value))))}]]
   [:div [:label {:for "city"} "Город: "]
    [:input {:type "text"
             :value (.-city (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "city" (-> % .-target .-value))))}]]
   [:div [:label {:for "street"} "Улица: "]
    [:input {:type "text"
             :value (.-street (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "street" (-> % .-target .-value))))}]]
   [:div [:label {:for "house"} "Дом: "]
    [:input {:type "text"
             :value (.-house (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "house" (-> % .-target .-value))))}]]
   [:div [:label {:for "building"} "Корпус/строение: "]
    [:input {:type "text"
             :value (.-building (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "building" (-> % .-target .-value))))}]]
   [:div [:label {:for "flat"} "Квартира: "]
    [:input {:type "text"
             :value (.-flat (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "flat" (-> % .-target .-value))))}]]
   [:div [:label {:for "policy"} "Полис: "]
    [:input {:type "text"
             :id "policy"
             :name "policy"
             :value (:policy @new-patient-data)
             :on-change #(reset! new-patient-data (assoc @new-patient-data :policy (-> % .-target .-value)) )}]]
   [:div [:label {:for "gender"} "Пол: "]
    [:select {:name "gender"
              :id "gender"
              :on-change #(reset! new-patient-data (assoc @new-patient-data :gender (-> % .-target .-value)) )}
     [:option {:value "f"} "ж"]
     [:option {:value "m"} "м"]]]
   [:div {:class "spacer"}]
   [:div [:input {:type "button"
                  :class "button"
                  :value "Пошёл страус!"
                  :on-click #(POST "/patients/"
                                   {:format :json
                                    :handler handle-patient-added
                                    :params @new-patient-data})}]]])

(defn page-content []
  [:div
   [:div {:id "inputforms"}
    [:div {:class "formwrapper"}
     [:h2 "Поиск"]
     [search-form]]
    [:div {:class "formwrapper" }
     [:h2 "Новый пациент"]
     [new-patient]]]
   [data-table]
   [pager]])

(reagent.dom/render
 [page-content]
 (js/document.getElementById "app"))
(fetch-patients)
