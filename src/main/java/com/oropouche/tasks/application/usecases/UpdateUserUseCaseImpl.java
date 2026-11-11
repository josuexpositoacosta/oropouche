package com.oropouche.tasks.application.usecases;

import java.util.Optional;

import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.in.UpdateUserUseCase;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;

public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UpdateUserUseCaseImpl(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Optional<User> updateUser(User datos) {

        if (datos == null || datos.getId() == null) {
            throw new IllegalArgumentException("El identificador del usuario es obligatorio");
        }

        return userRepositoryPort.findById(datos.getId())
                .map(user -> {
                    user.setFirstname(datos.getFirstname());
                    user.setLastname(datos.getLastname());
                    user.setCountry(datos.getCountry());

                    return userRepositoryPort.save(user);
                });
    }
}
