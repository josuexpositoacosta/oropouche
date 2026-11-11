package com.oropouche.tasks.domain.ports.in;

import com.oropouche.tasks.domain.models.user.User;

/**
 * Puerto de entrada: registrar un usuario nuevo.
 */
public interface RegisterUserUseCase {

    /**
     * @param newUser datos del usuario; {@code password} viene en texto plano
     * @return el token de acceso del usuario registrado
     * @throws com.oropouche.tasks.domain.exceptions.UserAlreadyExistsException si el nombre ya existe
     */
    String register(User newUser);
}
