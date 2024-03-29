ARG CONSUL_VERSION
ARG JDK_IMAGE=openjdk:17-alpine
ARG DISTRO=alpine:3.17.3

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
FROM ${DISTRO}

RUN apk add --no-cache supervisor

ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=jdk /jre-lightweight $JAVA_HOME

ARG APPLICATION_USER=appuser
RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER

USER 1000
WORKDIR /app

COPY --chown=1000:1000 --from=consul /bin/consul /app/consul

COPY --chown=1000:1000 ./supervisord.conf /app/supervisord.conf
COPY --chown=1000:1000 ./supervisor.d/ /app/supervisor.d/

COPY --chown=1000:1000 --from=build /app/target/app-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
CMD ["/usr/bin/supervisord", "-n"]