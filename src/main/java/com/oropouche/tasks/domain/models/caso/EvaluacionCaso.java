package com.oropouche.tasks.domain.models.caso;

/**
 * Un caso junto con la clasificación que sugiere la definición de caso vigente.
 */
public record EvaluacionCaso(Caso caso, ResultadoClasificacion sugerencia) {
}
