(ns eckardjf.seven-guis.guis.counter)

(defn counter [count]
  [:div.text-center.space-y-10
   [:p.text-5xl.font-bold @count]
   [:button.btn.btn-purple {:on-click #(swap! count inc)} "Count"]])