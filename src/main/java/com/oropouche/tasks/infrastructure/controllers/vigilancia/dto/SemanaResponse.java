package com.oropouche.tasks.infrastructure.controllers.vigilancia.dto;

import java.time.LocalDate;
import java.util.Map;

import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.vigilancia.ConteoSemana;

public record SemanaResponse(
        int anio,
        int semana,
        String etiqueta,
        LocalDate inicio,
        LocalDate fin,
        long total,
        Map<Clasificacion, Long> porClasificacion) {

    public static SemanaResponse from(ConteoSemana c) {
        return new SemanaResponse(c.semana().anio(), c.semana().semana(), c.semana().etiqueta(),
                c.semana().inicio(), c.semana().fin(), c.total(), c.porClasificacion());
    }
}
