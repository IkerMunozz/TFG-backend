package com.tienda.tienda.dao;

import com.tienda.tienda.dto.ProductoDTO;
import com.tienda.tienda.entities.Producto;
import com.tienda.tienda.entities.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DaoProducto extends JpaRepository<Producto, Integer> {

    List<ProductoDTO> findByIdvendedorAndVendido(Usuario idvendedor, Boolean vendido);
    List<ProductoDTO> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByNombreContainingIgnoreCaseAndVendidoFalse(String nombre);



}
