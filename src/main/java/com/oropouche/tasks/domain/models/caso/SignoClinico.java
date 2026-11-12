package com.oropouche.tasks.domain.models.caso;

import java.util.EnumSet;
import java.util.Set;

/**
 * Signos clínicos de Oropouche descritos por la OPS (Actualización Epidemiológica, 13/08/2025,
 * apartado "Detección y manejo clínico").
 */
public enum SignoClinico {
    POSTRACION,
    LUMBALGIA,
    VOMITOS_PERSISTENTES,
    PETEQUIAS,
    EPISTAXIS,
    SANGRADO_GINGIVAL,
    VERTIGO,
    LETARGIA,
    NISTAGMO,
    RIGIDEZ_NUCA;

    /** Signos de posible forma neuroinvasiva (meningitis o encefalitis). */
    public static final Set<SignoClinico> NEUROLOGICOS = EnumSet.of(VERTIGO, LETARGIA, NISTAGMO, RIGIDEZ_NUCA);

    public static final Set<SignoClinico> HEMORRAGICOS = EnumSet.of(PETEQUIAS, EPISTAXIS, SANGRADO_GINGIVAL);
}
