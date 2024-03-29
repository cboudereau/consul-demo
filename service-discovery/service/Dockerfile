ARG CONSUL_VERSION
ARG JDK_IMAGE
ARG DISTRO
ARG VECTOR_VERSION

FROM ${JDK_IMAGE} as jdk
# required for strip-debug to work
RUN apk add --no-cache binutils
RUN $JAVA_HOME/bin/jlink \
         --verbose \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /jre-lightweight

FROM ${JDK_IMAGE} as build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve dependency:go-offline -B 

COPY src ./src
RUN ./mvnw -o package

FROM hashicorp/consul:${CONSUL_VERSION} as consul
FROM timberio/vector:${VECTOR_VERSION} as vector
FROM ${DISTRO}

RUN apk add --no-cache supervisor

ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=jdk /jre-lightweight $JAVA_HOME

ARG APPLICATION_USER=appuser
ARG OTEL_JAVA_AGENT_VERSION

RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER

USER 1000
WORKDIR /app

RUN mkdir -p /app/log && mkdir -p /app/vector_data

ADD --chown=1000:1000 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/${OTEL_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar ./opentelemetry-javaagent.jar

COPY --chown=1000:1000 --from=consul /bin/consul /app/consul
COPY --chown=1000:1000 --from=vector /usr/local/bin/vector /app/vector

COPY --chown=1000:1000 ./supervisord.conf /app/supervisord.conf
COPY --chown=1000:1000 ./supervisor.d/ /app/supervisor.d/

COPY --chown=1000:1000 --from=build /app/target/service-0.0.1-SNAPSHOT.jar /app/main.jar

EXPOSE 8080
CMD ["/usr/bin/supervisord", "-n"]