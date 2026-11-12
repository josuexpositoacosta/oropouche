package com.oropouche.tasks.application.services.acceso;

import java.util.List;

import com.oropouche.tasks.domain.models.paciente.Paciente;
import com.oropouche.tasks.domain.models.user.Role;
import com.oropouche.tasks.domain.ports.in.PacienteUseCase;
import com.oropouche.tasks.domain.ports.out.PacienteRepositoryPort;

/** Aplica la política de acceso por área a la gestión de pacientes. */
public class PacienteUseCaseConAcceso implements PacienteUseCase {

    private final PacienteUseCase delegado;
    private final PacienteRepositoryPort pacientes;
    private final PoliticaAcceso politica;

    public PacienteUseCaseConAcceso(PacienteUseCase delegado, PacienteRepositoryPort pacientes, PoliticaAcceso politica) {
        this.delegado = delegado;
        this.pacientes = pacientes;
        this.politica = politica;
    }

    @Override
    public Paciente registrar(Paciente paciente) {
        if (paciente != null && paciente.getAreaSaludId() != null) {
            politica.verificarEscritura(paciente.getAreaSaludId());
        } else {
            politica.verificarRol(Role.MEDICO);
        }
        return delegado.registrar(paciente);
    }

    @Override
    public Paciente actualizar(Long id, Paciente paciente) {
        verificarEscrituraSobreExistente(id);
        if (paciente != null && paciente.getAreaSaludId() != null) {
            politica.verificarEscritura(paciente.getAreaSaludId());
        }
        return delegado.actualizar(id, paciente);
    }

    @Override
    public Paciente buscarPorId(Long id) {
        Paciente paciente = delegado.buscarPorId(id);
        politica.verificarLectura(paciente.getAreaSaludId());
        return paciente;
    }

    @Override
    public List<Paciente> listarTodos() {
        Long area = politica.areaParaListar(null);
        return area == null ? delegado.listarTodos() : delegado.buscarPorAreaSalud(area);
    }

    @Override
    public List<Paciente> buscarPorAreaSalud(Long areaSaludId) {
        politica.areaParaListar(areaSaludId);
        return delegado.buscarPorAreaSalud(areaSaludId);
    }

    @Override
    public Paciente desactivar(Long id) {
        verificarEscrituraSobreExistente(id);
        return delegado.desactivar(id);
    }

    private void verificarEscrituraSobreExistente(Long id) {
        if (id == null) {
            politica.verificarRol(Role.MEDICO);
            return;
        }
        pacientes.findById(id).ifPresentOrElse(
                p -> politica.verificarEscritura(p.getAreaSaludId()),
                () -> politica.verificarRol(Role.MEDICO));
    }
}
