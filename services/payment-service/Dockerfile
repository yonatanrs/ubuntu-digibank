FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src src
RUN mvn -B -q clean verify

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S bank && adduser -S -G bank -u 10001 bank
WORKDIR /app
COPY --from=build /workspace/target/payment-service-*.jar app.jar
USER 10001:10001
EXPOSE 8081
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-XX:+ExitOnOutOfMemoryError","-Djava.security.egd=file:/dev/urandom","-jar","/app/app.jar"]

