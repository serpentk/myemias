(ns emias.validation
  (:require [java-time :as jt]))

(defn check-gender [gender]
  (let [valid (or (= gender "f") (= gender "m"))
        error (if valid nil "Gender must be 'f' or 'm'")]
    [valid error])
  )

(defn check-birthdate [birthdate]
  (try
    (let [parsed (jt/local-date "yyyy-MM-dd" birthdate)]
      (if (= (str parsed) birthdate) [true nil] [false "Wrong day of month"]))
    (catch Exception e [false "Wrong birthdate format"])))

(defn validate-patient [patient]
  (let [gender-check (check-gender (patient :gender))
        birthdate-check (check-birthdate (patient :birthdate))]
    (if (and (first gender-check)
             (first birthdate-check)
             (some? (patient :policy))
             (some? (patient :name))
             (some? (patient :surname)))
      [true nil]
      [false (filter some? (map second [gender-check birthdate-check]))]
    )))

(defn validate-patient-edit [patient]
  (let [gender (patient :gender)
        birthdate (patient :birthdate)
        gender-check (if (some? gender) (check-gender gender) [true nil])
        birthdate-check (if (some? birthdate) (check-birthdate birthdate) [true nil])]
    (if (and (first gender-check) (first birthdate-check))
      [true nil]
      [false (filter some? (map second [gender-check birthdate-check]))]
    )))  
