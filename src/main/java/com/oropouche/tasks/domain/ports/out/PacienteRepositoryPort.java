
package com.oropouche.tasks.domain.ports.out;

import com.oropouche.tasks.domain.models.paciente.Paciente;

import java.util.List;
import java.util.Optional;

public interface PacienteRepositoryPort {

    Paciente save(Paciente paciente);

    Optional<Paciente> findById(Long id);

    Optional<Paciente> findByNumeroDocumento(
            String tipoDocumento,
            String numeroDocumento
    );

    List<Paciente> findByAreaSaludId(Long areaSaludId);

    List<Paciente> findAll();

    boolean existsByNumeroDocumento(
            String tipoDocumento,
            String numeroDocumento
    );
}
