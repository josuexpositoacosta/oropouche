package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Caso de fiebre de Oropouche de un paciente. */
@Getter
@Setter
@NoArgsConstructor
public class Caso {

    private Long id;
    private Long pacienteId;
    private Long areaSaludId;

    private LocalDate fechaInicioSintomas;
    private LocalDate fechaNotificacion;

    // Cuadro clínico
    private boolean fiebre;
    private boolean cefaleaIntensa;
    private Set<Manifestacion> manifestaciones = EnumSet.noneOf(Manifestacion.class);
    private boolean recaida;

    // Embarazo
    private boolean embarazada;
    private Integer semanasGestacion;

    // Criterios epidemiológicos
    private boolean areaTransmisionOViaje;
    private String detalleAreaTransmision;
    private boolean nexoCasoConfirmado;

    private Clasificacion clasificacion;
    private EstadoCaso estado;

    private List<ResultadoLaboratorio> resultados = new ArrayList<>();
    private List<CambioClasificacion> historial = new ArrayList<>();

    public void setManifestaciones(Set<Manifestacion> manifestaciones) {
        this.manifestaciones = manifestaciones == null || manifestaciones.isEmpty()
                ? EnumSet.noneOf(Manifestacion.class)
                : EnumSet.copyOf(manifestaciones);
    }

    public void setResultados(List<ResultadoLaboratorio> resultados) {
        this.resultados = resultados == null ? new ArrayList<>() : new ArrayList<>(resultados);
    }

    public void setHistorial(List<CambioClasificacion> historial) {
        this.historial = historial == null ? new ArrayList<>() : new ArrayList<>(historial);
    }

    /** Días entre el inicio de síntomas y la notificación; -1 si falta alguna fecha. */
    public long diasEvolucionAlNotificar() {
        if (fechaInicioSintomas == null || fechaNotificacion == null) {
            return -1;
        }
        return ChronoUnit.DAYS.between(fechaInicioSintomas, fechaNotificacion);
    }

    public boolean estaAbierto() {
        return estado == EstadoCaso.ABIERTO;
    }
}
