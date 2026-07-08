package com.tulicoreria.licoreria.exception;

/** Se lanza cuando una búsqueda por id (u otra clave) no encuentra el recurso solicitado. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
