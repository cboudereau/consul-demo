# consul 1.17 api breaking change 

## problem
Analyse how to fix this warning in the consul local agent ??

```
{"@level":"warn","@message":"This request used the token query parameter which is deprecated and will be removed in Consul 1.17","@module":"agent.http","@timestamp":"2023-04-21T09:16:25.442738Z","logUrl":"/v1/catalog/services?token=\u003chidden\u003e"}
```

## temporary workaround

set -log-level info instead of error when consul agent will be fixed https://developer.hashicorp.com/consul/docs/upgrading/upgrade-specific#deprecating-authentication-via-token-query-parameter 

