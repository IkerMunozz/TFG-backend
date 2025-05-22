package com.tienda.tienda.rest;

import java.util.Optional;
import com.tienda.tienda.dao.DaoToken;
import com.tienda.tienda.dto.CarritoDTO;
import com.tienda.tienda.dto.FavoritoDTO;
import com.tienda.tienda.dto.ProductoDTO;
import com.tienda.tienda.entities.Compra;
import com.tienda.tienda.entities.Producto;
import com.tienda.tienda.entities.Socio;
import com.tienda.tienda.exception.TiendaException;
import com.tienda.tienda.service.TiendaServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.core.subst.Token;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ui.Model;
import java.util.Map;


@Controller
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


    @GetMapping("/agregarProducto")
    public String mostrarFormularioVender(HttpSession session, 
        Model model) {


        String token = (String) session.getAttribute("token");
        if (token == null) {
            return "login";
        }
        if (token != null) {
        List<CarritoDTO> productosEnCarrito = tiendaServicio.obtenerProductosDelCarrito(token);
        model.addAttribute("productosEnCarrito", productosEnCarrito);
        List<FavoritoDTO> productosFavoritos = tiendaServicio.obtenerProductosFavoritos(token);
        model.addAttribute("productosFavoritos", productosFavoritos);
        }

        return "formvender"; 
    }

    @PostMapping("/agregarProducto")
public String addProducto(
        @RequestParam("nombre") String nombre,
        @RequestParam("precio") BigDecimal precio,
        @RequestParam("descripcion") String descripcion,
        @RequestParam("imagen") MultipartFile imagenFile,
        HttpSession session,
        Model model) {

    try {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No autenticado.");
            return "formvender";
        }

        // Cambiar la ruta a la carpeta fija en el contenedor
        Path uploadPath = Paths.get("/app/uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID() + "-" + imagenFile.getOriginalFilename();
        Path filePath = uploadPath.resolve(filename);

        imagenFile.transferTo(filePath.toFile());

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setDescripcion(descripcion);
        producto.setImagen(filename); // solo nombre para la web
        producto.setFechaSubida(Instant.now());

        // Ruta absoluta válida dentro del contenedor
        String rutaCompletaImagen = filePath.toAbsolutePath().toString();

        StringBuilder salidaPython = new StringBuilder();
        Producto guardado = tiendaServicio.addProducto(producto, token, rutaCompletaImagen, salidaPython);

        if (guardado != null) {
            model.addAttribute("correcto", "Producto subido correctamente.");
        } else {
            model.addAttribute("error", "No se pudo guardar el producto.");
        }

        return "formvender";

    } catch (TiendaException ex) {
        model.addAttribute("error", "Error: " + ex.getMessage());
        return "formvender";
    } catch (Exception ex) {
        ex.printStackTrace();
        model.addAttribute("error", "Error inesperado al subir producto.");
        return "formvender";
    }
}



    

    
    


