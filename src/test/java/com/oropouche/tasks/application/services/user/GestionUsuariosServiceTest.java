package com.oropouche.tasks.application.services.user;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oropouche.tasks.domain.exceptions.UserAlreadyExistsException;
import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.User;
import com.oropouche.tasks.domain.models.user.UsuarioActual;
import com.oropouche.tasks.domain.ports.out.AreaSaludRepositoryPort;
import com.oropouche.tasks.domain.ports.out.PasswordEncoderPort;
import com.oropouche.tasks.domain.ports.out.UserRepositoryPort;
import com.oropouche.tasks.domain.ports.out.UsuarioActualPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestionUsuariosServiceTest {

    @Mock
    private UserRepositoryPort usuarios;

    @Mock
    private PasswordEncoderPort encoder;

    @Mock
    private AreaSaludRepositoryPort areas;

    @Mock
    private UsuarioActualPort usuarioActual;

    private GestionUsuariosService service;

    @BeforeEach
    void setUp() {
        service = new GestionUsuariosService(usuarios, encoder, areas, usuarioActual);
    }

    private static AreaSalud area(boolean activa) {
        AreaSalud a = new AreaSalud();
        a.setId(3L);
        a.setCodigo("AS-3");
        a.setActivo(activa);
        return a;
    }

    private static User nuevo(Role role, Long area) {
        return User.builder().username(" dra.perez ").password("Clave1234").firstname("Ana").lastname("Pérez")
                .role(role).areaSaludId(area).build();
    }

    @Test
    void crearMedicoConAreaActivaCifraLaContrasena() {
        when(areas.findById(3L)).thenReturn(Optional.of(area(true)));
        when(usuarios.findByUsername("dra.perez")).thenReturn(Optional.empty());
        when(encoder.encode("Clave1234")).thenReturn("$hash$");
        when(usuarios.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User creado = service.crear(nuevo(Role.MEDICO, 3L));

        assertEquals("dra.perez", creado.getUsername());
        assertEquals("$hash$", creado.getPassword());
        assertEquals(Role.MEDICO, creado.getRole());
        assertTrue(creado.isActivo());
    }

    @Test
    void medicoSinAreaOConAreaDesactivadaSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> service.crear(nuevo(Role.MEDICO, null)));

        when(areas.findById(3L)).thenReturn(Optional.of(area(false)));
        assertThrows(IllegalArgumentException.class, () -> service.crear(nuevo(Role.MEDICO, 3L)));
        verify(usuarios, never()).save(any());
    }

    @Test
    void epidemiologoPuedeNoTenerArea() {
        when(usuarios.findByUsername("dra.perez")).thenReturn(Optional.empty());
        when(usuarios.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertNull(service.crear(nuevo(Role.EPIDEMIOLOGO, null)).getAreaSaludId());
    }

    @Test
    void validaUsuarioContrasenaYDuplicados() {
        User corta = nuevo(Role.EPIDEMIOLOGO, null);
        corta.setPassword("1234567");
        assertThrows(IllegalArgumentException.class, () -> service.crear(corta));

        User raro = nuevo(Role.EPIDEMIOLOGO, null);
        raro.setUsername("a b");
        assertThrows(IllegalArgumentException.class, () -> service.crear(raro));

        when(usuarios.findByUsername("dra.perez")).thenReturn(Optional.of(new User()));
        assertThrows(UserAlreadyExistsException.class, () -> service.crear(nuevo(Role.EPIDEMIOLOGO, null)));
    }

    @Test
    void noSePuedeQuitarElUltimoAdministrador() {
        User admin = User.builder().id(1).username("admin").role(Role.ADMIN).activo(true).build();
        when(usuarios.findById(1)).thenReturn(Optional.of(admin));
        when(usuarios.countActivosByRole(Role.ADMIN)).thenReturn(1L);

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(1, Role.EPIDEMIOLOGO, null, true));
        verify(usuarios, never()).save(any());
    }

    @Test
    void nadiePuedeDesactivarseASiMismo() {
        when(usuarios.findById(5)).thenReturn(Optional.of(User.builder().id(5).role(Role.EPIDEMIOLOGO).build()));
        when(usuarioActual.actual()).thenReturn(Optional.of(new UsuarioActual(5, "yo", Role.ADMIN, null)));

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(5, Role.EPIDEMIOLOGO, null, false));
    }

    @Test
    void cambiarRolYAreaDeOtroUsuario() {
        when(usuarios.findById(5)).thenReturn(Optional.of(User.builder().id(5).role(Role.EPIDEMIOLOGO).build()));
        when(areas.findById(3L)).thenReturn(Optional.of(area(true)));
        when(usuarios.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User r = service.actualizar(5, Role.MEDICO, 3L, true);

        assertEquals(Role.MEDICO, r.getRole());
        assertEquals(3L, r.getAreaSaludId());
    }

    @Test
    void adminInicialSoloSiNoHayNinguno() {
        when(usuarios.countActivosByRole(Role.ADMIN)).thenReturn(1L);
        assertFalse(service.crearAdminInicialSiNoExiste("admin", "Clave1234"));

        assertFalse(service.crearAdminInicialSiNoExiste("", "Clave1234"), "sin configuración no crea nada");

        when(usuarios.countActivosByRole(Role.ADMIN)).thenReturn(0L);
        when(usuarios.findByUsername("admin")).thenReturn(Optional.empty());
        when(usuarios.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(service.crearAdminInicialSiNoExiste("admin", "Clave1234"));
        ArgumentCaptor<User> guardado = ArgumentCaptor.forClass(User.class);
        verify(usuarios).save(guardado.capture());
        assertEquals(Role.ADMIN, guardado.getValue().getRole());
    }
}
