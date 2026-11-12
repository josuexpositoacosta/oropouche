package com.oropouche.tasks.domain.ports.in;

import java.time.LocalDate;
import java.util.List;

import com.oropouche.tasks.domain.models.caso.ControlPendiente;
import com.oropouche.tasks.domain.models.caso.Dictamen;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionClinica;
import com.oropouche.tasks.domain.models.caso.PlanAtencion;
import com.oropouche.tasks.domain.models.caso.Seguimiento;

/**
 * Puerto de entrada: atención clínica de un caso (registros inmutables), cierre y reapertura.
 */
public interface AtencionClinicaUseCase {

    EvaluacionCaso registrarEvaluacion(Long casoId, EvaluacionClinica evaluacion);

    EvaluacionCaso registrarDictamen(Long casoId, Dictamen dictamen);

    EvaluacionCaso registrarPlan(Long casoId, PlanAtencion plan);

    /** Un seguimiento con evolución RECAIDA marca el caso como recaída. */
    EvaluacionCaso registrarSeguimiento(Long casoId, Seguimiento seguimiento);

    /** Cierra el caso con un desenlace activo del catálogo. {@code fechaCierre} nula = hoy. */
    EvaluacionCaso cerrar(Long casoId, String codigoDesenlace, LocalDate fechaCierre, String observaciones);

    /** Reabre un caso cerrado (por ejemplo, por recaída tras el alta). Motivo obligatorio. */
    EvaluacionCaso reabrir(Long casoId, String motivo);

    /** Casos abiertos con próximo control hasta la fecha indicada (incluidos los vencidos). */
    List<ControlPendiente> controlesPendientes(Long areaSaludId, LocalDate hasta);
}
