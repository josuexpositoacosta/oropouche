package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import java.time.LocalDate;

import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.ControlPendiente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControlPendienteResponse {

    private Long casoId;
    private Long pacienteId;
    private Long areaSaludId;
    private Clasificacion clasificacion;
    private LocalDate fecha;
    private boolean vencido;

    public static ControlPendienteResponse from(ControlPendiente c) {
        return new ControlPendienteResponse(c.caso().getId(), c.caso().getPacienteId(), c.caso().getAreaSaludId(),
                c.caso().getClasificacion(), c.fecha(), c.vencido());
    }
}
