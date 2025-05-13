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
public class GrupoId implements Serializable {
    private static final long serialVersionUID = 812300088816806056L;
    @Column(name = "idgrupo", nullable = false, length = 20)
    private String idgrupo;

    @Column(name = "idusuario", nullable = false, length = 100)
    private String idusuario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        GrupoId entity = (GrupoId) o;
        return Objects.equals(this.idgrupo, entity.idgrupo) &&
                Objects.equals(this.idusuario, entity.idusuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idgrupo, idusuario);
    }

}