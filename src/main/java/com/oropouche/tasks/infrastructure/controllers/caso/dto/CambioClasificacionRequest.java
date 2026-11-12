package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import com.oropouche.tasks.domain.models.caso.Clasificacion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioClasificacionRequest {

    private Clasificacion clasificacion;
    private String motivo;
}
