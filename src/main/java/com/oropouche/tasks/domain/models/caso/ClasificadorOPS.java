package com.oropouche.tasks.domain.models.caso;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Definición de caso de la OPS (Actualización Epidemiológica Oropouche, 13 de agosto de 2025).
 * Sugiere la clasificación; la decisión final corresponde al profesional sanitario.
 */
public class ClasificadorOPS implements DefinicionCaso {

    static final int MAX_DIAS_FIEBRE = 5;
    static final int MIN_MANIFESTACIONES = 2;

    private static final List<TipoPrueba> PRUEBAS_CONFIRMATORIAS = List.of(
            TipoPrueba.RT_PCR, TipoPrueba.ANTIGENO, TipoPrueba.SEROCONVERSION_PAREADA, TipoPrueba.POST_MORTEM);

    @Override
    public String nombre() {
        return "OPS 2025";
    }

    @Override
    public ResultadoClasificacion evaluar(Caso caso) {

        List<String> criterios = new ArrayList<>();

        if (!cumpleSospechoso(caso, criterios)) {
            return new ResultadoClasificacion(Optional.empty(), criterios);
        }

        if (cumpleConfirmado(caso, criterios)) {
            return new ResultadoClasificacion(Optional.of(Clasificacion.CONFIRMADO), criterios);
        }

        if (cumpleProbable(caso, criterios)) {
            return new ResultadoClasificacion(Optional.of(Clasificacion.PROBABLE), criterios);
        }

        return new ResultadoClasificacion(Optional.of(Clasificacion.SOSPECHOSO), criterios);
    }

    private boolean cumpleSospechoso(Caso caso, List<String> criterios) {

        boolean fiebreValida = caso.isFiebre()
                && (caso.isRecaida() || enVentanaDeFiebre(caso));

        if (!caso.isFiebre()) {
            criterios.add("No cumple: sin fiebre");
        } else if (fiebreValida) {
            criterios.add(caso.isRecaida()
                    ? "Fiebre (recaída: no aplica el límite de " + MAX_DIAS_FIEBRE + " días)"
                    : "Fiebre de hasta " + MAX_DIAS_FIEBRE + " días de evolución");
        } else {
            criterios.add("No cumple: fiebre de más de " + MAX_DIAS_FIEBRE + " días al notificar");
        }

        criterios.add(caso.isCefaleaIntensa() ? "Cefalea intensa" : "No cumple: sin cefalea intensa");

        int manifestaciones = caso.getManifestaciones().size();
        boolean cuadroSuficiente;
        if (caso.isEmbarazada()) {
            cuadroSuficiente = true;
            criterios.add("Embarazada: no requiere manifestaciones adicionales");
        } else {
            cuadroSuficiente = manifestaciones >= MIN_MANIFESTACIONES;
            criterios.add(cuadroSuficiente
                    ? manifestaciones + " manifestaciones (mínimo " + MIN_MANIFESTACIONES + ")"
                    : "No cumple: " + manifestaciones + " manifestaciones (mínimo " + MIN_MANIFESTACIONES + ")");
        }

        return fiebreValida && caso.isCefaleaIntensa() && cuadroSuficiente;
    }

    private static boolean enVentanaDeFiebre(Caso caso) {
        long dias = caso.diasEvolucionAlNotificar();
        return dias >= 0 && dias <= MAX_DIAS_FIEBRE;
    }

    private boolean cumpleConfirmado(Caso caso, List<String> criterios) {

        boolean confirmado = false;

        for (TipoPrueba tipo : PRUEBAS_CONFIRMATORIAS) {
            if (tienePositivo(caso, tipo)) {
                criterios.add("Laboratorio confirmatorio: " + tipo + " de Oropouche positiva");
                confirmado = true;
            }
        }

        if (tienePositivo(caso, TipoPrueba.IGM_LCR)) {
            if (caso.getManifestaciones().contains(Manifestacion.NEUROLOGICA)) {
                criterios.add("Laboratorio confirmatorio: IgM positiva en LCR con manifestación neurológica");
                confirmado = true;
            } else {
                criterios.add("IgM en LCR positiva sin manifestación neurológica registrada: no confirma");
            }
        }

        return confirmado;
    }

    private boolean cumpleProbable(Caso caso, List<String> criterios) {

        boolean probable = false;

        if (caso.isAreaTransmisionOViaje()) {
            criterios.add("Vive o viajó a un área de transmisión confirmada");
            probable = true;
        }
        if (caso.isNexoCasoConfirmado()) {
            criterios.add("Nexo epidemiológico con un caso confirmado");
            probable = true;
        }
        if (tienePositivo(caso, TipoPrueba.ELISA_IGM)) {
            criterios.add("ELISA IgM de Oropouche positiva");
            probable = true;
        }

        return probable;
    }

    private static boolean tienePositivo(Caso caso, TipoPrueba tipo) {
        return caso.getResultados().stream()
                .anyMatch(r -> r.esPositivoDe(AgenteEtiologico.OROPOUCHE, tipo));
    }
}
