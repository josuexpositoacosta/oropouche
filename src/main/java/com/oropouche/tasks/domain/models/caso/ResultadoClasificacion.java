package com.oropouche.tasks.domain.models.caso;

import java.util.List;
import java.util.Optional;

/**
 * Resultado de evaluar un caso con una definición de caso.
 *
 * @param sugerida  clasificación sugerida; vacía si no cumple la definición de caso sospechoso
 * @param criterios explicación legible de los criterios cumplidos (y de los que faltan)
 */
public record ResultadoClasificacion(Optional<Clasificacion> sugerida, List<String> criterios) {

    public ResultadoClasificacion {
        criterios = List.copyOf(criterios);
    }
}
