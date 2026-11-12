package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.oropouche.tasks.domain.models.caso.AgenteEtiologico;
import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.EvolucionCaso;
import com.oropouche.tasks.domain.models.caso.Manifestacion;
import com.oropouche.tasks.domain.models.caso.ModalidadAtencion;
import com.oropouche.tasks.domain.models.caso.ResultadoPrueba;
import com.oropouche.tasks.domain.models.caso.SignoAlarmaDengue;
import com.oropouche.tasks.domain.models.caso.SignoClinico;
import com.oropouche.tasks.domain.models.caso.TipoAlerta;
import com.oropouche.tasks.domain.models.caso.TipoEventoCaso;
import com.oropouche.tasks.domain.models.caso.TipoMuestra;
import com.oropouche.tasks.domain.models.caso.TipoPrueba;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CasoResponse {

    private Long id;
    private Long pacienteId;
    private Long areaSaludId;
    private LocalDate fechaInicioSintomas;
    private LocalDate fechaNotificacion;
    private boolean fiebre;
    private boolean cefaleaIntensa;
    private Set<Manifestacion> manifestaciones;
    private boolean recaida;
    private boolean embarazada;
    private Integer semanasGestacion;
    private boolean areaTransmisionOViaje;
    private String detalleAreaTransmision;
    private boolean nexoCasoConfirmado;
    private Clasificacion clasificacion;
    private EstadoCaso estado;
    private Sugerencia sugerencia;
    private List<AlertaDto> alertas;
    private LocalDate proximoControl;
    private List<Resultado> resultados;
    private List<Cambio> historial;
    private List<Evaluacion> evaluaciones;
    private List<DictamenDto> dictamenes;
    private List<Plan> planes;
    private List<SeguimientoDto> seguimientos;
    private DesenlaceResponse desenlace;
    private LocalDate fechaCierre;
    private String observacionesCierre;
    private List<Evento> eventos;

    /** Clasificación que sugiere la definición de caso. {@code clasificacion} es nula si no cumple la de sospechoso. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sugerencia {
        private Clasificacion clasificacion;
        private boolean coincideConActual;
        private List<String> criterios;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertaDto {
        private TipoAlerta tipo;
        private String mensaje;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Resultado {
        private Long id;
        private AgenteEtiologico agente;
        private TipoPrueba tipoPrueba;
        private TipoMuestra tipoMuestra;
        private ResultadoPrueba resultado;
        private LocalDate fechaToma;
        private LocalDate fechaResultado;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cambio {
        private LocalDateTime fecha;
        private Clasificacion anterior;
        private Clasificacion nueva;
        private String motivo;
        private String usuario;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Evaluacion {
        private Long id;
        private LocalDateTime fecha;
        private BigDecimal temperatura;
        private Integer frecuenciaCardiaca;
        private Integer frecuenciaRespiratoria;
        private Integer tensionSistolica;
        private Integer tensionDiastolica;
        private Integer saturacionO2;
        private Set<SignoClinico> signosClinicos;
        private Set<SignoAlarmaDengue> signosAlarmaDengue;
        private String observaciones;
        private String usuario;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DictamenDto {
        private Long id;
        private LocalDateTime fecha;
        private String diagnosticoPrincipal;
        private String diagnosticosDiferenciales;
        private String conclusion;
        private String usuario;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Plan {
        private Long id;
        private LocalDateTime fecha;
        private ModalidadAtencion modalidad;
        private String indicaciones;
        private LocalDate proximoControl;
        private String usuario;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeguimientoDto {
        private Long id;
        private LocalDateTime fecha;
        private EvolucionCaso evolucion;
        private String observaciones;
        private LocalDate proximoControl;
        private String usuario;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Evento {
        private LocalDateTime fecha;
        private TipoEventoCaso tipo;
        private String detalle;
        private String usuario;
    }

    public static CasoResponse from(EvaluacionCaso evaluacion) {

        Caso c = evaluacion.caso();
        Clasificacion sugerida = evaluacion.sugerencia().sugerida().orElse(null);

        CasoResponse r = new CasoResponse();
        r.setId(c.getId());
        r.setPacienteId(c.getPacienteId());
        r.setAreaSaludId(c.getAreaSaludId());
        r.setFechaInicioSintomas(c.getFechaInicioSintomas());
        r.setFechaNotificacion(c.getFechaNotificacion());
        r.setFiebre(c.isFiebre());
        r.setCefaleaIntensa(c.isCefaleaIntensa());
        r.setManifestaciones(new TreeSet<>(c.getManifestaciones()));
        r.setRecaida(c.isRecaida());
        r.setEmbarazada(c.isEmbarazada());
        r.setSemanasGestacion(c.getSemanasGestacion());
        r.setAreaTransmisionOViaje(c.isAreaTransmisionOViaje());
        r.setDetalleAreaTransmision(c.getDetalleAreaTransmision());
        r.setNexoCasoConfirmado(c.isNexoCasoConfirmado());
        r.setClasificacion(c.getClasificacion());
        r.setEstado(c.getEstado());
        r.setSugerencia(new Sugerencia(sugerida, sugerida == c.getClasificacion(), evaluacion.sugerencia().criterios()));
        r.setAlertas(mapear(evaluacion.alertas(), a -> new AlertaDto(a.tipo(), a.mensaje())));
        r.setProximoControl(c.estaAbierto() ? c.proximoControl() : null);
        r.setResultados(mapear(c.getResultados(), x -> new Resultado(x.getId(), x.getAgente(), x.getTipoPrueba(),
                x.getTipoMuestra(), x.getResultado(), x.getFechaToma(), x.getFechaResultado())));
        r.setHistorial(mapear(c.getHistorial(), h -> new Cambio(h.getFecha(), h.getAnterior(), h.getNueva(),
                h.getMotivo(), h.getUsuario())));
        r.setEvaluaciones(mapear(c.getEvaluaciones(), e -> new Evaluacion(e.getId(), e.getFecha(), e.getTemperatura(),
                e.getFrecuenciaCardiaca(), e.getFrecuenciaRespiratoria(), e.getTensionSistolica(),
                e.getTensionDiastolica(), e.getSaturacionO2(), new TreeSet<>(e.getSignosClinicos()),
                new TreeSet<>(e.getSignosAlarmaDengue()), e.getObservaciones(), e.getUsuario())));
        r.setDictamenes(mapear(c.getDictamenes(), d -> new DictamenDto(d.getId(), d.getFecha(),
                d.getDiagnosticoPrincipal(), d.getDiagnosticosDiferenciales(), d.getConclusion(), d.getUsuario())));
        r.setPlanes(mapear(c.getPlanes(), p -> new Plan(p.getId(), p.getFecha(), p.getModalidad(),
                p.getIndicaciones(), p.getProximoControl(), p.getUsuario())));
        r.setSeguimientos(mapear(c.getSeguimientos(), s -> new SeguimientoDto(s.getId(), s.getFecha(),
                s.getEvolucion(), s.getObservaciones(), s.getProximoControl(), s.getUsuario())));
        r.setDesenlace(DesenlaceResponse.from(c.getDesenlace()));
        r.setFechaCierre(c.getFechaCierre());
        r.setObservacionesCierre(c.getObservacionesCierre());
        r.setEventos(mapear(c.getEventos(), e -> new Evento(e.getFecha(), e.getTipo(), e.getDetalle(), e.getUsuario())));
        return r;
    }

    private static <A, B> List<B> mapear(List<A> origen, Function<A, B> funcion) {
        return origen.stream().map(funcion).collect(Collectors.toList());
    }
}
