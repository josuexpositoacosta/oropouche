package com.oropouche.tasks.infrastructure.controllers.Auth;

import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;
import com.oropouche.tasks.infrastructure.jwt.JwtService;
import com.oropouche.tasks.infrastructure.security.UserDetailsAdapter;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepositoryPort userRepositoryPort;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userRepositoryPort.findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtService.getToken(new UserDetailsAdapter(user));

        return AuthResponse.builder()
                .token(token)
                .build();
    }

    public AuthResponse register(RegisterRequest request) {

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstname(request.getFirstname())
                .lastname(request.lastname)
                .country(request.getCountry())
                .role(Role.USER)
                .build();

        User savedUser = userRepositoryPort.save(user);

        return AuthResponse.builder()
                .token(jwtService.getToken(new UserDetailsAdapter(savedUser)))
                .build();
    }
}