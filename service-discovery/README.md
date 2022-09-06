# service-discovery

Consul Service Discovery + Automatic registration and client side load balancing with spring-cloud-starter-consul-discovery library.

## otel
https://sre.google/sre-book/monitoring-distributed-systems/
https://opentelemetry.io/docs/
https://opentelemetry.io/docs/instrumentation/java/automatic/agent-config/
https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#sampler

## TODO
Micrometer span_id / trace_id
Grafana/Prometheus exemplars

## demo
```bash
docker compose down --remove-orphans -v --rmi local && docker compose up
```

Spawn 4 services and call multiple times the client service and observer the round robin in the service logs

```bash
watch -n 0.5 -b -d -- curl http://localhost:8080/hello
```

Stop services and observer the failover (watch command)

```bash
docker stop service-discovery-service-<id of the service>
```

Check services in consul

Stop the java app
```bash
docker compose exec --index 1 service killall dumb-init java
```

List processes
```bash
docker compose exec --index 1 service ps
```

Check that the service has been gracefully deregistered from [consul nodes](http://localhost:8500/ui/dc1/services/users-service/instances) (only 3 of 4 services listed in the console)

Restart the service again
```bash
docker restart service-discovery-service-1
```

Kill the java app
```bash
docker compose exec --index 1 service killall -9 dumb-init java
```

Check that the service health check is down in the [consul console](http://localhost:8500/ui/dc1/services/users-service/instances)

Restart the service again
```bash
docker restart service-discovery-service-1
```

Check that the failing health check is up in the [console](http://localhost:8500/ui/dc1/services/users-service/instances)

## Java

### Create service
- vscode plugin: Spring Initializr Java Support
- ctrl+shift+p: Spring Initializr create project

### Dockerfile
https://snyk.io/blog/best-practices-to-build-java-containers-with-docker/

### Consul java
https://springframework.guru/consul-miniseries-spring-boot-application-and-consul-integration-part-1/
http://myjavaadventures.com/blog/2020/02/19/consul-spring-boot-and-docker/

## Grafana
dashboard 
- JVM : 4701
- Throughput : 5373