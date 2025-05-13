package com.tienda.tienda.dao;

import com.tienda.tienda.entities.Socio;
import com.tienda.tienda.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DaoToken extends JpaRepository<Token, String> {


    Optional<Token> findByValue(String value);


}
