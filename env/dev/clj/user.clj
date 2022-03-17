(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require
    [tc-demo.config :refer [env]]
    [clojure.pprint]
    [clojure.spec.alpha :as s]
    [expound.alpha :as expound]
    [mount.core :as mount]
    [tc-demo.core :refer [start-app]]
    [tc-demo.db.core]
    [conman.core :as conman]
    [luminus-migrations.core :as migrations]
    [clj-test-containers.core :as tc])
  (:import (org.testcontainers.containers PostgreSQLContainer)))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(add-tap (bound-fn* clojure.pprint/pprint))

(defn start
  "Starts application. You'll usually want to run this on startup."
  []
  (let [container (tc/init {:container     (PostgreSQLContainer. "postgres:14.1")
                             :exposed-ports [5432]})]
    (-> (mount/find-all-states)
        (mount/except [#'tc-demo.core/repl-server])
        (mount/swap-states {#'tc-demo.db.core/*db* {:start
                                                    #(let [c (:container (tc/start! container))]
                                                       (conman/connect! {:jdbc-url (.getJdbcUrl c)
                                                                         :user     (.getUsername c)
                                                                         :password (.getPassword c)}))

                                                    :stop #(do
                                                             (conman/disconnect! #'tc-demo.db.core/*db*)
                                                             (tc/stop! container))}})
        (mount/start))))

(comment
  #'tc-demo.db.core/*db*)

(defn stop
  "Stops application."
  []
  (mount/stop-except #'tc-demo.core/repl-server))

(defn restart
  "Restarts application."
  []
  (stop)
  (start))

(defn restart-db
  "Restarts database."
  []
  (mount/stop #'tc-demo.db.core/*db*)
  (mount/start #'tc-demo.db.core/*db*)
  (binding [*ns* (the-ns 'tc-demo.db.core)]
    (conman/bind-connection tc-demo.db.core/*db* "sql/queries.sql")))

(defn reset-db
  "Resets database."
  []
  (migrations/migrate ["reset"] {:db {:datasource tc-demo.db.core/*db*}}))

(defn migrate
  "Migrates database up for all outstanding migrations."
  []
  (migrations/migrate ["migrate"] {:db {:datasource tc-demo.db.core/*db*}}))

(defn rollback
  "Rollback latest database migration."
  []
  (migrations/migrate ["rollback"] {:db {:datasource tc-demo.db.core/*db*}}))

(defn create-migration
  "Create a new up and down migration file with a generated timestamp and `name`."
  [name]
  (migrations/create name {:db {:datasource tc-demo.db.core/*db*}}))


