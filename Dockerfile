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
    apt-get install -y python3 python3-pip python3-dev build-essential && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Crear carpeta para uploads y dar permisos
RUN mkdir -p /app/uploads && \
    chmod 777 /app/uploads

# Copiar scripts Python
COPY src/main/resources/python /app/python

# Instalar dependencias de Python y descargar el modelo YOLO
RUN echo "Instalando ultralytics..." && \
    pip3 install --no-cache-dir ultralytics && \
    echo "Descargando modelo YOLO..." && \
    python3 -c "from ultralytics import YOLO; print('Descargando modelo...'); YOLO('yolov8n.pt', task='detect'); print('Modelo descargado correctamente')"

# Copiar JAR construido
COPY --from=builder /app/target/*.jar app.jar

# Configurar variables de entorno para Java
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Verificar la instalación
RUN echo "Verificando instalación..." && \
    python3 --version && \
    pip3 list && \
    ls -la /app/python && \
    echo "Instalación completada"

ENTRYPOINT ["java", "-jar", "app.jar"]






