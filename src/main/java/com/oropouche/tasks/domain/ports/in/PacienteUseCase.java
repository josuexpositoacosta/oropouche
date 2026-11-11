package com.oropouche.tasks.domain.ports.in;

import java.util.List;

import com.oropouche.tasks.domain.models.paciente.Paciente;

/**
 * Puerto de entrada: gestión de pacientes.
 */
public interface PacienteUseCase {

    Paciente registrar(Paciente paciente);

    Paciente actualizar(Long id, Paciente paciente);

    Paciente buscarPorId(Long id);

    List<Paciente> listarTodos();

    List<Paciente> buscarPorAreaSalud(Long areaSaludId);

    Paciente desactivar(Long id);
}
