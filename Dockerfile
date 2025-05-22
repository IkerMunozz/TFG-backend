# Etapa 1: Construcción del JAR con Maven y Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final con Java 21 y Python 3
FROM eclipse-temurin:21-jdk

# Instalar Python
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiar el JAR generado
COPY --from=builder /app/target/*.jar app.jar

# Copiar los scripts Python a la ruta /app/scripts
COPY src/main/resources/scripts /app/scripts

# Establecer punto de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]




