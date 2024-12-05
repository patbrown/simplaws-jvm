(ns baby.pat.simplaws.ses 
  (:require [baby.pat.simplaws :refer [aws-clients]]
            [baby.pat.simplaws.s3 :as s3]
            [baby.pat.vt :as vt]
            [clojure-mail.message :as mail]
            [email-attachments.message :as attachments]
            [orchestra.core :refer [defn-spec]]))

(defn-spec get-object-as-email ::vt/any
  "Gets an object and returns it as a structured email (with attachments). Note: this will give you dirty shit. Nothing is going to be usable off this function."
  ([client ::vt/any bucket ::vt/str object ::vt/str]
   (attachments/stream->mime-message (:body (s3/get-object client bucket object)))))

(defn-spec read-object-as-email ::vt/any
  "This reads the object message of an email."
  ([client ::vt/any bucket ::vt/str object ::vt/str]
   (mail/read-message (get-object-as-email client bucket object))))
