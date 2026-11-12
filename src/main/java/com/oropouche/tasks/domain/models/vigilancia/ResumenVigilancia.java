package com.oropouche.tasks.domain.models.vigilancia;

import java.util.Map;

import com.oropouche.tasks.domain.models.caso.Clasificacion;

/**
 * Indicadores de vigilancia de un periodo.
 *
 * @param porcentajeConfirmados confirmados sobre el total de casos (0 si no hay casos)
 * @param conLaboratorio        casos con al menos un resultado de laboratorio de Oropouche
 * @param hospitalizados        casos con algún plan de ingreso (hospitalario o unidad especializada)
 * @param porDesenlace          casos cerrados por código de desenlace
 */
public record ResumenVigilancia(
        FiltroVigilancia filtro,
        long totalCasos,
        Map<Clasificacion, Long> porClasificacion,
        double porcentajeConfirmados,
        long abiertos,
        long cerrados,
        long clasificacionValidada,
        long pendientesDeValidar,
        long conLaboratorio,
        long gestantes,
        long neuroinvasivos,
        long recaidas,
        long hospitalizados,
        Map<String, Long> porDesenlace) {
}
