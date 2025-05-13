package com.tienda.tienda.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class TiendaErrorExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<TiendaErrorResponse> handleException(TiendaException exc) {
        TiendaErrorResponse error = new TiendaErrorResponse();
        error.setStatus(exc.getStatus().value());
        error.setMessage(exc.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler
    public ResponseEntity<TiendaErrorResponse> handleException(Exception exc) {
        TiendaErrorResponse error = new TiendaErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        String mensajeError = exc.getMessage();
        error.setMessage(exc.getMessage());
        if (mensajeError.contains("employee not found")) {
            error.setMessage("El codigo numerico debe ser numerico...");

        }
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
