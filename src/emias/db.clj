(ns emias.db
  (:require
   [clojure.java.jdbc :as jdbc]
   [emias.config :refer [db-config]]))

(defn create-gender-type []
  (jdbc/db-do-commands @db-config
                       "DO $$ BEGIN
                        create type gender as enum ('f', 'm');
                        EXCEPTION
                        WHEN duplicate_object THEN null;
                        END $$;"))

(defn create-patients-table []
  (jdbc/db-do-commands @db-config
               "create table if not exists patients
               (id serial PRIMARY KEY,
                name text not null,
                patronymic text,
                surname text not null,
                gender gender not null,
                birthdate date not null,
                policy text unique not null,
                active boolean not null,
                location json)"))

(defn prepare-patient [p]
  (let [location (:location p)]
    (assoc p
         :birthdate (.format (new java.text.SimpleDateFormat "yyyy-MM-dd") (:birthdate p))
         :location  (if (nil? location)
                      "{}"
                      (.toString location)))))

(defn create-patient [patient]
  (prepare-patient (first (jdbc/insert! @db-config :patients patient))))

(defn delete-patient [id]
  (jdbc/delete! @db-config :patients ["id=?" id]))

(defn update-patient [patient]
  (do
    (jdbc/update! @db-config  :patients patient ["id=?" (:id patient)])
    patient))

(defn get-patient [id]
  (let [patient (first (jdbc/query @db-config ["select * from patients where id=?" id]))]
    (if (some? patient)
      (prepare-patient patient) nil)))

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
  (map prepare-patient (jdbc/query @db-config
              (conj
               (into [(format "select * from patients %s order by %s offset ? limit ?"
                              (get-filter-string filters)
                              (get-sorting sorting))]
                     (vals filters))
              (or (:offset limits) 0)
              (or (:limit limits) default-limit)))))
