# Etapa de build
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY pom.xml /app
COPY src /app/src
RUN apt-get update && \
    apt-get install -y maven && \
    mkdir -p /app/efs \
    mvn clean package

# Etapa de runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/facturas-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

