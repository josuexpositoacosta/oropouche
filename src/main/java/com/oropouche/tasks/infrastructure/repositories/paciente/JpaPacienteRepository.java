package com.oropouche.tasks.infrastructure.repositories.paciente;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.oropouche.tasks.infrastructure.entities.PacienteEntity;

public interface JpaPacienteRepository extends JpaRepository<PacienteEntity, Long> {

    Optional<PacienteEntity> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);

    List<PacienteEntity> findByAreaSaludId(Long areaSaludId);

    boolean existsByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
}
