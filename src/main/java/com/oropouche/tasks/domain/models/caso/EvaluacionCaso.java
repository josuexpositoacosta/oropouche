package com.oropouche.tasks.domain.models.caso;

import java.util.List;

/**
 * Un caso junto con la clasificación que sugiere la definición de caso vigente y sus alertas clínicas.
 */
public record EvaluacionCaso(Caso caso, ResultadoClasificacion sugerencia, List<Alerta> alertas) {

    public EvaluacionCaso {
        alertas = alertas == null ? List.of() : List.copyOf(alertas);
    }
}
