package com.oropouche.tasks.domain.ports.in;

/**
 * Puerto de entrada: autenticar a un usuario.
 */
public interface LoginUseCase {

    /**
     * @return el token de acceso del usuario autenticado
     * @throws com.oropouche.tasks.domain.exceptions.InvalidCredentialsException si las credenciales no son válidas
     */
    String login(String username, String password);
}
