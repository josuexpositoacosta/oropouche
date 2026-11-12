package com.oropouche.tasks.infrastructure.controllers.Auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oropouche.tasks.domain.ports.in.LoginUseCase;

/**
 * Adaptador de entrada REST para autenticación. No hay registro público: los usuarios los
 * crea el administrador en /api/v1/admin/usuarios.
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:4200"})
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping(value = "login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        String token = loginUseCase.login(request.getUsername(), request.getPassword());

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
