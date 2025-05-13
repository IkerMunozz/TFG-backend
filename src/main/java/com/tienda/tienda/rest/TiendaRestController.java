package com.tienda.tienda.rest;

import java.util.Optional;
import com.tienda.tienda.dao.DaoToken;
import com.tienda.tienda.dto.CarritoDTO;
import com.tienda.tienda.dto.FavoritoDTO;
import com.tienda.tienda.dto.ProductoDTO;
import com.tienda.tienda.entities.Compra;
import com.tienda.tienda.entities.Producto;
import com.tienda.tienda.exception.TiendaException;
import com.tienda.tienda.service.TiendaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.subst.Token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TiendaRestController {

    //Inyeccion del servicio

    @Autowired
    TiendaServicio tiendaServicio;
    @Autowired
    DaoToken daoToken;

    private static final Logger logger = LoggerFactory.getLogger(TiendaRestController.class);



    public TiendaRestController(TiendaServicio tiendaServicio) {
        this.tiendaServicio = tiendaServicio;
    }


       
    @PostMapping("/agregarProducto")
    public ResponseEntity<?> addProducto(
            @RequestParam("nombre") String nombre,
            @RequestParam("precio") BigDecimal precio,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("imagen") MultipartFile imagenFile,
            @RequestHeader("Authorization") String authHeader) {
    
        try {
            // Guardar imagen
            String nombreArchivo = UUID.randomUUID() + "-" + imagenFile.getOriginalFilename();
            Path rutaGuardar = Paths.get("src/main/resources/static/uploads/", nombreArchivo).toAbsolutePath();
            Files.copy(imagenFile.getInputStream(), rutaGuardar, StandardCopyOption.REPLACE_EXISTING);
    
            // Crear el producto
            Producto producto = new Producto();
            producto.setNombre(nombre);
            producto.setPrecio(precio);
            producto.setDescripcion(descripcion);
            producto.setImagen(rutaGuardar.toString());
            producto.setFechaSubida(Instant.now());

    
            // Pasar el token directamente al servicio
            Producto guardado = tiendaServicio.addProducto(producto, authHeader);
    
            return ResponseEntity.ok(guardado);
    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la solicitud.");
        }
    }
    

    
    


    @GetMapping("/productos")
    public ResponseEntity<List<ProductoDTO>> getAllProductosDTO() {
    List<ProductoDTO> productos = tiendaServicio.getAllProductosDTO();
    return new ResponseEntity<>(productos, HttpStatus.OK);
}



    @DeleteMapping("/productos/{id}")
    public ResponseEntity<String> deleteProducto(@PathVariable("id") Integer id) {
        try {
            tiendaServicio.deleteProducto(id);  // Llamamos al servicio para eliminar el producto
            return new ResponseEntity<>("Producto eliminado con éxito", HttpStatus.OK);
        } catch (TiendaException e) {
            // En caso de que el producto no exista, devolvemos el error correspondiente
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/productos/{id}")
    public ResponseEntity<Producto> updateProducto(@PathVariable("id") int idproducto, @RequestBody Producto nuevosDatos) {
        try {
            Producto productoActualizado = tiendaServicio.updateProducto(idproducto, nuevosDatos);
            return new ResponseEntity<>(productoActualizado, HttpStatus.OK);
        } catch (TiendaException e) {
            // En caso de que el producto no exista, devolvemos el error correspondiente
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/agregarProductoAlCarrito/{idProducto}")
    public ResponseEntity<String> agregarProductoAlCarrito(@PathVariable("idProducto") Integer idProducto, @RequestHeader("Authorization") String authHeader) {
        try {
            tiendaServicio.agregarProductoAlCarrito(idProducto, authHeader);  // Llamamos al servicio para agregar el producto al carrito
            return new ResponseEntity<>("Producto agregado al carrito con éxito", HttpStatus.OK);
        } catch (TiendaException e) {
            // En caso de que el producto no exista, devolvemos el error correspondiente
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/productosCarrito")
    public ResponseEntity<List<CarritoDTO>> obtenerProductosDelCarrito(
            @RequestHeader("Authorization") String authHeader) {

        try {
            List<CarritoDTO> productosEnCarrito = tiendaServicio.obtenerProductosDelCarrito(authHeader);

            // Retornar los productos del carrito con código HTTP 200 OK
            return new ResponseEntity<>(productosEnCarrito, HttpStatus.OK);
        } catch (TiendaException e) {
            // Si ocurre un error, retornar el mensaje de la excepción con código HTTP correspondiente
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/crearCompra")
    public Compra crearCompra(@RequestHeader("Authorization") String tokenHeader) {
        try {
            // Log que indica que se está creando una compra
            logger.info("Iniciando la creación de la compra para el token: {}", tokenHeader);
    
            // Delegamos la creación de la compra al servicio
            Compra compra = tiendaServicio.crearCompra(tokenHeader);
    
            // Log para confirmar la creación de la compra
            logger.info("Compra creada exitosamente: {}", compra);
    
            return compra;
        } catch (TiendaException e) {
            // Capturamos cualquier excepción personalizada para manejarla adecuadamente
            logger.error("Error en la creación de la compra: {}", e.getMessage(), e);
            // Lanzamos la excepción nuevamente para que el controlador la maneje
            throw new TiendaException("Error al crear la compra. Detalles: " + e.getMessage(), e.getStatus());
        } catch (Exception e) {
            // Manejo de errores generales
            logger.error("Error inesperado al crear la compra: {}", e.getMessage(), e);
            // Lanzamos la excepción con más detalle para que el cliente reciba información
            throw new TiendaException("Error inesperado al crear la compra. Detalles: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    

    // Método para convertir un StackTrace en un String
    private String getStackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    @DeleteMapping("/productosCarrito/{idproducto}")
    public void deleteProductoCarrito(@PathVariable int idproducto, @RequestHeader("Authorization") String token) {
        tiendaServicio.deleteProductoCarrito(idproducto, token);
    }

    @PostMapping("/agregarSaldo/{idSocio}")
    public void agregarSaldo(@PathVariable int idSocio, @RequestBody BigDecimal saldo) {
        tiendaServicio.agregarSaldo(idSocio, saldo);
    }

    @GetMapping("/en-venta")
    public List<ProductoDTO> obtenerProductosEnVenta(@RequestHeader("Authorization") String tokenHeader) {
        return tiendaServicio.obtenerProductosPorEstadoYEmail(tokenHeader, false);
    }

    @GetMapping("/vendidos")
    public List<ProductoDTO> obtenerProductosVendidos(@RequestHeader("Authorization") String tokenHeader) {
        return tiendaServicio.obtenerProductosPorEstadoYEmail(tokenHeader, true);
    }

    @GetMapping("/buscarproductos")
    public List<ProductoDTO> buscarProductos(@RequestParam String terminoBusqueda) {
        return tiendaServicio.buscarProductos(terminoBusqueda);
    }

    @PostMapping("/agregarProductoAFavoritos/{idProducto}")
    public ResponseEntity<String> agregarProductoAFavoritos(
            @PathVariable("idProducto") Integer idProducto,
            @RequestHeader("Authorization") String authHeader) {
        try {
            tiendaServicio.agregarProductoAFavoritos(idProducto, authHeader);
            return new ResponseEntity<>("Producto agregado a favoritos con éxito", HttpStatus.OK);
        } catch (TiendaException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/productosFavoritos")
    public ResponseEntity<List<FavoritoDTO>> obtenerProductosFavoritos(
            @RequestHeader("Authorization") String authHeader) {
        try {
            List<FavoritoDTO> productosFavoritos = tiendaServicio.obtenerProductosFavoritos(authHeader);
            return new ResponseEntity<>(productosFavoritos, HttpStatus.OK);
        } catch (TiendaException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/productosFavoritos/{idproducto}")
    public void deleteProductoFavorito(@PathVariable int idproducto, @RequestHeader("Authorization") String token) {
        tiendaServicio.deleteProductoFavorito(idproducto, token);
    }

}

