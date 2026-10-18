(ns sonouchi.app
  "The shell: holds the one atom, renders the view into the page with
  Replicant, and renders again whenever the state changes."
  (:require [replicant.dom :as r]
            [sonouchi.core.view :as view]))

(defonce ^:private store (atom {:state/facts []}))

(defn- render!
  [state]
  (r/render (js/document.getElementById "app") (view/root state)))

(defn ^:dev/after-load reload
  "Renders the current state again. shadow-cljs calls it after each hot
  reload."
  []
  (render! @store))

(defn init
  "Starts the app. index.html loads it through shadow-cljs.edn."
  []
  (add-watch store :render (fn [_ _ _ state] (render! state)))
  (reload))
