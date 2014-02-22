(ns coins.api
  (require [clj-http.client :as h]
           [clojure.data.json :as js]
           [clojure.string :as s]))

(defn remote [& {:as cfg}]
  (merge
    {:url "http://localhost:8332/"
     :method :post
     }
    cfg))

(defmacro call* [conn rpc params res err & json-args]
  `(let [{res# ~res err# ~err}
         (-> ~conn
             (assoc :body
                    (js/write-str
                      {:id "rpc"
                       :method ~rpc
                       :params ~params}))
             h/request
             :body
             (js/read-str ~@json-args))]
     (if err#
       (throw (Exception. err#))
       res#)) )

(defn call [conn rpc & params]
  (call* conn rpc params :result :error :key-fn keyword))

(defn call-str [conn rpc & params]
  (call* conn rpc params "result" "error"))

(defn- grow [req opt]
  (map
    #(into (vec req)
           (vec (take % opt)))
    (range (inc (count opt)))))

(defn- part [args]
  (loop [req [] args args]
    (let [f (first args)]
      (if (not f)
        (list req)
        (if (= f '&opt)
          (list req (vec (rest args)))
          (recur (conj req f) (rest args)))))))

(defmacro ^:private defrpc
  [n doc params & {:keys [rpc call-fn]
                   :or {rpc (s/replace n #"[!-]" "")
                        call-fn call}}]
  (let [[req opt] (part params)
        conn 'conn]
    `(defn ~(with-meta n (assoc (meta n) :rpc rpc)) ~doc
       ~@(if opt
           (map
             (fn [ps] (list (into [conn] ps) `(~call-fn ~conn ~rpc ~@ps)))
             (grow req opt))
           (let [params (into req opt)]
             `(~(into [conn] params) (~call-fn ~conn ~rpc ~@params)))))))

(defrpc add-multisig-address!
  "Add a nrequired-to-sign multisignature address to the wallet. Each key is an
  address or hex-encoded public key. If `account` is specified, assign address
  to `account`."
  [nrequired hex-keys &opt account])

(defrpc add-node!
  "Attempts add or remove `node` from the addnode list or try a connection to
  `node` once."
  [node add-remove-onetry])

(defrpc backup-wallet
  "Safely copies wallet.dat to destination, which can be a directory or a path
  with filename."
  [destination])

(defrpc create-multisig!
  "Creates a multi-signature address and returns a JSON object."
  [nrequired keys])

(defrpc create-raw-tx!
  "Creates a raw transaction spending given inputs."
  [txs addresses-amounts]
  :rpc "createrawtransaction")

(defrpc decode-raw-transaction
  "Produces a human-readable JSON object for a raw transaction."
  [hex-string])

(defrpc dump-private-key
  "Reveals the private key corresponding to `address`."
  [address]
  :rpc "dumpprivkey")

(defrpc encrypt-wallet!
  "Encrypts the wallet with `passphrase`."
  [passphrase])

(defrpc account
  "Returns the account associated with the given address."
  [address]
  :rpc "getaccount")

(defrpc account-address
  "Returns the current address for receiving payments to this account."
  [account]
  :rpc "getaccountaddress")

(defrpc added-node-info
  "Returns information about the given added node, or all added nodes. Note
  that onetry addnodes are not listed here).
  If `dns` is false, only a list of added nodes will be provided, otherwise
  connected information will also be available."
  [dns &opt node]
  :rpc "getaddednodeinfo")

(defrpc addresses-for-account
  "Returns the list of addresses for `account`."
  [account]
  :rpc "getaddressesbyaccount")

(defrpc balance
  "If `account` is not specified, returns the server's total available balance.
  If `account` is specified, returns the balance in the account."
  [&opt account min-conf]
  :rpc "getbalance")

(defrpc best-block-hash
  "Returns the hash of the best (tip) block in the longest block chain."
  []
  :rpc "getbestblockhash")

