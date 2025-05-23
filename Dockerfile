# Etapa 1: Construcción del JAR con Maven y Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final con Java 21 y Python 3
FROM python:3.9-slim

# Instalar Java y dependencias necesarias
RUN apt-get update && \
    apt-get install -y wget gnupg && \
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - && \
    echo "deb https://packages.adoptium.net/artifactory/deb bullseye main" | tee /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y temurin-21-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Crear carpeta para uploads y dar permisos
RUN mkdir -p /app/uploads && \
    chmod 777 /app/uploads

# Copiar scripts Python y modelo
COPY src/main/resources/python /app/python
COPY src/main/resources/models /app/python/models

# Dar permisos de ejecución al script Python
RUN chmod +x /app/python/detectar_objeto.py

# Instalar dependencias de Python paso a paso
RUN pip3 install --upgrade pip && \
    pip3 install --no-cache-dir numpy==1.24.3

RUN pip3 install --no-cache-dir torch==2.0.1+cpu torchvision==0.15.2+cpu -f https://download.pytorch.org/whl/torch_stable.html

RUN pip3 install --no-cache-dir ultralytics==8.0.0

# Copiar JAR construido
COPY --from=builder /app/target/*.jar app.jar

# Configurar variables de entorno para Java y Python
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV PYTHONUNBUFFERED=1
ENV PIP_NO_CACHE_DIR=1
ENV TORCH_CUDA_VERSION=cpu

# Verificar la instalación
RUN echo "Verificando instalación..." && \
    python3 --version && \
    pip3 list && \
    ls -la /app/python && \
    ls -la /app/python/models && \
    echo "Instalación completada"

ENTRYPOINT ["java", "-jar", "app.jar"]






