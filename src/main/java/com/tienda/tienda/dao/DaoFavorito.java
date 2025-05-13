package com.tienda.tienda.dao;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tienda.tienda.entities.Favorito;
import com.tienda.tienda.entities.FavoritoId;
import com.tienda.tienda.entities.Producto;
import com.tienda.tienda.entities.Socio;

public interface DaoFavorito extends JpaRepository<Favorito, FavoritoId> {

    List<Favorito> findByIdusuario(Socio socio);
    void deleteByIdusuario(Socio socio);
    Optional <Favorito> findByIdusuarioAndIdproducto(Socio usuario, Producto producto);
    
}
