package com.oropouche.tasks.application.services.acceso;

import java.time.LocalDate;
import java.util.List;

import com.oropouche.tasks.domain.models.caso.ControlPendiente;
import com.oropouche.tasks.domain.models.caso.Dictamen;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionClinica;
import com.oropouche.tasks.domain.models.caso.PlanAtencion;
import com.oropouche.tasks.domain.models.caso.Seguimiento;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.ports.in.AtencionClinicaUseCase;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;

/** La atención clínica la registra el médico, solo en casos de su área. */
public class AtencionClinicaUseCaseConAcceso implements AtencionClinicaUseCase {

    private final AtencionClinicaUseCase delegado;
    private final CasoRepositoryPort casos;
    private final PoliticaAcceso politica;

    public AtencionClinicaUseCaseConAcceso(AtencionClinicaUseCase delegado, CasoRepositoryPort casos,
                                           PoliticaAcceso politica) {
        this.delegado = delegado;
        this.casos = casos;
        this.politica = politica;
    }

    @Override
    public EvaluacionCaso registrarEvaluacion(Long casoId, EvaluacionClinica evaluacion) {
        verificarEscritura(casoId);
        return delegado.registrarEvaluacion(casoId, evaluacion);
    }

    @Override
    public EvaluacionCaso registrarDictamen(Long casoId, Dictamen dictamen) {
        verificarEscritura(casoId);
        return delegado.registrarDictamen(casoId, dictamen);
    }

    @Override
    public EvaluacionCaso registrarPlan(Long casoId, PlanAtencion plan) {
        verificarEscritura(casoId);
        return delegado.registrarPlan(casoId, plan);
    }

    @Override
    public EvaluacionCaso registrarSeguimiento(Long casoId, Seguimiento seguimiento) {
        verificarEscritura(casoId);
        return delegado.registrarSeguimiento(casoId, seguimiento);
    }

    @Override
    public EvaluacionCaso cerrar(Long casoId, String codigoDesenlace, LocalDate fechaCierre, String observaciones) {
        verificarEscritura(casoId);
        return delegado.cerrar(casoId, codigoDesenlace, fechaCierre, observaciones);
    }

    @Override
    public EvaluacionCaso reabrir(Long casoId, String motivo) {
        verificarEscritura(casoId);
        return delegado.reabrir(casoId, motivo);
    }

    @Override
    public List<ControlPendiente> controlesPendientes(Long areaSaludId, LocalDate hasta) {
        return delegado.controlesPendientes(politica.areaParaListar(areaSaludId), hasta);
    }

    private void verificarEscritura(Long casoId) {
        if (casoId == null) {
            politica.verificarRol(Role.MEDICO);
            return;
        }
        casos.findById(casoId).ifPresentOrElse(
                c -> politica.verificarEscritura(c.getAreaSaludId()),
                () -> politica.verificarRol(Role.MEDICO));
    }
}
