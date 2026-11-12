package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlertasClinicasTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 12);

    private final AlertasClinicas alertas = new AlertasClinicas();

    private static Caso casoAbierto() {
        Caso caso = new Caso();
        caso.setEstado(EstadoCaso.ABIERTO);
        return caso;
    }

    private static EvaluacionClinica evaluacion(LocalDateTime fecha, EnumSet<SignoClinico> signos,
                                                EnumSet<SignoAlarmaDengue> alarma) {
        EvaluacionClinica e = new EvaluacionClinica();
        e.setFecha(fecha);
        e.setSignosClinicos(signos);
        e.setSignosAlarmaDengue(alarma);
        return e;
    }

    private List<TipoAlerta> tipos(Caso caso) {
        return alertas.evaluar(caso, HOY).stream().map(Alerta::tipo).collect(Collectors.toList());
    }

    @Test
    void sinDatosNoHayAlertas() {
        assertTrue(tipos(casoAbierto()).isEmpty());
    }

    @Test
    void signoNeurologicoGeneraAlertaNeuroinvasiva() {
        Caso caso = casoAbierto();
        caso.getEvaluaciones().add(evaluacion(HOY.atTime(9, 0),
                EnumSet.of(SignoClinico.RIGIDEZ_NUCA), EnumSet.noneOf(SignoAlarmaDengue.class)));

        List<Alerta> resultado = alertas.evaluar(caso, HOY);

        assertEquals(List.of(TipoAlerta.NEUROINVASIVA), resultado.stream().map(Alerta::tipo).collect(Collectors.toList()));
        assertTrue(resultado.get(0).mensaje().contains("RIGIDEZ_NUCA"));
        assertTrue(resultado.get(0).mensaje().contains("unidad especializada"));
    }

    @Test
    void manifestacionNeurologicaDelCasoTambienAlerta() {
        Caso caso = casoAbierto();
        caso.setManifestaciones(EnumSet.of(Manifestacion.NEUROLOGICA));
        assertEquals(List.of(TipoAlerta.NEUROINVASIVA), tipos(caso));
    }

    @Test
    void soloCuentaLaEvaluacionMasReciente() {
        Caso caso = casoAbierto();
        caso.getEvaluaciones().add(evaluacion(HOY.atTime(12, 0),
                EnumSet.noneOf(SignoClinico.class), EnumSet.noneOf(SignoAlarmaDengue.class)));
        caso.getEvaluaciones().add(evaluacion(HOY.minusDays(1).atTime(9, 0),
                EnumSet.of(SignoClinico.LETARGIA, SignoClinico.EPISTAXIS),
                EnumSet.of(SignoAlarmaDengue.VOMITOS_PERSISTENTES)));

        assertTrue(tipos(caso).isEmpty(), "la evaluación de hoy ya no presenta esos signos");
    }

    @Test
    void signosDeAlarmaDeDengueYHemorragias() {
        Caso caso = casoAbierto();
        caso.getEvaluaciones().add(evaluacion(HOY.atTime(9, 0),
                EnumSet.of(SignoClinico.PETEQUIAS), EnumSet.of(SignoAlarmaDengue.DOLOR_ABDOMINAL_INTENSO)));

        assertEquals(List.of(TipoAlerta.SIGNOS_ALARMA_DENGUE, TipoAlerta.HEMORRAGIA), tipos(caso));
    }

    @Test
    void gestanteYControlVencido() {
        Caso caso = casoAbierto();
        caso.setEmbarazada(true);
        caso.getPlanes().add(new PlanAtencion(null, HOY.minusDays(5).atTime(9, 0), ModalidadAtencion.AMBULATORIO,
                "Reposo", HOY.minusDays(1), "dra"));

        assertEquals(List.of(TipoAlerta.GESTANTE, TipoAlerta.CONTROL_VENCIDO), tipos(caso));
    }

    @Test
    void elProximoControlLoMarcaElRegistroMasReciente() {
        Caso caso = casoAbierto();
        caso.getPlanes().add(new PlanAtencion(null, HOY.minusDays(5).atTime(9, 0), ModalidadAtencion.AMBULATORIO,
                "Reposo", HOY.minusDays(1), "dra"));
        caso.getSeguimientos().add(new Seguimiento(null, HOY.minusDays(1).atTime(9, 0), EvolucionCaso.MEJORIA,
                null, HOY.plusDays(3), "dra"));

        assertEquals(HOY.plusDays(3), caso.proximoControl());
        assertTrue(tipos(caso).isEmpty());
    }

    @Test
    void casoCerradoNoTieneAlertas() {
        Caso caso = casoAbierto();
        caso.setEmbarazada(true);
        caso.setEstado(EstadoCaso.CERRADO);
        assertTrue(tipos(caso).isEmpty());
    }
}
