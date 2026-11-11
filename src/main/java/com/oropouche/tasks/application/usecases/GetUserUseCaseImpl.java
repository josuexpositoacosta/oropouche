package com.oropouche.tasks.application.usecases;

import java.util.Optional;

import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.in.GetUserUseCase;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;

public class GetUserUseCaseImpl implements GetUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public GetUserUseCaseImpl(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Optional<User> getUser(Integer id) {

        if (id == null) {
            return Optional.empty();
        }

        return userRepositoryPort.findById(id);
    }
}
