package com.oropouche.tasks.domain.models.caso;

/** Manifestaciones clínicas de la definición de caso sospechoso de la OPS (además de fiebre y cefalea intensa). */
public enum Manifestacion {
    MIALGIA_ARTRALGIA,
    ESCALOFRIOS,
    FOTOFOBIA,
    MAREOS,
    DOLOR_RETROOCULAR,
    /** Náusea, vómitos o diarrea (3 o más episodios en 24 h con cambio de consistencia). */
    GASTROINTESTINAL,
    /** Diplopía, parestesia, meningitis, encefalitis o meningoencefalitis. */
    NEUROLOGICA
}
