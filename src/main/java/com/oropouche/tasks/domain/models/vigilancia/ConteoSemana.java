package com.oropouche.tasks.domain.models.vigilancia;

import java.util.Map;

import com.oropouche.tasks.domain.models.caso.Clasificacion;

/** Casos de una semana epidemiológica (curva epidémica). */
public record ConteoSemana(SemanaEpidemiologica semana, long total, Map<Clasificacion, Long> porClasificacion) {
}
