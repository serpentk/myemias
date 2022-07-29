(ns emias.web
  (:require [compojure.core :refer [defroutes GET DELETE POST PUT]]
            [compojure.route :refer [resources]]
            [emias.db :refer [filter-patients get-patient create-patient delete-patient update-patient]]
            [clojure.data.json :as json]))

(defn patients [page limit]
  {:status  200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str (filter-patients {}))})

(defn get-patient-info [id]
  {:status 200
   :headers {"Content-Type" "application/json"}
   :body (json/write-str (get-patient id))})

(defn new-patient [req]
  (let [data (json/read-json (slurp (:body req)))]
    {:status 201
     :headers {"Content-Type" "application/json"}
     :body (json/write-str (create-patient data))}))

(defn edit-patient [id r]
  (let [data (json/read-json (slurp (:body r)))]
   {:status 200
    :headers {"Content-Type" "application/json"}
    :body (json/write-str (update-patient (assoc data :id id)))}))

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
  (GET "/" [] index)
  (resources "/")
  (GET "/patients/" [page limit] (patients page limit))
  (GET "/patients/:id/" [id] (get-patient-info id))
  (DELETE "/patients/:id/" [id] (del-patient id))
  (POST "/patients/" req (new-patient req))
  (PUT "/patients/:id/" [id :as r] (edit-patient id r)))
