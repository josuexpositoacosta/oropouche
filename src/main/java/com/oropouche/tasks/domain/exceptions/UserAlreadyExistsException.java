package com.oropouche.tasks.domain.exceptions;

/**
 * Se lanza al intentar registrar un nombre de usuario que ya existe.
 */
public class UserAlreadyExistsException extends RecursoDuplicadoException {

    public UserAlreadyExistsException(String username) {
        super("Ya existe un usuario con el nombre: " + username);
    }
}
