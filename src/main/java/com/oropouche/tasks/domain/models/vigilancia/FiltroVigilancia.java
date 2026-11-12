package com.oropouche.tasks.domain.models.vigilancia;

import java.time.LocalDate;

/** Periodo, área (null = todas) y criterio de fecha de una consulta de vigilancia. */
public record FiltroVigilancia(LocalDate desde, LocalDate hasta, Long areaSaludId, CriterioFecha criterio) {
}
