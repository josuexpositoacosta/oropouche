package com.oropouche.tasks.domain.models.caso;

/**
 * Definición de caso usada para sugerir la clasificación. Permite añadir definiciones
 * nacionales además de la de la OPS.
 */
public interface DefinicionCaso {

    String nombre();

    ResultadoClasificacion evaluar(Caso caso);
}
