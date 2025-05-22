package com.tienda.tienda.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "token")
public class Token {

    @Id
    @Column(name = "email", nullable = false, length = 100)
    private String email;  // Se mantiene el campo email


    /*@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", referencedColumnName = "email", insertable = false, updatable = false)
    private Socio socio;  // Relación con socio (por email)*/

    @Column(name = "clave", nullable = false, length = 64)
    private String clave;

    @Column(name = "value", nullable = false, length = 255, unique = true)
    private String value;


    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "fecha_inicio")
    private Instant fechaInicio;


    

 

    public Token() {
    }



    
}