package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reglas de la definición de caso de la OPS (Actualización Epidemiológica, 13/08/2025).
 */
class ClasificadorOPSTest {

    private final ClasificadorOPS clasificador = new ClasificadorOPS();

    /** Caso que cumple exactamente la definición de sospechoso: fiebre 2 días, cefalea intensa, 2 manifestaciones. */
    private static Caso sospechosoBase() {
        Caso caso = new Caso();
        caso.setFechaInicioSintomas(LocalDate.of(2026, 11, 1));
        caso.setFechaNotificacion(LocalDate.of(2026, 11, 3));
        caso.setFiebre(true);
        caso.setCefaleaIntensa(true);
        caso.setManifestaciones(EnumSet.of(Manifestacion.MIALGIA_ARTRALGIA, Manifestacion.ESCALOFRIOS));
        return caso;
    }

    private static ResultadoLaboratorio prueba(AgenteEtiologico agente, TipoPrueba tipo, ResultadoPrueba resultado) {
        return new ResultadoLaboratorio(null, agente, tipo, TipoMuestra.SUERO, resultado,
                LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 5));
    }

    private Optional<Clasificacion> sugerida(Caso caso) {
        return clasificador.evaluar(caso).sugerida();
    }

    // --- Caso sospechoso

    @Test
    void cumpleSospechosoConFiebreCefaleaYDosManifestaciones() {
        assertEquals(Optional.of(Clasificacion.SOSPECHOSO), sugerida(sospechosoBase()));
    }

    @Test
    void conUnaSolaManifestacionNoCumpleSospechoso() {
        Caso caso = sospechosoBase();
        caso.setManifestaciones(EnumSet.of(Manifestacion.FOTOFOBIA));

        ResultadoClasificacion r = clasificador.evaluar(caso);

        assertTrue(r.sugerida().isEmpty());
        assertTrue(r.criterios().stream().anyMatch(c -> c.contains("1 manifestaciones (mínimo 2)")));
    }

    @Test
    void sinCefaleaIntensaNoCumpleSospechoso() {
        Caso caso = sospechosoBase();
        caso.setCefaleaIntensa(false);
        assertTrue(sugerida(caso).isEmpty());
    }

    @Test
    void sinFiebreNoCumpleSospechoso() {
        Caso caso = sospechosoBase();
        caso.setFiebre(false);
        assertTrue(sugerida(caso).isEmpty());
    }

    @Test
    void fiebreDeCincoDiasCumpleYDeSeisNo() {
        Caso caso = sospechosoBase();
        caso.setFechaNotificacion(caso.getFechaInicioSintomas().plusDays(5));
        assertEquals(Optional.of(Clasificacion.SOSPECHOSO), sugerida(caso), "5 días es el límite incluido");

        caso.setFechaNotificacion(caso.getFechaInicioSintomas().plusDays(6));
        assertTrue(sugerida(caso).isEmpty(), "6 días supera el límite");
    }

    @Test
    void recaidaNoAplicaElLimiteDeCincoDias() {
        Caso caso = sospechosoBase();
        caso.setFechaNotificacion(caso.getFechaInicioSintomas().plusDays(12));
        caso.setRecaida(true);

        assertEquals(Optional.of(Clasificacion.SOSPECHOSO), sugerida(caso));
    }

    @Test
    void embarazadaSoloRequiereFiebreYCefaleaIntensa() {
        Caso caso = sospechosoBase();
        caso.setManifestaciones(EnumSet.noneOf(Manifestacion.class));
        caso.setEmbarazada(true);

        assertEquals(Optional.of(Clasificacion.SOSPECHOSO), sugerida(caso));
    }

    @Test
    void embarazadaSinCefaleaIntensaNoCumple() {
        Caso caso = sospechosoBase();
        caso.setEmbarazada(true);
        caso.setCefaleaIntensa(false);
        assertTrue(sugerida(caso).isEmpty());
    }

    // --- Caso probable

    @Test
    void probablePorAreaDeTransmisionOViaje() {
        Caso caso = sospechosoBase();
        caso.setAreaTransmisionOViaje(true);
        assertEquals(Optional.of(Clasificacion.PROBABLE), sugerida(caso));
    }

    @Test
    void probablePorNexoConCasoConfirmado() {
        Caso caso = sospechosoBase();
        caso.setNexoCasoConfirmado(true);
        assertEquals(Optional.of(Clasificacion.PROBABLE), sugerida(caso));
    }

    @Test
    void probablePorElisaIgmPositiva() {
        Caso caso = sospechosoBase();
        caso.getResultados().add(prueba(AgenteEtiologico.OROPOUCHE, TipoPrueba.ELISA_IGM, ResultadoPrueba.POSITIVO));
        assertEquals(Optional.of(Clasificacion.PROBABLE), sugerida(caso));
    }

    @Test
    void criterioEpidemiologicoSinCuadroClinicoNoEsProbable() {
        Caso caso = sospechosoBase();
        caso.setCefaleaIntensa(false);
        caso.setAreaTransmisionOViaje(true);
        assertTrue(sugerida(caso).isEmpty(), "probable exige ser antes caso sospechoso");
    }

    // --- Caso confirmado

    @Test
    void confirmadoPorCadaPruebaConfirmatoriaPositiva() {
        for (TipoPrueba tipo : new TipoPrueba[] {
                TipoPrueba.RT_PCR, TipoPrueba.ANTIGENO, TipoPrueba.SEROCONVERSION_PAREADA, TipoPrueba.POST_MORTEM}) {
            Caso caso = sospechosoBase();
            caso.getResultados().add(prueba(AgenteEtiologico.OROPOUCHE, tipo, ResultadoPrueba.POSITIVO));
            assertEquals(Optional.of(Clasificacion.CONFIRMADO), sugerida(caso), "debe confirmar: " + tipo);
        }
    }

    @Test
    void confirmadoPrevaleceSobreProbable() {
        Caso caso = sospechosoBase();
        caso.setAreaTransmisionOViaje(true);
        caso.getResultados().add(prueba(AgenteEtiologico.OROPOUCHE, TipoPrueba.RT_PCR, ResultadoPrueba.POSITIVO));
        assertEquals(Optional.of(Clasificacion.CONFIRMADO), sugerida(caso));
    }

    @Test
    void rtPcrNegativaNoConfirma() {
        Caso caso = sospechosoBase();
        caso.getResultados().add(prueba(AgenteEtiologico.OROPOUCHE, TipoPrueba.RT_PCR, ResultadoPrueba.NEGATIVO));
        assertEquals(Optional.of(Clasificacion.SOSPECHOSO), sugerida(caso));
    }

    @Test
    void rtPcrPositivaDeDengueNoConfirmaOropouche() {
        Caso caso = sospechosoBase();
        caso.getResultados().add(prueba(AgenteEtiologico.DENGUE, TipoPrueba.RT_PCR, ResultadoPrueba.POSITIVO));
        assertEquals(Optional.of(Clasificacion.SOSPECHOSO), sugerida(caso));
    }

    @Test
    void igmEnLcrConfirmaSoloConManifestacionNeurologica() {
        Caso sinNeuro = sospechosoBase();
        sinNeuro.getResultados().add(prueba(AgenteEtiologico.OROPOUCHE, TipoPrueba.IGM_LCR, ResultadoPrueba.POSITIVO));
        assertEquals(Optional.of(Clasificacion.SOSPECHOSO), sugerida(sinNeuro));

        Caso conNeuro = sospechosoBase();
        conNeuro.setManifestaciones(EnumSet.of(Manifestacion.NEUROLOGICA, Manifestacion.MAREOS));
        conNeuro.getResultados().add(prueba(AgenteEtiologico.OROPOUCHE, TipoPrueba.IGM_LCR, ResultadoPrueba.POSITIVO));
        assertEquals(Optional.of(Clasificacion.CONFIRMADO), sugerida(conNeuro));
    }

    @Test
    void laboratorioPositivoSinCuadroClinicoNoConfirma() {
        Caso caso = sospechosoBase();
        caso.setFiebre(false);
        caso.getResultados().add(prueba(AgenteEtiologico.OROPOUCHE, TipoPrueba.RT_PCR, ResultadoPrueba.POSITIVO));
        assertTrue(sugerida(caso).isEmpty(), "confirmado exige ser caso sospechoso (definición OPS)");
    }

    @Test
    void losCriteriosExplicanLaSugerencia() {
        Caso caso = sospechosoBase();
        caso.setNexoCasoConfirmado(true);

        ResultadoClasificacion r = clasificador.evaluar(caso);

        assertTrue(r.criterios().contains("Cefalea intensa"));
        assertTrue(r.criterios().contains("Nexo epidemiológico con un caso confirmado"));
        assertEquals("OPS 2025", clasificador.nombre());
    }
}
