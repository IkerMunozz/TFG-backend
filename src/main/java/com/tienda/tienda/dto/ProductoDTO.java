package com.tienda.tienda.dto;

import java.math.BigDecimal;

import com.tienda.tienda.entities.Producto;

public class ProductoDTO {

    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private String imagen;

    // Constructor
    public ProductoDTO(String nombre, String descripcion, BigDecimal precio, String imagen) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagen = imagen;
    }

    public ProductoDTO(Producto producto) {
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.imagen = producto.getImagen();
    }

    // Getters y Setters (o usa Lombok si prefieres)
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}

