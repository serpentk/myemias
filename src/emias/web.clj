(ns emias.web
  (:require [compojure.core :refer [defroutes GET DELETE POST PUT]]
            [compojure.route :refer [resources]]
            [emias.db :refer [filter-patients get-patient create-patient delete-patient update-patient default-limit]]
            [ring.middleware.params :as wp]
            [ring.util.response :refer [resource-response]]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [emias.validation :refer [validate-patient]]))

(defn int-or-default [x default]
  (try (Integer/parseUnsignedInt x) (catch NumberFormatException _ default)))

(defn get-limits [params]
  (let [limit (int-or-default (params "limit") default-limit)
        page-index (- (int-or-default (params "page") 1) 1)]
    {:offset (* page-index limit)
     :limit (+ 1 limit)}))

(defn get-filters [params]
  (let [allowed '(:id :name :surname :patronymic :birthdate :policy :gender :active)]
    (reduce #(if (params (name %2))
               (assoc %1 %2 (params (name %2)))
               %1) {} allowed)))

(def sort-fields (set [:id :surname :birthdate :policy]))

(defn get-sorting [params]
  (if (params "sort")
       (let [sorting (params "sort")
          direction (if (string/starts-with? sorting "-") :desc :asc)
          field-param (if (= direction :desc) (subs sorting 1) sorting)
          field (if (sort-fields (keyword field-param)) (keyword field-param) :id)
          ]
      {field direction}
      )
    {:id :asc})
  )

(defn patients [req]
  (let [params (:params req)
        limits (get-limits params)
        sorting (get-sorting params)
        filters (get-filters params)
        data (filter-patients filters :limits limits :sorting sorting)
        current-page (int-or-default (params "page") 1)
        has-next (= (count data) (:limit limits))
        ]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str {:page {:current current-page :has-next has-next} 
                            :result (into [] (take (- (:limit limits) 1) data))})}))

(defn get-patient-info [id]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str (get-patient id))})

(defn new-patient [req]
  (let [data (json/read-json (slurp (:body req)))
        validation (validate-patient data)]
    (if (first validation)
      {:status 201
       :headers {"Content-Type" "application/json"}
       :body (json/write-str (create-patient data))}
      {:status 400
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {:error (second validation)})})))

(defn edit-patient [id r]
  (let [data (json/read-json (slurp (:body r)))
        validation (validate-patient data)]
    (if (first validation)
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write-str (update-patient (assoc data :id id)))}
      {:status 400
       :headers {"Content-Type" "application/json"}
       :body (json/write-str {:error (second validation)})})))

(defn del-patient [id]
  (do
    (delete-patient id)
    {:status 204
    :headers {"Content-Type" "application/json"}}))

(defroutes app
  (GET "/patients/" req ((wp/wrap-params patients) req))
  (GET "/patients/:id/" [id] (get-patient-info id))
  (DELETE "/patients/:id/" [id] (del-patient id))
  (POST "/patients/" req (new-patient req))
  (PUT "/patients/:id/" [id :as r] (edit-patient id r))
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (resources "/") 
 )
