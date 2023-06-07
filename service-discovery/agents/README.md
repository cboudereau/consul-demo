# Collector/Exporter

### Demo

```bash
./up.sh
```

grafana is available at http://localhost:3000

``client`` java app embeds ``otelcol-contrib`` as an agent and tranforms log to counter.

``otelcol-contrib`` has been configured to export its own metrics, logs and host metrics integrated into grafana dashboard.

``service`` java app embeds ``vector`` has an agent and transforms log to counter and sum. It has been configured to export its own metrics, logs and host metrics integrated into 2 grafana dashboards. The hostmetric source is compatible with ``node exporter`` dashboard with small changes.

### Conclusion
Vector is best at pipeline transformation like log to metric transformation / aggregation while it does not support nor full OTLP and traces. Vector uses its own protocol which can conflict with OTLP (ie: OTLP trace). Vector supports also a better disk buffer implementation to offer a maximum decoupling guarantee on performance issue or failure.

OpenTelemetry Collector Contrib is fully compliant with OTLP but the transformation pipeline is not as mature as vector.dev. Otelcol-contrib does not support a true disk buffering like vector but implements a file receiver and exporter which can act as a gateway between cluster/VPC. Buffering and Pipeline transformation (connectors) are tracked in the project though. It supports [Tail sampling processor](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/tailsamplingprocessor#tail-sampling-processor) which can reduce the number of traces in order to keep only traces having performance issues.

Vector is highly recommended for log 2 metric without tracing support while OpenTelemetry Collector is recommended for pure 1:1 observability.

Vector and OpenTelemetry Collector can be both used to have the maximum benefits. Vector should be used only for logs and metrics as an agent/aggregator on client side for legacy app support when refactoring to metric cannot be done at once or on time. On the backend side, vector can forward to an OpenTelemetry Collector agent/aggregator.

## Benchmark

### Table
|||mtail|vector|promtail/grafana agent|otelcontribcol|
|-|-|-|-|-|-|
||Total|28|111|41|107|
||Editor|Google|Datadog|Grafana|Opentelemetry|
|Delivery|Delivery 1 - Best effort 13 - At least once|1|13|5|8|
|Exporter method|(13) Push|13|13|5|13|
|Source|(5) File Source|1|5|3|5|
|Source|(8) OTLP Metrics|0|0|0|8|
|Source|(8) OTLP Logs|0|8|0|5|
|Source|(2) OTLP Traces|0|0|0|2|
|Source|(1) HTTP Json Source|0|1|0|0|
|Source|(8) TCP Json Source|0|8|0|8|
|Source|(8) Elasticsearch / Logstash|0|5|0|5|
|Source|(1) StatsD Source|0|1|0|1|
|Source|(3) Prometheus scrape|0|3|0|1|
|Source|(1) Prometheus remote write|0|1|0|0|
|Sink|(1) StatsD Sink|1|1|0|0|
|Sink|(5) OTLP Metrics||0|0|5|
|Sink|(3) OTLP Logs||0|0|3|
|Sink|(2) OTLP Traces|0|0|0|2|
|Sink|(5) Loki|0|3|5|3|
|Sink|(1) HTTP Json|0|1|0|0|
|Sink|(5) File|0|5|0|5|
|Sink|(2) Mimir (prom remote write)|0|1|2|0|
|Event|(5) Metrics|0|5|3|5|
|Event|(5) Logs|5|5|5|5|
|Event|(2) Traces|0|0|0|2|
|Transformation|(5) Metrics to log|5|5|2|1|
|Language|(1) Grok|0|1|0|0|
|Transformation|(3) Transformation|1|3|2|2|
|Documentation|(13) Documentation|1|13|3|8|
|Internal monitoring|(5) Internal logs||5|3|5|
|Internal monitoring|(5) Internal metrics||5|3|5|
