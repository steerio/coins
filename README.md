# Coins

Coins is a Bitcoin/Litecoin/Dogecoin JSON RPC client library.

## Usage

### Connection

```clojure
(require '[coins.api :as ca])

(def doge
  (ca/remote
    :uri "http://localhost:22555/"
    :basic-auth ["shibe" "password"]))
```

### Calling methods

```clojure
user=> (ca/balance doge)
1000000.0
user=> (ca/block-count doge)
111805
user=> (ca/block-hash doge (ca/block-count doge))
"452ce95a61deb7508937acd47b00bc14fefe95c04720b9f0b8441be7e5731147"
```

As seen above, this library uses idomatic Clojure function names instead of the
RPC method names. Should you prefer to use the original names, you can use the
`call` and `call-str` functions. The difference is that the latter will not
keywordize the received JSON keys.

```clojure
user=> (ca/call doge :getblockcount)
111805
user=> (ca/call doge "getblockhash" 111805)
"452ce95a61deb7508937acd47b00bc14fefe95c04720b9f0b8441be7e5731147"
```

### Discovering the API

All the API functions have proper signatures and meaningful argument names.
Feel free to use the `clojure.repl/doc` function to discover them.

## License

Copyright © 2014 Roland Venesz

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
