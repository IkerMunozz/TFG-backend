package com.tienda.tienda.service;

import java.io.File;
import com.tienda.tienda.dao.*;
import com.tienda.tienda.dto.CarritoDTO;
import com.tienda.tienda.dto.FavoritoDTO;
import com.tienda.tienda.dto.ProductoDTO;
import com.tienda.tienda.entities.*;
import com.tienda.tienda.exception.TiendaException;

import jakarta.persistence.criteria.CriteriaBuilder.In;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.net.URISyntaxException;

@Service
public class TiendaServicioImpl implements TiendaServicio {


    @Autowired
    DaoCarrito daoCarrito;

    @Autowired
    DaoSocio daoSocio;

    @Autowired
    DaoCompra daoCompra;

    @Autowired
    DaoGrupo daoGrupo;

    @Autowired
    DaoUsuario daoUsuario;

    @Autowired
    DaoCompraProducto daoCompraProducto;

    @Autowired
    DaoProducto daoProducto;

    @Autowired
    DaoToken daoToken;

    @Autowired
    DaoFavorito daoFavorito;


    public TiendaServicioImpl(DaoCarrito daoCarrito, DaoSocio daoSocio, DaoCompra daoCompra, DaoGrupo daoGrupo, DaoUsuario daoUsuario, DaoProducto daoProducto, DaoCompraProducto daoCompraProducto, DaoToken daoToken) {
        this.daoCarrito = daoCarrito;
        this.daoSocio = daoSocio;
        this.daoCompra = daoCompra;
        this.daoGrupo = daoGrupo;
        this.daoUsuario = daoUsuario;
        this.daoProducto = daoProducto;
        this.daoCompraProducto = daoCompraProducto;
        this.daoToken = daoToken;
    }

    @Override
    public Usuario insertarUsuario(Usuario usuario) {
        Usuario u = new Usuario();

        return daoUsuario.save(usuario);
    }

    @Override
    public Optional<Token> obtenerTokenPorValor(String token) {
        return daoToken.findByValue(token);
    }



    @Override
    public Optional<Token> insertarToken(Token token) {
        return Optional.of(daoToken.save(token));
    }

