from ultralytics import YOLO
import sys
import os

# Cargar el modelo YOLO pre-entrenado
model = YOLO('yolov8n.pt')

def detect_objects(image_path):
    try:
        print(f"Intentando detectar objetos en: {image_path}")
        results = model(image_path, verbose=False)
        predictions = results[0].boxes
        num_objects = len(predictions)
        print(f"Número de objetos detectados: {num_objects}")
        return num_objects > 0
    except Exception as e:
        print(f"Error durante la detección: {str(e)}")
        return False

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("No se proporcionó la ruta de la imagen.")
        sys.exit(2)

    image_path = sys.argv[1]
    print(f"Ruta de la imagen recibida: {image_path}")

    if not os.path.exists(image_path):
        print(f"Imagen no encontrada en: {image_path}")
        sys.exit(3)

    print(f"Tamaño de la imagen: {os.path.getsize(image_path)} bytes")
    
    found = detect_objects(image_path)
    if found:
        print("Se detectaron objetos en la imagen")
        sys.exit(0)
    else:
        print("No se detectó ningún objeto en la imagen")
        sys.exit(1)


