package com.tienda.tienda.dao;

import com.tienda.tienda.entities.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DaoUsuario extends JpaRepository<Usuario, String> {
    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    @Lock(LockModeType.PESSIMISTIC_WRITE)  // Bloqueo exclusivo para escritura
    Optional<Usuario> findByEmailWithLock(@Param("email") String email);
    
}
