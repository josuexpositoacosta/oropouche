package com.oropouche.tasks.domain.models.caso;

public enum TipoPrueba {
    /** Detección de ARN viral. Confirmatoria. */
    RT_PCR,
    /** Detección de antígenos virales. Confirmatoria. */
    ANTIGENO,
    /** Seroconversión o aumento de 4 veces del título en muestras pareadas. Confirmatoria. */
    SEROCONVERSION_PAREADA,
    /** RT-PCR o inmunohistoquímica post mortem. Confirmatoria. */
    POST_MORTEM,
    /** IgM en líquido cefalorraquídeo. Confirmatoria solo con manifestación neurológica. */
    IGM_LCR,
    /** ELISA IgM en suero. Criterio de caso probable. */
    ELISA_IGM
}
