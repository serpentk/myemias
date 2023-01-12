(ns emias.config)
(def db-config
  (atom {:dbtype "postgresql"
         :dbname "emias"
         :host "localhost"
         :user "emias"
         :password "123456"
         :stringtype "unspecified"}))
