package com.oropouche.tasks.infrastructure.adapters.paciente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import com.oropouche.tasks.domain.models.paciente.Paciente;
import com.oropouche.tasks.infrastructure.repositories.paciente.JpaPacienteRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaPacienteRepositoryAdapter.class)
class JpaPacienteRepositoryAdapterTest {

    @Autowired
    private JpaPacienteRepositoryAdapter adapter;

    @Autowired
    private JpaPacienteRepository jpaRepository;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    private static Paciente paciente(String numero, Long areaId) {
        return new Paciente(null, "CI", numero, "Juan", "Pérez",
                LocalDate.of(1990, 5, 20), "M", "Calle 1", "555",
                areaId, LocalDateTime.of(2026, 11, 11, 10, 0), true);
    }

    @Test
    void debeGuardarYRecuperarTodosLosCampos() {

        Paciente guardado = adapter.save(paciente("123", 1L));

        assertNotNull(guardado.getId());

        Paciente leido = adapter.findById(guardado.getId()).orElseThrow();
        assertEquals("CI", leido.getTipoDocumento());
        assertEquals("123", leido.getNumeroDocumento());
        assertEquals(LocalDate.of(1990, 5, 20), leido.getFechaNacimiento());
        assertEquals(1L, leido.getAreaSaludId());
        assertTrue(leido.isActivo());
    }

    @Test
    void debeBuscarPorDocumentoYComprobarExistencia() {

        adapter.save(paciente("123", 1L));

        assertTrue(adapter.findByNumeroDocumento("CI", "123").isPresent());
        assertTrue(adapter.existsByNumeroDocumento("CI", "123"));
        assertFalse(adapter.existsByNumeroDocumento("PASAPORTE", "123"));
        assertEquals(Optional.empty(), adapter.findByNumeroDocumento("CI", "999"));
    }

    @Test
    void debeFiltrarPorAreaSalud() {

        adapter.save(paciente("1", 1L));
        adapter.save(paciente("2", 1L));
        adapter.save(paciente("3", 2L));

        assertEquals(2, adapter.findByAreaSaludId(1L).size());
        assertEquals(3, adapter.findAll().size());
    }

    @Test
    void laBaseDeDatosDebeImpedirDocumentoDuplicado() {

        adapter.save(paciente("123", 1L));

        assertThrows(DataIntegrityViolationException.class, () -> {
            adapter.save(paciente("123", 2L));
            jpaRepository.flush();
        });
    }
}
