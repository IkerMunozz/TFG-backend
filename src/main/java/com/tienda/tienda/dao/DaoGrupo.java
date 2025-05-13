package com.tienda.tienda.dao;


import com.tienda.tienda.entities.Grupo;
import com.tienda.tienda.entities.GrupoId;
import com.tienda.tienda.entities.Socio;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DaoGrupo extends JpaRepository<Grupo, GrupoId> {


}
