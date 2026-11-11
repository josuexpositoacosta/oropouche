package com.oropouche.tasks.domain.ports.out;

import com.oropouche.tasks.domain.models.user.User;

/**
 * Puerto de salida: emisión de tokens de acceso.
 */
public interface TokenProviderPort {

    String generateToken(User user);
}
