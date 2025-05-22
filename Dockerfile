# Etapa 1: Construcción del JAR con Maven y Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final con Java 21 y Python 3
FROM eclipse-temurin:21-jdk

# Instalar Python y dependencias necesarias
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Crear carpeta para uploads y dar permisos
RUN mkdir -p /app/uploads && \
    chmod 777 /app/uploads

# Copiar scripts Python y el modelo
COPY src/main/resources/python /app/python
COPY src/main/resources/model.pt /app/model.pt

# Instalar dependencias de Python
RUN pip3 install ultralytics

# Copiar JAR construido
COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]






