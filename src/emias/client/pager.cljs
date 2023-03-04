(ns emias.client.pager
  (:require [reagent.core]
            [ajax.core :refer [GET]]
            [emias.client.data :refer [page search-params reset-patients]]))

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
   [:span "Страница: "]
   (if (> (:current @page) 1) [prev-page])
   [:span (:current @page)]
   (if (:has-next @page) [next-page])])
