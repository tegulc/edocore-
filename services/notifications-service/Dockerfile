# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY .mvn/settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    for i in 1 2 3 4 5 6 7 8 9 10; do \
      echo "=== Maven build notifications - tentative $i/10 ===" && \
      mvn -B clean package -DskipTests && exit 0; \
      echo "Echec, nouvel essai dans 30s..."; \
      sleep 30; \
    done; \
    echo "Build notifications abandonne apres 10 tentatives."; \
    exit 1

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN addgroup --system educore && adduser --system --ingroup educore educore
COPY --from=build /app/target/notifications-service-*.jar app.jar
USER educore
EXPOSE 8081
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]
