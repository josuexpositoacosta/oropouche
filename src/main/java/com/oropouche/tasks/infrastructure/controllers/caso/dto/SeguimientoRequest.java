package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import java.time.LocalDate;

import com.oropouche.tasks.domain.models.caso.EvolucionCaso;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeguimientoRequest {

    private EvolucionCaso evolucion;
    private String observaciones;
    private LocalDate proximoControl;
}
