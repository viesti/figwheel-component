(ns figwheel-component.core
  (:require [com.stuartsierra.component :as component]
            [figwheel-sidecar.core :as core]
            [figwheel-sidecar.repl-api :as repl]))

(defrecord Figwheel [build-id]
  component/Lifecycle
  (start [this]
    (if (:started this)
      this
      (let [project (->> "project.clj" slurp read-string (drop 3) (apply hash-map))
            figwheel-options (-> project :figwheel)
            build (-> project :cljsbuild :builds build-id)
            {:keys [source-paths compiler]} build]
        (repl/start-figwheel! {:figwheel-options figwheel-options
                               :build-ids [(name build-id)]
                               :all-builds [{:id (name build-id)
                                             :source-paths source-paths
                                             :compiler compiler}]})
        (repl/start-autobuild)
        (assoc this :started true))))
  (stop [this]
    (assoc this :started nil)))

(defn figwheel-component [build-id]
  (->Figwheel build-id))
