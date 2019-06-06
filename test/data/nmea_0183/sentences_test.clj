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

(deftest test-gsa
  (is (= #::f{:faa-mode :automatic
              :fix-status :2d
              :satellite-ids ["01" "12" "14" "15" "17" "19" "24" "25" "32"]
              :position-dop 1.0
              :horizontal-dop 0.7
              :vertical-dop 0.7}
         (::s/gsa (string->sentence "$GPGSA,A,2,01,12,14,15,17,19,24,25,32,,,,1.0,0.7,0.7*3F")))))

(deftest test-grs
  (is (= #::f {:time #::t {:hours 22 :minutes 3 :seconds 20.0}
               :residuals-recomputed 0
               :grs-range-residuals [-0.8 -0.2 -0.1 -0.2 0.8 0.6]}
         (::s/grs (string->sentence "$GPGRS,220320.0,0,-0.8,-0.2,-0.1,-0.2,0.8,0.6,,,,,,,*55")))))
