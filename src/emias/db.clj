(ns emias.db
  (:require
    [clojure.java.jdbc :as jdbc]))

(def pg-db
  {:dbtype "postgresql"
   :dbname "emias"
   :host "localhost"
   :user "emias"
   :password "123456"
   :stringtype "unspecified"})

(defn create-patient [patient]
  (jdbc/insert! pg-db :patients patient)
   ;; {:name "Vasily" :surname "Pupkin" :gender "m" :birthdate "1970-01-01" :policy "123456" :address "Gadyukino 20"})
)

(defn delete-patient [id]
  (jdbc/delete! pg-db :patients ["id=?" id]))

(defn update-patient [patient]
  (jdbc/update! pg-db :patients patient ["id=?" (:id patient)]))

(defn get-patient [id]
  (jdbc/query pg-db ["select * from patients where id=?" id]))

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
  (jdbc/query pg-db
              (conj
               (into [(format "select * from patients %s order by %s offset ? limit ?"
                              (get-filter-string filters)
                              (get-sorting sorting))]
                     (vals filters))
              (or (:offset limits) 0)
              (or (:limit limits) default-limit))))
