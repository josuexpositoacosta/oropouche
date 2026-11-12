package com.oropouche.tasks.application.usecases;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCasesTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    private GetUserUseCaseImpl getUserUseCase;
    private UpdateUserUseCaseImpl updateUserUseCase;

    @BeforeEach
    void setUp() {
        getUserUseCase = new GetUserUseCaseImpl(userRepositoryPort);
        updateUserUseCase = new UpdateUserUseCaseImpl(userRepositoryPort);
    }

    @Test
    void getUserShouldReturnUserFromRepository() {

        User user = User.builder().id(1).username("ana").build();
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(user));

        assertEquals(Optional.of(user), getUserUseCase.getUser(1));
    }

    @Test
    void getUserShouldReturnEmptyForNullId() {

        assertTrue(getUserUseCase.getUser(null).isEmpty());
        verifyNoInteractions(userRepositoryPort);
    }

    @Test
    void updateUserShouldChangeOnlyPersonalData() {

        User existing = User.builder()
                .id(1).username("ana").password("$hash$").role(Role.MEDICO)
                .firstname("Ana").lastname("Old").country("CU")
                .build();
        when(userRepositoryPort.findById(1)).thenReturn(Optional.of(existing));
        when(userRepositoryPort.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User datos = User.builder()
                .id(1).username("hacker").password("nueva").role(Role.ADMIN)
                .firstname("Ana María").lastname("Pérez").country("ES")
                .build();

        User result = updateUserUseCase.updateUser(datos).orElseThrow();

        assertEquals("Ana María", result.getFirstname());
        assertEquals("Pérez", result.getLastname());
        assertEquals("ES", result.getCountry());
        assertEquals("ana", result.getUsername(), "no debe cambiar el usuario");
        assertEquals("$hash$", result.getPassword(), "no debe cambiar la contraseña");
        assertEquals(Role.MEDICO, result.getRole(), "no debe cambiar el rol");
    }

    @Test
    void updateUserShouldReturnEmptyWhenUserDoesNotExist() {

        when(userRepositoryPort.findById(99)).thenReturn(Optional.empty());

        assertTrue(updateUserUseCase.updateUser(User.builder().id(99).build()).isEmpty());
        verify(userRepositoryPort, never()).save(any());
    }

    @Test
    void updateUserShouldRejectMissingId() {

        assertThrows(IllegalArgumentException.class,
                () -> updateUserUseCase.updateUser(User.builder().firstname("x").build()));
        assertThrows(IllegalArgumentException.class,
                () -> updateUserUseCase.updateUser(null));
        verifyNoInteractions(userRepositoryPort);
    }
}
