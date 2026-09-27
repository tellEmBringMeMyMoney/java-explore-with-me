FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./pom.xml
COPY ewm-main-service/pom.xml ./ewm-main-service/pom.xml
COPY ewm-stats-service/pom.xml ./ewm-stats-service/pom.xml
COPY ewm-stats-service/ewm-stats-dto/pom.xml ./ewm-stats-service/ewm-stats-dto/pom.xml
COPY ewm-stats-service/ewm-stats-client/pom.xml ./ewm-stats-service/ewm-stats-client/pom.xml
COPY ewm-stats-service/ewm-stats-server/pom.xml ./ewm-stats-service/ewm-stats-server/pom.xml
COPY ewm-stats-service/ewm-stats-dto/src ./ewm-stats-service/ewm-stats-dto/src
COPY ewm-stats-service/ewm-stats-client/src ./ewm-stats-service/ewm-stats-client/src
COPY ewm-stats-service/ewm-stats-server/src ./ewm-stats-service/ewm-stats-server/src
RUN mvn -q package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/ewm-stats-service/ewm-stats-server/target/ewm-stats-server-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
