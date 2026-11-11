package com.oropouche.tasks.application.usecases;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oropouche.tasks.domain.exceptions.UserAlreadyExistsException;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.out.PasswordEncoderPort;
import com.oropouche.tasks.domain.ports.out.TokenProviderPort;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    private RegisterUserUseCaseImpl registerUserUseCase;

    @BeforeEach
    void setUp() {
        registerUserUseCase =
                new RegisterUserUseCaseImpl(userRepositoryPort, passwordEncoderPort, tokenProviderPort);
    }

    @Test
    void shouldRegisterUserWithEncodedPasswordAndUserRole() {

        User input = User.builder()
                .username(" ana ")
                .password("secreta")
                .firstname("Ana")
                .lastname("Pérez")
                .country("CU")
                .role(Role.ADMIN)
                .build();

        when(userRepositoryPort.findByUsername("ana")).thenReturn(Optional.empty());
        when(passwordEncoderPort.encode("secreta")).thenReturn("$hash$");
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenProviderPort.generateToken(any(User.class))).thenReturn("token-abc");

        String token = registerUserUseCase.register(input);

        assertEquals("token-abc", token);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepositoryPort).save(saved.capture());
        assertEquals("ana", saved.getValue().getUsername());
        assertEquals("$hash$", saved.getValue().getPassword());
        assertEquals(Role.USER, saved.getValue().getRole(), "el registro público nunca concede ADMIN");
        assertEquals("Ana", saved.getValue().getFirstname());
    }

    @Test
    void shouldRejectDuplicateUsername() {

        when(userRepositoryPort.findByUsername("ana"))
                .thenReturn(Optional.of(User.builder().id(1).username("ana").build()));

        User input = User.builder().username("ana").password("x").build();

        assertThrows(UserAlreadyExistsException.class, () -> registerUserUseCase.register(input));

        verify(userRepositoryPort, never()).save(any());
        verifyNoInteractions(passwordEncoderPort, tokenProviderPort);
    }

    @Test
    void shouldRejectMissingUsernameOrPassword() {

        assertThrows(IllegalArgumentException.class,
                () -> registerUserUseCase.register(User.builder().username("").password("x").build()));
        assertThrows(IllegalArgumentException.class,
                () -> registerUserUseCase.register(User.builder().username("ana").build()));
        assertThrows(IllegalArgumentException.class,
                () -> registerUserUseCase.register(null));

        verifyNoInteractions(userRepositoryPort, passwordEncoderPort, tokenProviderPort);
    }
}
