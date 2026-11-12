package com.oropouche.tasks.domain.models.caso;

public enum EvolucionCaso {
    MEJORIA,
    ESTABLE,
    EMPEORAMIENTO,
    /** Reaparición de síntomas tras la mejoría (hasta el 60 % de los casos según la OPS). */
    RECAIDA
}
