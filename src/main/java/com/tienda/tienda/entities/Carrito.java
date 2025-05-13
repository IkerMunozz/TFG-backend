package com.tienda.tienda.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "carrito")
public class Carrito {
    @EmbeddedId
    private CarritoId id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idusuario")  
    @JoinColumn(name = "idusuario", nullable = false)
    private Socio idusuario; 

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idproducto")  
    @JoinColumn(name = "idproducto", nullable = false)
    private Producto idproducto; 

    

}