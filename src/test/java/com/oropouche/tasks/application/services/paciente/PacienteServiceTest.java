package com.oropouche.tasks.application.services.paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oropouche.tasks.domain.exceptions.RecursoDuplicadoException;
import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.models.paciente.Paciente;
import com.oropouche.tasks.domain.ports.out.AreaSaludRepositoryPort;
import com.oropouche.tasks.domain.ports.out.PacienteRepositoryPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepositoryPort pacienteRepository;

    @Mock
    private AreaSaludRepositoryPort areaSaludRepository;

    private PacienteService service;

    @BeforeEach
    void setUp() {
        service = new PacienteService(pacienteRepository, areaSaludRepository);
    }

    private static Paciente crearPaciente(Long areaSaludId) {
        Paciente p = new Paciente();
        p.setTipoDocumento(" CI ");
        p.setNumeroDocumento(" 85010112345 ");
        p.setNombres(" Juan ");
        p.setApellidos(" Pérez ");
        p.setFechaNacimiento(LocalDate.of(1985, 1, 1));
        p.setSexo("M");
        p.setAreaSaludId(areaSaludId);
        return p;
    }

    private static AreaSalud area(Long id, boolean activa) {
        AreaSalud a = new AreaSalud();
        a.setId(id);
        a.setCodigo("AS-" + id);
        a.setActivo(activa);
        return a;
    }

    @Test
    void registrarDebeGuardarPacienteEnAreaActiva() {

        when(areaSaludRepository.findById(1L)).thenReturn(Optional.of(area(1L, true)));
        when(pacienteRepository.existsByNumeroDocumento("CI", "85010112345")).thenReturn(false);
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        Paciente guardado = service.registrar(crearPaciente(1L));

        assertTrue(guardado.isActivo());
        assertNotNull(guardado.getFechaRegistro());
        assertEquals("Juan", guardado.getNombres(), "debe recortar espacios");
    }

    @Test
    void registrarDebeRechazarAreaInexistente() {

        when(areaSaludRepository.findById(7L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrar(crearPaciente(7L)));

        assertTrue(ex.getMessage().contains("No existe el área de salud"));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void registrarDebeRechazarAreaDesactivada() {

        when(areaSaludRepository.findById(2L)).thenReturn(Optional.of(area(2L, false)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrar(crearPaciente(2L)));

        assertTrue(ex.getMessage().contains("desactivada"));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void registrarDebeRechazarDocumentoDuplicado() {

        when(areaSaludRepository.findById(1L)).thenReturn(Optional.of(area(1L, true)));
        when(pacienteRepository.existsByNumeroDocumento("CI", "85010112345")).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> service.registrar(crearPaciente(1L)));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void actualizarEnMismaAreaDesactivadaDebePermitirse() {

        Paciente existente = crearPaciente(2L);
        existente.setId(10L);
        existente.setFechaRegistro(LocalDateTime.of(2026, 1, 1, 8, 0));
        existente.setActivo(true);

        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(pacienteRepository.findByNumeroDocumento("CI", "85010112345")).thenReturn(Optional.of(existente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        Paciente cambios = crearPaciente(2L);
        cambios.setTelefono("555-1234");

        Paciente resultado = service.actualizar(10L, cambios);

        assertEquals("555-1234", resultado.getTelefono());
        assertEquals(LocalDateTime.of(2026, 1, 1, 8, 0), resultado.getFechaRegistro());
        verifyNoInteractions(areaSaludRepository);
    }

    @Test
    void actualizarCambiandoAAreaDesactivadaDebeRechazarse() {

        Paciente existente = crearPaciente(1L);
        existente.setId(10L);
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(areaSaludRepository.findById(2L)).thenReturn(Optional.of(area(2L, false)));

        assertThrows(IllegalArgumentException.class, () -> service.actualizar(10L, crearPaciente(2L)));
        verify(pacienteRepository, never()).save(any());
    }

    @Test
    void buscarPorIdInexistenteDebeLanzarNoEncontrado() {

        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorId(99L));
    }

    @Test
    void buscarPorAreaInexistenteDebeLanzarNoEncontrado() {

        when(areaSaludRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.buscarPorAreaSalud(5L));
    }

    @Test
    void buscarPorAreaDebeDevolverSusPacientes() {

        when(areaSaludRepository.findById(1L)).thenReturn(Optional.of(area(1L, false)));
        when(pacienteRepository.findByAreaSaludId(1L)).thenReturn(List.of(crearPaciente(1L)));

        assertEquals(1, service.buscarPorAreaSalud(1L).size());
    }

    @Test
    void desactivarDebeMarcarInactivoSinBorrar() {

        Paciente existente = crearPaciente(1L);
        existente.setId(10L);
        existente.setActivo(true);
        when(pacienteRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(inv -> inv.getArgument(0));

        assertFalse(service.desactivar(10L).isActivo());
    }
}
