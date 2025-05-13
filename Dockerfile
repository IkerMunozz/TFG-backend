# Etapa 1: Construcción del JAR
FROM maven:3.8.5-openjdk-17 AS builder

WORKDIR /app

# Copiamos el pom.xml y resolvemos dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el resto del código fuente
COPY src ./src

# Construimos el JAR
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final para ejecutar la app
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copiamos el JAR desde el contenedor anterior
COPY --from=builder /app/target/*.jar app.jar

# Ejecutamos la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]

