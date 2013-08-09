drake-honeyql
=============

A Drake [HoneyQL](https://github.com/dirtyvagabond/honey-ql) plugin. Allows Drake steps to run HoneyQL queries against Factual and save the results.

This plugin has been deployed to Clojars. To use it, include the plugin in your `plugins.edn` file in your Drake workflow dir. E.g.:
```clojure
{:plugins [[dirtyvagabond/drake-honeyql "0.0.2"]]}
```

Then your Drake workflow can use the `honeyql` protocol to query Factual and store the results in your output file. E.g.:

```clojure
starbucks.json <- [honeyql]
  select name from places where name like 'starbuck%'

restaurants.json <- [honeyql]
  SELECT name, owner FROM restaurants WHERE owner IS NOT NULL
```
