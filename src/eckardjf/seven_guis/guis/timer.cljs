(ns eckardjf.seven-guis.guis.timer
  (:require [reagent.core :as r]))

(defn timer [state]
  (let [elapsed (r/cursor state [:elapsed])
        duration (r/cursor state [:duration])
        interval (r/atom nil)
        start-timer! (fn []
                       (reset! interval (js/setInterval #(swap! elapsed + 0.02) 20)))
        stop-timer! (fn []
                      (js/clearInterval @interval)
                      (reset! interval nil))]
    (r/create-class
      {:component-name
       "timer"

       :component-did-mount
       start-timer!

       :reagent-render
       (fn []
         [:div.flex.flex-col.space-y-4.w-48.text-green-900
          [:div
           [:p.mb-1 [:span.font-semibold "Elapsed time: "] (.toFixed @elapsed 1)]
           [:div.h-4.rounded.overflow-hidden.bg-green-100
            [:div.w-full.h-full.origin-left.bg-green-400
             {:style {:transform (str "scaleX(" (/ @elapsed @duration) ")")}}]]]
          [:div
           [:p [:span.font-semibold "Duration: "] @duration]
           [:input.w-full.h-1.appearance-none.outline-none.rounded.slider-thumb.bg-green-400
            {:type      "range"
             :min       1
             :max       60
             :value     @duration
             :on-change (fn [e] (reset! duration (js/Number (.. e -target -value))))}]]
          [:button.w-full.font-bold.py-2.px-4.rounded.transition.duration-150.bg-green-100.hover:bg-green-200
           {:on-click (fn [_] (reset! elapsed 0))}
           "Reset"]])

       :component-did-update
       (fn []
         (if (>= @elapsed @duration)
           (stop-timer!)
           (when (nil? @interval)
             (start-timer!))))

       :component-will-unmount
       stop-timer!})))