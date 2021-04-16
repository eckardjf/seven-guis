(ns eckardjf.seven-guis.app
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [eckardjf.seven-guis.guis.counter :refer [counter]]
            [eckardjf.seven-guis.guis.temp-converter :refer [temp-converter]]
            [eckardjf.seven-guis.guis.flight-booker :refer [flight-booker]]
            [eckardjf.seven-guis.guis.timer :refer [timer]]
            [eckardjf.seven-guis.guis.crud :refer [crud]]))

(defn gradient-panel [{:keys [title from-color to-color]} children]
  [:div.mx-4
   [:h2.my-2.font-bold title]
   [:div.flex.justify-center.rounded-lg.py-10.px-16.bg-gradient-to-r {:class [from-color to-color]}
    [:div.p-10.shadow-lg.rounded-lg.bg-white.text-gray-800 children]]])

(def app-db (r/atom {:count   0
                     :temp    {:celsius "0" :fahrenheit "32"}
                     :flight  {:flight-type :one-way}
                     :timer   {:elapsed 0 :duration 6}
                     :user-db {}}))

(defn app []
  [:div.w-full.grid.grid-cols-1.gap-12.my-12.mx-auto {:class "lg:w-1/2"}
   [gradient-panel {:title "Counter" :from-color "from-pink-500" :to-color "to-rose-500"}
    [counter (r/cursor app-db [:count])]]
   [gradient-panel {:title "Temperature Converter" :from-color "from-yellow-500" :to-color "to-orange-500"}
    [temp-converter (r/cursor app-db [:temp])]]
   [gradient-panel {:title "Flight Booker" :from-color "from-yellow-300" :to-color "to-yellow-500"}
    [flight-booker (r/cursor app-db [:flight])]]
   [gradient-panel {:title "Timer" :from-color "from-green-500" :to-color "to-teal-500"}
    [timer (r/cursor app-db [:timer])]]
   [gradient-panel {:title "CRUD" :from-color "from-lightBlue-500" :to-color "to-blue-500"}
    [crud (r/cursor app-db [:user-db])]]])

;; start is called by init and after code reloading finishes
(defn ^:dev/after-load start []
  (rd/render [app] (.getElementById js/document "app")))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced release builds
  (start))