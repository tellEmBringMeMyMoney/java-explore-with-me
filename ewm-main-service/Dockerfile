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
COPY ewm-main-service/src ./ewm-main-service/src
RUN mvn -q -pl ewm-main-service -am package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/ewm-main-service/target/ewm-main-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
