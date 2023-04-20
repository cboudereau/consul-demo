FROM hashicorp/consul:1.15.2 as consul
FROM prom/prometheus:v2.43.0

COPY --from=consul /bin/consul /bin/consul
COPY ./docker-entrypoint.sh /docker-entrypoint.sh

USER root
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT [ "/docker-entrypoint.sh" ]