@GetMapping("/productos")
    public String getAllProductosDTO(Model model, HttpSession session) {

        String token = (String) session.getAttribute("token");
        List<ProductoDTO> productos = tiendaServicio.getAllProductosDTO();

        if (token != null) {
            Socio socioLogueado = tiendaServicio.obtenerSocioPorToken(token);
            if (socioLogueado != null) {
                String emailSocio = socioLogueado.getEmail();
                productos = productos.stream()
                    .filter(p -> !p.getIdVendedor().equals(emailSocio))
                    .collect(Collectors.toList());
            }
        }

        model.addAttribute("productos", productos != null ? productos : new ArrayList<>());

        if (token != null) {
            List<CarritoDTO> productosEnCarrito = tiendaServicio.obtenerProductosDelCarrito(token);
            model.addAttribute("productosEnCarrito", productosEnCarrito);
            List<FavoritoDTO> productosFavoritos = tiendaServicio.obtenerProductosFavoritos(token);
            model.addAttribute("productosFavoritos", productosFavoritos);
        }

        return "index";
    }




    @Transactional
   @PostMapping("/productos/{id}")
    public String deleteProducto(@PathVariable("id") Integer id, RedirectAttributes redirectAttrs) {
        try {
            tiendaServicio.eliminarProductoConReferencias(id);
            redirectAttrs.addFlashAttribute("correcto", "Producto eliminado con éxito.");
        } catch (TiendaException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/api/v1/perfil";
    }


    @GetMapping("/actualizarproductos/{id}")
    public String mostrarFormularioActualizarProducto(
            @PathVariable("id") Integer idproducto,
            Model model) {
        try {
            ProductoDTO productoExistente = tiendaServicio.obtenerProductoPorId(idproducto);
            model.addAttribute("producto", productoExistente);
            return "formActualizarProducto";  
        } catch (TiendaException e) {
            model.addAttribute("error", "No se encontró el producto: " + e.getMessage());
            return "redirect:/api/v1/perfil"; 
        }
    }

   @PostMapping("/actualizarproductos/{id}")
    public String updateProducto(
            @PathVariable("id") int idproducto,
            @ModelAttribute Producto nuevosDatos,
            Model model) {
        try {
            tiendaServicio.updateProducto(idproducto, nuevosDatos); 
            model.addAttribute("mensaje", "Producto actualizado con éxito.");
            return "redirect:/api/v1/perfil";
        } catch (TiendaException e) {
            model.addAttribute("error", "No se pudo actualizar el producto: " + e.getMessage());
            return "formActualizarProducto"; 
        }
    }



@PostMapping("/agregarProductoAlCarrito/{idProducto}")
@ResponseBody
public ResponseEntity<?> agregarProductoAlCarrito(@PathVariable Integer idProducto,
                                                  HttpSession session) {
    String token = (String) session.getAttribute("token");
    if (token == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(Map.of("error", "Debes iniciar sesión."));
    }

    try {
        if (tiendaServicio.productoYaEnCarrito(token, idProducto)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(Map.of("error", "El producto ya está en tu carrito."));
        }

        tiendaServicio.agregarProductoAlCarrito(idProducto, token);
        return ResponseEntity.ok(Map.of("correcto", "Producto agregado al carrito."));
    } catch (TiendaException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Map.of("error", e.getMessage()));
    }
}




    @GetMapping("/carrito")
    @ResponseBody
    public ResponseEntity<?> obtenerCarrito(HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return ResponseEntity.status(401).body("No autenticado");

        List<CarritoDTO> productos = tiendaServicio.obtenerProductosDelCarrito(token);
        return ResponseEntity.ok(productos);
    }



@PostMapping("/crearCompra")
public String crearCompra(HttpSession session, Model model) {
    try {
        String token = (String) session.getAttribute("token");

        if (token == null) {
            throw new TiendaException("No hay token en la sesión", HttpStatus.UNAUTHORIZED);
        }

        Compra compra = tiendaServicio.crearCompra(token);
        
        model.addAttribute("compra", compra);
        model.addAttribute("correcto", "Compra creada con éxito.");
        return "redirect:/api/v1/productos";
    } catch (Exception e) {
        e.printStackTrace(); 
        model.addAttribute("error", "Error inesperado al crear la compra");
        return "index";
    }
}


    

    // // Método para convertir un StackTrace en un String
    // private String getStackTraceAsString(Exception e) {
    //     StringBuilder sb = new StringBuilder();
    //     for (StackTraceElement element : e.getStackTrace()) {
    //         sb.append(element.toString()).append("\n");
    //     }
    //     return sb.toString();
    // }

