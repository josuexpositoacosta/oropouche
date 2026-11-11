package com.oropouche.tasks.domain.ports.out;

/**
 * Puerto de salida: cifrado de contraseñas.
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);
}
