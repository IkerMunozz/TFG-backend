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

# Forzar la escritura inmediata de logs
sys.stdout.reconfigure(line_buffering=True)
sys.stderr.reconfigure(line_buffering=True)

print("=== Iniciando script de detección de objetos ===")
print(f"Python version: {sys.version}")
print(f"Directorio actual: {os.getcwd()}")
print(f"Argumentos recibidos: {sys.argv}")

# Configurar logging
def setup_logging():
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    log_dir = "/app/logs"
    if not os.path.exists(log_dir):
        os.makedirs(log_dir)
    
    log_file = os.path.join(log_dir, f"detection_{timestamp}.log")
    
    # Configurar el logger
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(levelname)s - %(message)s',
        handlers=[
            logging.FileHandler(log_file),
            logging.StreamHandler(sys.stdout)
        ]
    )
    
    # Forzar la escritura inmediata
    for handler in logging.getLogger().handlers:
        handler.flush()
    
    return logging.getLogger()

logger = setup_logging()

def log_system_info():
    """Registra información del sistema"""
    try:
        print("=== Información del Sistema ===")
        print(f"Python version: {sys.version}")
        print(f"PyTorch version: {torch.__version__}")
        print(f"CUDA disponible: {torch.cuda.is_available()}")
        if torch.cuda.is_available():
            print(f"CUDA version: {torch.version.cuda}")
        print(f"Directorio actual: {os.getcwd()}")
        print(f"Contenido del directorio actual: {os.listdir('.')}")
        print(f"Contenido del directorio models: {os.listdir('models')}")
        print(f"Memoria disponible: {torch.cuda.get_device_properties(0).total_memory if torch.cuda.is_available() else 'CPU only'}")
    except Exception as e:
        print(f"Error al obtener información del sistema: {str(e)}")

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

if __name__ == "__main__":
    try:
        print("Script iniciado")
        print(f"Directorio actual: {os.getcwd()}")
        print(f"Contenido del directorio: {os.listdir('.')}")
        print(f"Contenido de models: {os.listdir('models')}")
        
        if len(sys.argv) != 2:
            print("Uso: python detectar_objeto.py <ruta_imagen>")
            sys.exit(1)
            
        image_path = sys.argv[1]
        print(f"Procesando imagen: {image_path}")
        
        result = detect_objects(image_path)
        print(f"Resultado de la detección: {'Éxito' if result else 'Falló'}")
        
        # Forzar la escritura de logs antes de salir
        sys.stdout.flush()
        sys.stderr.flush()
        
        sys.exit(0 if result else 1)
        
    except Exception as e:
        print(f"Error general: {str(e)}")
        traceback.print_exc()
        # Forzar la escritura de logs antes de salir
        sys.stdout.flush()
        sys.stderr.flush()
        sys.exit(1)


