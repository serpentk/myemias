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

(defn prepare-patient [p]
  (assoc p :processing false :birthdate (subs (:birthdate p) 0 10)))

(defn prepare-patients-dict [datalist]
  (reduce #(assoc %1 (:id %2) (prepare-patient %2)) {} datalist))

(defn reset-patient [patient-data]
  (reset! patients (assoc @patients (:id patient-data) (prepare-patient patient-data)))
  )

(defn reset-patients [data]
  (do
    (reset! patients (prepare-patients-dict (:result data)))
    (reset! page (:page data))))

(defn fetch-patient [id]
  #(GET (str "/patients/" id "/")
             {:response-format :json
              :handler reset-patient
              :keywords? :true
              }))

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
    [:div "????????????: " [:input {:type "text"
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
    [:div "????????????: " [:input {:type "text"
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
    [:div "??????????: " [:input {:type "text"
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
    [:div "??????????: " [:input {:type "text"
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
    [:div "??????: " [:input {:type "text"
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
    [:div "????????????: " [:input {:type "text"
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
    [:div "????????????????: " [:input {:type "text"
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
         [:option {:value "f"} "??"]
         [:option {:value "m"} "??"]]]
   [:td (if (:active p) "??????????????" "??????????????????")]
   [:td
    [:input {:type "button"
             :class "button"
             :value "?????????? ????????????!"
             :on-click #(PUT (str "/patients/" (:id p) "/")
                             {:format :json
                              :handler (handle-edited p)
                              :params (select-keys (get @patients (:id p)) edit-fields) })}]
    [:input {:type "button"
             :class "button"
             :value "????????????"
             :on-click #(GET (str "/patients/" (:id p) "/")
                             {:format :json
                              :handler (fetch-patient (:id p))})}]]])

(defn edit-patient-button [p]
  [:input {:type "button"
           :class "button"
           :value "??????????????????"
           :on-click #(reset! patients (assoc @patients (:id p) (assoc p :processing true)))}])

;; (defn delete-patient-button [p]
;;   [:input {:type "button"
;;            :value "???????? ???? ??????????"
;;            :on-click #(DELETE (str "/patients/" (:id p) "/")
;;                               {:handler fetch-patients})}])

(defn delete-patient-button [p]
  [:input {:type "button"
           :class "button"
           :value "???????? ???? ??????????"
           :on-click #(PUT (str "/patients/" (:id p) "/")
                           {:format :json
                            :handler (handle-deleted p)
                            :params {:active false}})}])

(defn restore-patient-button [p]
  [:input {:type "button"
           :class "button"
           :value "????????????????????"
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
      [:div "????????????: " (.-index (parse-patient-location p))]
      [:div "????????????: " (.-region (parse-patient-location p))]
      [:div "??????. ??????????: " (.-city (parse-patient-location p))]
      [:div "??????????: " (.-street (parse-patient-location p))]
      [:div "??????: " (.-house (parse-patient-location p))]
      [:div "????????????/????????????????: " (.-building (parse-patient-location p))]
      [:div "????????????????: " (.-flat (parse-patient-location p))]]
     [:td (:policy p)]
     [:td (if (= (:gender p) "f") "??" "??")]
     [:td (if (:active p) "??????????????" "??????????????????")]
     [:td (edit-patient-button p)
      (if (:active p) (delete-patient-button p) (restore-patient-button p))]]))

(defn data-table []
  [:table
   (into
    [:tbody
     [:tr
      [:th "ID"]
      [:th "??????"]
      [:th "????????????????"]
      [:th "??????????????"]
      [:th "???????? ????????????????"]
      [:th "??????????"]
      [:th "?????????? ????????????"]
      [:th "??????"]
      [:th "????????????"]
      [:th "?????????????? ??????-????????????"]]]
    (map patient-row (vals @patients)))])

(defn change-page [page-num]
  #(GET "/patients/"
        {:response-format :json
         :handler reset-patients
         :keywords? :true
         :params (into {:page page-num}
                       (filter (fn [p] (not (= (val p) "")))
                               @search-params))}))

(defn next-page []
  [:input {:type "button"
           :class "button"
           :value ">"
           :on-click (change-page (+ 1 (:current @page)))}])

(defn prev-page []
  [:input {:type "button"
           :class "button"
           :value "<"
           :on-click (change-page (- (:current @page) 1))}])

(defn pager []
  [:div {:class "pager"}
   [:span "????????????????: "]
   (if (> (:current @page) 1) [prev-page])
   [:span (:current @page)]
   (if (:has-next @page) [next-page])])

(defn handle-patient-added []
  (do
    (reset! new-patient-data {:gender "f"})
    (fetch-patients)))

(defn search-form []
  [:form {:id "search" :on-submit #(.preventDefault %)}
   [:div {:class "textwrapper"}
    [:label {:for "name"} "??????: "]
    [:input {:type "text"
             :id "name"
             :name "name"
             :value (:name @search-params)
             :on-change #(reset! search-params (assoc @search-params :name (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "patronymic"} "????????????????: "]
    [:input {:type "text"
             :id "patronymic"
             :name "patronymic"
             :value (:patronymic @search-params)
             :on-change #(reset! search-params (assoc @search-params :patronymic (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "surname"} "??????????????: "]
    [:input {:type "text"
             :id "surname"
             :name "surname"
             :value (:surname @search-params)
             :on-change #(reset! search-params (assoc @search-params :surname (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "birthdate"} "???????? ????????????????: "]
    [:input {:type "text"
             :id "birthdate"
             :name "birthdate"
             :value (:birthdate @search-params)
             :on-change #(reset! search-params (assoc @search-params :birthdate (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "index"} "????????????: "]
    [:input {:type "text"
             :id "index"
             :name "index"
             :value (:index @search-params)
             :on-change #(reset! search-params (assoc @search-params :index (-> % .-target .-value)))}]]
   [:div {:class "textwrapper"}
    [:label {:for "region"} "????????????: "]
    [:input {:type "text"
             :id "region"
             :name "region"
             :value (:region @search-params)
             :on-change #(reset! search-params (assoc @search-params :region (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "city"} "??????. ??????????: "]
    [:input {:type "text"
             :id "city"
             :name "city"
             :value (:city @search-params)
             :on-change #(reset! search-params (assoc @search-params :city (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "street"} "??????????: "]
    [:input {:type "text"
             :id "street"
             :name "street"
             :value (:street @search-params)
             :on-change #(reset! search-params (assoc @search-params :street (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "house"} "??????: "]
    [:input {:type "text"
             :id "house"
             :name "house"
             :value (:house @search-params)
             :on-change #(reset! search-params (assoc @search-params :house (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "building"} "????????????/????????????????: "]
    [:input {:type "text"
             :id "building"
             :name "building"
             :value (:building @search-params)
             :on-change #(reset! search-params (assoc @search-params :building (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "flat"} "????????????????: "]
    [:input {:type "text"
             :id "flat"
             :name "flat"
             :value (:flat @search-params)
             :on-change #(reset! search-params (assoc @search-params :flat (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "policy"} "??????????: "]
    [:input {:type "text"
             :id "policy"
             :name "policy"
             :value (:policy @search-params)
             :on-change #(reset! search-params (assoc @search-params :policy (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "gender"} "??????: "]
    [:select {:name "gender"
              :id "gender"
              :value (:gender @search-params)
              :on-change #(reset! search-params (assoc @search-params :gender (-> % .-target .-value)) )}
     [:option {:value ""} "???? ??????????"]
     [:option {:value "f"} "??"]
     [:option {:value "m"} "??"]]]
   [:div {:class "spacer"}]
   [:div [:input {:type "button"
                  :class "button"
                  :value "?????????? ????????????!"
                  :on-click fetch-patients}]]])

(defn new-patient []
  [:form {:on-submit #(.preventDefault %)}
   [:div {:class "textwrapper"}
    [:label {:for "name"} "??????: "]
    [:input {:type "text"
             :id "name"
             :name "name"
             :value (:name @new-patient-data)
             :on-change #(reset! new-patient-data (assoc @new-patient-data :name (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "patronymic"} "????????????????: "]
    [:input {:type "text"
             :id "patronymic"
             :name "patronymic"
             :value (:patronymic @new-patient-data)
             :on-change #(reset! new-patient-data (assoc @new-patient-data :patronymic (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "surname"} "??????????????: "]
    [:input {:type "text"
             :id "surname"
             :name "surname"
             :value (:surname @new-patient-data)
             :on-change #(reset! new-patient-data (assoc @new-patient-data :surname (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "birthdate"} "???????? ????????????????: "]
    [:input {:type "text"
             :id "birthdate"
             :name "birthdate"
             :value (:birthdate @new-patient-data)
             :on-change #(reset! new-patient-data (assoc @new-patient-data :birthdate (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "index"} "????????????: "]
    [:input {:type "text"
             :value (.-index (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                        ;:index (-> % .-target .-value)
                                  :location
                                  (make-location-json @new-patient-data "index" (-> % .-target .-value))))}]]
   [:div {:class "textwrapper"}
    [:label {:for "region"} "????????????: "]
    [:input {:type "text"
             :value (.-region (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "region" (-> % .-target .-value))))}]]
   [:div {:class "textwrapper"}
    [:label {:for "city"} "??????????: "]
    [:input {:type "text"
             :value (.-city (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "city" (-> % .-target .-value))))}]]
   [:div {:class "textwrapper"}
    [:label {:for "street"} "??????????: "]
    [:input {:type "text"
             :value (.-street (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "street" (-> % .-target .-value))))}]]
   [:div {:class "textwrapper"}
    [:label {:for "house"} "??????: "]
    [:input {:type "text"
             :value (.-house (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "house" (-> % .-target .-value))))}]]
   [:div {:class "textwrapper"}
    [:label {:for "building"} "????????????/????????????????: "]
    [:input {:type "text"
             :value (.-building (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "building" (-> % .-target .-value))))}]]
   [:div {:class "textwrapper"}
    [:label {:for "flat"} "????????????????: "]
    [:input {:type "text"
             :value (.-flat (parse-patient-location @new-patient-data))
             :on-change #(reset! new-patient-data
                                 (assoc
                                  @new-patient-data
                                  :location
                                  (make-location-json @new-patient-data "flat" (-> % .-target .-value))))}]]
   [:div {:class "textwrapper"}
    [:label {:for "policy"} "??????????: "]
    [:input {:type "text"
             :id "policy"
             :name "policy"
             :value (:policy @new-patient-data)
             :on-change #(reset! new-patient-data (assoc @new-patient-data :policy (-> % .-target .-value)) )}]]
   [:div {:class "textwrapper"}
    [:label {:for "gender"} "??????: "]
    [:select {:name "gender"
              :id "gender"
              :on-change #(reset! new-patient-data (assoc @new-patient-data :gender (-> % .-target .-value)) )}
     [:option {:value "f"} "??"]
     [:option {:value "m"} "??"]]]
   [:div {:class "spacer"}]
   [:div [:input {:type "button"
                  :class "button"
                  :value "?????????? ????????????!"
                  :on-click #(POST "/patients/"
                                   {:format :json
                                    :handler handle-patient-added
                                    :params @new-patient-data})}]]])

(defn page-content []
  [:div
   [:div {:id "inputforms"}
    [:div {:class "formwrapper"}
     [:h2 "??????????"]
     [search-form]]
    [:div {:class "formwrapper" }
     [:h2 "?????????? ??????????????"]
     [new-patient]]]
   [:div {:id "tablecontainer"}
    [data-table]]
   [pager]])

(reagent.dom/render
 [page-content]
 (js/document.getElementById "app"))
(fetch-patients)
