(ns emias.core
  (:require [org.httpkit.server :refer [run-server]]
            [com.stuartsierra.component :as component]
            [emias.web :refer [app]]
           )
  (:gen-class))

(defn start-server [handler port]
  (run-server handler {:port port}))

(defn stop-server [server] (when server (server)))

(defrecord Emias []
  component/Lifecycle
  (start [this]
    assoc this :server (start-server #'app 8080))
  (stop [this]
    (stop-server (:server this))
    (dissoc this :server)))

(defn create-system []
  (Emias.))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (.start (create-system)))
