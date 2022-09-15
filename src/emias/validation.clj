(ns emias.validation
  (:require [java-time :as jt]))

(defn check-gender [gender]
  (let [valid (or (= gender "f") (= gender "m"))
        error (if valid nil "Gender must be 'f' or 'm'")]
    [valid error])
  )

(defn check-birthdate [birthdate]
  (try
    (do
      (jt/local-date "yyyy-MM-dd" birthdate)
      [true nil])
    (catch Exception e [false "Wrong birthdate format"])))

(defn validate-patient [patient]
  (let [gender-check (check-gender (patient :gender))
        birthdate-check (check-birthdate (patient :birthdate))]
    (if (and (first gender-check) (first birthdate-check))
      [true nil]
      [false (filter some? (map second [gender-check birthdate-check]))]
    )))  
