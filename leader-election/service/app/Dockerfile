FROM openjdk:17-alpine as build
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve dependency:go-offline -B 

COPY src ./src
RUN ./mvnw -o package -DskipTests

FROM hashicorp/consul:1.13.1 as consul
FROM openjdk:17-alpine
WORKDIR /app
ADD https://github.com/Yelp/dumb-init/releases/download/v1.2.5/dumb-init_1.2.5_x86_64 /usr/local/bin/dumb-init
RUN chmod +x /usr/local/bin/dumb-init

COPY --from=consul /bin/consul /bin/consul
COPY ./docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

COPY --from=build /app/target/app-0.0.1-SNAPSHOT.jar /app/app.jar

CMD ["/docker-entrypoint.sh"]