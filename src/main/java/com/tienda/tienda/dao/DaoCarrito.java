package com.tienda.tienda.dao;

import com.tienda.tienda.entities.Carrito;
import com.tienda.tienda.entities.CarritoId;
import com.tienda.tienda.entities.Producto;
import com.tienda.tienda.entities.Socio;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DaoCarrito extends JpaRepository<Carrito, CarritoId> {

    List<Carrito> findByIdusuario(Socio socio);
    void deleteByIdusuario(Socio socio);
    Optional <Carrito> findByIdusuarioAndIdproducto(Socio usuario, Producto producto);
    void deleteById_Idproducto(Integer idProducto);



}




