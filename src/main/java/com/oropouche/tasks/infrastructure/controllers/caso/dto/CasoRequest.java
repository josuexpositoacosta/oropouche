package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import java.time.LocalDate;
import java.util.Set;

import com.oropouche.tasks.domain.models.caso.Manifestacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos para registrar o actualizar un caso. {@code pacienteId} y {@code motivo} solo se usan al registrar. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CasoRequest {

    private Long pacienteId;
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
    private String motivo;
}
