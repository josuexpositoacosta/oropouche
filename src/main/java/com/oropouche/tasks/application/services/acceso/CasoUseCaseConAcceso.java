package com.oropouche.tasks.application.services.acceso;

import java.util.List;

import com.oropouche.tasks.domain.models.caso.Caso;
import com.oropouche.tasks.domain.models.caso.Clasificacion;
import com.oropouche.tasks.domain.models.caso.EstadoCaso;
import com.oropouche.tasks.domain.models.caso.EvaluacionCaso;
import com.oropouche.tasks.domain.models.caso.ResultadoLaboratorio;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.models.user.UsuarioActual;
import com.oropouche.tasks.domain.ports.in.CasoUseCase;
import com.oropouche.tasks.domain.ports.out.CasoRepositoryPort;
import com.oropouche.tasks.domain.ports.out.PacienteRepositoryPort;

/** Aplica la política de acceso por rol y área a los casos. */
public class CasoUseCaseConAcceso implements CasoUseCase {

    private final CasoUseCase delegado;
    private final CasoRepositoryPort casos;
    private final PacienteRepositoryPort pacientes;
    private final PoliticaAcceso politica;

    public CasoUseCaseConAcceso(CasoUseCase delegado, CasoRepositoryPort casos,
                                PacienteRepositoryPort pacientes, PoliticaAcceso politica) {
        this.delegado = delegado;
        this.casos = casos;
        this.pacientes = pacientes;
        this.politica = politica;
    }

    @Override
    public EvaluacionCaso registrar(Caso caso, String motivo) {
        if (caso != null && caso.getPacienteId() != null) {
            pacientes.findById(caso.getPacienteId()).ifPresentOrElse(
                    p -> politica.verificarEscritura(p.getAreaSaludId()),
                    () -> politica.verificarRol(Role.MEDICO));
        } else {
            politica.verificarRol(Role.MEDICO);
        }
        return delegado.registrar(caso, motivo);
    }

    @Override
    public EvaluacionCaso buscarPorId(Long id) {
        EvaluacionCaso evaluacion = delegado.buscarPorId(id);
        politica.verificarLectura(evaluacion.caso().getAreaSaludId());
        return evaluacion;
    }

    @Override
    public List<EvaluacionCaso> listar(Long areaSaludId, Clasificacion clasificacion, EstadoCaso estado) {
        return delegado.listar(politica.areaParaListar(areaSaludId), clasificacion, estado);
    }

    @Override
    public List<EvaluacionCaso> listarPorPaciente(Long pacienteId) {
        if (pacienteId != null) {
            pacientes.findById(pacienteId).ifPresent(p -> politica.verificarLectura(p.getAreaSaludId()));
        }
        return delegado.listarPorPaciente(pacienteId);
    }

    @Override
    public EvaluacionCaso actualizar(Long id, Caso datos) {
        verificarEscritura(id);
        return delegado.actualizar(id, datos);
    }

    @Override
    public EvaluacionCaso registrarResultado(Long casoId, ResultadoLaboratorio resultado) {
        verificarEscritura(casoId);
        return delegado.registrarResultado(casoId, resultado);
    }

    /** El médico propone en su área; el epidemiólogo puede reclasificar en cualquiera. */
    @Override
    public EvaluacionCaso cambiarClasificacion(Long casoId, Clasificacion nueva, String motivo) {
        UsuarioActual usuario = politica.verificarRol(Role.MEDICO, Role.EPIDEMIOLOGO);
        if (usuario.es(Role.MEDICO)) {
            verificarEscritura(casoId);
        }
        return delegado.cambiarClasificacion(casoId, nueva, motivo);
    }

    @Override
    public EvaluacionCaso validarClasificacion(Long casoId, String observaciones) {
        politica.verificarRol(Role.EPIDEMIOLOGO);
        return delegado.validarClasificacion(casoId, observaciones);
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
