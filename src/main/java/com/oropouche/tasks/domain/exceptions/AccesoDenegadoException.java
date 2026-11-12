package com.oropouche.tasks.domain.exceptions;

/** El usuario está autenticado pero no tiene permiso para la operación o el área. */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
