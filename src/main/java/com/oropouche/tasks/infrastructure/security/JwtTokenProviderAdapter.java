package com.oropouche.tasks.infrastructure.security;

import org.springframework.stereotype.Component;

import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.out.TokenProviderPort;
import com.oropouche.tasks.infrastructure.jwt.JwtService;

/**
 * Adaptador de tokens: emite tokens JWT mediante {@link JwtService}.
 */
@Component
public class JwtTokenProviderAdapter implements TokenProviderPort {

    private final JwtService jwtService;

    public JwtTokenProviderAdapter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String generateToken(User user) {
        return jwtService.getToken(new UserDetailsAdapter(user));
    }
}
