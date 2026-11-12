package com.oropouche.tasks.domain.ports.in;

import java.util.List;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.ResultadoLaboratorio;

/**
 * Puerto de entrada: gestión de casos de Oropouche.
 */
public interface CasoUseCase {

    /**
     * Registra un caso. La clasificación inicial es la sugerida; si no cumple la definición de
     * caso sospechoso, {@code motivo} es obligatorio y el caso queda como SOSPECHOSO.
     */
    EvaluacionCaso registrar(Caso caso, String motivo);

    EvaluacionCaso buscarPorId(Long id);

    /** Filtros opcionales (null = sin filtro). */
    List<EvaluacionCaso> listar(Long areaSaludId, Clasificacion clasificacion, EstadoCaso estado);

    List<EvaluacionCaso> listarPorPaciente(Long pacienteId);

    /** Actualiza datos clínicos y epidemiológicos. Recalcula la sugerencia sin cambiar la clasificación. */
    EvaluacionCaso actualizar(Long id, Caso datos);

    /** Añade un resultado de laboratorio. Recalcula la sugerencia sin cambiar la clasificación. */
    EvaluacionCaso registrarResultado(Long casoId, ResultadoLaboratorio resultado);

    /**
     * Cambia la clasificación. Motivo obligatorio; CONFIRMADO exige laboratorio confirmatorio.
     * Si la cambia un epidemiólogo queda validada; si la cambia un médico, pendiente de validar.
     */
    EvaluacionCaso cambiarClasificacion(Long casoId, Clasificacion nueva, String motivo);

    /** El epidemiólogo valida la clasificación actual. */
    EvaluacionCaso validarClasificacion(Long casoId, String observaciones);
}
