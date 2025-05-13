package com.tienda.tienda.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "compra_producto")
public class CompraProducto {
    @EmbeddedId
    private CompraProductoId id;

    @MapsId("idcompra")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idcompra", nullable = false)
    private Compra idcompra;

    @MapsId("idproducto")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "idproducto", nullable = false)
    private com.tienda.tienda.entities.Producto idproducto;

    @ColumnDefault("1")
    @Column(name = "cantidad")
    private Integer cantidad;

}