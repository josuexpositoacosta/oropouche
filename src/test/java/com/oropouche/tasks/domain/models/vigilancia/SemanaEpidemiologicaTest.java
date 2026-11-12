package com.oropouche.tasks.domain.models.vigilancia;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SemanaEpidemiologicaTest {

    @Test
    void semanaUnoDe2026EsDel4Al10DeEnero() {
        SemanaEpidemiologica s = SemanaEpidemiologica.de(LocalDate.of(2026, 1, 7));

        assertEquals(2026, s.anio());
        assertEquals(1, s.semana());
        assertEquals(LocalDate.of(2026, 1, 4), s.inicio(), "empieza en domingo");
        assertEquals(LocalDate.of(2026, 1, 10), s.fin(), "termina en sábado");
        assertEquals("2026-SE01", s.etiqueta());
    }

    @Test
    void losPrimerosDiasDeEnero2026SonLaUltimaSemanaDe2025() {
        SemanaEpidemiologica s = SemanaEpidemiologica.de(LocalDate.of(2026, 1, 1));

        assertEquals(2025, s.anio());
        assertEquals(53, s.semana());
        assertEquals(LocalDate.of(2025, 12, 28), s.inicio());
    }

    @Test
    void laSemanaUnoPuedeEmpezarEnDiciembre() {
        SemanaEpidemiologica s = SemanaEpidemiologica.de(LocalDate.of(2024, 12, 30));

        assertEquals(2025, s.anio());
        assertEquals(1, s.semana());
        assertEquals(LocalDate.of(2024, 12, 29), s.inicio());
        assertEquals(LocalDate.of(2025, 1, 4), s.fin());
    }

    @Test
    void domingoYSabadoPertenecenALaMismaSemana() {
        assertEquals(SemanaEpidemiologica.de(LocalDate.of(2026, 11, 8)), SemanaEpidemiologica.de(LocalDate.of(2026, 11, 14)));
        assertNotEquals(SemanaEpidemiologica.de(LocalDate.of(2026, 11, 14)), SemanaEpidemiologica.de(LocalDate.of(2026, 11, 15)));
        assertEquals(SemanaEpidemiologica.de(LocalDate.of(2026, 11, 15)), SemanaEpidemiologica.de(LocalDate.of(2026, 11, 8)).siguiente());
    }
}
