(ns eckardjf.seven-guis.guis.circle-drawer
  (:require [reagent.core :as r]
            [reagent.ratom :as ratom]))

(defn event->point [e]
  (let [ne (.-nativeEvent e)]
    [(.-offsetX ne) (.-offsetY ne)]))

(defn distance [p1 p2]
  (Math/sqrt (->> (map - p1 p2) (map #(* % %)) (reduce +))))

(defn origin [c]
  ((juxt :x :y) c))

(defn make-history [ref]
  (r/atom {:states [@ref] :cursor 0}))

(def max-states 20)

(defn save! [history ref]
  (when (not= @ref (nth (:states @history) (:cursor @history)))
    (swap! history assoc-in [:states]
           (conj (vec (take (min max-states (inc (:cursor @history))) (:states @history))) @ref))
    (swap! history update-in [:cursor] inc)))

(defn can-undo? [history]
  (> (:cursor @history) 0))

(defn undo! [history ref]
  (when (can-undo? history)
    (swap! history update-in [:cursor] dec)
    (reset! ref (nth (:states @history) (:cursor @history)))))

(defn can-redo? [history]
  (< (:cursor @history) (dec (count (:states @history)))))

(defn redo! [history ref]
  (when (can-redo? history)
    (swap! history update-in [:cursor] inc)
    (reset! ref (nth (:states @history) (:cursor @history)))))

(defn circle-drawer [state]
  (let [circles (r/cursor state [:circles])
        selected (r/cursor state [:selected])

        history (make-history circles)

        canvas (r/atom nil)
        register-canvas (fn [el] (when el (reset! canvas el)))

        repaint (fn []
                  (when @canvas
                    (let [ctx (.getContext @canvas "2d")]
                      (.clearRect ctx 0 0 (.-width @canvas) (.-height @canvas))
                      (doseq [[i {:keys [x y d]}] (map-indexed vector @circles)]
                        (.beginPath ctx)
                        (.arc ctx x y (/ d 2) 0 (* 2 Math/PI))
                        (set! (.-strokeStyle ctx) "#111827")
                        (.stroke ctx)
                        (when (= i @selected)
                          (set! (.-fillStyle ctx) "#E5E7EB")
                          (.fill ctx))))))
        painter (r/track! repaint)                          ;; TODO - do we need to call dispose! on this?

        context-menu (r/atom {:open? false :x 0 :y 0})
        diameter-dialog (r/atom {:open? false :x 0 :y 0})
        adjusting? (ratom/reaction (or (:open? @context-menu) (:open? @diameter-dialog)))

        nearest-at (fn [p]
                     (let [[i nearest] (apply min-key #(distance p (origin (second %))) (map-indexed vector @circles))
                           dist (distance p (origin nearest))]
                       (when (<= dist (/ (:d nearest) 2))
                         i)))
        select-at (fn [e] (when-not @adjusting?
                            (reset! selected (nearest-at (event->point e)))))
        default-diameter 30
        add-circle (fn [[x y]]
                     (swap! circles conj {:x x :y y :d default-diameter}))]
    (fn []
      (println "render")
      [:div
       [:div.flex.items-center.justify-center.space-x-2..mb-4.text-lightBlue-800
        [:button.btn.btn-lightBlue
         {:disabled (or @adjusting? (false? (can-undo? history)))
          :on-click (fn [] (undo! history circles))} "Undo"]
        [:button.btn.btn-lightBlue
         {:disabled (or @adjusting? (false? (can-redo? history)))
          :on-click (fn [] (redo! history circles))} "Redo"]]
       [:div.relative
        [:canvas.w-full.border.rounded
         {:ref             register-canvas
          :on-mouse-move   select-at
          :on-click        (fn [e]
                             (if @adjusting?
                               (do
                                 (swap! context-menu assoc :open? false)
                                 (swap! diameter-dialog assoc :open? false)
                                 (save! history circles)    ;; TODO - only save if diameter was changed?
                                 (select-at e))
                               (when (nil? @selected)
                                 (add-circle (event->point e))
                                 (save! history circles)
                                 (select-at e))))
          :on-context-menu (fn [e]
                             (.preventDefault e)
                             (when (and (false? @adjusting?) @selected)
                               (let [{:keys [x y d]} (nth @circles @selected)]
                                 (swap! context-menu assoc :x x :y (+ y (/ d 4)) :open? true)
                                 (println @context-menu))))}]
        (let [{:keys [open? x y]} @context-menu]
          (when open?
            [:ul.absolute.rounded.border.shadow-md.bg-white
             {:style {:left (str x "px") :top (str y "px")}}
             [:li.text-sm.hover:bg-gray-100.p-2.rounded.whitespace-nowrap
              [:button {:on-click (fn [_]
                                    (swap! context-menu assoc :open? false)
                                    (swap! diameter-dialog assoc :open? true :x x :y y))}
               "Adjust diameter..."]]]))
        (let [{:keys [open? x y]} @diameter-dialog]
          (when open?
            [:div.absolute.rounded.border.shadow-md.bg-white.p-4.flex.flex-col.justify-center.space-y-4
             {:style {:left (str x "px") :top (str y "px")}}
             (let [{:keys [x y]} (nth @circles @selected)]
               [:div.text-xs.whitespace-nowrap "Adjust diameter of circle at " x "," y])
             [:input.w-full.h-1.appearance-none.outline-none.rounded.slider-thumb.bg-gray-400
              {:type      "range"
               :min       10
               :max       60
               :value     (-> @circles (nth @selected) :d)
               :on-change (fn [e]
                            (swap! circles assoc-in [@selected :d] (.. e -target -value)))}]]))]])))