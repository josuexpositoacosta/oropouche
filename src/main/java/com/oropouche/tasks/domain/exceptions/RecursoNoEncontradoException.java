package com.oropouche.tasks.domain.exceptions;

/**
 * Se lanza cuando se busca un recurso de negocio (área, paciente…) que no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
