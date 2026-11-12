package com.oropouche.tasks.application.services.caso;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oropouche.tasks.domain.exceptions.RecursoDuplicadoException;
import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.models.caso.Desenlace;
import com.oropouche.tasks.domain.ports.out.DesenlaceRepositoryPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesenlaceServiceTest {

    @Mock
    private DesenlaceRepositoryPort repository;

    private DesenlaceService service;

    @BeforeEach
    void setUp() {
        service = new DesenlaceService(repository);
    }

    @Test
    void crearNormalizaElCodigo() {

        when(repository.findByCodigo("ALTA_VOLUNTARIA")).thenReturn(Optional.empty());
        when(repository.save(any(Desenlace.class))).thenAnswer(inv -> inv.getArgument(0));

        Desenlace creado = service.crear(" alta voluntaria ", " Alta voluntaria ");

        assertEquals("ALTA_VOLUNTARIA", creado.getCodigo());
        assertEquals("Alta voluntaria", creado.getNombre());
        assertTrue(creado.isActivo());
    }

    @Test
    void crearRechazaDuplicadosYCodigosInvalidos() {

        when(repository.findByCodigo("RECUPERADO"))
                .thenReturn(Optional.of(new Desenlace(1L, "RECUPERADO", "Recuperado", true)));

        assertThrows(RecursoDuplicadoException.class, () -> service.crear("recuperado", "Otro"));
        assertThrows(IllegalArgumentException.class, () -> service.crear("Ñandú", "x"));
        assertThrows(IllegalArgumentException.class, () -> service.crear("OK", " "));
    }

    @Test
    void renombrarYDesactivarSinBorrar() {

        when(repository.findById(1L)).thenReturn(Optional.of(new Desenlace(1L, "RECUPERADO", "Recuperado", true)));
        when(repository.save(any(Desenlace.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals("Recuperado (alta médica)", service.renombrar(1L, "Recuperado (alta médica)").getNombre());
        assertFalse(service.cambiarEstado(1L, false).isActivo());
        assertEquals("RECUPERADO", service.cambiarEstado(1L, true).getCodigo(), "el código no cambia");
    }

    @Test
    void desenlaceInexistenteDevuelveNoEncontrado() {

        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.renombrar(9L, "x"));
    }

    @Test
    void listarSoloActivos() {

        when(repository.findAll()).thenReturn(List.of(
                new Desenlace(1L, "A", "A", true), new Desenlace(2L, "B", "B", false)));

        assertEquals(1, service.listar(true).size());
        assertEquals(2, service.listar(false).size());
    }

    @Test
    void inicializaLosCincoValoresSoloSiElCatalogoEstaVacio() {

        when(repository.count()).thenReturn(0L);
        service.inicializarSiVacio();

        ArgumentCaptor<Desenlace> guardados = ArgumentCaptor.forClass(Desenlace.class);
        verify(repository, times(5)).save(guardados.capture());
        assertEquals("RECUPERADO", guardados.getAllValues().get(0).getCodigo());

        reset(repository);
        when(repository.count()).thenReturn(5L);
        service.inicializarSiVacio();
        verify(repository, never()).save(any());
    }
}
