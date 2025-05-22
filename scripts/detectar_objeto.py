from ultralytics import YOLO
import sys
import os

model = YOLO("model.pt")  

def detect_objects(image_path):
    results = model(image_path, verbose=False)
    predictions = results[0].boxes
    return len(predictions) > 0

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("No se proporcionó la ruta de la imagen.")
        sys.exit(2)

    image_path = sys.argv[1]

    if not os.path.exists(image_path):
        print("Imagen no encontrada.")
        sys.exit(3)

    found = detect_objects(image_path)
    if found:
        sys.exit(0)
    else:
        print("No se detectó ningún objeto.")
        sys.exit(1)