package com.oropouche.tasks.domain.ports.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.vigilancia.CriterioFecha;

/**
 * Puerto de salida: persistencia de casos. Guarda el caso junto con sus resultados de
 * laboratorio e historial de clasificación de forma atómica.
 */
public interface CasoRepositoryPort {

    Caso save(Caso caso);

    Optional<Caso> findById(Long id);

    List<Caso> findAll(Long areaSaludId, Clasificacion clasificacion, EstadoCaso estado);

    List<Caso> findByPacienteId(Long pacienteId);

    boolean existsAbiertoByPacienteId(Long pacienteId);

    /** Casos con la fecha indicada por el criterio dentro de [desde, hasta]; área nula = todas. */
    List<Caso> findEntreFechas(Long areaSaludId, LocalDate desde, LocalDate hasta, CriterioFecha criterio);
}
