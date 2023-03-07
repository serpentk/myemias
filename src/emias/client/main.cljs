(ns emias.client
  (:require [reagent.dom]
            [emias.client.data :refer [fetch-patients info]]
            [emias.client.patients :refer [data-table]]
            [emias.client.create :refer [new-patient]]
            [emias.client.search :refer [search-form]]
            [emias.client.pager :refer [pager]]))

(defn page-content []
  [:div
   [:div {:id "inputforms"}
    [:div {:class "formwrapper"}
     [:h2 "Поиск"]
     [search-form]]
    [:div {:class "formwrapper" }
     [:h2 "Новый пациент"]
     [new-patient]]]
   [:div {:class "info"} @info]
   [:div {:id "tablecontainer"}
    [data-table]]
   [pager]])

(reagent.dom/render
 [page-content]
 (js/document.getElementById "app"))
(fetch-patients)
