package com.oropouche.tasks.application.services.caso;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.oropouche.tasks.domain.exceptions.RecursoNoEncontradoException;
import com.oropouche.tasks.domain.models.caso.AlertasClinicas;
import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.DefinicionCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;

/** Operaciones comunes de los servicios de casos. */
class CasoSoporte {

    private final CasoRepositoryPort casoRepositoryPort;
    private final DefinicionCaso definicionCaso;
    private final AlertasClinicas alertasClinicas = new AlertasClinicas();
    private final Clock clock;

    CasoSoporte(CasoRepositoryPort casoRepositoryPort, DefinicionCaso definicionCaso, Clock clock) {
        this.casoRepositoryPort = casoRepositoryPort;
        this.definicionCaso = definicionCaso;
        this.clock = clock;
    }

    Caso obtener(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El identificador del caso no es válido");
        }

        return casoRepositoryPort.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el caso con ID: " + id));
    }

    Caso obtenerAbierto(Long id) {

        Caso caso = obtener(id);

        if (!caso.estaAbierto()) {
            throw new IllegalArgumentException("El caso está cerrado y no admite cambios");
        }

        return caso;
    }

    EvaluacionCaso guardarYEvaluar(Caso caso) {
        return evaluar(casoRepositoryPort.save(caso));
    }

    EvaluacionCaso evaluar(Caso caso) {
        return new EvaluacionCaso(caso, definicionCaso.evaluar(caso), alertasClinicas.evaluar(caso, hoy()));
    }

    LocalDate hoy() {
        return LocalDate.now(clock);
    }

    LocalDateTime ahora() {
        return LocalDateTime.now(clock);
    }

    static boolean esBlanco(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
