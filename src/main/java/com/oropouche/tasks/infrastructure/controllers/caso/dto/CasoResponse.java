package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.oropouche.tasks.domain.models.caso.AgenteEtiologico;
import com.oropouche.tasks.domain.models.caso.CambioClasificacion;
import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.Manifestacion;
import com.oropouche.tasks.domain.models.caso.ResultadoLaboratorio;
import com.oropouche.tasks.domain.models.caso.ResultadoPrueba;
import com.oropouche.tasks.domain.models.caso.TipoMuestra;
import com.oropouche.tasks.domain.models.caso.TipoPrueba;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private List<Resultado> resultados;
    private List<Cambio> historial;

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

    public static CasoResponse from(EvaluacionCaso evaluacion) {

        Caso c = evaluacion.caso();
        Clasificacion sugerida = evaluacion.sugerencia().sugerida().orElse(null);

        return new CasoResponse(
                c.getId(),
                c.getPacienteId(),
                c.getAreaSaludId(),
                c.getFechaInicioSintomas(),
                c.getFechaNotificacion(),
                c.isFiebre(),
                c.isCefaleaIntensa(),
                new TreeSet<>(c.getManifestaciones()),
                c.isRecaida(),
                c.isEmbarazada(),
                c.getSemanasGestacion(),
                c.isAreaTransmisionOViaje(),
                c.getDetalleAreaTransmision(),
                c.isNexoCasoConfirmado(),
                c.getClasificacion(),
                c.getEstado(),
                new Sugerencia(sugerida, sugerida == c.getClasificacion(), evaluacion.sugerencia().criterios()),
                c.getResultados().stream().map(CasoResponse::toResultado).collect(Collectors.toList()),
                c.getHistorial().stream().map(CasoResponse::toCambio).collect(Collectors.toList())
        );
    }

    private static Resultado toResultado(ResultadoLaboratorio r) {
        return new Resultado(r.getId(), r.getAgente(), r.getTipoPrueba(), r.getTipoMuestra(),
                r.getResultado(), r.getFechaToma(), r.getFechaResultado());
    }

    private static Cambio toCambio(CambioClasificacion h) {
        return new Cambio(h.getFecha(), h.getAnterior(), h.getNueva(), h.getMotivo(), h.getUsuario());
    }
}
