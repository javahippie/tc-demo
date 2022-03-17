(ns tc-demo.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[tc-demo started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[tc-demo has shut down successfully]=-"))
   :middleware identity})
