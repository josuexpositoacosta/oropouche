package com.oropouche.tasks.infrastructure.adapters.caso;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.oropouche.tasks.domain.models.caso.AgenteEtiologico;
import com.oropouche.tasks.domain.models.caso.Desenlace;
import com.oropouche.tasks.domain.models.caso.Dictamen;
import com.oropouche.tasks.domain.models.caso.EvaluacionClinica;
import com.oropouche.tasks.domain.models.caso.EventoCaso;
import com.oropouche.tasks.domain.models.caso.EvolucionCaso;
import com.oropouche.tasks.domain.models.caso.ModalidadAtencion;
import com.oropouche.tasks.domain.models.caso.PlanAtencion;
import com.oropouche.tasks.domain.models.caso.Seguimiento;
import com.oropouche.tasks.domain.models.caso.SignoAlarmaDengue;
import com.oropouche.tasks.domain.models.caso.SignoClinico;
import com.oropouche.tasks.domain.models.caso.TipoEventoCaso;
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
@Import({JpaCasoRepositoryAdapter.class, JpaDesenlaceRepositoryAdapter.class})
class JpaCasoRepositoryAdapterTest {

    @Autowired
    private JpaCasoRepositoryAdapter adapter;

    @Autowired
    private JpaCasoRepository jpaRepository;

    @Autowired
    private JpaDesenlaceRepositoryAdapter desenlaceAdapter;

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

    @Test
    void debeGuardarRegistrosClinicosDesenlaceYEventos() {

        Desenlace recuperado = desenlaceAdapter.save(new Desenlace(null, "RECUPERADO", "Recuperado", true));
        Caso caso = adapter.save(caso(1L, 10L, Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO));

        EvaluacionClinica evaluacion = new EvaluacionClinica();
        evaluacion.setFecha(LocalDateTime.of(2026, 11, 4, 9, 0));
        evaluacion.setTemperatura(new BigDecimal("38.7"));
        evaluacion.setTensionSistolica(110);
        evaluacion.setTensionDiastolica(70);
        evaluacion.setSignosClinicos(EnumSet.of(SignoClinico.POSTRACION, SignoClinico.EPISTAXIS));
        evaluacion.setSignosAlarmaDengue(EnumSet.of(SignoAlarmaDengue.VOMITOS_PERSISTENTES));
        evaluacion.setUsuario("dr.lopez");
        caso.getEvaluaciones().add(evaluacion);
        caso.getDictamenes().add(new Dictamen(null, LocalDateTime.of(2026, 11, 4, 9, 30),
                "Fiebre de Oropouche", "Dengue", "Cuadro compatible", "dr.lopez"));
        caso.getPlanes().add(new PlanAtencion(null, LocalDateTime.of(2026, 11, 4, 9, 40),
                ModalidadAtencion.AMBULATORIO, "Reposo e hidratación", LocalDate.of(2026, 11, 7), "dr.lopez"));
        caso.getSeguimientos().add(new Seguimiento(null, LocalDateTime.of(2026, 11, 7, 9, 0),
                EvolucionCaso.MEJORIA, "Afebril", null, "dr.lopez"));
        caso.setEstado(EstadoCaso.CERRADO);
        caso.setDesenlace(recuperado);
        caso.setFechaCierre(LocalDate.of(2026, 11, 10));
        caso.getEventos().add(new EventoCaso(null, LocalDateTime.of(2026, 11, 10, 9, 0),
                TipoEventoCaso.CIERRE, "Desenlace: Recuperado", "dr.lopez"));
        adapter.save(caso);

        Caso leido = adapter.findById(caso.getId()).orElseThrow();

        EvaluacionClinica ev = leido.getEvaluaciones().get(0);
        assertEquals(0, new BigDecimal("38.7").compareTo(ev.getTemperatura()));
        assertEquals(EnumSet.of(SignoClinico.POSTRACION, SignoClinico.EPISTAXIS), ev.getSignosClinicos());
        assertEquals(EnumSet.of(SignoAlarmaDengue.VOMITOS_PERSISTENTES), ev.getSignosAlarmaDengue());
        assertEquals("Cuadro compatible", leido.getDictamenes().get(0).getConclusion());
        assertEquals(LocalDate.of(2026, 11, 7), leido.getPlanes().get(0).getProximoControl());
        assertEquals(EvolucionCaso.MEJORIA, leido.getSeguimientos().get(0).getEvolucion());
        assertEquals("RECUPERADO", leido.getDesenlace().getCodigo());
        assertEquals(LocalDate.of(2026, 11, 10), leido.getFechaCierre());
        assertEquals(TipoEventoCaso.CIERRE, leido.getEventos().get(0).getTipo());
        assertEquals(1, leido.getHistorial().size(), "el historial de clasificación se conserva");
    }

    @Test
    void todosLosValoresDeLosEnumeradosCabenEnSusColumnas() {

        Caso caso = adapter.save(caso(9L, 10L, Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO));
        for (TipoEventoCaso tipo : TipoEventoCaso.values()) {
            caso.getEventos().add(new EventoCaso(null, LocalDateTime.of(2026, 11, 12, 9, 0), tipo, "x", "u"));
        }
        caso.setManifestaciones(EnumSet.allOf(Manifestacion.class));
        EvaluacionClinica evaluacion = new EvaluacionClinica();
        evaluacion.setFecha(LocalDateTime.of(2026, 11, 12, 9, 0));
        evaluacion.setUsuario("u");
        evaluacion.setSignosClinicos(EnumSet.allOf(SignoClinico.class));
        evaluacion.setSignosAlarmaDengue(EnumSet.allOf(SignoAlarmaDengue.class));
        caso.getEvaluaciones().add(evaluacion);
        for (ModalidadAtencion modalidad : ModalidadAtencion.values()) {
            caso.getPlanes().add(new PlanAtencion(null, LocalDateTime.of(2026, 11, 12, 9, 0), modalidad, "x", null, "u"));
        }
        for (EvolucionCaso evolucion : EvolucionCaso.values()) {
            caso.getSeguimientos().add(new Seguimiento(null, LocalDateTime.of(2026, 11, 12, 9, 0), evolucion, null, null, "u"));
        }
        for (TipoPrueba tipo : TipoPrueba.values()) {
            caso.getResultados().add(new ResultadoLaboratorio(null, AgenteEtiologico.OROPOUCHE, tipo,
                    TipoMuestra.SUERO, ResultadoPrueba.INDETERMINADO, LocalDate.of(2026, 11, 12), null));
        }

        adapter.save(caso);
        jpaRepository.flush();

        Caso leido = adapter.findById(caso.getId()).orElseThrow();
        assertEquals(TipoEventoCaso.values().length, leido.getEventos().size());
        assertEquals(TipoPrueba.values().length, leido.getResultados().size());
    }
}
