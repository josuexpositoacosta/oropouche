package com.oropouche.tasks.infrastructure.adapters.caso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.oropouche.tasks.domain.models.caso.AgenteEtiologico;
import com.oropouche.tasks.domain.models.caso.CambioClasificacion;
import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.Manifestacion;
import com.oropouche.tasks.domain.models.caso.ResultadoLaboratorio;
import com.oropouche.tasks.domain.models.caso.ResultadoPrueba;
import com.oropouche.tasks.domain.models.caso.TipoMuestra;
import com.oropouche.tasks.domain.models.caso.TipoPrueba;
import com.oropouche.tasks.infrastructure.repositories.caso.JpaCasoRepository;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaCasoRepositoryAdapter.class)
class JpaCasoRepositoryAdapterTest {

    @Autowired
    private JpaCasoRepositoryAdapter adapter;

    @Autowired
    private JpaCasoRepository jpaRepository;

    @BeforeEach
    void setUp() {
        jpaRepository.deleteAll();
    }

    private static Caso caso(Long pacienteId, Long areaId, Clasificacion clasificacion, EstadoCaso estado) {
        Caso c = new Caso();
        c.setPacienteId(pacienteId);
        c.setAreaSaludId(areaId);
        c.setFechaInicioSintomas(LocalDate.of(2026, 11, 1));
        c.setFechaNotificacion(LocalDate.of(2026, 11, 3));
        c.setFiebre(true);
        c.setCefaleaIntensa(true);
        c.setManifestaciones(EnumSet.of(Manifestacion.MAREOS, Manifestacion.FOTOFOBIA));
        c.setEmbarazada(true);
        c.setSemanasGestacion(20);
        c.setDetalleAreaTransmision("Viaje a zona con brote");
        c.setClasificacion(clasificacion);
        c.setEstado(estado);
        c.getHistorial().add(new CambioClasificacion(null, LocalDateTime.of(2026, 11, 3, 9, 0),
                null, clasificacion, "Registro inicial", "dra.perez"));
        return c;
    }

    @Test
    void debeGuardarCasoConManifestacionesEHistorialYRecuperarlo() {

        Caso guardado = adapter.save(caso(1L, 10L, Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO));

        Caso leido = adapter.findById(guardado.getId()).orElseThrow();

        assertEquals(EnumSet.of(Manifestacion.MAREOS, Manifestacion.FOTOFOBIA), leido.getManifestaciones());
        assertEquals(20, leido.getSemanasGestacion());
        assertEquals("Viaje a zona con brote", leido.getDetalleAreaTransmision());
        assertEquals(1, leido.getHistorial().size());
        assertNotNull(leido.getHistorial().get(0).getId());
        assertEquals("dra.perez", leido.getHistorial().get(0).getUsuario());
    }

    @Test
    void alAnadirResultadoEHistorialSeConservanLosAnteriores() {

        Caso caso = adapter.save(caso(1L, 10L, Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO));

        caso.getResultados().add(new ResultadoLaboratorio(null, AgenteEtiologico.OROPOUCHE, TipoPrueba.RT_PCR,
                TipoMuestra.SUERO, ResultadoPrueba.POSITIVO, LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 4)));
        caso.getHistorial().add(new CambioClasificacion(null, LocalDateTime.of(2026, 11, 4, 10, 0),
                Clasificacion.SOSPECHOSO, Clasificacion.CONFIRMADO, "RT-PCR positiva", "epi.gomez"));
        caso.setClasificacion(Clasificacion.CONFIRMADO);
        caso.setManifestaciones(EnumSet.of(Manifestacion.NEUROLOGICA));
        adapter.save(caso);

        Caso leido = adapter.findById(caso.getId()).orElseThrow();

        assertEquals(Clasificacion.CONFIRMADO, leido.getClasificacion());
        assertEquals(1, leido.getResultados().size());
        assertEquals(TipoPrueba.RT_PCR, leido.getResultados().get(0).getTipoPrueba());
        assertEquals(2, leido.getHistorial().size(), "el historial no se pierde al actualizar");
        assertEquals("Registro inicial", leido.getHistorial().get(0).getMotivo());
        assertEquals(EnumSet.of(Manifestacion.NEUROLOGICA), leido.getManifestaciones());
    }

    @Test
    void debeFiltrarPorAreaClasificacionYEstado() {

        adapter.save(caso(1L, 10L, Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO));
        adapter.save(caso(2L, 10L, Clasificacion.CONFIRMADO, EstadoCaso.ABIERTO));
        adapter.save(caso(3L, 20L, Clasificacion.CONFIRMADO, EstadoCaso.CERRADO));

        assertEquals(3, adapter.findAll(null, null, null).size());
        assertEquals(2, adapter.findAll(10L, null, null).size());
        assertEquals(2, adapter.findAll(null, Clasificacion.CONFIRMADO, null).size());
        assertEquals(1, adapter.findAll(10L, Clasificacion.CONFIRMADO, EstadoCaso.ABIERTO).size());
        assertEquals(1, adapter.findAll(null, null, EstadoCaso.CERRADO).size());
    }

    @Test
    void debeDetectarCasoAbiertoPorPaciente() {

        adapter.save(caso(1L, 10L, Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO));
        adapter.save(caso(2L, 10L, Clasificacion.DESCARTADO, EstadoCaso.CERRADO));

        assertTrue(adapter.existsAbiertoByPacienteId(1L));
        assertFalse(adapter.existsAbiertoByPacienteId(2L), "un caso cerrado no cuenta");
        assertEquals(1, adapter.findByPacienteId(2L).size());
    }
}
