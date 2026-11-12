package com.oropouche.tasks.infrastructure.controllers.vigilancia;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.Manifestacion;

import static org.junit.jupiter.api.Assertions.*;

class LineaCasosCsvTest {

    @Test
    void generaCabeceraYUnaFilaPorCasoSinDatosPersonales() {
        Caso c = new Caso();
        c.setId(5L);
        c.setPacienteId(9L);
        c.setAreaSaludId(3L);
        c.setFechaInicioSintomas(LocalDate.of(2026, 1, 7));
        c.setFechaNotificacion(LocalDate.of(2026, 1, 8));
        c.setManifestaciones(EnumSet.of(Manifestacion.MAREOS, Manifestacion.ESCALOFRIOS));
        c.setClasificacion(Clasificacion.PROBABLE);
        c.setEstado(EstadoCaso.ABIERTO);
        c.setEmbarazada(true);

        String csv = LineaCasosCsv.generar(List.of(c), Map.of(3L, "NORTE"));
        String[] lineas = csv.split("\r\n");

        assertTrue(csv.startsWith(LineaCasosCsv.BOM), "BOM para que Excel lea bien las tildes");
        assertEquals(2, lineas.length);
        assertTrue(lineas[0].contains("semana_epidemiologica"));
        assertFalse(lineas[0].contains("nombre"), "no exporta nombres");
        assertEquals("5;9;NORTE;2026-01-07;2026-01-08;2026-SE01;PROBABLE;NO;ABIERTO;SI;NO;NO;NO;ESCALOFRIOS|MAREOS;;",
                lineas[1]);
    }

    @Test
    void escapaSeparadoresYNeutralizaFormulas() {
        assertEquals("\"a;b\"", LineaCasosCsv.campo("a;b"));
        assertEquals("\"di \"\"x\"\"\"", LineaCasosCsv.campo("di \"x\""));
        assertEquals("'=HYPERLINK(1)", LineaCasosCsv.campo("=HYPERLINK(1)"));
        assertEquals("", LineaCasosCsv.campo(null));
    }
}
