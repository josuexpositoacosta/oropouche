package com.oropouche.tasks.domain.ports.in;

import java.util.Optional;

import com.oropouche.tasks.domain.models.user.User;

/**
 * Puerto de entrada: consultar un usuario.
 */
public interface GetUserUseCase {

    Optional<User> getUser(Integer id);
}
