(ns data.nmea-0183.core
  "NMEA 0183 parser and writer."
  (:require [data.nmea-0183.message :as msg]
            [data.nmea-0183.sentences :as sentences]))

(defn parse
  "Read and parse one sentence from the input source."
  [input]
  (let [{:keys [sentence fields] :as m} (msg/read-message input)]
    (when-not sentence
      (throw (ex-info "No identifiable NMEA sentence found"
                      {:type :parse-error})))

    (when-not (get-method sentences/parse-sentence sentence)
      (throw (ex-info (str "Unsupported sentence " sentence)
                      {:type :unsupported-sentence})))
    (try
      (merge m (sentences/parse-sentence sentence fields))
      (catch Exception e
        (throw (ex-info (str "Cannot parse sentence " sentence " fields " fields)
                        {:type :parse-error}
                        e))))))
