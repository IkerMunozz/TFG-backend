# Etapa 1: Construcción del JAR con Maven y Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final con Python 3.10 y Java 21
FROM python:3.10

# Instalar Java 21 y herramientas necesarias
RUN apt-get update && \
    apt-get install -y wget gnupg curl build-essential python3-dev libgl1 libglib2.0-0 libsm6 libxrender1 libxext6 && \
    wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - && \
    echo "deb https://packages.adoptium.net/artifactory/deb bullseye main" | tee /etc/apt/sources.list.d/adoptium.list && \
    apt-get update && \
    apt-get install -y temurin-21-jdk && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Crear carpetas necesarias
WORKDIR /app
RUN mkdir -p /app/uploads /app/logs /tmp/logs && \
    chmod -R 777 /app/uploads /app/logs /tmp/logs

# Copiar scripts y modelo
COPY src/main/resources/python /app/python

# Dar permisos al script Python
RUN chmod +x /app/python/detectar_objeto.py

# Instalar dependencias de Python paso a paso
RUN pip3 install --upgrade pip
RUN pip3 install --no-cache-dir numpy==1.24.3
RUN pip3 install --no-cache-dir torch==2.0.1+cpu torchvision==0.15.2+cpu -f https://download.pytorch.org/whl/torch_stable.html
RUN pip3 install --no-cache-dir ultralytics==8.0.0
RUN pip3 install --no-cache-dir Pillow==10.0.0

# Copiar el JAR compilado desde la etapa builder
COPY --from=builder /app/target/*.jar /app/app.jar

# Variables de entorno
ENV JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
ENV PYTHONUNBUFFERED=1
ENV PIP_NO_CACHE_DIR=1
ENV TORCH_CUDA_VERSION=cpu
ENV PYTHONPATH=/app/python
ENV PYTHONIOENCODING=utf-8
ENV PYTHONHASHSEED=0

# Verificaciones finales
RUN echo "✅ Instalación lista" && \
    java -version && \
    python3 --version && \
    pip3 list && \
    ls -la /app/python && \
    ls -la /app/python/models

COPY requirements.txt /app/requirements.txt
RUN pip3 install --no-cache-dir -r /app/requirements.txt

ENTRYPOINT ["java", "-jar", "/app/app.jar"]







