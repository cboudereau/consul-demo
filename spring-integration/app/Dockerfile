FROM openjdk:17-alpine as jdk
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

FROM openjdk:17-alpine as build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve dependency:go-offline -B 

COPY src ./src
RUN ./mvnw -o package -DskipTests

FROM hashicorp/consul:1.13.1 as consul
FROM alpine:3.16

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