package com.oropouche.tasks.domain.models.vigilancia;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

/**
 * Semana epidemiológica: de domingo a sábado. La semana 1 es la que termina en el primer sábado de
 * enero que incluye cuatro o más días de enero, aunque empiece en diciembre.
 */
public record SemanaEpidemiologica(int anio, int semana, LocalDate inicio, LocalDate fin) {

    /** Domingo como primer día y semana 1 con al menos 4 días del año. */
    private static final WeekFields REGLA = WeekFields.of(DayOfWeek.SUNDAY, 4);

    public static SemanaEpidemiologica de(LocalDate fecha) {
        LocalDate inicio = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        return new SemanaEpidemiologica(
                fecha.get(REGLA.weekBasedYear()),
                fecha.get(REGLA.weekOfWeekBasedYear()),
                inicio,
                inicio.plusDays(6));
    }

    public SemanaEpidemiologica siguiente() {
        return de(inicio.plusWeeks(1));
    }

    /** Etiqueta habitual, por ejemplo "2026-SE01". */
    public String etiqueta() {
        return String.format("%d-SE%02d", anio, semana);
    }
}
