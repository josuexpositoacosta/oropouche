package com.oropouche.tasks.application.services.acceso;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.oropouche.tasks.domain.exceptions.AccesoDenegadoException;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.UsuarioActual;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaAccesoTest {

    private static PoliticaAcceso como(Role role, Long area) {
        return new PoliticaAcceso(() -> Optional.of(new UsuarioActual(1, "u", role, area)));
    }

    @Test
    void sinUsuarioSeDeniega() {
        PoliticaAcceso politica = new PoliticaAcceso(Optional::empty);
        assertThrows(AccesoDenegadoException.class, politica::requerido);
    }

    @Test
    void medicoLeeYEscribeSoloSuArea() {
        PoliticaAcceso medico = como(Role.MEDICO, 3L);

        assertDoesNotThrow(() -> medico.verificarLectura(3L));
        assertDoesNotThrow(() -> medico.verificarEscritura(3L));
        assertThrows(AccesoDenegadoException.class, () -> medico.verificarLectura(4L));
        assertThrows(AccesoDenegadoException.class, () -> medico.verificarEscritura(4L));
    }

    @Test
    void epidemiologoLeeTodoPeroNoEscribe() {
        PoliticaAcceso epi = como(Role.EPIDEMIOLOGO, null);

        assertDoesNotThrow(() -> epi.verificarLectura(3L));
        assertDoesNotThrow(() -> epi.verificarLectura(99L));
        assertThrows(AccesoDenegadoException.class, () -> epi.verificarEscritura(3L));
    }

    @Test
    void adminNoAccedeADatosClinicos() {
        PoliticaAcceso admin = como(Role.ADMIN, null);

        assertThrows(AccesoDenegadoException.class, () -> admin.verificarLectura(3L));
        assertThrows(AccesoDenegadoException.class, () -> admin.areaParaListar(null));
    }

    @Test
    void areaParaListar() {
        assertEquals(3L, como(Role.MEDICO, 3L).areaParaListar(null), "el médico siempre filtra por su área");
        assertEquals(3L, como(Role.MEDICO, 3L).areaParaListar(3L));
        assertThrows(AccesoDenegadoException.class, () -> como(Role.MEDICO, 3L).areaParaListar(4L));

        assertNull(como(Role.EPIDEMIOLOGO, null).areaParaListar(null), "el epidemiólogo puede ver todas");
        assertEquals(4L, como(Role.EPIDEMIOLOGO, null).areaParaListar(4L));
    }
}
