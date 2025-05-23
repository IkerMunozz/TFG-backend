from ultralytics import YOLO
import sys
import os
import torch
import logging
import gc
from PIL import Image
import traceback
from datetime import datetime

# Configurar logging a archivo
log_dir = '/app/logs'
os.makedirs(log_dir, exist_ok=True)
log_file = os.path.join(log_dir, f'detection_{datetime.now().strftime("%Y%m%d_%H%M%S")}.log')

# Configurar logging
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(log_file),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

def log_system_info():
    """Registra información del sistema"""
    try:
        logger.info("=== Información del Sistema ===")
        logger.info(f"Python version: {sys.version}")
        logger.info(f"PyTorch version: {torch.__version__}")
        logger.info(f"CUDA disponible: {torch.cuda.is_available()}")
        if torch.cuda.is_available():
            logger.info(f"CUDA version: {torch.version.cuda}")
        logger.info(f"Directorio actual: {os.getcwd()}")
        logger.info(f"Contenido del directorio actual: {os.listdir('.')}")
        logger.info(f"Contenido del directorio models: {os.listdir('models')}")
        logger.info(f"Memoria disponible: {torch.cuda.get_device_properties(0).total_memory if torch.cuda.is_available() else 'CPU only'}")
    except Exception as e:
        logger.error(f"Error al obtener información del sistema: {str(e)}")

def detect_objects(image_path):
    try:
        logger.info(f"=== Iniciando detección de objetos ===")
        logger.info(f"Ruta de la imagen: {image_path}")
        
        # Verificar que el modelo existe
        model_path = 'models/yolov8n.pt'
        if not os.path.exists(model_path):
            logger.error(f"Modelo no encontrado en: {model_path}")
            return False
        logger.info(f"Modelo encontrado en: {model_path}")
        
        # Cargar el modelo con configuración optimizada
        logger.info("Cargando modelo YOLO...")
        model = YOLO(model_path)
        model.to('cpu')  # Forzar uso de CPU
        logger.info("Modelo cargado correctamente")
        
        # Verificar la imagen
        logger.info("Verificando imagen...")
        if not os.path.exists(image_path):
            logger.error(f"Imagen no encontrada en: {image_path}")
            return False
        
        # Reducir el tamaño de la imagen para ahorrar memoria
        logger.info("Procesando imagen...")
        img = Image.open(image_path)
        logger.info(f"Tamaño original de la imagen: {img.size}")
        img.thumbnail((640, 640))  # Reducir tamaño manteniendo proporción
        logger.info(f"Tamaño reducido de la imagen: {img.size}")
        
        # Realizar la detección
        logger.info("Iniciando detección...")
        results = model(img, conf=0.25)  # Reducir umbral de confianza
        
        # Verificar si se detectaron objetos
        if len(results[0].boxes) > 0:
            logger.info(f"Se detectaron {len(results[0].boxes)} objetos")
            for box in results[0].boxes:
                logger.info(f"Objeto detectado: {box.cls}, confianza: {box.conf}")
            return True
        else:
            logger.info("No se detectaron objetos")
            return False
            
    except Exception as e:
        logger.error(f"Error durante la detección: {str(e)}")
        logger.error("Traceback completo:")
        logger.error(traceback.format_exc())
        return False
    finally:
        # Limpiar memoria
        logger.info("Limpiando memoria...")
        if 'model' in locals():
            del model
        if 'results' in locals():
            del results
        gc.collect()
        torch.cuda.empty_cache() if torch.cuda.is_available() else None
        logger.info("Memoria limpiada")

if __name__ == "__main__":
    try:
        logger.info("=== Iniciando script de detección de objetos ===")
        log_system_info()
        
        if len(sys.argv) < 2:
            logger.error("No se proporcionó la ruta de la imagen.")
            sys.exit(2)

        image_path = sys.argv[1]
        logger.info(f"Ruta de la imagen recibida: {image_path}")

        if not os.path.exists(image_path):
            logger.error(f"Imagen no encontrada en: {image_path}")
            sys.exit(3)

        logger.info(f"Tamaño de la imagen: {os.path.getsize(image_path)} bytes")
        
        found = detect_objects(image_path)
        if found:
            logger.info("Detección completada exitosamente")
            sys.exit(0)
        else:
            logger.error("No se detectaron objetos en la imagen")
            sys.exit(1)
    except Exception as e:
        logger.error(f"Error general: {str(e)}")
        logger.error("Traceback completo:")
        logger.error(traceback.format_exc())
        sys.exit(1)
    finally:
        # Limpiar memoria al finalizar
        logger.info("Limpiando memoria final...")
        gc.collect()
        torch.cuda.empty_cache() if torch.cuda.is_available() else None
        logger.info("Script finalizado")


