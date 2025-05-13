package com.tienda.tienda.exception;

public class TiendaErrorResponse {

    private String message;
    private int status;

    public TiendaErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
    }

    public TiendaErrorResponse() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
