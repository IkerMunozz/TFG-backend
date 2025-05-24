from ultralytics import YOLO
import sys
import os
import torch
import logging
import gc
from PIL import Image
import traceback
from datetime import datetime
import time




def detect_objects(image_path):
    try:
        print("Iniciando detección de objetos...")
        print(f"Ruta de la imagen: {image_path}")
        
        # Ruta del modelo
        models_dir = os.path.join(os.path.dirname(__file__), 'models')
        model_path = os.path.join(models_dir, 'yolov8n.pt')
        print(f"Modelo YOLO: {model_path}")
        
        # Verificar imagen
        if not os.path.exists(image_path):
            print(f"La imagen no existe: {image_path}")
            return False
        
        # Cargar imagen y forzar a RGB
        image = Image.open(image_path).convert("RGB")
        image = image.resize((640, 640))
        
        # Cargar modelo
        model = YOLO(model_path)
        
        # Detectar con mayor umbral de confianza
        results = model(image, conf=0.6)
        
        boxes = results[0].boxes

        # Asegurar que boxes no es None
        if boxes is None or len(boxes) == 0:
            print("❌ No se detectaron objetos.")
            return False

        # Verificar que cada detección tenga confianza > 0.6 (ya filtrado, pero por seguridad)
        objetos_validos = 0
        for box in boxes:
            conf = float(box.conf[0])
            if conf >= 0.6:
                objetos_validos += 1
                print(f"✅ Objeto válido detectado. Confianza: {conf:.2f}, Clase: {int(box.cls[0])}")

        if objetos_validos == 0:
            print("⚠️ Detecciones descartadas por baja confianza.")
            return False
        else:
            print(f"🎯 Se detectaron {objetos_validos} objetos válidos.")
            return True

    except Exception as e:
        print(f"Error en la detección: {e}")
        traceback.print_exc()
        return False

    finally:
        gc.collect()
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
        print("🧹 Memoria liberada.")





