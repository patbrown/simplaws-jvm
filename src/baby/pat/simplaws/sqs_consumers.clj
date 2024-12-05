(ns baby.pat.simplaws.sqs-consumers
  (:require [clojure.core.async :as a]
            [orchestra.core :refer [defn-spec]]
            [baby.pat.simplaws :as aws :refer [aws-clients]]
            [baby.pat.simplaws.sqs :as sqs]
            [baby.pat.vt :as vt]
            [com.climate.squeedo.sqs :as squeedo]
            [com.climate.squeedo.sqs-consumer :as squeedo-consumer]))

;; Wrapper around squeedo
(defn-spec dequeue ::vt/any
  "Read messages from a queue. If there is nothing to read before
  poll-timeout-seconds, return [].
  This does *not* remove the messages from the queue! For that, see ack.
  In case of exception, logs the exception and returns []."
  ([client ::vt/any queue-id ::vt/qkw-or-str opts ::vt/map]
   (let [client (aws/retrieve-client client)
         url (sqs/url-for-queue client queue-id)]
     (squeedo/dequeue {:client client :queue-name queue-id :queue-url url} opts))))

(defn-spec ack ::vt/any
  "Acks a receipt."
  ([client ::vt/any queue-id ::vt/str receipt-handle ::vt/str]
   (let [client (aws/retrieve-client client)
         url (sqs/url-for-queue client queue-id)]
     (squeedo/ack {:client client
                   :queue-url url}
                  {:receipt-handle receipt-handle}))))

(defn-spec nack ::vt/any "Nacks a receipt."
  ([client ::vt/any queue-id ::vt/str receipt-handle ::vt/str]
   (let [client (aws/retrieve-client client)
         url (sqs/url-for-queue client queue-id)]
     (squeedo/nack {:client client
                    :queue-url url}
                   {:receipt-handle receipt-handle}))))

(defn-spec start-consumer ::vt/any [queue-id ::vt/qkw-or-str compute ::vt/fn & opts ::vt/coll]
  (apply squeedo-consumer/start-consumer (flatten queue-id compute opts))) 
(defn-spec stop-consumer ::vt/any [consumer ::vt/map]
  (squeedo-consumer/stop-consumer consumer))

(defn-spec pipe-sqs-messages-to-cca-channel ::vt/any [sqs-channel ::vt/qkw-or-str cca-channel ::vt/any]
  (squeedo-consumer/start-consumer sqs-channel (fn [msg] (a/put! cca-channel msg) :client :sqs-consumer/default)))

(comment

  ;; Polling looks interesting

;; SQUEEDO USAGE

  (require '
           '[clojure.core.async :refer [put!]])

  (defn compute [message done-channel]
    (println (:body message))
    (put! done-channel message))
(require '[baby.pat.over-simplified-aws :as aws])
  (def consumer (start-consumer "mcsquiddles" compute :client aws/sqs-consumer-client))
(stop-consumer consumer)
;
  
  )