@PostMapping("/eliminarProductoDelCarrito/{idProducto}")
@ResponseBody
public ResponseEntity<?> eliminarProductoDelCarrito(@PathVariable("idProducto") Integer idProducto,
                                                   HttpSession session) {
    String token = (String) session.getAttribute("token");
    if (token == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(Map.of("error", "Debes iniciar sesión para eliminar productos del carrito."));
    }

    try {
        tiendaServicio.deleteProductoCarrito(idProducto, token);

        // Opcional: puedes devolver el carrito actualizado si quieres refrescarlo en el cliente
        List<CarritoDTO> productosEnCarrito = tiendaServicio.obtenerProductosDelCarrito(token);

        return ResponseEntity.ok(Map.of("correcto", "Producto eliminado del carrito."));


    } catch (TiendaException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Map.of("error", e.getMessage()));
    }
}


    
    @GetMapping("/perfil")
    public String mostrarPerfil(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token != null) {
            model.addAttribute("productosEnCarrito", tiendaServicio.obtenerProductosDelCarrito(token));
            model.addAttribute("productosFavoritos", tiendaServicio.obtenerProductosFavoritos(token));
        }

        List<ProductoDTO> productosEnVenta = tiendaServicio.obtenerProductosPorEstadoYEmail(token, false);
        if(productosEnVenta==null){
            model.addAttribute("error", "No se encontraron productos en venta.");
        }else{
             model.addAttribute("productos", productosEnVenta);
        }
        List<ProductoDTO> productosVendidos = tiendaServicio.obtenerProductosPorEstadoYEmail(token, true);

        if(productosVendidos==null){
            model.addAttribute("error", "No se encontraron productos vendidos.");
        }else{
             model.addAttribute("productosVendidos", productosVendidos);
        }

        Socio socio = tiendaServicio.obtenerSocioPorToken(token); 
        model.addAttribute("socio", socio);
        model.addAttribute("publicados", tiendaServicio.contarArticulosPublicados(token));
        model.addAttribute("vendidos", tiendaServicio.contarArticulosVendidos(token));
        model.addAttribute("enVenta", tiendaServicio.contarArticulosEnVenta(token));
        model.addAttribute("comprados", tiendaServicio.contarArticulosComprados(token));

        return "perfil"; 
    }
    

    @GetMapping("/agregarSaldo/{idSocio}")
    public String mostrarFormularioAgregarSaldo(@PathVariable int idSocio, Model model) {
        Socio socio = tiendaServicio.obtenerSocioPorId(idSocio);
        
        if (socio == null) {
            model.addAttribute("error", "Socio no encontrado");
            return "perfil";  
        }
        
        model.addAttribute("socio", socio);
        return "formAgregarSaldo";  
    }

    @PostMapping("/agregarSaldo/{idSocio}")
    public String agregarSaldo(@PathVariable int idSocio, @RequestParam BigDecimal saldo, Model model) {
        try {
            tiendaServicio.agregarSaldo(idSocio, saldo);
            model.addAttribute("correcto", "Saldo actualizado correctamente");
        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar saldo");
        }
            return "redirect:/api/v1/perfil";     
        }


    @GetMapping("/en-venta")
    public String obtenerProductosEnVenta(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token"); 

        if (token == null) {
            return "redirect:/login"; 
        }else{
            model.addAttribute("productosEnCarrito", tiendaServicio.obtenerProductosDelCarrito(token));
            model.addAttribute("productosFavoritos", tiendaServicio.obtenerProductosFavoritos(token));
        }

        List<ProductoDTO> productosEnVenta = tiendaServicio.obtenerProductosPorEstadoYEmail(token, false);
        if(productosEnVenta==null){
            model.addAttribute("error", "No se encontraron productos en venta.");
        }else{
             model.addAttribute("productos", productosEnVenta);
        }



        return "perfil"; 
    }


    @GetMapping("/vendidos")
    public String obtenerProductosVendidos(HttpSession session, Model model) {
        String token = (String) session.getAttribute("token"); 

        if (token == null) {
            return "redirect:/login"; 
        }else{
            model.addAttribute("productosEnCarrito", tiendaServicio.obtenerProductosDelCarrito(token));
            model.addAttribute("productosFavoritos", tiendaServicio.obtenerProductosFavoritos(token));
        }

        List<ProductoDTO> productosVendidos = tiendaServicio.obtenerProductosPorEstadoYEmail(token, true);

        if(productosVendidos==null){
            model.addAttribute("error", "No se encontraron productos vendidos.");
        }else{
             model.addAttribute("productosVendidos", productosVendidos);
        }



        return "perfil"; 
    }

    @GetMapping("/buscarproductos")
    @ResponseBody
    public List<ProductoDTO> buscarProductos(@RequestParam String terminoBusqueda, HttpSession session) {
        List<ProductoDTO> productos = tiendaServicio.buscarProductos(terminoBusqueda);

        String token = (String) session.getAttribute("token");
        if (token != null) {
            // Si quieres, puedes hacer algo con carrito o favoritos
            List<CarritoDTO> productosEnCarrito = tiendaServicio.obtenerProductosDelCarrito(token);
            List<FavoritoDTO> productosFavoritos = tiendaServicio.obtenerProductosFavoritos(token);
            // pero no necesitas agregarlos al model porque no se usa para vista
        }

        return productos;  // solo devuelves la lista de productos como JSON
    }




