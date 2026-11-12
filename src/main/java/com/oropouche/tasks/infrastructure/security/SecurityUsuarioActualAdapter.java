package com.oropouche.tasks.infrastructure.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.oropouche.tasks.domain.ports.out.UsuarioActualPort;

/**
 * Adaptador: obtiene el usuario autenticado del contexto de Spring Security.
 */
@Component
public class SecurityUsuarioActualAdapter implements UsuarioActualPort {

    static final String SISTEMA = "sistema";

    @Override
    public String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return SISTEMA;
        }
        return auth.getName();
    }
}
