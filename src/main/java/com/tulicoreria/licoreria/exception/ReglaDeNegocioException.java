package com.tulicoreria.licoreria.exception;

/** Se lanza cuando una operación viola una regla de negocio (duplicados, estado inválido, validación). */
public class ReglaDeNegocioException extends RuntimeException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
