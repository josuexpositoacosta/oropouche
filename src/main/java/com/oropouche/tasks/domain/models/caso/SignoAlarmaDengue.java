package com.oropouche.tasks.domain.models.caso;

/**
 * Signos de alarma de dengue según los Algoritmos para el manejo clínico de los casos de dengue
 * (OPS, 2024). La OPS indica internar en hospital o unidades de dengue (DCSA).
 */
public enum SignoAlarmaDengue {
    /** Dolor abdominal intenso y sostenido, o dolor a la palpación del abdomen. */
    DOLOR_ABDOMINAL_INTENSO,
    VOMITOS_PERSISTENTES,
    ACUMULACION_LIQUIDOS,
    SANGRADO_MUCOSAS,
    LETARGO_IRRITABILIDAD,
    /** Hipotensión postural (lipotimia). */
    HIPOTENSION_POSTURAL,
    /** Hepatomegalia mayor de 2 cm. */
    HEPATOMEGALIA,
    AUMENTO_PROGRESIVO_HEMATOCRITO
}
