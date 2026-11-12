package com.oropouche.tasks.application.services.caso;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.ClasificadorOPS;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.ControlPendiente;
import com.oropouche.tasks.domain.models.caso.Desenlace;
import com.oropouche.tasks.domain.models.caso.Dictamen;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionClinica;
import com.oropouche.tasks.domain.models.caso.EvolucionCaso;
import com.oropouche.tasks.domain.models.caso.Manifestacion;
import com.oropouche.tasks.domain.models.caso.ModalidadAtencion;
import com.oropouche.tasks.domain.models.caso.PlanAtencion;
import com.oropouche.tasks.domain.models.caso.Seguimiento;
import com.oropouche.tasks.domain.models.caso.SignoClinico;
import com.oropouche.tasks.domain.models.caso.TipoAlerta;
import com.oropouche.tasks.domain.models.caso.TipoEventoCaso;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;
import com.oropouche.tasks.domain.ports.out.DesenlaceRepositoryPort;
import com.oropouche.tasks.domain.ports.out.UsuarioActualPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtencionClinicaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 12);

    @Mock
    private CasoRepositoryPort casoRepository;

    @Mock
    private DesenlaceRepositoryPort desenlaceRepository;

    @Mock
    private UsuarioActualPort usuarioActual;

    private AtencionClinicaService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(HOY.atTime(10, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new AtencionClinicaService(casoRepository, desenlaceRepository, usuarioActual,
                new ClasificadorOPS(), clock);
    }

    private static Caso caso(EstadoCaso estado) {
        Caso c = new Caso();
        c.setId(1L);
        c.setPacienteId(7L);
        c.setAreaSaludId(3L);
        c.setFechaInicioSintomas(HOY.minusDays(4));
        c.setFechaNotificacion(HOY.minusDays(2));
        c.setFiebre(true);
        c.setCefaleaIntensa(true);
        c.setManifestaciones(EnumSet.of(Manifestacion.MAREOS, Manifestacion.ESCALOFRIOS));
        c.setClasificacion(Clasificacion.SOSPECHOSO);
        c.setEstado(estado);
        return c;
    }

    private void conCaso(Caso c) {
        when(casoRepository.findById(1L)).thenReturn(Optional.of(c));
    }

    private void guardarDevuelveLoMismo() {
        when(casoRepository.save(any(Caso.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // --- Evaluación clínica

    @Test
    void evaluacionLaFechaYElUsuarioLosPoneElSistemaYGeneraAlertas() {

        conCaso(caso(EstadoCaso.ABIERTO));
        guardarDevuelveLoMismo();
        when(usuarioActual.username()).thenReturn("dr.lopez");

        EvaluacionClinica e = new EvaluacionClinica();
        e.setTemperatura(new BigDecimal("38.9"));
        e.setSignosClinicos(EnumSet.of(SignoClinico.NISTAGMO));
        e.setFecha(HOY.minusYears(1).atStartOfDay());
        e.setUsuario("otro");

        EvaluacionCaso r = service.registrarEvaluacion(1L, e);

        EvaluacionClinica guardada = r.caso().getEvaluaciones().get(0);
        assertEquals(HOY.atTime(10, 0), guardada.getFecha(), "la fecha la pone el sistema");
        assertEquals("dr.lopez", guardada.getUsuario());
        assertTrue(r.alertas().stream().anyMatch(a -> a.tipo() == TipoAlerta.NEUROINVASIVA));
    }

    @Test
    void evaluacionVaciaOConSignosVitalesImposiblesSeRechaza() {

        conCaso(caso(EstadoCaso.ABIERTO));

        assertThrows(IllegalArgumentException.class, () -> service.registrarEvaluacion(1L, new EvaluacionClinica()));

        EvaluacionClinica fiebre50 = new EvaluacionClinica();
        fiebre50.setTemperatura(new BigDecimal("50"));
        assertThrows(IllegalArgumentException.class, () -> service.registrarEvaluacion(1L, fiebre50));

        EvaluacionClinica tension = new EvaluacionClinica();
        tension.setTensionSistolica(80);
        tension.setTensionDiastolica(90);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarEvaluacion(1L, tension));
        assertTrue(ex.getMessage().contains("sistólica debe ser mayor"));

        verify(casoRepository, never()).save(any());
    }

    // --- Dictamen y plan

    @Test
    void dictamenExigeDiagnosticoYConclusion() {

        conCaso(caso(EstadoCaso.ABIERTO));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.registrarDictamen(1L, new Dictamen(null, null, "Fiebre de Oropouche", null, " ", null)));
        assertEquals("La conclusión es obligatoria", ex.getMessage());
    }

    @Test
    void planNoAdmiteProximoControlPasado() {

        conCaso(caso(EstadoCaso.ABIERTO));

        assertThrows(IllegalArgumentException.class, () -> service.registrarPlan(1L,
                new PlanAtencion(null, null, ModalidadAtencion.AMBULATORIO, "Reposo e hidratación",
                        HOY.minusDays(1), null)));
    }

    @Test
    void planValidoSeRegistra() {

        conCaso(caso(EstadoCaso.ABIERTO));
        guardarDevuelveLoMismo();

        EvaluacionCaso r = service.registrarPlan(1L, new PlanAtencion(null, null,
                ModalidadAtencion.AMBULATORIO, " Reposo, hidratación y antipiréticos ", HOY.plusDays(3), null));

        assertEquals("Reposo, hidratación y antipiréticos", r.caso().getPlanes().get(0).getIndicaciones());
        assertEquals(HOY.plusDays(3), r.caso().proximoControl());
    }

    // --- Seguimiento

    @Test
    void seguimientoConRecaidaMarcaElCaso() {

        conCaso(caso(EstadoCaso.ABIERTO));
        guardarDevuelveLoMismo();

        EvaluacionCaso r = service.registrarSeguimiento(1L,
                new Seguimiento(null, null, EvolucionCaso.RECAIDA, "Reaparece la fiebre", null, null));

        assertTrue(r.caso().isRecaida());
    }

    // --- Cierre y reapertura

    @Test
    void cerrarConDesenlaceActivoRegistraEvento() {

        conCaso(caso(EstadoCaso.ABIERTO));
        guardarDevuelveLoMismo();
        when(desenlaceRepository.findByCodigo("RECUPERADO"))
                .thenReturn(Optional.of(new Desenlace(1L, "RECUPERADO", "Recuperado", true)));

        EvaluacionCaso r = service.cerrar(1L, "RECUPERADO", null, "Alta");

        assertEquals(EstadoCaso.CERRADO, r.caso().getEstado());
        assertEquals(HOY, r.caso().getFechaCierre());
        assertEquals(TipoEventoCaso.CIERRE, r.caso().getEventos().get(0).getTipo());
        assertTrue(r.caso().getEventos().get(0).getDetalle().contains("Recuperado"));
    }

    @Test
    void cerrarValidaDesenlaceYFecha() {

        conCaso(caso(EstadoCaso.ABIERTO));

        when(desenlaceRepository.findByCodigo("TRASLADADO"))
                .thenReturn(Optional.of(new Desenlace(4L, "TRASLADADO", "Trasladado", false)));
        assertThrows(IllegalArgumentException.class, () -> service.cerrar(1L, "TRASLADADO", null, null),
                "desenlace desactivado");

        when(desenlaceRepository.findByCodigo("INVENTADO")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.cerrar(1L, "INVENTADO", null, null));

        when(desenlaceRepository.findByCodigo("RECUPERADO"))
                .thenReturn(Optional.of(new Desenlace(1L, "RECUPERADO", "Recuperado", true)));
        assertThrows(IllegalArgumentException.class,
                () -> service.cerrar(1L, "RECUPERADO", HOY.minusDays(10), null), "antes de la notificación");
        assertThrows(IllegalArgumentException.class,
                () -> service.cerrar(1L, "RECUPERADO", HOY.plusDays(1), null), "fecha futura");

        verify(casoRepository, never()).save(any());
    }

    @Test
    void casoCerradoNoAdmiteRegistrosClinicos() {

        conCaso(caso(EstadoCaso.CERRADO));

        assertThrows(IllegalArgumentException.class, () -> service.registrarSeguimiento(1L,
                new Seguimiento(null, null, EvolucionCaso.MEJORIA, null, null, null)));
    }

    @Test
    void reabrirExigeMotivoYConservaElCierreAnteriorEnElEvento() {

        Caso cerrado = caso(EstadoCaso.CERRADO);
        cerrado.setDesenlace(new Desenlace(1L, "RECUPERADO", "Recuperado", true));
        cerrado.setFechaCierre(HOY.minusDays(1));
        conCaso(cerrado);

        assertThrows(IllegalArgumentException.class, () -> service.reabrir(1L, " "));

        guardarDevuelveLoMismo();
        EvaluacionCaso r = service.reabrir(1L, "Recaída tras el alta");

        assertEquals(EstadoCaso.ABIERTO, r.caso().getEstado());
        assertNull(r.caso().getDesenlace());
        assertEquals(TipoEventoCaso.REAPERTURA, r.caso().getEventos().get(0).getTipo());
        assertTrue(r.caso().getEventos().get(0).getDetalle().contains("cierre anterior: Recuperado"));
    }

    @Test
    void reabrirRechazaSiElPacienteTieneOtroCasoAbierto() {

        conCaso(caso(EstadoCaso.CERRADO));
        when(casoRepository.existsAbiertoByPacienteId(7L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.reabrir(1L, "Recaída"));
    }

    // --- Agenda de controles

    @Test
    void controlesPendientesIncluyeVencidosYOrdenaPorFecha() {

        Caso vencido = caso(EstadoCaso.ABIERTO);
        vencido.getPlanes().add(new PlanAtencion(1L, HOY.minusDays(5).atTime(9, 0), ModalidadAtencion.AMBULATORIO,
                "x", HOY.minusDays(1), "dra"));
        Caso proximo = caso(EstadoCaso.ABIERTO);
        proximo.setId(2L);
        proximo.getPlanes().add(new PlanAtencion(2L, HOY.atTime(8, 0), ModalidadAtencion.AMBULATORIO,
                "x", HOY.plusDays(2), "dra"));
        Caso lejano = caso(EstadoCaso.ABIERTO);
        lejano.setId(3L);
        lejano.getPlanes().add(new PlanAtencion(3L, HOY.atTime(8, 0), ModalidadAtencion.AMBULATORIO,
                "x", HOY.plusDays(30), "dra"));
        Caso sinControl = caso(EstadoCaso.ABIERTO);
        sinControl.setId(4L);

        when(casoRepository.findAll(3L, null, EstadoCaso.ABIERTO)).thenReturn(List.of(proximo, lejano, vencido, sinControl));

        List<ControlPendiente> r = service.controlesPendientes(3L, HOY.plusDays(7));

        assertEquals(2, r.size());
        assertEquals(1L, r.get(0).caso().getId());
        assertTrue(r.get(0).vencido());
        assertEquals(2L, r.get(1).caso().getId());
        assertFalse(r.get(1).vencido());
    }
}
