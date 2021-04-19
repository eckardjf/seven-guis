(ns eckardjf.seven-guis.guis.cells
  (:require [cljs.reader :refer [read-string]]
            [clojure.string :as string]
            [reagent.core :as r]
            [instaparse.core :as insta :refer-macros [defparser]]
            [instaparse.failure :as failure]))

(defprotocol Formula
  (evaluate [this data])
  (refs [this data])
  (eval-list [this data]))

(defrecord Textual [value]
  Formula
  (evaluate [this data] 0)
  (refs [this data] [])
  (eval-list [this data] [(evaluate this data)])
  Object
  (toString [_] value))

(defrecord Number [value]
  Formula
  (evaluate [this data] value)
  (refs [this data] [])
  (eval-list [this data] [(evaluate this data)])
  Object
  (toString [_] (str value)))

(defrecord Coord [row column]
  Formula
  (evaluate [this data] (:value (get data [row column])))
  (refs [this data] [(get data [row column])])
  (eval-list [this data] [(evaluate this data)])
  Object
  (toString [_] (str (char (+ 65 column)) row)))

(defrecord CellRange [coord1 coord2]
  Formula
  (evaluate [this data] js/NaN)
  (refs [this data] (for [row (range (:row coord1) (inc (:row coord2)))
                          col (range (:column coord1) (inc (:column coord2)))]
                      (get data [row col])))
  (eval-list [this data] (map #(:value %) (refs this data)))
  Object
  (toString [_] (str coord1 ":" coord2)))

(def op-table
  {"add"  #(+ %1 %2)
   "sub"  #(- %1 %2)
   "div"  #(/ %1 %2)
   "mul"  #(* %1 %2)
   "mod"  #(mod %1 %2)
   "sum"  +
   "prod" *})

(defrecord Application [function arguments]
  Formula
  (evaluate [this data]
    (try
      (apply (get op-table function) (mapcat #(eval-list % data) arguments))
      (catch js/Error e js/NaN)))
  (refs [this data] (mapcat #(refs % data) arguments))
  (eval-list [this data] [(evaluate this data)])
  Object
  (toString [_] (str function "(" (string/join ", " (map str arguments)) ")")))

(def EmptyCell (Textual. ""))

(defparser parser
           "formula     = number / textual / (<'='> expr)
            expr        = range / cell / number / application
            application = ident <'('> (expr <','>)* expr <')'>
            range       = cell <':'> cell
            cell        = #'[A-Za-z]\\d+'
            textual     = #'[^=].*'
            ident       = #'[a-zA-Z_]\\w*'
            number      = #'-?\\d+(\\.\\d*)?'
           "
           #_#_:auto-whitespace :standard)

(defn parse [s]
  (let [result (insta/parse parser s)]
    (if (insta/failure? result)
      (Textual. (with-out-str (failure/pprint-failure (insta/get-failure result))))
      (insta/transform
        {:number      #(Number. (js/parseFloat %))
         :ident       str
         :textual     #(Textual. %)
         :cell        #(Coord. (read-string (subs % 1)) (- (int (.charCodeAt % 0)) 65))
         :range       #(CellRange. %1 %2)
         :application (fn [f & args] (Application. f (vec args)))
         :expr        identity
         :formula     identity
         } result))))

(defn change-prop [{:keys [x y formula value observers]} data]
  (let [new-value (evaluate formula @data)]
    #_(println "[" x ":" y "]" "value:" value "new-value: " new-value "observers:" observers)
    (when-not (= value new-value)
      (swap! data assoc-in [[x y] :value] new-value)
      (doseq [o observers] (change-prop (get @data o) data)))))

(defn cell [state data]
  (let [{:keys [editing? x y text formula value]} @state]
    [:td.border.border-gray-300.p-0.w-20.h-8
     (if editing?
       [:input.w-20.h-8.border-none.text-sm.p-1
        {:type          "text"
         :auto-focus    true
         :on-key-down   (fn [e] (case (.-key e)
                                  "Enter" (let [new-text (.. e -target -value)
                                                new-formula (if (string/blank? new-text) EmptyCell (parse new-text))]
                                            (doseq [cell (refs formula @data)]
                                              (swap! data update-in [[(:x cell) (:y cell)] :observers] #(disj % [x y])))
                                            (doseq [cell (refs new-formula @data)]
                                              (swap! data update-in [[(:x cell) (:y cell)] :observers] #(conj % [x y])))
                                            (swap! state assoc :formula new-formula :editing? false :text new-text)
                                            (change-prop @state data))
                                  "Escape" (.. e -target blur) #_(swap! state assoc :editing? false)
                                  :default))
         :on-blur       #(swap! state assoc :editing? false)
         :default-value text}]
       [:div.w-20.h-8.text-right.p-1
        {:on-click #(swap! state assoc :editing? true)}
        (str (if (instance? Textual formula) formula value))])]))

(defn make-data [height width]
  (into {} (for [x (range height) y (range width)]
             [[x y] {:x x :y y :text "" :value 0 :formula EmptyCell :observers #{}}])))

(defn cells [cols rows]
  (let [data (r/atom (make-data rows cols))]
    (fn []
      [:div.overflow-auto.h-80.w-80.xl:w-96
       [:table.border-collapse.border
        [:thead.bg-gray-200
         [:tr [:th.border.border-gray-300.p-0]
          (for [c (range cols)] ^{:key c} [:th.border.border-gray-300.p-0 c])]]
        [:tbody
         (for [r (range rows)]
           ^{:key r}
           [:tr [:th.border.border-gray-300.text-center.p-1.leading-none r]
            (for [c (range cols)]
              ^{:key [c r]}
              [cell (r/cursor data [[r c]]) data])])]]])))

;; TODO - fixed column / rows headers
;; TODO - keyboard nav
;; TODO - allow more than one alpha prefix
;; TODO - change grammar to exclude range from formula def
