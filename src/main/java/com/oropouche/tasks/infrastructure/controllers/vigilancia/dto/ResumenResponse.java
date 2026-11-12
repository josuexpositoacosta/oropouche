package com.oropouche.tasks.infrastructure.controllers.vigilancia.dto;

import java.time.LocalDate;
import java.util.Map;

import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.vigilancia.CriterioFecha;
import com.oropouche.tasks.domain.models.vigilancia.ResumenVigilancia;

public record ResumenResponse(
        LocalDate desde,
        LocalDate hasta,
        Long areaSaludId,
        CriterioFecha criterio,
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

    public static ResumenResponse from(ResumenVigilancia r) {
        return new ResumenResponse(r.filtro().desde(), r.filtro().hasta(), r.filtro().areaSaludId(),
                r.filtro().criterio(), r.totalCasos(), r.porClasificacion(), r.porcentajeConfirmados(),
                r.abiertos(), r.cerrados(), r.clasificacionValidada(), r.pendientesDeValidar(),
                r.conLaboratorio(), r.gestantes(), r.neuroinvasivos(), r.recaidas(), r.hospitalizados(),
                r.porDesenlace());
    }
}
