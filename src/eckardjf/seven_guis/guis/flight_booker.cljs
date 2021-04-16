(ns eckardjf.seven-guis.guis.flight-booker
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]
            [cljc.java-time.local-date :as local-date]
            [cljc.java-time.format.date-time-formatter :as formatter]))

(def date-formatter (formatter/of-pattern "dd.MM.yyyy"))

(defn parse-date [s]
  (try
    (local-date/parse s date-formatter)
    (catch js/Error _)))

(defn flight-booker [flight]
  (let [flight-type (r/cursor flight [:flight-type])
        start (r/cursor flight [:start])
        start-date (ratom/make-reaction #(parse-date @start))
        return (r/cursor flight [:return])
        return-date (ratom/make-reaction #(parse-date @return))
        handle-flight-type-change (fn [e] (reset! flight-type (keyword (.. e -target -value))))]
    (fn []
      [:form
       {:on-submit
        (fn [e]
          (js/alert (str "You have booked a " (case @flight-type
                                                :one-way "one way"
                                                :round-trip "round trip")
                         " flight on " @start (when (= :round-trip @flight-type)
                                                (str " returning " @return)) "."))
          (.preventDefault e))}
       [:div.flex.flex-col.space-y-4
        [:div.flex.flex-nowrap.justify-start
         [:div.flex-1.flex.items-center.mr-4
          [:input#radio1.hidden {:type      "radio"
                                 :name      "flight-type"
                                 :value     "one-way"
                                 :checked   (= :one-way @flight-type)
                                 :on-change handle-flight-type-change}]
          [:label.flex.items-center.cursor-pointer.leading-none.text-sm.whitespace-nowrap {:for "radio1"}
           [:span.w-5.h-5.inline-block.mr-2.rounded-full.border.border-gray-300] "One Way"]]
         [:div.flex-1.flex.items-center.mr-4
          [:input#radio2.hidden {:type      "radio"
                                 :name      "flight-type"
                                 :value     "round-trip"
                                 :checked   (= :round-trip @flight-type)
                                 :on-change handle-flight-type-change}]
          [:label.flex.items-center.cursor-pointer.leading-none.text-sm.whitespace-nowrap {:for "radio2"}
           [:span.w-5.h-5.inline-block.mr-2.rounded-full.border.border-gray-300] "Round Trip"]]]
        [:div [:input.field.w-full
               {:id          "start"
                :type        "text"
                :placeholder "DD.MM.YYYY"
                :on-change   (fn [e] (reset! start (.. e -target -value)))}]]
        (when (= :round-trip @flight-type)
          [:div [:input.field.w-full
                 {:id          "return"
                  :type        "text"
                  :placeholder "DD.MM.YYYY"
                  :on-change   (fn [e] (reset! return (.. e -target -value)))}]])
        [:input.btn.btn-yellow-dark
         {:type     "submit"
          :value    "Book"
          :disabled (case @flight-type
                      :one-way (nil? @start-date)
                      :round-trip (or (nil? @start-date)
                                      (nil? @return-date)
                                      (not (local-date/is-after @return-date @start-date))))}]]])))