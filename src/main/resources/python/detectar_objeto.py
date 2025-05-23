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

# Configurar logging a archivo
log_dir = '/app/logs'
try:
    # Intentar crear el directorio si no existe
    if not os.path.exists(log_dir):
        print(f"Creando directorio de logs en: {log_dir}")
        os.makedirs(log_dir, exist_ok=True)
        print(f"Directorio de logs creado exitosamente")
    else:
        print(f"El directorio de logs ya existe en: {log_dir}")
    
    # Verificar permisos
    if not os.access(log_dir, os.W_OK):
        print(f"ADVERTENCIA: No hay permisos de escritura en {log_dir}")
        # Intentar cambiar permisos
        os.chmod(log_dir, 0o777)
        print(f"Permisos actualizados para {log_dir}")
    
    log_file = os.path.join(log_dir, f'detection_{datetime.now().strftime("%Y%m%d_%H%M%S")}.log')
    print(f"Archivo de log será creado en: {log_file}")
except Exception as e:
    print(f"Error al configurar el directorio de logs: {str(e)}")
    # Fallback a un directorio temporal
    log_dir = '/tmp/logs'
    os.makedirs(log_dir, exist_ok=True)
    log_file = os.path.join(log_dir, f'detection_{datetime.now().strftime("%Y%m%d_%H%M%S")}.log')
    print(f"Usando directorio temporal para logs: {log_file}")

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
        start_time = time.time()
        
        # Configurar PyTorch para usar CPU y optimizar memoria
        torch.set_num_threads(4)  # Limitar threads de CPU
        torch.set_num_interop_threads(4)
        
        # Cargar modelo con configuración optimizada
        model = YOLO(model_path)
        model.to('cpu')
        
        # Configurar el modelo para inferencia
        model.conf = 0.25  # Umbral de confianza
        model.iou = 0.45   # Umbral de IOU
        model.max_det = 100  # Máximo número de detecciones
        
        load_time = time.time() - start_time
        logger.info(f"Modelo cargado en {load_time:.2f} segundos")
        
        # Verificar la imagen
        logger.info("Verificando imagen...")
        if not os.path.exists(image_path):
            logger.error(f"Imagen no encontrada en: {image_path}")
            return False
        
        # Reducir el tamaño de la imagen para ahorrar memoria
        logger.info("Procesando imagen...")
        img = Image.open(image_path)
        logger.info(f"Tamaño original de la imagen: {img.size}")
        
        # Reducir tamaño manteniendo proporción
        max_size = 640
        ratio = min(max_size/img.size[0], max_size/img.size[1])
        new_size = tuple(int(dim * ratio) for dim in img.size)
        img = img.resize(new_size, Image.Resampling.LANCZOS)
        logger.info(f"Tamaño reducido de la imagen: {img.size}")
        
        # Realizar la detección
        logger.info("Iniciando detección...")
        start_time = time.time()
        results = model(img, verbose=False)
        detection_time = time.time() - start_time
        logger.info(f"Detección completada en {detection_time:.2f} segundos")
        
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


