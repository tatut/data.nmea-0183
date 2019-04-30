(ns data.nmea-0183.sentences-test
  (:require [data.nmea-0183.message :as msg]
            [data.nmea-0183.input :as input]
            [clojure.test :refer [deftest testing is]]
            [clojure.java.io :as io]
            [data.nmea-0183.core :as nmea-0183]
            [data.nmea-0183.sentences :as s]
            [data.nmea-0183.fields :as f]
            [data.nmea-0183.types :as t]))


(defn- string->sentence [string]
  (nmea-0183/parse
   (input/input-stream
    (java.io.ByteArrayInputStream.
     (.getBytes (str string \return \newline) "US-ASCII")))))

(deftest test-zda
  (is (= #::f {:time #::t {:hours 9 :minutes 53 :seconds 20.0}
               :day 29 :month 3 :year 2019
               :local-zone-hours 0
               :local-zone-minutes 0}
         (::s/zda (string->sentence "$GPZDA,095320.00,29,03,2019,00,00*69")))))
