package com.tienda.tienda.dao;

import com.tienda.tienda.entities.Socio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DaoSocio extends JpaRepository<Socio, Integer> {

    Optional<Socio> findByEmail(String email);
    Socio existsByEmail(String email);
}
