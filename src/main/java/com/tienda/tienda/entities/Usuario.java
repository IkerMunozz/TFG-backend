package com.tienda.tienda.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @Column(name = "email", nullable = false, length = 100)
    private String email;

   /* @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "email", referencedColumnName = "email", insertable = false, updatable = false)
    private Socio socio;*/

    @Column(name = "clave", nullable = false, length = 64)
    private String clave;

}