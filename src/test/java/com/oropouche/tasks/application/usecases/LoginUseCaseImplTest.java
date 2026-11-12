package com.oropouche.tasks.application.usecases;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oropouche.tasks.domain.exceptions.InvalidCredentialsException;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.out.AuthenticationPort;
import com.oropouche.tasks.domain.ports.out.TokenProviderPort;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    @Mock
    private AuthenticationPort authenticationPort;

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private TokenProviderPort tokenProviderPort;

    private LoginUseCaseImpl loginUseCase;

    @BeforeEach
    void setUp() {
        loginUseCase = new LoginUseCaseImpl(authenticationPort, userRepositoryPort, tokenProviderPort);
    }

    @Test
    void shouldReturnTokenWhenCredentialsAreValid() {

        User user = User.builder().id(1).username("ana").role(Role.MEDICO).build();
        when(userRepositoryPort.findByUsername("ana")).thenReturn(Optional.of(user));
        when(tokenProviderPort.generateToken(user)).thenReturn("token-123");

        String token = loginUseCase.login("ana", "secreta");

        assertEquals("token-123", token);
        verify(authenticationPort).authenticate("ana", "secreta");
    }

    @Test
    void shouldPropagateInvalidCredentialsAndNotIssueToken() {

        doThrow(new InvalidCredentialsException())
                .when(authenticationPort).authenticate("ana", "mala");

        assertThrows(InvalidCredentialsException.class,
                () -> loginUseCase.login("ana", "mala"));

        verifyNoInteractions(tokenProviderPort);
    }

    @Test
    void shouldFailWhenUserDisappearsAfterAuthentication() {

        when(userRepositoryPort.findByUsername("ana")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> loginUseCase.login("ana", "secreta"));

        verifyNoInteractions(tokenProviderPort);
    }

    @Test
    void shouldRejectBlankCredentialsWithoutCallingAuthentication() {

        assertThrows(InvalidCredentialsException.class, () -> loginUseCase.login(" ", "x"));
        assertThrows(InvalidCredentialsException.class, () -> loginUseCase.login("ana", null));

        verifyNoInteractions(authenticationPort, userRepositoryPort, tokenProviderPort);
    }
}
