package com.tienda.tienda.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "grupo")
public class Grupo {
    @EmbeddedId
    private GrupoId id;

    @MapsId("idusuario")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "idusuario", nullable = false)
    private com.tienda.tienda.entities.Usuario idusuario;

}