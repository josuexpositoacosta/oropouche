package com.oropouche.tasks.domain.ports.out;

/**
 * Puerto de salida: verificación de credenciales.
 */
public interface AuthenticationPort {

    /**
     * @throws com.oropouche.tasks.domain.exceptions.InvalidCredentialsException si no son válidas
     */
    void authenticate(String username, String password);
}
