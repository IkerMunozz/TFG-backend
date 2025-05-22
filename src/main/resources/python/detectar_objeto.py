from ultralytics import YOLO
import sys
import os
import torch
import logging

# Configurar logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def detect_objects(image_path):
    try:
        logger.info(f"Intentando detectar objetos en: {image_path}")
        logger.info(f"Verificando disponibilidad de CUDA...")
        logger.info(f"CUDA disponible: {torch.cuda.is_available()}")
        if torch.cuda.is_available():
            logger.info(f"Dispositivo CUDA: {torch.cuda.get_device_name(0)}")
        
        logger.info("Cargando modelo YOLO...")
        model = YOLO('yolov8n.pt', task='detect')
        logger.info("Modelo cargado correctamente")
        
        logger.info("Realizando detección...")
        results = model(image_path, verbose=False, conf=0.25, iou=0.45)
        predictions = results[0].boxes
        num_objects = len(predictions)
        logger.info(f"Número de objetos detectados: {num_objects}")
        
        if num_objects > 0:
            logger.info("Clases detectadas:")
            for box in predictions:
                logger.info(f"- Clase: {box.cls.item()}, Confianza: {box.conf.item():.2f}")
        
        # Liberar memoria
        del model
        torch.cuda.empty_cache() if torch.cuda.is_available() else None
        return num_objects > 0
    except Exception as e:
        logger.error(f"Error durante la detección: {str(e)}")
        import traceback
        logger.error("Traceback completo:")
        logger.error(traceback.format_exc())
        return False

if __name__ == "__main__":
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
        logger.info("Se detectaron objetos en la imagen")
        sys.exit(0)
    else:
        logger.error("No se detectó ningún objeto en la imagen")
        sys.exit(1)


