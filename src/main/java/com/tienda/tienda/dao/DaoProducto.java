package com.tienda.tienda.dao;

import com.tienda.tienda.dto.ProductoDTO;
import com.tienda.tienda.entities.Producto;
import com.tienda.tienda.entities.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DaoProducto extends JpaRepository<Producto, Integer> {

    List<ProductoDTO> findByIdvendedorAndVendido(Usuario idvendedor, Boolean vendido);
    List<ProductoDTO> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByNombreContainingIgnoreCaseAndVendidoFalse(String nombre);

    @Query(value = "SELECT COUNT(*) FROM producto p WHERE p.idvendedor = :email", nativeQuery = true)
    int contarArticulosPublicados(@Param("email") String email);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.idvendedor.email = :email AND p.vendido = true")
    int contarArticulosVendidos(@Param("email") String email);

    @Query("SELECT COUNT(p) FROM Producto p WHERE p.idvendedor.email = :email AND p.vendido = false")
    int contarArticulosEnVenta(@Param("email") String email);

    @Query("SELECT COUNT(c) FROM Compra c WHERE c.idcomprador.email = :email")
    int contarArticulosComprados(@Param("email") String email);


}
