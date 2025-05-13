package com.tienda.tienda.service;

import com.tienda.tienda.dto.CarritoDTO;
import com.tienda.tienda.dto.FavoritoDTO;
import com.tienda.tienda.dto.ProductoDTO;
import com.tienda.tienda.entities.*;

import jakarta.persistence.criteria.CriteriaBuilder.In;

import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TiendaServicio {


    Usuario insertarUsuario(Usuario usuario);
    Optional<Token> obtenerTokenPorValor(String token);
    Grupo insertarGrupo(String email);
    Optional<Token> insertarToken(Token token);
    Producto addProducto(Producto producto, String emailVendedor);
    List<ProductoDTO> getAllProductosDTO();
    void deleteProducto(Integer idproducto);
    Producto updateProducto(int idproducto, Producto nuevosDatos);
    void agregarProductoAlCarrito(Integer idProducto, String tokenHeader);
    List<CarritoDTO> obtenerProductosDelCarrito(String tokenHeader);
    Compra crearCompra(String tokenHeader);
    void deleteProductoCarrito(int idproducto, String tokenHeader);
    void agregarSaldo(Integer idSocio, BigDecimal saldo);
    List<ProductoDTO> obtenerProductosPorEstadoYEmail(String email, boolean vendido);
    List<ProductoDTO> buscarProductos(String terminoBusqueda);
    void agregarProductoAFavoritos(Integer idProducto, String tokenHeader);
    List<FavoritoDTO> obtenerProductosFavoritos(String tokenHeader);
    void deleteProductoFavorito(int idproducto, String tokenHeader);

}

