# Stage 1: Build artifact with Maven and OpenJDK 21
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime image with Eclipse Temurin JRE 21
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root application user
RUN addgroup -S rimsgroup && adduser -S rimsuser -G rimsgroup

COPY --from=builder /app/target/rims-backend-*.jar app.jar

RUN chown rimsuser:rimsgroup app.jar

USER rimsuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
