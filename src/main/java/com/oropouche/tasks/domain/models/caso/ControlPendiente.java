package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDate;

/** Caso abierto con un próximo control programado. */
public record ControlPendiente(Caso caso, LocalDate fecha, boolean vencido) {
}
