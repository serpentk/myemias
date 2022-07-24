(ns user
  (:require [reloaded.repl :refer [system init start stop go reset reset-all]]
            [emias.core]))

(reloaded.repl/set-init! #'emias.core/create-system)
