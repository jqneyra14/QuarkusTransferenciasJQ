# Stage 1: Build (compilar con Maven)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar pom.xml y descargar dependencias (se cachea)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar (saltar tests para acelerar)
RUN mvn clean package -DskipTests

# Stage 2: Run (ejecutar solo el JAR)
FROM registry.access.redhat.com/ubi9/openjdk-21:1.18

ENV LANGUAGE='en_US:en'

# Copiar artefactos desde stage build
COPY --from=build --chown=185 /app/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8083

USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT [ "java", "-jar", "/deployments/quarkus-run.jar" ]