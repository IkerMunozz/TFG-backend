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
public class CarritoId implements Serializable {
    private static final long serialVersionUID = 8934642822445431530L;
    @Column(name = "idusuario", nullable = false)
    private Integer idusuario;

    @Column(name = "idproducto", nullable = false)
    private Integer idproducto;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        CarritoId entity = (CarritoId) o;
        return Objects.equals(this.idproducto, entity.idproducto) &&
                Objects.equals(this.idusuario, entity.idusuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idproducto, idusuario);
    }

}