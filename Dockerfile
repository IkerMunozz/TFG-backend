# Usamos una imagen base con OpenJDK
FROM openjdk:11-jre-slim

# Definimos el directorio de trabajo
WORKDIR /app

# Copiamos el archivo .jar de tu proyecto a la imagen
COPY target/tienda-0.0.1-SNAPSHOT.jar app.jar

# Comando para ejecutar el JAR de Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
