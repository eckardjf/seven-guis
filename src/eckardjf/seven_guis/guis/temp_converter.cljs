(ns eckardjf.seven-guis.guis.temp-converter)

(defn celsius->fahrenheit [c]
  (-> c (* 9) (/ 5) (+ 32)))

(defn fahrenheit->celsius [f]
  (-> f (- 32) (* 5) (/ 9)))

(defn parse-temp [s]
  (let [x (js/parseFloat s)]
    (when-not (js/isNaN x) x)))

(defn format-temp [x]
  (str (Math/round x)))

(defn temp-converter [temp]
  [:div.flex.items-center
   [:div
    [:input {:id        "celsius"
             :class     ["w-28" "appearance-none" "block" "rounded" "py-2" "px-4" "border-gray-300" "bg-gray-50"
                         "focus:border-indigo-300" "focus:ring" "focus:ring-indigo-200" "focus:ring-opacity-50"
                         (when (and (not-empty (:celsius @temp))
                                    (nil? (:fahrenheit @temp))) "bg-red-50 border-red-400 focus:border-red-300 focus:ring-red-300")]
             :type      "text"
             :value     (:celsius @temp)
             :on-change (fn [e]
                          (let [v (.. e -target -value)]
                            (reset! temp {:celsius    v
                                          :fahrenheit (some-> (parse-temp v) celsius->fahrenheit format-temp)})))}]
    [:label {:for   "celsius"
             :class "block tracking-wide font-bold text-xs leading-3 mt-2 ml-1"} "Celsius"]]
   [:div.mx-4.text-4xl.-mt-6 "="]
   [:div
    [:input {:id        "fahrenheit"
             :class     ["w-28" "appearance-none" "block" "rounded" "py-2" "px-4" "border-gray-300" "bg-gray-50"
                         "focus:border-indigo-300" "focus:ring" "focus:ring-indigo-200" "focus:ring-opacity-50"
                         (when (and (not-empty (:fahrenheit @temp))
                                    (nil? (:celsius @temp))) "bg-red-50 border-red-300 focus:border-red-300 focus:ring-red-200")]
             :type      "text"
             :value     (:fahrenheit @temp)
             :on-change (fn [e]
                          (let [v (.. e -target -value)]
                            (reset! temp {:fahrenheit v
                                          :celsius    (some-> (parse-temp v) fahrenheit->celsius format-temp)})))}]
    [:label {:for   "fahrenheit"
             :class "block tracking-wide font-bold text-xs leading-3 mt-2 ml-1"} "Fahrenheit"]]])