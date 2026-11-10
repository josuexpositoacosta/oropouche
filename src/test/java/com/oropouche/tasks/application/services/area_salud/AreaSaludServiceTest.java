
package com.oropouche.tasks.application.services.area_salud;

import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.ports.out.AreaSaludRepositoryPort;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AreaSaludServiceTest {

    @Mock
    private AreaSaludRepositoryPort repository;

    @InjectMocks
    private AreaSaludService service;

    private AreaSalud crearArea() {
        return new AreaSalud(
                null,
                "AS-001",
                "Área de Salud Norte",
                "Santiago de Cuba",
                "Santiago de Cuba",
                "Calle Principal",
                "22555555",
                true,
                null
        );
    }

    @Test
    void registrarDebeGuardarAreaCorrectamente() {

        AreaSalud area = crearArea();

        when(repository.existsByCodigo("AS-001")).thenReturn(false);
        when(repository.existsByNombre("Área de Salud Norte")).thenReturn(false);
        when(repository.save(any(AreaSalud.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AreaSalud resultado = service.registrar(area);

        assertNotNull(resultado);
        assertEquals("AS-001", resultado.getCodigo());
        assertEquals("Área de Salud Norte", resultado.getNombre());
        assertTrue(resultado.isActivo());
        assertNotNull(resultado.getFechaRegistro());

        verify(repository).save(any(AreaSalud.class));
    }

    @Test
    void registrarDebeRechazarCodigoDuplicado() {

        AreaSalud area = crearArea();

        when(repository.existsByCodigo("AS-001")).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrar(area)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void registrarDebeRechazarNombreDuplicado() {

        AreaSalud area = crearArea();

        when(repository.existsByCodigo("AS-001")).thenReturn(false);
        when(repository.existsByNombre("Área de Salud Norte")).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrar(area)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void registrarDebeRechazarDatosNulos() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrar(null)
        );

        verifyNoInteractions(repository);
    }

    @Test
    void buscarPorIdDebeRetornarAreaExistente() {

        AreaSalud area = crearArea();
        area.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(area));

        AreaSalud resultado = service.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("AS-001", resultado.getCodigo());
    }

    @Test
    void buscarPorIdDebeRechazarIdentificadorInexistente() {

        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarPorId(99L)
        );
    }

    @Test
    void listarTodasDebeRetornarTodasLasAreas() {

        AreaSalud area1 = crearArea();
        area1.setId(1L);

        AreaSalud area2 = crearArea();
        area2.setId(2L);
        area2.setCodigo("AS-002");

        when(repository.findAll()).thenReturn(Arrays.asList(area1, area2));

        assertEquals(2, service.listarTodas().size());
    }

    @Test
    void actualizarDebeConservarFechaRegistro() {

        LocalDateTime fechaOriginal = LocalDateTime.of(
                2025, 1, 10, 8, 30
        );

        AreaSalud existente = crearArea();
        existente.setId(1L);
        existente.setFechaRegistro(fechaOriginal);

        AreaSalud datos = crearArea();
        datos.setCodigo("AS-001-A");
        datos.setNombre("Área de Salud Actualizada");

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.findByCodigo("AS-001-A")).thenReturn(Optional.empty());
        when(repository.findAll()).thenReturn(Collections.singletonList(existente));
        when(repository.save(any(AreaSalud.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AreaSalud resultado = service.actualizar(1L, datos);

        assertEquals("AS-001-A", resultado.getCodigo());
        assertEquals("Área de Salud Actualizada", resultado.getNombre());
        assertEquals(fechaOriginal, resultado.getFechaRegistro());
        assertEquals(1L, resultado.getId());

        verify(repository).save(existente);
    }

    @Test
    void desactivarDebeCambiarEstadoSinEliminarArea() {

        AreaSalud existente = crearArea();
        existente.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.save(any(AreaSalud.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AreaSalud resultado = service.desactivar(1L);

        assertFalse(resultado.isActivo());

        verify(repository).save(existente);
    }
}
