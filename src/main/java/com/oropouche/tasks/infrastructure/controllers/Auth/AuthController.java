package com.oropouche.tasks.infrastructure.controllers.Auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.in.LoginUseCase;
import com.oropouche.tasks.domain.ports.in.RegisterUserUseCase;

/**
 * Adaptador de entrada REST para autenticación. Solo traduce HTTP ↔ casos de uso.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:4200"})
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUserUseCase registerUserUseCase;

    public AuthController(LoginUseCase loginUseCase, RegisterUserUseCase registerUserUseCase) {
        this.loginUseCase = loginUseCase;
        this.registerUserUseCase = registerUserUseCase;
    }

    @PostMapping(value = "login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        String token = loginUseCase.login(request.getUsername(), request.getPassword());

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping(value = "register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {

        User newUser = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .country(request.getCountry())
                .build();

        return ResponseEntity.ok(new AuthResponse(registerUserUseCase.register(newUser)));
    }
}
