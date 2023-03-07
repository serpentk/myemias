(ns emias.client.data
  (:require [reagent.core]
            [ajax.core :refer [GET]]))

(def new-patient-data (reagent.core/atom {:gender "f" :active true}))

(def page (reagent.core/atom {:current 1 :has-next false}))

(def info (reagent.core/atom "Данные загружаются"))

(def patients (reagent.core/atom []))

(defn prepare-patient [p]
  (assoc p :processing false :birthdate (subs (:birthdate p) 0 10)))

(defn prepare-patients-dict [datalist]
  (reduce #(assoc %1 (:id %2) (prepare-patient %2)) {} datalist))

(defn reset-patients [data]
  (do
    (reset! info "Данные загружены")
    (reset! patients (prepare-patients-dict (:result data)))
    (reset! page (:page data))))

(def search-params (reagent.core/atom {:gender ""}))

(defn handle-fetch-error [r]
  (if (= 500 (:status r))
    (reset! info "Что-то пошло не так")
    (reset! info "Всё вообще сломано...")))

(defn fetch-patients []
  (GET "/patients/"
             {:response-format :json
              :handler reset-patients
              :error-handler handle-fetch-error
              :keywords? :true
              :params (into {:page (:current @page)}
                            (filter (fn [p] (not (= (val p) "")))
                                    @search-params))}))
