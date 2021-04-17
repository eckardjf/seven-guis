(ns eckardjf.seven-guis.guis.crud
  (:require [clojure.string :as string]))

(defn reset-form! [state]
  (swap! state dissoc :selected :first-name :last-name))

(defn crud [state]
  [:div.grid.grid-cols-1.gap-4.xl:grid-cols-2
   [:div
    [:div#bottom-ring {:class "relative flex items-center border-b border-gray-300 focus-within:border-emerald-500"}
     [:svg.w-6.h-6.text-gray-500 {:fill "none" :stroke "currentColor" :viewBox "0 0 24 24" :xmlns "http://www.w3.org/2000/svg"}
      [:path {:stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2"
              :d              "M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z"}]]
     [:input {:class       "appearance-none w-full border-none focus:ring-0 focus:border-none focus:outline-none pl-2 pr-4 py-2 placeholder-gray-400"
              :type        "text"
              :placeholder "Filter prefix..."
              :on-change   (fn [e] (swap! state assoc :filter (.. e -target -value)))}]]]
   [:div.col-start-1
    [:ul {:class "w-full h-52 max-h-52 rounded py-1 border border-gray-300 overflow-auto focus:outline-none"
          :role  "listbox"}
     (let [prefix (:filter @state "")
           filtered-users (filter (fn [[_ {:keys [last-name]}]]
                                    (string/starts-with? (string/upper-case last-name) (string/upper-case prefix)))
                                  (:users @state))]
       (doall
         (for [[id user] (sort-by (comp :last-name val) filtered-users)]
           ^{:key id}
           [:li {:class        ["text-gray-900 cursor-default select-none relative py-2 pl-3 pr-9 hover:bg-gray-200"
                                (when (= id (:selected @state)) "bg-gray-100")]
                 :role         "option"
                 :data-user-id (:id user)
                 :on-click     (fn [_] (if (= id (:selected @state))
                                         (reset-form! state)
                                         (do
                                           (swap! state assoc :selected id)
                                           (swap! state merge (select-keys user [:first-name :last-name])))))}
            [:div.flex.items-center
             [:div.rounded-full.bg-emerald-200 {:class "p-1.5"}
              [:svg.w-5.h-5.text-emerald-500 {:fill "currentColor" :viewBox "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
               [:path {:fill-rule "evenodd" :d "M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" :clip-rule "evenodd"}]]]
             [:span.font-normal.ml-3.block.truncate (str (:last-name user) ", " (:first-name user))]
             (when (= id (:selected @state))
               [:span.text-emerald-500.absolute.inset-y-0.right-0.flex.items-center.pr-4
                [:svg.w-5.h-5 {:fill "currentColor" :viewBox "0 0 20 20" :xmlns "http://www.w3.org/2000/svg"}
                 [:path {:fill-rule "evenodd" :d "M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-8.707l-3-3a1 1 0 00-1.414 1.414L10.586 9H7a1 1 0 100 2h3.586l-1.293 1.293a1 1 0 101.414 1.414l3-3a1 1 0 000-1.414z" :clip-rule "evenodd"}]]])]])))]]
   [:div.space-y-4
    [:input.field.w-full {:type      "text"
                          :value     (:first-name @state)
                          :on-change (fn [e] (swap! state assoc :first-name (.. e -target -value)))}]
    [:input.field.w-full {:type      "text"
                          :value     (:last-name @state)
                          :on-change (fn [e] (swap! state assoc :last-name (.. e -target -value)))}]
    (if-not (:selected @state)
      [:button.btn.btn-emerald-dark
       {:disabled (or (string/blank? (:first-name @state))
                      (string/blank? (:last-name @state)))
        :on-click (fn [_]
                    (swap! state assoc-in [:users (random-uuid)] (select-keys @state [:first-name :last-name]))
                    (reset-form! state))}
       "Create"]
      [:div.space-x-4
       [:button.btn.btn-emerald-dark
        {:on-click (fn [_]
                     (swap! state assoc-in [:users (:selected @state)] (select-keys @state [:first-name :last-name]))
                     (reset-form! state))}
        "Update"]
       [:button.btn.btn-emerald-dark
        {:on-click (fn [_]
                     (swap! state update :users dissoc (:selected @state))
                     (reset-form! state))}
        "Delete"]])]])