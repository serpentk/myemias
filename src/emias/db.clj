(ns emias.db
  (:require
   [clojure.java.jdbc :as jdbc]
   [emias.config :refer [db-config]]))

(def pg-db @db-config)

(defn prepare-patient [p]
  (let [location (:location p)]
    (assoc p
         :birthdate (.format (new java.text.SimpleDateFormat "yyyy-MM-dd") (:birthdate p))
         :location  (if (nil? location)
                      "{}"
                      (.toString location)))))

(defn create-patient [patient]
  (prepare-patient (first (jdbc/insert! pg-db :patients patient)))
)

(defn delete-patient [id]
  (jdbc/delete! pg-db :patients ["id=?" id]))

(defn update-patient [patient]
  (do
    (jdbc/update! pg-db :patients patient ["id=?" (:id patient)])
    patient))

(defn get-patient [id]
  (prepare-patient (first (jdbc/query pg-db ["select * from patients where id=?" id]))))

(defn get-filter-string [filters]
  (reduce #(format "%s and %s=?" %1 (name %2)) "where true" (keys filters))
  )

(defn get-sorting [sorting]
  (clojure.string/join
   ", "
   (map
    #(format "%s %s" (name (first %)) (name (second %)))
    (seq sorting))))

(def default-limit 10)

(defn filter-patients [filters &
                       {:keys [limits sorting]
                        :or {limits {:offset 0 :limit 10} sorting {:id :asc}}}]
  (map prepare-patient (jdbc/query pg-db
              (conj
               (into [(format "select * from patients %s order by %s offset ? limit ?"
                              (get-filter-string filters)
                              (get-sorting sorting))]
                     (vals filters))
              (or (:offset limits) 0)
              (or (:limit limits) default-limit)))))
