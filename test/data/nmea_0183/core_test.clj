(ns data.nmea-0183.core-test
  (:require [data.nmea-0183.core :as sut]
            [clojure.test :as t :refer [deftest is testing]]
            [data.nmea-0183.input :as input]
            [data.nmea-0183.fields :as f]
            [data.nmea-0183.sentences :as sentences]
            [clojure.java.io :as io]))

(defn- input [msg-str]
  (input/input-stream (java.io.ByteArrayInputStream.
                       (.getBytes (str msg-str \return \newline) "US-ASCII"))))


(deftest parse-gsa-test
  (let [m (sut/parse (input "$GPGSA,A,3,10,07,05,02,29,04,08,13,,,,,1.72,1.03,1.38*0A"))
        {::f/keys [faa-mode fix-status satellite-ids position-dop horizontal-dop vertical-dop] :as gsa}
        (::sentences/gsa m)]
    (println (pr-str gsa))
    (is (= :automatic faa-mode))
    (is (= :3d fix-status))
    (is (= ["10" "07" "05" "02" "29" "04" "08" "13"] satellite-ids))
    (is (= 1.72 position-dop))
    (is (= 1.03 horizontal-dop))
    (is (= 1.38 vertical-dop))))

(deftest parse-rre-test
  (let [m (sut/parse (input "$GPRRE,10,01,-000.105,06,-000.030,10,+000.034,12,+000.166,15,-000.193,17,+000.059,19,-000.014,24,-000.118,25,+000.479,32,-000.126,0000.240,0000.334*76"))
        {::f/keys [satellites-in-use range-residuals hpos-err-estimate vpos-err-estimate] :as rre}
        (::sentences/rre m)]
    (println (pr-str rre))
    (is (= 10 satellites-in-use))
    (is (= {"01" -0.105
            "06" -0.03
            "10" 0.034
            "12" 0.166
            "15" -0.193
            "17" 0.059,
            "19" -0.014
            "24" -0.118
            "25" 0.479
            "32" -0.126} range-residuals))
    (is (= 0.24 hpos-err-estimate))
    (is (= 0.334 vpos-err-estimate))))

(deftest parse-oulu-nmea
  (testing "Parse generated GPS log data"
    (let [in (input/input-stream (io/input-stream (io/resource "resources/oulu.nmea")))
          msgs (doall (repeatedly 60 #(sut/parse in)))]
      ;; check only that the messages are parsed correctly
      ;; FIXME: could add more validation
      (is msgs))))

(deftest parse-unsupported-sentence
  (testing "Parsing unsupported sentence throws :unsupported-sentence"
    (let [error (try (sut/parse (input "$GPGNS,120226.00,6304.8655,N,02134.3280,E,D,09,0.9,-2.4,23,8,0724,S*77"))
                     (catch Exception e
                       e))
          msg (ex-data error)]
      (is (= (:type msg) :unsupported-sentence) error)))

  (testing "Parsing unsupported sentence without talker throws :unsupported-sentence"
    (let [error (try (sut/parse (input "$PSAT,HPR,120227.00,206.89,3.19,-1.0,N*34"))
                     (catch Exception e
                       e))
          msg (ex-data error)]
      (is (= (:type msg) :unsupported-sentence)))))
