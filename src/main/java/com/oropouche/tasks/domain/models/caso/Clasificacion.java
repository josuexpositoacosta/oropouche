package com.oropouche.tasks.domain.models.caso;

/** Clasificación de un caso según las definiciones de la OPS. DESCARTADO no lo define la OPS: lo asigna el profesional. */
public enum Clasificacion {
    SOSPECHOSO,
    PROBABLE,
    CONFIRMADO,
    DESCARTADO
}
