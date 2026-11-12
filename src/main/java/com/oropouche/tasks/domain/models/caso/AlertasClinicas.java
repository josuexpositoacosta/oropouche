package com.oropouche.tasks.domain.models.caso;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Genera avisos clínicos a partir del estado actual del caso y de su evaluación más reciente.
 * Son avisos basados en las recomendaciones de la OPS; la decisión clínica corresponde al médico.
 */
public class AlertasClinicas {

    public List<Alerta> evaluar(Caso caso, LocalDate hoy) {

        List<Alerta> alertas = new ArrayList<>();

        if (!caso.estaAbierto()) {
            return alertas;
        }

        EvaluacionClinica ultima = caso.getEvaluaciones().stream()
                .max(Comparator.comparing(EvaluacionClinica::getFecha))
                .orElse(null);

        Set<SignoClinico> neuro = ultima == null ? EnumSet.noneOf(SignoClinico.class)
                : interseccion(ultima.getSignosClinicos(), SignoClinico.NEUROLOGICOS);

        if (!neuro.isEmpty() || caso.getManifestaciones().contains(Manifestacion.NEUROLOGICA)) {
            String origen = neuro.isEmpty() ? "manifestación neurológica registrada en el caso" : nombres(neuro);
            alertas.add(new Alerta(TipoAlerta.NEUROINVASIVA,
                    "Posible forma neuroinvasiva (" + origen + "): la OPS indica valorar el ingreso en una"
                            + " unidad especializada con monitoreo constante y considerar meningitis o encefalitis"));
        }

        if (ultima != null && !ultima.getSignosAlarmaDengue().isEmpty()) {
            alertas.add(new Alerta(TipoAlerta.SIGNOS_ALARMA_DENGUE,
                    "Signos de alarma de dengue (" + nombres(ultima.getSignosAlarmaDengue()) + "): valorar"
                            + " dengue según el algoritmo de la OPS, que indica internar en hospital o unidad de dengue"));
        }

        if (ultima != null) {
            Set<SignoClinico> hemorragias = interseccion(ultima.getSignosClinicos(), SignoClinico.HEMORRAGICOS);
            if (!hemorragias.isEmpty()) {
                alertas.add(new Alerta(TipoAlerta.HEMORRAGIA,
                        "Manifestaciones hemorrágicas: " + nombres(hemorragias)));
            }
        }

        if (caso.isEmbarazada()) {
            alertas.add(new Alerta(TipoAlerta.GESTANTE,
                    "Caso en gestante: seguimiento estrecho y vigilancia de posible transmisión vertical (OPS)"));
        }

        LocalDate proximo = caso.proximoControl();
        if (proximo != null && proximo.isBefore(hoy)) {
            alertas.add(new Alerta(TipoAlerta.CONTROL_VENCIDO, "Control vencido desde el " + proximo));
        }

        return alertas;
    }

    private static <E extends Enum<E>> Set<E> interseccion(Set<E> a, Set<E> b) {
        Set<E> resultado = new HashSet<>(a);
        resultado.retainAll(b);
        return resultado;
    }

    private static String nombres(Set<? extends Enum<?>> valores) {
        return valores.stream().map(Enum::name).sorted().collect(Collectors.joining(", "));
    }
}
