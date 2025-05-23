from ultralytics import YOLO
import sys
import os
import torch
import logging
import gc
from PIL import Image

# Configurar logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def detect_objects(image_path):
    try:
        logger.info(f"Intentando detectar objetos en: {image_path}")
        
        # Solo verificar que podemos abrir la imagen
        try:
            with Image.open(image_path) as img:
                logger.info(f"Imagen abierta correctamente. Tamaño: {img.size}, Formato: {img.format}")
                return True
        except Exception as e:
            logger.error(f"Error al abrir la imagen: {str(e)}")
            return False
            
    except Exception as e:
        logger.error(f"Error durante la detección: {str(e)}")
        import traceback
        logger.error("Traceback completo:")
        logger.error(traceback.format_exc())
        return False

if __name__ == "__main__":
    try:
        logger.info("=== Iniciando script de detección de objetos ===")
        logger.info(f"Python version: {sys.version}")
        logger.info(f"PyTorch version: {torch.__version__}")
        logger.info(f"Directorio actual: {os.getcwd()}")
        logger.info(f"Contenido del directorio actual: {os.listdir('.')}")
        
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
            logger.info("Imagen verificada correctamente")
            sys.exit(0)
        else:
            logger.error("Error al verificar la imagen")
            sys.exit(1)
    except Exception as e:
        logger.error(f"Error general: {str(e)}")
        import traceback
        logger.error(traceback.format_exc())
        sys.exit(1)
    finally:
        # Limpiar memoria al finalizar
        gc.collect()
        torch.cuda.empty_cache() if torch.cuda.is_available() else None


