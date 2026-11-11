package com.oropouche.tasks.infrastructure.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.oropouche.tasks.domain.exceptions.InvalidCredentialsException;
import com.oropouche.tasks.domain.ports.out.AuthenticationPort;

/**
 * Adaptador de autenticación: valida credenciales con el {@link AuthenticationManager} de Spring
 * y traduce sus errores a la excepción de dominio.
 */
@Component
public class SpringAuthenticationAdapter implements AuthenticationPort {

    private final AuthenticationManager authenticationManager;

    public SpringAuthenticationAdapter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException();
        }
    }
}