@PostMapping("/agregarProductoAFavoritos/{idProducto}")
@ResponseBody
    public ResponseEntity<?> agregarProductoAFavoritos(
            @PathVariable("idProducto") Integer idProducto,
            HttpSession session) {

        String token = (String) session.getAttribute("token");
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "Debes iniciar sesión para agregar a favoritos."));
        }

        try {
            if (tiendaServicio.productoYaEnFavoritos(token, idProducto)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                    .body(Map.of("error", "El producto ya está en favoritos."));
            } else {
                tiendaServicio.agregarProductoAFavoritos(idProducto, token);
                return ResponseEntity.ok(Map.of("correcto", "Producto agregado a favoritos."));
            }
        } catch (TiendaException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", e.getMessage()));
        }
    }

    
    @GetMapping("/productosFavoritos")
    @ResponseBody
    public ResponseEntity<?> obtenerProductosFavoritos(HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("error", "Debes iniciar sesión para ver favoritos."));
        }

        try {
            List<FavoritoDTO> productosFavoritos = tiendaServicio.obtenerProductosFavoritos(token);
            return ResponseEntity.ok(productosFavoritos);  // JSON con los favoritos
        } catch (TiendaException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("error", e.getMessage()));
        }
    }


@PostMapping("/productosFavoritos/{idproducto}")
@ResponseBody
public ResponseEntity<?> deleteProductoFavorito(@PathVariable int idproducto, HttpSession session) {
    String token = (String) session.getAttribute("token");

    if (token == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(Map.of("error", "Debes iniciar sesión para eliminar productos de favoritos."));
    }

    try {
        tiendaServicio.deleteProductoFavorito(idproducto, token);
        return ResponseEntity.ok(Map.of("correcto", "Producto eliminado de favoritos."));
    } catch (TiendaException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Map.of("error", e.getMessage()));
    }
}


@PostMapping("/agregarProductoAlCarritoEnFavoritos/{idProducto}")
@ResponseBody
public ResponseEntity<?> agregarProductoAlCarritoEnFavoritos(@PathVariable("idProducto") Integer idProducto,
                                                             HttpSession session) {
    String token = (String) session.getAttribute("token");

    if (token == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(Map.of("error", "Debes iniciar sesión para agregar productos al carrito."));
    }

    try {
        if (tiendaServicio.productoYaEnCarrito(token, idProducto)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(Map.of("error", "El producto ya está en tu carrito."));
        } else {
            tiendaServicio.agregarProductoAlCarrito(idProducto, token);
            tiendaServicio.deleteProductoFavorito(idProducto, token);

            return ResponseEntity.ok(Map.of("correcto", "Producto agregado al carrito y eliminado de favoritos."));
        }
    } catch (TiendaException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Map.of("error", e.getMessage()));
    }
}



    @GetMapping("/puntosEnvio")
    public String mostrarPuntosDeEnvio(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token != null) {
            model.addAttribute("productosEnCarrito", tiendaServicio.obtenerProductosDelCarrito(token));
            model.addAttribute("productosFavoritos", tiendaServicio.obtenerProductosFavoritos(token));
        }

        return "puntosEnvio"; 
    }

//     @GetMapping("/perfil/detalles")
// public String verPerfil(HttpSession session, Model model) {
//     String token = (String) session.getAttribute("token");

//     if (token == null) {
//         return "redirect:/login";
//     }

//     try {
//         Socio socio = tiendaServicio.obtenerSocioPorToken(token); 
//         model.addAttribute("socio", socio);
//         model.addAttribute("publicados", tiendaServicio.contarArticulosPublicados(token));
//         model.addAttribute("vendidos", tiendaServicio.contarArticulosVendidos(token));
//         model.addAttribute("enVenta", tiendaServicio.contarArticulosEnVenta(token));
//         model.addAttribute("comprados", tiendaServicio.contarArticulosComprados(token));

//     } catch (Exception e) {
//         model.addAttribute("error", "Error al cargar perfil: " + e.getMessage());
//         return "error"; // o una vista personalizada
//     }

//     return "perfil";
// }





}

