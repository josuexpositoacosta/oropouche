package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import java.time.LocalDate;

import com.oropouche.tasks.domain.models.caso.ModalidadAtencion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanAtencionRequest {

    private ModalidadAtencion modalidad;
    private String indicaciones;
    private LocalDate proximoControl;
}
