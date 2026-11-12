package com.oropouche.tasks.application.services.vigilancia;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.oropouche.tasks.domain.models.area_salud.AreaSalud;
import com.oropouche.tasks.domain.models.caso.AgenteEtiologico;
import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.Desenlace;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.Manifestacion;
import com.oropouche.tasks.domain.models.caso.ModalidadAtencion;
import com.oropouche.tasks.domain.models.caso.PlanAtencion;
import com.oropouche.tasks.domain.models.caso.ResultadoLaboratorio;
import com.oropouche.tasks.domain.models.caso.ResultadoPrueba;
import com.oropouche.tasks.domain.models.caso.TipoMuestra;
import com.oropouche.tasks.domain.models.caso.TipoPrueba;
import com.oropouche.tasks.domain.models.vigilancia.ConteoArea;
import com.oropouche.tasks.domain.models.vigilancia.ConteoSemana;
import com.oropouche.tasks.domain.models.vigilancia.CriterioFecha;
import com.oropouche.tasks.domain.models.vigilancia.FiltroVigilancia;
import com.oropouche.tasks.domain.models.vigilancia.ResumenVigilancia;
import com.oropouche.tasks.domain.ports.out.AreaSaludRepositoryPort;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VigilanciaServiceTest {

    /** Jueves 12 de noviembre de 2026 (semana del 8 al 14). */
    private static final LocalDate HOY = LocalDate.of(2026, 11, 12);

    @Mock
    private CasoRepositoryPort casos;

    @Mock
    private AreaSaludRepositoryPort areas;

    private VigilanciaService service;

    @BeforeEach
    void setUp() {
        service = new VigilanciaService(casos, areas,
                Clock.fixed(HOY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
    }

    private static Caso caso(long id, long area, LocalDate inicio, Clasificacion clasificacion, EstadoCaso estado) {
        Caso c = new Caso();
        c.setId(id);
        c.setAreaSaludId(area);
        c.setFechaInicioSintomas(inicio);
        c.setFechaNotificacion(inicio.plusDays(2));
        c.setClasificacion(clasificacion);
        c.setEstado(estado);
        return c;
    }

    private static AreaSalud area(long id, String codigo) {
        AreaSalud a = new AreaSalud();
        a.setId(id);
        a.setCodigo(codigo);
        a.setNombre("Área " + codigo);
        return a;
    }

    @Test
    void porDefectoConsultaLasUltimas12SemanasPorInicioDeSintomas() {
        FiltroVigilancia f = service.normalizar(null);

        assertEquals(HOY, f.hasta());
        assertEquals(LocalDate.of(2026, 8, 23), f.desde(), "domingo de 11 semanas antes de la semana actual");
        assertEquals(CriterioFecha.INICIO_SINTOMAS, f.criterio());
    }

    @Test
    void periodoInvertidoOMuyLargoSeRechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> service.normalizar(new FiltroVigilancia(HOY, HOY.minusDays(1), null, null)));
        assertThrows(IllegalArgumentException.class,
                () -> service.normalizar(new FiltroVigilancia(HOY.minusYears(4), HOY, null, null)));
    }

    @Test
    void resumenCuentaIndicadores() {
        Caso confirmado = caso(1, 3, HOY.minusDays(3), Clasificacion.CONFIRMADO, EstadoCaso.CERRADO);
        confirmado.setDesenlace(new Desenlace(1L, "RECUPERADO", "Recuperado", true));
        confirmado.setClasificacionValidada(true);
        confirmado.getResultados().add(new ResultadoLaboratorio(null, AgenteEtiologico.OROPOUCHE, TipoPrueba.RT_PCR,
                TipoMuestra.SUERO, ResultadoPrueba.POSITIVO, HOY.minusDays(2), HOY.minusDays(1)));
        Caso gestanteNeuro = caso(2, 3, HOY.minusDays(1), Clasificacion.PROBABLE, EstadoCaso.ABIERTO);
        gestanteNeuro.setEmbarazada(true);
        gestanteNeuro.setManifestaciones(EnumSet.of(Manifestacion.NEUROLOGICA));
        gestanteNeuro.getPlanes().add(new PlanAtencion(null, LocalDateTime.of(2026, 11, 11, 9, 0),
                ModalidadAtencion.INGRESO_UNIDAD_ESPECIALIZADA, "Monitoreo", null, "dr"));
        Caso sospechoso = caso(3, 4, HOY.minusDays(10), Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO);
        sospechoso.setRecaida(true);
        Caso descartado = caso(4, 4, HOY.minusDays(20), Clasificacion.DESCARTADO, EstadoCaso.CERRADO);
        descartado.setDesenlace(new Desenlace(1L, "RECUPERADO", "Recuperado", true));
        descartado.getResultados().add(new ResultadoLaboratorio(null, AgenteEtiologico.DENGUE, TipoPrueba.RT_PCR,
                TipoMuestra.SUERO, ResultadoPrueba.POSITIVO, HOY.minusDays(19), null));

        when(casos.findEntreFechas(any(), any(), any(), any()))
                .thenReturn(List.of(confirmado, gestanteNeuro, sospechoso, descartado));

        ResumenVigilancia r = service.resumen(null);

        assertEquals(4, r.totalCasos());
        assertEquals(1L, r.porClasificacion().get(Clasificacion.CONFIRMADO));
        assertEquals(1L, r.porClasificacion().get(Clasificacion.DESCARTADO));
        assertEquals(25.0, r.porcentajeConfirmados());
        assertEquals(2, r.abiertos());
        assertEquals(2, r.cerrados());
        assertEquals(1, r.clasificacionValidada());
        assertEquals(3, r.pendientesDeValidar());
        assertEquals(1, r.conLaboratorio(), "solo cuenta laboratorio de Oropouche");
        assertEquals(1, r.gestantes());
        assertEquals(1, r.neuroinvasivos());
        assertEquals(1, r.recaidas());
        assertEquals(1, r.hospitalizados());
        assertEquals(2L, r.porDesenlace().get("RECUPERADO"));
    }

    @Test
    void resumenSinCasosNoDivideEntreCero() {
        when(casos.findEntreFechas(any(), any(), any(), any())).thenReturn(List.of());

        ResumenVigilancia r = service.resumen(null);

        assertEquals(0, r.totalCasos());
        assertEquals(0.0, r.porcentajeConfirmados());
        assertEquals(0L, r.porClasificacion().get(Clasificacion.SOSPECHOSO), "todas las clasificaciones aparecen");
    }

    @Test
    void curvaIncluyeSemanasSinCasos() {
        LocalDate desde = LocalDate.of(2026, 10, 18);
        when(casos.findEntreFechas(null, desde, HOY, CriterioFecha.INICIO_SINTOMAS)).thenReturn(List.of(
                caso(1, 3, LocalDate.of(2026, 10, 19), Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO),
                caso(2, 3, LocalDate.of(2026, 10, 24), Clasificacion.CONFIRMADO, EstadoCaso.ABIERTO),
                caso(3, 3, LocalDate.of(2026, 11, 9), Clasificacion.PROBABLE, EstadoCaso.ABIERTO)));

        List<ConteoSemana> curva = service.curvaEpidemica(new FiltroVigilancia(desde, HOY, null, null));

        assertEquals(4, curva.size(), "del 18/10 al 14/11 son 4 semanas");
        assertEquals(List.of(2L, 0L, 0L, 1L), curva.stream().map(ConteoSemana::total).toList());
        assertEquals(1L, curva.get(0).porClasificacion().get(Clasificacion.CONFIRMADO));
        assertEquals(LocalDate.of(2026, 11, 8), curva.get(3).semana().inicio());
    }

    @Test
    void curvaPorNotificacionUsaEsaFecha() {
        LocalDate desde = LocalDate.of(2026, 11, 1);
        Caso c = caso(1, 3, LocalDate.of(2026, 10, 31), Clasificacion.SOSPECHOSO, EstadoCaso.ABIERTO);
        when(casos.findEntreFechas(null, desde, HOY, CriterioFecha.NOTIFICACION)).thenReturn(List.of(c));

        List<ConteoSemana> curva = service.curvaEpidemica(
                new FiltroVigilancia(desde, HOY, null, CriterioFecha.NOTIFICACION));

        assertEquals(1L, curva.get(0).total(), "notificado el 2/11 (semana del 1 al 7)");
    }

    @Test
    void porAreaIncluyeAreasSinCasos() {
        when(areas.findAll()).thenReturn(List.of(area(3, "NORTE"), area(4, "SUR")));
        when(casos.findEntreFechas(any(), any(), any(), any())).thenReturn(List.of(
                caso(1, 3, HOY.minusDays(1), Clasificacion.CONFIRMADO, EstadoCaso.ABIERTO),
                caso(2, 3, HOY.minusDays(2), Clasificacion.SOSPECHOSO, EstadoCaso.CERRADO)));

        List<ConteoArea> r = service.porArea(null);

        assertEquals(2, r.size());
        assertEquals("NORTE", r.get(0).codigo());
        assertEquals(2, r.get(0).total());
        assertEquals(1, r.get(0).abiertos());
        assertEquals(0, r.get(1).total());
    }
}
