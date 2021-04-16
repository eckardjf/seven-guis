(ns eckardjf.seven-guis.app
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [eckardjf.seven-guis.guis.counter :refer [counter]]))

(defn gradient-panel [{:keys [title from-color to-color]} children]
  [:div.mx-4
   [:h2.my-2.font-bold title]
   [:div.flex.justify-center.rounded-lg.py-10.px-16.bg-gradient-to-r {:class [from-color to-color]}
    [:div.p-10.shadow-lg.rounded-lg.bg-white.text-gray-800 children]]])

(def app-db (r/atom {:count 0}))

(defn app []
  [:div.w-full.grid.grid-cols-1.gap-12.my-12.mx-auto {:class "lg:w-1/2"}
   [gradient-panel {:title "Counter" :from-color "from-pink-500" :to-color "to-rose-500"}
    [counter (r/cursor app-db [:count])]]])

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (rd/render [app] (.getElementById js/document "app")))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))