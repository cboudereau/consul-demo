ARG CONSUL_VERSION
ARG JDK_IMAGE
ARG DISTRO
ARG OTEL_CONTRIB_COL

FROM ghcr.io/graalvm/native-image:ol8-java17-22.3.3 as build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve dependency:go-offline -B 

COPY src ./src
RUN ./mvnw -o package -Pnative

FROM hashicorp/consul:${CONSUL_VERSION} as consul
FROM otel/opentelemetry-collector-contrib:${OTEL_CONTRIB_COL} as otelcontribcol
FROM oraclelinux:8.8

RUN dnf update -y && dnf install -y epel-release && dnf install -y supervisor jq

RUN mkdir -p /etc/otelcol-contrib

WORKDIR /app

RUN mkdir -p /app/log /app/logstate/client /app/logstate/agent

COPY --from=consul /bin/consul /app/consul
COPY --from=otelcontribcol /otelcol-contrib /app/otelcol-contrib

COPY ./supervisord.conf /app/supervisord.conf
COPY ./supervisor.d/ /app/supervisor.d/

COPY --from=build /app/target/client /app/client

EXPOSE 8080
CMD ["/usr/bin/supervisord", "-n"]