    @Override
    public  Grupo insertarGrupo(String email) {
    // 1. Buscar o crear el usuario
    Usuario usuario = daoUsuario.findById(email).orElseGet(() -> {
        Usuario nuevo = new Usuario();
        nuevo.setEmail(email); 
        return daoUsuario.save(nuevo);
    });

    // 2. Crear la clave compuesta
    GrupoId id = new GrupoId();
    id.setIdgrupo("SOCIO");        
    id.setIdusuario(usuario.getEmail());

    // 3. Crear y guardar el grupo
    Grupo grupo = new Grupo();
    grupo.setId(id);
    grupo.setIdusuario(usuario);

    return daoGrupo.save(grupo);
}


@Transactional
@Override
public Producto addProducto(Producto producto, String tokenHeader, String rutaImagenAbsoluta, StringBuilder salidaPython) {
    try {
        System.out.println("=== Iniciando proceso de detección de objetos ===");
        System.out.println("Ruta de la imagen: " + rutaImagenAbsoluta);

        String token = tokenHeader.replace("Bearer ", "").trim();
        System.out.println("Token recibido: " + token);

        Optional<Token> tokenOpt = daoToken.findByValue(token);
        if (tokenOpt.isEmpty()) {
            System.out.println("ERROR: Token no encontrado");
            throw new TiendaException("Token inválido o expirado", HttpStatus.UNAUTHORIZED);
        }

        String emailVendedor = tokenOpt.get().getEmail();
        System.out.println("Email del vendedor: " + emailVendedor);

        Usuario vendedor = daoUsuario.findByEmailWithLock(emailVendedor)
                .orElseThrow(() -> {
                    System.out.println("ERROR: Vendedor no encontrado para email: " + emailVendedor);
                    return new TiendaException("Vendedor no encontrado", HttpStatus.NOT_FOUND);
                });

        producto.setIdvendedor(vendedor);
        System.out.println("Vendedor asignado correctamente");

        String scriptPath = "/app/python/detectar_objeto.py";
        System.out.println("Ruta del script Python: " + scriptPath);

        try {
            File scriptFile = new File(scriptPath);
            System.out.println("¿Existe script?: " + scriptFile.exists());
            System.out.println("¿Es ejecutable?: " + scriptFile.canExecute());
            System.out.println("Ruta absoluta del script: " + scriptFile.getAbsolutePath());

            if (!scriptFile.exists()) {
                throw new TiendaException("No se encontró el script de detección en: " + scriptPath, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (!scriptFile.canExecute()) {
                throw new TiendaException("El script de detección no tiene permisos de ejecución", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            File imagenFile = new File(rutaImagenAbsoluta);
            if (!imagenFile.exists()) {
                throw new TiendaException("No se encontró la imagen en: " + rutaImagenAbsoluta, HttpStatus.BAD_REQUEST);
            }
            if (!imagenFile.canRead()) {
                throw new TiendaException("No se puede leer la imagen en: " + rutaImagenAbsoluta, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if (imagenFile.length() == 0) {
                throw new TiendaException("La imagen está vacía", HttpStatus.BAD_REQUEST);
            }

            System.out.println("Verificando si 'python' está disponible...");
            try {
                Process checkPython = new ProcessBuilder("which", "python").start();
                BufferedReader pythonReader = new BufferedReader(new InputStreamReader(checkPython.getInputStream()));
                pythonReader.lines().forEach(line -> System.out.println("Ubicación de Python: " + line));
            } catch (IOException e) {
                System.out.println("No se pudo verificar la ubicación de Python");
            }

            System.out.println("Ejecutando script Python con ruta de imagen...");
            ProcessBuilder pb = new ProcessBuilder(
                    "python",
                    "-u",
                    scriptPath,
                    rutaImagenAbsoluta
            );

            Map<String, String> env = pb.environment();
            env.put("PYTHONUNBUFFERED", "1");
            env.put("TORCH_CUDA_VERSION", "cpu");
            env.put("PYTHONIOENCODING", "utf-8");
            env.put("PYTHONPATH", "/app/python");
            env.put("PYTHONHASHSEED", "0");

            pb.redirectErrorStream(true);

            Process process;
            try {
                System.out.println("Iniciando proceso Python...");
                process = pb.start();
                System.out.println("Proceso Python iniciado con PID: " + process.pid());
            } catch (IOException e) {
                System.out.println("IOException al ejecutar el proceso Python:");
                e.printStackTrace();
                salidaPython.append("ERROR: ").append(e.getMessage());
                throw new TiendaException("No se pudo ejecutar el script de detección: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }

            StringBuilder output = new StringBuilder();
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[PYTHON] " + line);
                        output.append(line).append("\n");
                        salidaPython.append(line).append("\n");
                        System.out.flush(); // Forzar escritura inmediata
                    }
                } catch (IOException e) {
                    System.out.println("Error leyendo la salida del script: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            outputThread.start();

            try {
                boolean completed = process.waitFor(120, TimeUnit.SECONDS);
                if (!completed) {
                    process.destroyForcibly();
                    throw new TiendaException("El proceso de detección excedió el tiempo límite de 2 minutos", HttpStatus.REQUEST_TIMEOUT);
                }

                outputThread.join(5000);

                int exitCode = process.exitValue();
                System.out.println("Código de salida del script Python: " + exitCode);
                System.out.println("Salida completa del script: " + output.toString());
                System.out.flush(); // Forzar escritura inmediata

                if (exitCode == 1) {
                    throw new TiendaException("No se ha detectado ningún producto en la imagen. Ruta: " + rutaImagenAbsoluta + "\nSalida del script: " + output.toString(), HttpStatus.BAD_REQUEST);
                } else if (exitCode != 0) {
                    throw new TiendaException("Error al procesar la imagen: " + output.toString(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
                throw new TiendaException("El proceso de detección fue interrumpido", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            System.out.println("Proceso de detección completado exitosamente");

        } catch (Exception e) {
            System.out.println("ERROR GENERAL dentro del bloque de detección: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        System.out.println("Guardando producto en la base de datos...");
        Producto productoGuardado = daoProducto.save(producto);
        System.out.println("Producto guardado correctamente con ID: " + productoGuardado.getId());
        return productoGuardado;

    } catch (Exception e) {
        System.out.println("ERROR GENERAL: " + e.getMessage());
        e.printStackTrace();
        throw e;
    }

}   





    




    public List<ProductoDTO> getAllProductosDTO() {
        List<Producto> productos = daoProducto.findAll();
        return productos.stream()
                .filter(p -> !p.isVendido())  // Productos que no estan vendidos
                .map(ProductoDTO::new)
                .collect(Collectors.toList());  
    }
    


    @Override
    public void deleteProducto(Integer productoId) {

        Optional<Producto> paborrar = daoProducto.findById(productoId);

        if (!paborrar.isPresent()) {
            throw new TiendaException("El producto no existe", HttpStatus.NOT_FOUND);
        }

        daoProducto.delete(paborrar.get());
    }
    @Override
    public Producto updateProducto(int idproducto, Producto nuevosDatos) {
        Optional<Producto> paeditar = daoProducto.findById(idproducto);
        if (!paeditar.isPresent()) {
            throw new TiendaException("El producto no existe", HttpStatus.NOT_FOUND);
        }

        Producto existente = paeditar.get();

        if (nuevosDatos.getNombre() != null) {
            existente.setNombre(nuevosDatos.getNombre());
        }

        if (nuevosDatos.getPrecio() != null) {
            existente.setPrecio(nuevosDatos.getPrecio());
        }

        if (nuevosDatos.getDescripcion() != null) {
            existente.setDescripcion(nuevosDatos.getDescripcion());
        }

        if (nuevosDatos.getImagen() != null) {
            existente.setImagen(nuevosDatos.getImagen());
        }

        return daoProducto.save(existente);
    }

    @Override
    public void agregarProductoAlCarrito(Integer idProducto, String tokenHeader) {
        // 1. Extraer el token real (sin "Bearer ")
        String token = tokenHeader.replace("Bearer ", "").trim();

        // 2. Buscar el token en la base de datos
        Optional<Token> tokenOpt = daoToken.findByValue(token);
        if (tokenOpt.isEmpty()) {
            throw new TiendaException("Token inválido o expirado", HttpStatus.UNAUTHORIZED);
        }

        // 3. Obtener el email del usuario desde el token
        String emailUsuario = tokenOpt.get().getEmail();

        // 4. Buscar el usuario (Socio) con ese email
        Socio socio = daoSocio.findByEmail(emailUsuario)
                .orElseThrow(() -> new TiendaException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        // 5. Buscar el producto
        Producto producto = daoProducto.findById(idProducto)
                .orElseThrow(() -> new TiendaException("Producto no encontrado", HttpStatus.NOT_FOUND));

        // 6. Verifica que el producto no sea null antes de asignarlo
        if (producto == null) {
            throw new TiendaException("El producto es nulo", HttpStatus.BAD_REQUEST);
        }

        // 7. Crear la clave primaria compuesta
        CarritoId carritoId = new CarritoId();
        carritoId.setIdusuario(socio.getId());  // Asigna el id del usuario (Socio)
        carritoId.setIdproducto(producto.getId());  // Asigna el id del producto

        // 8. Crear la entrada de carrito
        Carrito carrito = new Carrito();
        carrito.setId(carritoId);  // Asigna el CarritoId al carrito

        // 9. Asignar la relación ManyToOne de 'idusuario' y 'idproducto'
        carrito.setIdusuario(socio);  
        carrito.setIdproducto(producto); 

        // 10. Guardar en la base de datos
        daoCarrito.save(carrito);
    }

    @Override
    public List<CarritoDTO> obtenerProductosDelCarrito(String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();

        Optional<Token> tokenOpt = daoToken.findByValue(token);
        if (tokenOpt.isEmpty()) {
            throw new TiendaException("Token inválido o expirado", HttpStatus.UNAUTHORIZED);
        }

        String emailUsuario = tokenOpt.get().getEmail();
        Socio socio = daoSocio.findByEmail(emailUsuario)
                .orElseThrow(() -> new TiendaException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        List<Carrito> carritoList = daoCarrito.findByIdusuario(socio);

        return carritoList.stream()
                .map(c -> {
                    Producto p = c.getIdproducto();
                    return new CarritoDTO(
                            p.getId(),
                            p.getNombre(),
                            p.getDescripcion(),
                            p.getPrecio(),
                            p.getImagen()
                    );
                })
                .collect(Collectors.toList());
    }

    public boolean productoYaEnCarrito(String tokenHeader, Integer idProducto) {
        String token = tokenHeader.replace("Bearer ", "").trim();
    
        Optional<Token> tokenOpt = daoToken.findByValue(token);
        if (tokenOpt.isEmpty()) {
            throw new TiendaException("Token inválido o expirado", HttpStatus.UNAUTHORIZED);
        }
    
        String emailUsuario = tokenOpt.get().getEmail();
        Socio socio = daoSocio.findByEmail(emailUsuario)
                .orElseThrow(() -> new TiendaException("Usuario no encontrado", HttpStatus.NOT_FOUND));
    
        List<Carrito> carritoList = daoCarrito.findByIdusuario(socio);
    
        return carritoList.stream()
                .anyMatch(c -> c.getIdproducto().getId().equals(idProducto));
    }
    
    
    @Override
    public Compra crearCompra(String tokenHeader) {
        // 1. Extraer el token real (sin "Bearer ")
        String token = tokenHeader.replace("Bearer ", "").trim();

        // 2. Buscar el token en la base de datos
        Optional<Token> tokenOpt = daoToken.findByValue(token);
        if (tokenOpt.isEmpty()) {
            throw new TiendaException("Token inválido o expirado", HttpStatus.UNAUTHORIZED);
        }

        // 3. Obtener el email del usuario desde el token
        String emailUsuario = tokenOpt.get().getEmail();

        // 4. Buscar el usuario (Socio) con ese email
        Socio socio = daoSocio.findByEmail(emailUsuario)
                .orElseThrow(() -> new TiendaException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        // 5. Obtener los productos del carrito del comprador
        List<Carrito> carritoList = daoCarrito.findByIdusuario(socio);

        // 6. Inicializar el total de la compra y la lista de productos
        BigDecimal total = BigDecimal.ZERO;
        List<Producto> productos = new ArrayList<>();

        // 7. Calcular el total y preparar los productos para la compra
        for (Carrito item : carritoList) {
            Producto producto = item.getIdproducto();

            // Verificar si el producto ya ha sido vendido
            if (producto.isVendido()) {
                throw new TiendaException("Producto ya vendido: " + producto.getNombre(), HttpStatus.CONFLICT);
            }

            total = total.add(producto.getPrecio());
            productos.add(producto);

            if(socio.getSaldo().compareTo(total) < 0) {
                throw new TiendaException("Saldo insuficiente", HttpStatus.CONFLICT);
            }

            // Marcar el producto como vendido
            producto.setVendido(true);
            daoProducto.save(producto);  // Guardar el estado actualizado del producto

                socio.setSaldo(socio.getSaldo().subtract(producto.getPrecio()));
    
                Usuario usuario = producto.getIdvendedor();
                Socio vendedor = daoSocio.findByEmail(usuario.getEmail())
                        .orElseThrow(() -> new TiendaException("Vendedor no encontrado", HttpStatus.NOT_FOUND));
                vendedor.setSaldo(vendedor.getSaldo().add(producto.getPrecio()));
                daoSocio.save(vendedor);

        }

        // 8. Crear la compra y asignarle el comprador
        Compra compra = new Compra();
        compra.setIdcomprador(socio);  // Asignar el socio como comprador
        compra.setFechaCompra(Instant.now());
        compra.setTotal(total);  // Asignar el total calculado de la compra

        // 9. Guardar la compra en la base de datos
        Compra compraGuardada = daoCompra.save(compra);

        // 10. Insertar los productos en la tabla compra_producto
        for (Producto producto : productos) {
            CompraProducto cp = new CompraProducto();

            // Crear la clave primaria compuesta para la tabla compra_producto
            CompraProductoId cpId = new CompraProductoId();
            cpId.setIdcompra(compraGuardada.getId());  // ID de la compra guardada
            cpId.setIdproducto(producto.getId());  // ID del producto

            cp.setId(cpId);  // Asignar la clave primaria compuesta
            cp.setIdcompra(compraGuardada);  // Relación con la compra
            cp.setIdproducto(producto);  // Relación con el producto
            cp.setCantidad(1);  // Establecer la cantidad de cada producto como 1 (ajustar según sea necesario)

            // Guardar en la tabla compra_producto
            daoCompraProducto.save(cp);
        }

        // 11. Vaciar el carrito del usuario
        daoCarrito.deleteAll(carritoList);

        // 12. El método retorna la compra creada
        return compraGuardada;
    }

    @Override
    public void deleteProductoCarrito(int idproducto, String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();
        Token tokenEntity = daoToken.findByValue(token)
                .orElseThrow(() -> new TiendaException("Token inválido", HttpStatus.UNAUTHORIZED));
        
        Socio socio = daoSocio.findByEmail(tokenEntity.getEmail())
                .orElseThrow(() -> new TiendaException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        
        Producto producto = daoProducto.findById(idproducto)
                .orElseThrow(() -> new TiendaException("Producto no encontrado", HttpStatus.NOT_FOUND));

        Carrito item = daoCarrito.findByIdusuarioAndIdproducto(socio, producto)
                .orElseThrow(() -> new TiendaException("Producto no está en el carrito", HttpStatus.NOT_FOUND));

        daoCarrito.delete(item);
    }

   @Override
    public void agregarSaldo(Integer idSocio, BigDecimal saldo) {
        
        Socio socio = daoSocio.findById(idSocio)
                .orElseThrow(() -> new TiendaException("Socio no encontrado", HttpStatus.NOT_FOUND));
        
        socio.setSaldo(saldo); 
        daoSocio.save(socio);
    }


    @Override
    public List<ProductoDTO> obtenerProductosPorEstadoYEmail(String tokenHeader, boolean vendido) {
        // Extraer el token
        String token = tokenHeader.replace("Bearer ", "").trim();

        // Buscar el token en la base de datos
        Token tokenEntity = daoToken.findByValue(token)
            .orElseThrow(() -> new TiendaException("Token inválido o expirado", HttpStatus.UNAUTHORIZED));

        // Buscar el usuario asociado al token
        Usuario usuario = daoUsuario.findById(tokenEntity.getEmail())
            .orElseThrow(() -> new TiendaException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        try {
            List<ProductoDTO> productos = daoProducto.findByIdvendedorAndVendido(usuario, vendido);
            return productos;

        } catch (Exception e) {
            throw new TiendaException("Error al obtener los productos: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public List<ProductoDTO> buscarProductos(String terminoBusqueda) {
        List<Producto> productos = daoProducto.findByNombreContainingIgnoreCaseAndVendidoFalse(terminoBusqueda);
        return productos.stream()
            .map(ProductoDTO::new)
            .collect(Collectors.toList());    
    }


    @Override
    public void agregarProductoAFavoritos(Integer idProducto, String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();

        Optional<Token> tokenOpt = daoToken.findByValue(token);
        if (tokenOpt.isEmpty()) {
            throw new TiendaException("Token inválido o expirado", HttpStatus.UNAUTHORIZED);
        }

        String emailUsuario = tokenOpt.get().getEmail();
        Socio socio = daoSocio.findByEmail(emailUsuario)
                .orElseThrow(() -> new TiendaException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        Producto producto = daoProducto.findById(idProducto)
                .orElseThrow(() -> new TiendaException("Producto no encontrado", HttpStatus.NOT_FOUND));

        FavoritoId favoritosId = new FavoritoId();
        favoritosId.setIdusuario(socio.getId());
        favoritosId.setIdproducto(producto.getId());

        Favorito favorito = new Favorito();
        favorito.setId(favoritosId);
        favorito.setIdusuario(socio);
        favorito.setIdproducto(producto);

        daoFavorito.save(favorito);
}


@Override
public List <FavoritoDTO> obtenerProductosFavoritos(String tokenHeader) {
    String token = tokenHeader.replace("Bearer ", "").trim();

    Optional<Token> tokenOpt = daoToken.findByValue(token);
    if (tokenOpt.isEmpty()) {
        throw new TiendaException("Token inválido o expirado", HttpStatus.UNAUTHORIZED);
    }

    String emailUsuario = tokenOpt.get().getEmail();
    Socio socio = daoSocio.findByEmail(emailUsuario)
            .orElseThrow(() -> new TiendaException("Usuario no encontrado", HttpStatus.NOT_FOUND));

    List<Favorito> favoritosList = daoFavorito.findByIdusuario(socio);

    return favoritosList.stream()
            .map(f -> {
                Producto p = f.getIdproducto();
                return new FavoritoDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getDescripcion(),
                        p.getPrecio(),
                        p.getImagen()
                );
            })
            .collect(Collectors.toList());
}


@Override
    public void deleteProductoFavorito(int idproducto, String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "").trim();

        Token tokenEntity = daoToken.findByValue(token)
                .orElseThrow(() -> new TiendaException("Token inválido", HttpStatus.UNAUTHORIZED));

        Socio socio = daoSocio.findByEmail(tokenEntity.getEmail())
                .orElseThrow(() -> new TiendaException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        Producto producto = daoProducto.findById(idproducto)
                .orElseThrow(() -> new TiendaException("Producto no encontrado", HttpStatus.NOT_FOUND));

        Favorito item = daoFavorito.findByIdusuarioAndIdproducto(socio, producto)
                .orElseThrow(() -> new TiendaException("Producto no está en favoritos", HttpStatus.NOT_FOUND));

        daoFavorito.delete(item);
    }

        public boolean productoYaEnFavoritos(String tokenHeader, Integer idProducto) {
        String token = tokenHeader.replace("Bearer ", "").trim();
    
        Optional<Token> tokenOpt = daoToken.findByValue(token);
        if (tokenOpt.isEmpty()) {
            throw new TiendaException("Token inválido o expirado", HttpStatus.UNAUTHORIZED);
        }
    
        String emailUsuario = tokenOpt.get().getEmail();
        Socio socio = daoSocio.findByEmail(emailUsuario)
                .orElseThrow(() -> new TiendaException("Usuario no encontrado", HttpStatus.NOT_FOUND));
    
        List<Favorito> favoritoList = daoFavorito.findByIdusuario(socio);
    
        return favoritoList.stream()
                .anyMatch(c -> c.getIdproducto().getId().equals(idProducto));
    }


  @Transactional
        public void eliminarProductoConReferencias(Integer idproducto) {
            daoCarrito.deleteById_Idproducto(idproducto);
            daoFavorito.deleteById_Idproducto(idproducto);
            daoProducto.deleteById(idproducto);
        }

      public Socio obtenerSocioPorToken(String token) throws TiendaException {
            Token tokenEntity = daoToken.findByValue(token)
                .orElseThrow(() -> new TiendaException("Token no válido", HttpStatus.UNAUTHORIZED));

        return daoSocio.findByEmail(tokenEntity.getEmail())
                .orElseThrow(() -> new TiendaException("No se encontró un socio con ese email", HttpStatus.NOT_FOUND));
        }


     public String obtenerEmailDesdeToken(String token) throws TiendaException {
    Token tokenEntity = daoToken.findByValue(token)
        .orElseThrow(() -> new TiendaException("Token no válido", HttpStatus.UNAUTHORIZED));
    return tokenEntity.getEmail();
    }

    

        public int contarArticulosPublicados(String token) {
            String email = obtenerEmailDesdeToken(token); 
            return daoProducto.contarArticulosPublicados(email);
        }

        public int contarArticulosVendidos(String token) {
            String email = obtenerEmailDesdeToken(token); 
            return daoProducto.contarArticulosVendidos(email);
        }

        public int contarArticulosEnVenta(String token) {
            String email = obtenerEmailDesdeToken(token); 
            return daoProducto.contarArticulosEnVenta(email);
        }

        public int contarArticulosComprados(String token) {
            String email = obtenerEmailDesdeToken(token); 
            return daoProducto.contarArticulosComprados(email);
        }


        @Override
        public ProductoDTO obtenerProductoPorId(Integer id) {
            Producto producto = daoProducto.findById(id)
                .orElseThrow(() -> new TiendaException("Producto no encontrado", HttpStatus.NOT_FOUND));

            return new ProductoDTO(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getImagen(),
                producto.getIdvendedor().getEmail()
            ); 
        }

        @Override
        public Socio obtenerSocioPorId(Integer id) {
            Socio socio = daoSocio.findById(id)
                .orElseThrow(() -> new TiendaException("Socio no encontrado", HttpStatus.NOT_FOUND));        
            
            return socio;
            
        }

        


















}