(defrpc block
  "Returns information about the block with the given hash."
  [block-hash]
  :rpc "getblock")

(defrpc block-count
  "Returns the number of blocks in the longest block chain."
  []
  :rpc "getblockcount")

(defrpc block-hash
  "Returns hash of block in best-block-chain at `index`; index 0 is the genesis block."
  [index]
  :rpc "getblockhash")

(defrpc block-template
  "Returns data needed to construct a block to work on."
  [&opt params]
  :rpc "getblocktemplate")

(defrpc connections
  "Returns the number of connections to other nodes."
  []
  :rpc "getconnectioncount")

(defrpc difficulty
  "Returns the proof-of-work difficulty as a multiple of the minimum difficulty."
  []
  :rpc "getdifficulty")

(defrpc generating?
  "Returns true or false whether daemon is currently generating hashes."
  []
  :rpc "getgenerate")

(defrpc hash-speed
  "Returns a recent hashes per second performance measurement while generating."
  []
  :rpc "gethashespersec")

(defrpc info
  "Returns an object containing various state info."
  []
  :rpc "getinfo")

(defrpc memory-pool
  "Replaced in v0.7.0 with getblocktemplate, submitblock, getrawmempool."
  [&opt data]
  :rpc "getmemorypool")

(defrpc mining-info
  "Returns an object containing mining-related information."
  []
  :rpc "getmininginfo")

(defrpc create-address!
  "Returns a new address for receiving payments. If `account` is specified
  payments received with the address will be credited to `account`."
  [&opt account]
  :rpc "getnewaddress")

(defrpc peer-info
  "Returns data about each connected node."
  []
  :rpc "getpeerinfo")

(defrpc create-raw-change-address!
  "Returns a new address, for receiving change. This is for use with raw
  transactions, NOT normal use."
  [&opt account]
  :rpc "getrawchangeaddress")

(defrpc raw-mem-pool
  "Returns all transaction ids in memory pool."
  []
  :rpc "getrawmempool")

(defrpc raw-tx
  "Returns a raw transaction representation for the given transaction ID."
  [txid &opt verbos]
  :rpc "getrawtransaction")

(defrpc amount-received
  "Returns the total amount received by addresses with `account` in
  transactions with at least `min-conf` confirmations. If `account` is not
  provided, it will include all transactions to all accounts."
  [&opt account min-conf]
  :rpc "getreceivedbyaccount")

(defrpc amount-received-by-address
  "Returns the amount received by `address` in transactions with at least
  `min-conf` confirmations. It correctly handles the case where someone has
  sent to the address in multiple transactions. Keep in mind that addresses
  are only ever used for receiving transactions. Works only for addresses in
  the local wallet, external addresses will always show 0."
  [address &opt min-conf]
  :rpc "getreceivedbyaddress")

(defrpc tx
  "Returns an object about the given transaction containing."
  [txid]
  :rpc "gettransaction")

(defrpc tx-out
  "Returns details about an unspent transaction output (UTXO)."
  [txid n &opt include-mempool]
  :rpc "gettxout")

(defrpc tx-out-setinfo
  "Returns statistics about the unspent transaction output (UTXO) set."
  []
  :rpc "gettxoutsetinfo")

(defrpc work
  "If [data] is not specified, returns formatted hash data to work on."
  [&opt data]
  :rpc "getwork")

(defrpc help
  "List commands, or get help for a command."
  [&opt command])

(defrpc import-privkey
  "Adds a private key (as returned by dump-private-key) to your wallet. This
  may take a while, as a rescan is done, looking for existing transactions.
  Optional [rescan] parameter added in 0.8.0."
  [privkey &opt label rescan])

(defrpc refill-keypool
  "Fills the keypool, requires wallet passphrase to be set."
  []
  :rpc "keypoolrefill")

(defrpc accounts
  "Returns Object that has account names as keys, account balances as values"
  [&opt min-conf]
  :rpc "listaccounts"
  :call-fn call-str)

