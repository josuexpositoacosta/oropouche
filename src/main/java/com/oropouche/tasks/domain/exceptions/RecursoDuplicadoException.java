package com.oropouche.tasks.domain.exceptions;

/**
 * Se lanza cuando se intenta crear o modificar un recurso de forma que choca con otro existente
 * (código de área, documento de paciente, nombre de usuario…).
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
