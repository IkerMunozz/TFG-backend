package com.tienda.tienda.dao;

import com.tienda.tienda.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DaoCompra extends JpaRepository<Compra, Integer> {
}
