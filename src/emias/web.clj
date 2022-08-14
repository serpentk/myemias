(ns emias.web
  (:require [compojure.core :refer [defroutes GET DELETE POST PUT]]
            [compojure.route :refer [resources]]
            [emias.db :refer [filter-patients get-patient create-patient delete-patient update-patient default-limit]]
            [ring.middleware.params :as wp]
            [clojure.string :as string]
            [clojure.data.json :as json]))

(defn int-or-default [x default]
  (try (Integer/parseUnsignedInt x) (catch NumberFormatException _ default)))

(defn get-limits [params]
  (let [limit (int-or-default (params "limit") default-limit)
        page-index (- (int-or-default (params "page") 1) 1)]
    {:offset (* page-index limit)
     :limit limit}))

(defn get-filters [params]
  (let [allowed '(:id :name :surname :patronymic :birthdate :policy)]
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
  (let [params (:params req)]
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str (filter-patients (get-filters params)
                                            :limits (get-limits params)
                                            :sorting (get-sorting params)))}))

(defn check-gender [gender]
  (let [valid (or (= gender "f") (= gender "m"))
        error (if valid "" "Gender must be 'f' or 'm'")]
    [valid error])
  )

(defn validate-patient [patient]
  (check-gender (patient :gender))
  )

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

(defn index [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello from Compojure!"})

(defroutes app
  (GET "/patients/" req ((wp/wrap-params patients) req))
  (GET "/patients/:id/" [id] (get-patient-info id))
  (DELETE "/patients/:id/" [id] (del-patient id))
  (POST "/patients/" req (new-patient req))
  (PUT "/patients/:id/" [id :as r] (edit-patient id r))
  (GET "/" [] index)
  (resources "/") 
 )
