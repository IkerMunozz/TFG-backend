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
        # Definir ruta absoluta a la carpeta models
        models_dir = os.path.join(os.path.dirname(__file__), 'models')
        model_path = os.path.join(models_dir, 'yolov8n.pt')
        print(f"Modelo YOLO: {model_path}")
        print(f"Contenido de models: {os.listdir(models_dir)}")
        
        # Verificar que la imagen existe
        if not os.path.exists(image_path):
            print(f"La imagen no existe en la ruta: {image_path}")
            return False
        
        # Cargar y redimensionar la imagen
        print("Cargando imagen...")
        image = Image.open(image_path)
        print(f"Tamaño original de la imagen: {image.size}")
        
        # Redimensionar la imagen a 640x640
        image = image.resize((640, 640))
        print(f"Tamaño redimensionado: {image.size}")
        
        # Cargar el modelo YOLO
        print("Cargando modelo YOLO...")
        start_time = time.time()
        model = YOLO(model_path)
        print(f"Modelo cargado en {time.time() - start_time:.2f} segundos")
        
        # Realizar la detección
        print("Iniciando detección...")
        results = model(image, conf=0.25)
        print(f"Detección completada en {time.time() - start_time:.2f} segundos")
        
        # Verificar resultados
        if len(results) > 0 and len(results[0].boxes) > 0:
            print(f"Objetos detectados: {len(results[0].boxes)}")
            for box in results[0].boxes:
                print(f"Confianza: {box.conf[0]:.2f}, Clase: {box.cls[0]}")
            return True
        else:
            print("No se detectaron objetos en la imagen")
            return False
            
    except Exception as e:
        print(f"Error durante la detección: {str(e)}")
        traceback.print_exc()
        return False
    finally:
        # Limpiar memoria
        gc.collect()
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
        print("Memoria liberada")




