# vector

Log > vector > LOKI > log + metrics via recorded rules

Log > vector > mimir (via prometheus remote write)

## Config example
https://github.com/vectordotdev/vector/tree/master/config

## Going to production
https://vector.dev/docs/setup/going-to-prod/
https://vector.dev/docs/about/under-the-hood/architecture/end-to-end-acknowledgements/
https://vector.dev/docs/setup/going-to-prod/architecting/#routing-to-your-system-of-record-archiving

## Demo
### analyse vector errors
```bash
dc logs -f vector | grep ERROR
```

### unit tests
https://vector.dev/docs/reference/configuration/unit-tests/ 
https://github.com/vectordotdev/vector/tree/2e5cb6491feca6b8fbb56abe682c202e6b9c1f7f/tests/behavior/transforms

## Guarantees
https://vector.dev/docs/about/under-the-hood/guarantees/#beta

## parser playground (vrl: vector remap language)
https://playground.vrl.dev/
https://vector.dev/docs/reference/vrl/expressions/#regular-expression

## vector monitoring
https://vector.dev/docs/administration/monitoring/

grafana dashboards 
- https://github.com/cboudereau/vector-community
- https://grafana.com/grafana/dashboards/15105-vector-stats/
- https://github.com/zamazan4ik/vector-community/tree/main/grafana_dashboards

- node exporter (host_metrics with node namespace) : https://grafana.com/grafana/dashboards/1860-node-exporter-full/