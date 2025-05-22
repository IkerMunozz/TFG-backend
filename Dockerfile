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
    apt-get install -y openjdk-21-jdk wget && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Crear carpeta para uploads y dar permisos
RUN mkdir -p /app/uploads && \
    chmod 777 /app/uploads

# Copiar scripts Python
COPY src/main/resources/python /app/python

# Instalar dependencias de Python paso a paso
RUN pip3 install --upgrade pip && \
    pip3 install --no-cache-dir numpy==1.24.3

RUN pip3 install --no-cache-dir torch==2.0.1+cpu torchvision==0.15.2+cpu -f https://download.pytorch.org/whl/torch_stable.html

RUN pip3 install --no-cache-dir ultralytics==8.0.0

# Descargar el modelo YOLO optimizado
RUN wget https://github.com/ultralytics/assets/releases/download/v0.0.0/yolov8n.pt -O /app/python/yolov8n.pt && \
    python3 -c "from ultralytics import YOLO; model = YOLO('/app/python/yolov8n.pt'); model.export(format='onnx', simplify=True, opset=12)"

# Copiar JAR construido
COPY --from=builder /app/target/*.jar app.jar

# Configurar variables de entorno para Java y Python
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV PYTHONUNBUFFERED=1
ENV PIP_NO_CACHE_DIR=1

# Verificar la instalación
RUN echo "Verificando instalación..." && \
    python3 --version && \
    pip3 list && \
    ls -la /app/python && \
    echo "Instalación completada"

ENTRYPOINT ["java", "-jar", "app.jar"]






