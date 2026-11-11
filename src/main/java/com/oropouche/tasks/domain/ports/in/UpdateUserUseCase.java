package com.oropouche.tasks.domain.ports.in;

import java.util.Optional;

import com.oropouche.tasks.domain.models.user.User;

/**
 * Puerto de entrada: actualizar los datos personales de un usuario.
 */
public interface UpdateUserUseCase {

    /**
     * Actualiza nombre, apellidos y país. No modifica usuario, contraseña ni rol.
     *
     * @return el usuario actualizado, o vacío si no existe
     */
    Optional<User> updateUser(User datos);
}
