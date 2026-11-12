package com.oropouche.tasks.infrastructure.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.models.user.UsuarioActual;
import com.oropouche.tasks.domain.ports.out.UsuarioActualPort;

/**
 * Adaptador: obtiene el usuario autenticado del contexto de Spring Security
 * (cargado de la base de datos en cada petición por el filtro JWT).
 */
@Component
public class SecurityUsuarioActualAdapter implements UsuarioActualPort {

    @Override
    public Optional<UsuarioActual> actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserDetailsAdapter)) {
            return Optional.empty();
        }
        User user = ((UserDetailsAdapter) auth.getPrincipal()).getUser();
        return Optional.of(new UsuarioActual(user.getId(), user.getUsername(), user.getRole(), user.getAreaSaludId()));
    }
}
