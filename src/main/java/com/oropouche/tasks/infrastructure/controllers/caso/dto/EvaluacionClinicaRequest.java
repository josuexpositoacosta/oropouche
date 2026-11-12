package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import java.math.BigDecimal;
import java.util.Set;

import com.oropouche.tasks.domain.models.caso.SignoAlarmaDengue;
import com.oropouche.tasks.domain.models.caso.SignoClinico;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionClinicaRequest {

    private BigDecimal temperatura;
    private Integer frecuenciaCardiaca;
    private Integer frecuenciaRespiratoria;
    private Integer tensionSistolica;
    private Integer tensionDiastolica;
    private Integer saturacionO2;
    private Set<SignoClinico> signosClinicos;
    private Set<SignoAlarmaDengue> signosAlarmaDengue;
    private String observaciones;
}
