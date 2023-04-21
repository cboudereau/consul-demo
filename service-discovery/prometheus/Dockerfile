ARG CONSUL_VERSION
ARG PROMETHEUS_VERSION

FROM hashicorp/consul:${CONSUL_VERSION} as consul
FROM prom/prometheus:${PROMETHEUS_VERSION}

COPY --from=consul /bin/consul /bin/consul
COPY ./docker-entrypoint.sh /docker-entrypoint.sh

USER root
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT [ "/docker-entrypoint.sh" ]
