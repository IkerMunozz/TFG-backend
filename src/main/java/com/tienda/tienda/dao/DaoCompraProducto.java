package com.tienda.tienda.dao;

import com.tienda.tienda.entities.Compra;
import com.tienda.tienda.entities.CompraProducto;
import com.tienda.tienda.entities.CompraProductoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DaoCompraProducto extends JpaRepository<CompraProducto, CompraProductoId> {
}
