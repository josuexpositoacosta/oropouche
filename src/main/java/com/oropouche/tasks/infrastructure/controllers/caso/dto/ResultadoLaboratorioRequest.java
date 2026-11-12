package com.oropouche.tasks.infrastructure.controllers.caso.dto;

import java.time.LocalDate;

import com.oropouche.tasks.domain.models.caso.AgenteEtiologico;
import com.oropouche.tasks.domain.models.caso.ResultadoPrueba;
import com.oropouche.tasks.domain.models.caso.TipoMuestra;
import com.oropouche.tasks.domain.models.caso.TipoPrueba;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoLaboratorioRequest {

    private AgenteEtiologico agente;
    private TipoPrueba tipoPrueba;
    private TipoMuestra tipoMuestra;
    private ResultadoPrueba resultado;
    private LocalDate fechaToma;
    private LocalDate fechaResultado;
}
