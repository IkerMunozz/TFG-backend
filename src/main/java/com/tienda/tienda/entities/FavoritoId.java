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
public class FavoritoId implements Serializable {
    private static final long serialVersionUID = 4173714198764746842L;
    @Column(name = "idusuario", nullable = false)
    private Integer idusuario;

    @Column(name = "idproducto", nullable = false)
    private Integer idproducto;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        FavoritoId entity = (FavoritoId) o;
        return Objects.equals(this.idproducto, entity.idproducto) &&
                Objects.equals(this.idusuario, entity.idusuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idproducto, idusuario);
    }

}