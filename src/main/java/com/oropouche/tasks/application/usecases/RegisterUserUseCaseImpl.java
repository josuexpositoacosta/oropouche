package com.oropouche.tasks.application.usecases;

import com.oropouche.tasks.domain.exceptions.UserAlreadyExistsException;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.in.RegisterUserUseCase;
import com.oropouche.tasks.domain.ports.out.PasswordEncoderPort;
import com.oropouche.tasks.domain.ports.out.TokenProviderPort;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;

public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenProviderPort tokenProviderPort;

    public RegisterUserUseCaseImpl(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoderPort passwordEncoderPort,
            TokenProviderPort tokenProviderPort) {

        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public String register(User newUser) {

        if (newUser == null) {
            throw new IllegalArgumentException("Los datos del usuario son obligatorios");
        }
        validarTexto(newUser.getUsername(), "El nombre de usuario es obligatorio");
        validarTexto(newUser.getPassword(), "La contraseña es obligatoria");

        String username = newUser.getUsername().trim();

        if (userRepositoryPort.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException(username);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoderPort.encode(newUser.getPassword()))
                .firstname(newUser.getFirstname())
                .lastname(newUser.getLastname())
                .country(newUser.getCountry())
                .role(Role.USER)
                .build();

        User savedUser = userRepositoryPort.save(user);

        return tokenProviderPort.generateToken(savedUser);
    }

    private static void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
