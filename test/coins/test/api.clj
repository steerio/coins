(ns coins.test.api
  (:require [clojure.test :refer :all]
            [coins.api :refer :all]))

(def ^:private props
  (into {} (doto (java.util.Properties.)
             (.load (-> (Thread/currentThread)
                        .getContextClassLoader
                        (.getResourceAsStream "test.properties"))))))

(def ^:private conn
  (remote :url (props "coin.uri")
          :basic-auth [(props "coin.rpcuser") (props "coin.rpcpassword")]
          :insecure? (not (nil? (props "coin.insecure")))))

(deftest requests
  (let [add (first (addresses-for-account conn ""))]
    (is (string? add))
    (is (:isvalid (validate-address conn add)))
    (is (not (:isvalid (validate-address conn "LOL"))))
    (is (integer? (block-count conn)))))

(deftest string-requests
  (is (float?
        (get (accounts conn) ""))))

(deftest multiple-signatures
  (is (float? (balance conn)))
  (is (float? (balance conn ""))))
