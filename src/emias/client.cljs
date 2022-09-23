(ns emias.client
  (:require [reagent.core]
            [reagent.dom]
            [ajax.core :refer [GET POST DELETE]]))

(def patients (reagent.core/atom []))

(def new-patient-data (reagent.core/atom {:gender "f"}))

(def search-params (reagent.core/atom {}))

(def page (reagent.core/atom {:current 1 :has-next false}))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn reset-patients [data]
  (do
    (reset! patients (:result data))
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

(defn delete-patient-button [p]
  [:input {:type "button"
           :value "Этот не нужен"
           :on-click #(DELETE (str "/patients/" (:id p) "/")
                              {:handler fetch-patients})}
           ]
  )
(defn patient-row [p]
  [:tr [:td (str (:id p))] [:td (:name p)] [:td (:surname p)] [:td (delete-patient-button p)]])
  


(defn data-table []
  (into
   [:table [:tr [:th "ID"] [:th "Имя"] [:th "Фамилия"]]]
   (map patient-row @patients)))

(defn page-content []
  [:div
  [:h4 "Поиск"]
   [search-form]
  [:h4 "Новый пациент"]
   [new-patient]
   [data-table]
   [pager]
   ])

(defn change-page [page-num]
  #(GET "/patients/"
        {:response-format :json
         :handler reset-patients
         :keywords? :true
         :params (into {:page page-num}
                       (filter (fn [p] (not (= (val p) "")))
                               @search-params))})
  )

(defn next-page []
  [:span {:on-click (change-page (+ 1 (:current @page)))}        
          " >"])

(defn prev-page []
  [:span {:on-click (change-page (- (:current @page) 1))}        
          "< "])

(defn pager []
  [:div
   [:span "Страница: "]
   (if (> (:current @page) 1) [prev-page])
   [:span (:current @page)]
   (if (:has-next @page) [next-page])
   ]
  )

(defn handle-patient-added []
  (do
    (reset! new-patient-data {:gender "f"})
    (fetch-patients)))

(defn search-form []
    [:form {:on-submit #(.preventDefault %)}
   [:label {:for "name"} "Имя: "]
   [:input {:type "text"
            :id "name"
            :name "name"
            :value (:name @search-params)
            :on-change #(reset! search-params (assoc @search-params :name (-> % .-target .-value)) )}]
   [:label {:for "patronymic"} "Отчество: "]
   [:input {:type "text"
            :id "patronymic"
            :name "patronymic"
            :value (:patronymic @search-params)
            :on-change #(reset! search-params (assoc @search-params :patronymic (-> % .-target .-value)) )}]
   [:label {:for "surname"} "Фамилия: "]
   [:input {:type "text"
            :id "surname"
            :name "surname"
            :value (:surname @search-params)
            :on-change #(reset! search-params (assoc @search-params :surname (-> % .-target .-value)) )}]
   [:label {:for "birthdate"} "Дата рождения: "]
   [:input {:type "text"
            :id "birthdate"
            :name "birthdate"
            :value (:birthdate @search-params)
            :on-change #(reset! search-params (assoc @search-params :birthdate (-> % .-target .-value)) )}]
   [:label {:for "address"} "Адрес: "]
   [:input {:type "text"
            :id "address"
            :name "address"
            :value (:address @search-params)
            :on-change #(reset! search-params (@assoc @search-params :address (-> % .-target .-value)) )}]
   [:label {:for "policy"} "Полис: "]
   [:input {:type "text"
            :id "policy"
            :name "policy"
            :value (:policy @search-params)
            :on-change #(reset! search-params (assoc @search-params :policy (-> % .-target .-value)) )}]
   [:label {:for "gender"} "Пол: "]
   [:select {:name "gender"
             :id "gender"
             :on-change #(reset! search-params (assoc @search-params :gender (-> % .-target .-value)) )}
    [:option {:value "" :selected (= "" (:gender @search-params))} "Не важно"]
    [:option {:value "f" :selected (= "f" (:gender @search-params))} "ж"]
    [:option {:value "m":selected (= "m" (:gender @search-params)) } "м"]] 
   [:input {:type "button"
            :value "Пошёл страус!"
            :on-click fetch-patients}]]
  )

(defn new-patient []
  [:form {:on-submit #(.preventDefault %)}
   [:label {:for "name"} "Имя: "]
   [:input {:type "text"
            :id "name"
            :name "name"
            :value (:name @new-patient-data)
            :on-change #(reset! new-patient-data (assoc @new-patient-data :name (-> % .-target .-value)) )}]
   [:label {:for "patronymic"} "Отчество: "]
   [:input {:type "text"
            :id "patronymic"
            :name "patronymic"
            :value (:patronymic @new-patient-data)
            :on-change #(reset! new-patient-data (assoc @new-patient-data :patronymic (-> % .-target .-value)) )}]
   [:label {:for "surname"} "Фамилия: "]
   [:input {:type "text"
            :id "surname"
            :name "surname"
            :value (:surname @new-patient-data)
            :on-change #(reset! new-patient-data (assoc @new-patient-data :surname (-> % .-target .-value)) )}]
   [:label {:for "birthdate"} "Дата рождения: "]
   [:input {:type "text"
            :id "birthdate"
            :name "birthdate"
            :value (:birthdate @new-patient-data)
            :on-change #(reset! new-patient-data (assoc @new-patient-data :birthdate (-> % .-target .-value)) )}]
   [:label {:for "address"} "Адрес: "]
   [:input {:type "text"
            :id "address"
            :name "address"
            :value (:address @new-patient-data)
            :on-change #(reset! new-patient-data (assoc @new-patient-data :address (-> % .-target .-value)) )}]
   [:label {:for "policy"} "Полис: "]
   [:input {:type "text"
            :id "policy"
            :name "policy"
            :value (:policy @new-patient-data)
            :on-change #(reset! new-patient-data (assoc @new-patient-data :policy (-> % .-target .-value)) )}]
   [:label {:for "gender"} "Пол: "]
   [:select {:name "gender"
             :id "gender"
             :on-change #(reset! new-patient-data (assoc @new-patient-data :gender (-> % .-target .-value)) )}
    [:option {:value "f"} "ж"]
    [:option {:value "m"} "м"]] 
   [:input {:type "button"
            :value "Пошёл страус!"
            :on-click #(POST "/patients/"
                             {:format :json
                              :handler handle-patient-added
                              :params @new-patient-data})}]])

(reagent.dom/render
 [page-content]
 (js/document.getElementById "app"))
(fetch-patients)