(defrpc address-groupings
  "Returns all addresses in the wallet and info used for coincontrol"
  []
  :rpc "listaddressgroupings")

(defrpc received
  "Returns a vector of maps containing account, amount and number of
  confirmations."
  [&opt min-conf include-empty]
  :rpc "listreceivedbyaccount")

(defrpc received-by-address
  "Returns a vector of maps containing account, address, amount and number of
  confirmations."
  [&opt min-conf include-empty]
  :rpc "listreceivedbyaddress")

(defrpc txs-since-block
  "Get all transactions in blocks since block `blockhash`, or all transactions
  if omitted."
  [&opt blockhash target-confirmations]
  :rpc "listsinceblock")

(defrpc txs
  "Returns up to `tx-count` most recent transactions skipping the first `from`
  transactions for account `account`. If `account` not provided, it will
  return recent transaction from all accounts."
  [&opt account tx-count from]
  :rpc "listtransactions")

(defrpc unspent
  "Returns array of unspent transaction inputs in the wallet."
  [&opt min-conf max-conf]
  :rpc "listunspent")

(defrpc unspendable
  "Returns list of temporarily unspendable outputs."
  []
  :rpc "listlockunspent")

(defrpc update-unspendable!
  "Updates list of temporarily unspendable outputs."
  [unlock &opt array-of-objects]
  :rpc "lockunspent")

(defrpc move
  "Move from one account in your wallet to another."
  [from-account to-account amount &opt min-conf remark])

(defrpc send-from!
  "Will send the given `amount` (rounded to 8 decimal places) to the given
  address, ensuring the account has a valid balance using `min-conf`
  confirmations. Returns the transaction ID if successful (not in JSON
  object)"
  [from-account to-address amount &opt min-conf remark comment-to])

(defrpc send-many!
  "See send-from!"
  [from-account addresses-amounts &opt min-conf remark])

(defrpc send-raw-tx
  "Submits raw transaction (serialized, hex-encoded) to local node and network."
  [hex-string]
  :rpc "sendrawtransaction")

(defrpc send-to!
  "See send-from! Returns the transaction ID if successful."
  [address amount &opt remark comment-to]
  :rpc "sendtoaddress")

(defrpc set-account!
  "Sets the `account` associated with the given `address`. Assigning an address
  that is already assigned to the same account will create a new address
  associated with that account."
  [address account])

(defrpc generate!
  "Turns generation on or off.
  Generation is limited to `proc-limit` processors, -1 is unlimited."
  [generate &opt proc-limit])

(defrpc set-tx-fee!
  "Sets transaction fee. Amount is rounded to the nearest 0.00000001."
  [amount])

(defrpc sign-message
  "Signs a message with the private key of an address."
  [address message])

(defrpc sign-raw-tx
  "Adds signatures to a raw transaction and returns the resulting raw
  transaction."
  [hexstring &opt txs priv-keys]
  :rpc "signrawtransaction")

(defrpc stop
  "Stops server."
  [])

(defrpc submit-block!
  "Attempts to submit new block to network."
  [hex-data &opt optional-params-obj])

(defrpc validate-address
  "Returns information about `address`."
  [address])

(defrpc verify-message
  "Verify a signed message."
  [address signature message])

(defrpc lock-wallet!
  "Removes the wallet encryption key from memory, locking the wallet. After
  calling this method, you will need to call `cache-passphrase` again before
  being able to call any methods which require the wallet to be unlocked."
  []
  :rpc "walletlock")

(defrpc unlock-wallet!
  "Stores the wallet decryption key in memory for `timeout` seconds."
  [passphrase timeout]
  :rpc "walletpassphrase")

(defrpc change-passphrase!
  "Changes the wallet passphrase from `old-passphrase` to `passphrase`."
  [old-passphrase passphrase]
  :rpc "walletpassphrasechange")
