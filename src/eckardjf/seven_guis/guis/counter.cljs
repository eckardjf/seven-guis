(ns eckardjf.seven-guis.guis.counter)

(defn counter [count]
  [:div.text-center.space-y-10
   [:p.text-5xl.font-bold @count]
   [:button {:class    ["rounded" "py-2" "px-4" "font-bold" "transition" "duration-150"
                        "bg-purple-100" "hover:bg-purple-200" "text-purple-900"
                        "focus:outline-none" "focus:ring" "focus:ring-purple-300"]
             :on-click #(swap! count inc)} "Count"]])