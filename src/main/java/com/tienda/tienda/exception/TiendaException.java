package com.tienda.tienda.exception;

import org.springframework.http.HttpStatus;

public class TiendaException extends RuntimeException {

    private HttpStatus status;
    public TiendaException(String message, HttpStatus status) {
        super(message);
        this.status=status;
    }


    public HttpStatus getStatus() {
        return status;
    }
}
