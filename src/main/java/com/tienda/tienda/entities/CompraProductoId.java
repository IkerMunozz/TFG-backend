package com.tienda.tienda.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class CompraProductoId implements Serializable {
    private static final long serialVersionUID = 1733671308152221437L;
    @Column(name = "idcompra", nullable = false)
    private Integer idcompra;

    @Column(name = "idproducto", nullable = false)
    private Integer idproducto;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CompraProductoId entity = (CompraProductoId) o;
        return Objects.equals(this.idcompra, entity.idcompra) &&
                Objects.equals(this.idproducto, entity.idproducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idcompra, idproducto);
    }

}